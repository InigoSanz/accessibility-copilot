package com.inigosanz.backend.infrastructure.adapter.out.scanner;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.port.out.WebAccessibilityScannerPort;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.PlaywrightException;
import com.microsoft.playwright.options.WaitUntilState;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class PlaywrightWebAccessibilityScannerAdapter implements WebAccessibilityScannerPort {

    private static final String AXE_RESOURCE_PATH = "scanner/axe.min.js";
    private static final double NAVIGATION_TIMEOUT_MILLIS = 30000;
    private static final String AXE_RUN_SCRIPT = """
            async () => {
              if (!window.axe) {
                throw new Error('axe-core script was not loaded in the browser page');
              }
              return JSON.stringify(await window.axe.run(document));
            }
            """;

    private final ObjectMapper objectMapper;
    private final String axeScriptContent;

    public PlaywrightWebAccessibilityScannerAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.axeScriptContent = loadAxeScriptFromClasspath();
    }

    @Override
    public List<AccessibilityIssue> scan(Long scanId, String pageUrl) {
        try (
                Playwright playwright = Playwright.create();
                Browser browser = createBrowser(playwright);
                BrowserContext context = browser.newContext()
        ) {
            Page page = navigatePage(context, pageUrl);
            injectAxeScript(page);
            String axeResultJson = runAxe(page);
            return mapIssues(scanId, page.url(), axeResultJson);
        } catch (PlaywrightException exception) {
            throw new IllegalStateException(
                    "Unable to execute accessibility scan for URL '%s': %s".formatted(pageUrl, exception.getMessage()),
                    exception
            );
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse accessibility scan output for URL '%s'".formatted(pageUrl), exception);
        }
    }

    private Browser createBrowser(Playwright playwright) {
        return playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
    }

    private Page navigatePage(BrowserContext context, String pageUrl) {
        Page page = context.newPage();
        page.navigate(
                pageUrl,
                new Page.NavigateOptions()
                        .setTimeout(NAVIGATION_TIMEOUT_MILLIS)
                        .setWaitUntil(WaitUntilState.DOMCONTENTLOADED)
        );
        return page;
    }

    private void injectAxeScript(Page page) {
        page.addScriptTag(new Page.AddScriptTagOptions().setContent(axeScriptContent));
    }

    private String runAxe(Page page) {
        return (String) page.evaluate(AXE_RUN_SCRIPT);
    }

    private List<AccessibilityIssue> mapIssues(Long scanId, String pageUrl, String axeResultJson) throws IOException {
        JsonNode root = objectMapper.readTree(axeResultJson);
        JsonNode violations = root.path("violations");

        List<AccessibilityIssue> issues = new ArrayList<>();
        for (JsonNode violation : violations) {
            String ruleCode = safeText(violation, "id", "unknown-rule");
            String severity = safeText(violation, "impact", "unknown").toLowerCase();
            String fallbackMessage = firstNonBlank(
                    nullableText(violation, "help"),
                    nullableText(violation, "description"),
                    "Accessibility issue detected"
            );
            String recommendation = firstNonBlank(
                    nullableText(violation, "help"),
                    nullableText(violation, "description"),
                    "Review this violation and apply WCAG recommendations"
            );
            String helpUrl = safeText(violation, "helpUrl", null);
            String wcagCriterion = extractWcagCriterion(violation.path("tags"));

            JsonNode nodes = violation.path("nodes");
            if (!nodes.isArray() || nodes.isEmpty()) {
                issues.add(buildIssue(
                        scanId,
                        ruleCode,
                        fallbackMessage,
                        severity,
                        wcagCriterion,
                        pageUrl,
                        null,
                        null,
                        recommendation,
                        helpUrl
                ));
                continue;
            }

            for (JsonNode node : nodes) {
                String nodeMessage = firstNonBlank(
                        nullableText(node, "failureSummary"),
                        fallbackMessage
                );
                String nodeRecommendation = firstNonBlank(
                        nullableText(node, "failureSummary"),
                        recommendation
                );
                issues.add(buildIssue(
                        scanId,
                        ruleCode,
                        nodeMessage,
                        severity,
                        wcagCriterion,
                        pageUrl,
                        nullableText(node, "html"),
                        extractSelector(node.path("target")),
                        nodeRecommendation,
                        helpUrl
                ));
            }
        }

        return issues;
    }

    private AccessibilityIssue buildIssue(
            Long scanId,
            String ruleCode,
            String message,
            String severity,
            String wcagCriterion,
            String pageUrl,
            String htmlSnippet,
            String selector,
            String recommendation,
            String helpUrl
    ) {
        return new AccessibilityIssue(
                null,
                scanId,
                ruleCode,
                message,
                severity,
                wcagCriterion,
                pageUrl,
                htmlSnippet,
                selector,
                recommendation,
                helpUrl
        );
    }

    private String extractWcagCriterion(JsonNode tagsNode) {
        if (!tagsNode.isArray()) {
            return null;
        }
        for (JsonNode tag : tagsNode) {
            String value = tag.asText(null);
            if (value != null && value.toLowerCase().startsWith("wcag")) {
                return value;
            }
        }
        return null;
    }

    private String extractSelector(JsonNode targetNode) {
        if (!targetNode.isArray() || targetNode.isEmpty()) {
            return null;
        }
        List<String> selectorParts = new ArrayList<>();
        for (JsonNode selector : targetNode) {
            String value = selector.asText(null);
            if (value != null && !value.isBlank()) {
                selectorParts.add(value);
            }
        }
        if (selectorParts.isEmpty()) {
            return null;
        }
        return String.join(" | ", selectorParts);
    }

    private String safeText(JsonNode node, String fieldName, String fallback) {
        String value = nullableText(node, fieldName);
        return value == null ? fallback : value;
    }

    private String firstNonBlank(String... values) {
        return Arrays.stream(values)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String nullableText(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText();
        return value == null || value.isBlank() ? null : value;
    }

    private String loadAxeScriptFromClasspath() {
        ClassPathResource resource = new ClassPathResource(AXE_RESOURCE_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load axe-core script from classpath resource: " + AXE_RESOURCE_PATH, exception);
        }
    }
}

