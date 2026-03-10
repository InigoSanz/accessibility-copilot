package com.inigosanz.backend.application.service;

import com.inigosanz.backend.domain.model.AccessibilityIssue;
import com.inigosanz.backend.domain.model.Scan;
import com.inigosanz.backend.domain.model.ScanStatus;
import com.inigosanz.backend.domain.port.in.CreateScanUseCase;
import com.inigosanz.backend.domain.port.in.GetScanByIdUseCase;
import com.inigosanz.backend.domain.port.in.ListScansByProjectUseCase;
import com.inigosanz.backend.domain.port.out.AccessibilityIssueRepositoryPort;
import com.inigosanz.backend.domain.port.out.ProjectRepositoryPort;
import com.inigosanz.backend.domain.port.out.ScanRepositoryPort;
import com.inigosanz.backend.shared.exception.ProjectNotFoundException;
import com.inigosanz.backend.shared.exception.ScanNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class ScanService implements CreateScanUseCase, ListScansByProjectUseCase, GetScanByIdUseCase {

    private final ProjectRepositoryPort projectRepositoryPort;
    private final ScanRepositoryPort scanRepositoryPort;
    private final AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort;

    public ScanService(
            ProjectRepositoryPort projectRepositoryPort,
            ScanRepositoryPort scanRepositoryPort,
            AccessibilityIssueRepositoryPort accessibilityIssueRepositoryPort
    ) {
        this.projectRepositoryPort = projectRepositoryPort;
        this.scanRepositoryPort = scanRepositoryPort;
        this.accessibilityIssueRepositoryPort = accessibilityIssueRepositoryPort;
    }

    @Override
    public Scan create(Long projectId) {
        Objects.requireNonNull(projectId, "projectId is required");

        projectRepositoryPort.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        Scan scan = new Scan(
                null,
                projectId,
                ScanStatus.COMPLETED,
                LocalDateTime.now(),
                LocalDateTime.now()
        );

        Scan savedScan = scanRepositoryPort.save(scan);
        accessibilityIssueRepositoryPort.saveAll(buildFakeIssues(savedScan.getId()));

        return savedScan;
    }

    @Override
    public List<Scan> findByProjectId(Long projectId) {
        Objects.requireNonNull(projectId, "projectId is required");

        projectRepositoryPort.findById(projectId)
                .orElseThrow(ProjectNotFoundException::new);

        return scanRepositoryPort.findByProjectId(projectId);
    }

    @Override
    public Scan findById(Long scanId) {
        Objects.requireNonNull(scanId, "scanId is required");

        return scanRepositoryPort.findById(scanId)
                .orElseThrow(ScanNotFoundException::new);
    }

    private List<AccessibilityIssue> buildFakeIssues(Long scanId) {
        return List.of(
                new AccessibilityIssue(
                        null,
                        scanId,
                        "image-alt",
                        "Image elements must have alternative text",
                        "serious",
                        "1.1.1",
                        "https://example.com",
                        "<img src='hero.jpg'>",
                        "img.hero-banner",
                        "Add a meaningful alt attribute describing the image purpose.",
                        "https://www.w3.org/WAI/WCAG21/Understanding/non-text-content.html"
                ),
                new AccessibilityIssue(
                        null,
                        scanId,
                        "color-contrast",
                        "Elements must meet minimum color contrast ratio thresholds",
                        "serious",
                        "1.4.3",
                        "https://example.com",
                        "<button style='color:#aaa;background:#fff'>Submit</button>",
                        "button.submit-primary",
                        "Increase text and background contrast to meet at least a 4.5:1 ratio.",
                        "https://www.w3.org/WAI/WCAG21/Understanding/contrast-minimum.html"
                ),
                new AccessibilityIssue(
                        null,
                        scanId,
                        "label",
                        "Form elements must have labels",
                        "moderate",
                        "3.3.2",
                        "https://example.com/contact",
                        "<input type='email'>",
                        "form#contact input[type='email']",
                        "Associate the input with a visible label using the for and id attributes.",
                        "https://www.w3.org/WAI/WCAG21/Understanding/labels-or-instructions.html"
                )
        );
    }
}
