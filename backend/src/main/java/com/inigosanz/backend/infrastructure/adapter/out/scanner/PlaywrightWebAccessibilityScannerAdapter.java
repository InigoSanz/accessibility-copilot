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
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class PlaywrightWebAccessibilityScannerAdapter implements WebAccessibilityScannerPort {

    private static final String AXE_CORE_CDN_URL = "https://cdnjs.cloudflare.com/ajax/libs/axe-core/4.10.2/axe.min.js";
    private static final double NAVIGATION_TIMEOUT_MILLIS = 30000;

    private final ObjectMapper objectMapper;

    public PlaywrightWebAccessibilityScannerAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public List<AccessibilityIssue> scan(Long scanId, String pageUrl) {
        try (
                Playwright playwright = Playwright.create();
                Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
                BrowserContext context = browser.newContext()
        ) {
            Page page = context.newPage();
            page.navigate(
                    pageUrl,
                    new Page.NavigateOptions().setTimeout(NAVIGATION_TIMEOUT_MILLIS)
            );
            page.addScriptTag(new Page.AddScriptTagOptions().setUrl(AXE_CORE_CDN_URL));

            String axeResultJson = (String) page.evaluate("async () => JSON.stringify(await axe.run(document))");
            return mapIssues(scanId, page.url(), axeResultJson);
        } catch (PlaywrightException exception) {
            throw new IllegalStateException("Unable to execute accessibility scan for URL: " + pageUrl, exception);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to parse accessibility scan output", exception);
        }
    }

    private List<AccessibilityIssue> mapIssues(Long scanId, String pageUrl, String axeResultJson) throws IOException {
        JsonNode root = objectMapper.readTree(axeResultJson);
        JsonNode violations = root.path("violations");

        List<AccessibilityIssue> issues = new ArrayList<>();
        for (JsonNode violation : violations) {
            String ruleCode = safeText(violation, "id", "unknown-rule");
            String severity = safeText(violation, "impact", "unknown");
            String message = safeText(violation, "description", safeText(violation, "help", "Accessibility issue"));
            String recommendation = safeText(violation, "help", null);
            String helpUrl = safeText(violation, "helpUrl", null);
            String wcagCriterion = extractWcagCriterion(violation.path("tags"));

            JsonNode nodes = violation.path("nodes");
            if (!nodes.isArray() || nodes.isEmpty()) {
                issues.add(buildIssue(
                        scanId,
                        ruleCode,
                        message,
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
                issues.add(buildIssue(
                        scanId,
                        ruleCode,
                        message,
                        severity,
                        wcagCriterion,
                        pageUrl,
                        nullableText(node, "html"),
                        extractSelector(node.path("target")),
                        safeText(node, "failureSummary", recommendation),
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

    private String nullableText(JsonNode node, String fieldName) {
        JsonNode valueNode = node.get(fieldName);
        if (valueNode == null || valueNode.isNull()) {
            return null;
        }
        String value = valueNode.asText();
        return value == null || value.isBlank() ? null : value;
    }
}

