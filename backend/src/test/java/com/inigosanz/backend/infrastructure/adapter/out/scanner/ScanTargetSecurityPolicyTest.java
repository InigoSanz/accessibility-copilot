package com.inigosanz.backend.infrastructure.adapter.out.scanner;

import com.inigosanz.backend.shared.exception.UnsafeScanTargetException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScanTargetSecurityPolicyTest {

    @Test
    void shouldRejectNonHttpSchemes() {
        ScanTargetSecurityPolicy policy = new ScanTargetSecurityPolicy(new ScannerSecurityProperties());

        assertThrows(UnsafeScanTargetException.class, () -> policy.validateTarget("ftp://example.com"));
    }

    @Test
    void shouldRejectLocalhostWhenPrivateNetworksAreBlocked() {
        ScanTargetSecurityPolicy policy = new ScanTargetSecurityPolicy(new ScannerSecurityProperties());

        assertThrows(UnsafeScanTargetException.class, () -> policy.validateTarget("http://localhost:8080"));
    }

    @Test
    void shouldRejectHostOutsideWhitelistWhenWhitelistIsEnforced() {
        ScannerSecurityProperties properties = new ScannerSecurityProperties();
        properties.setEnforceWhitelist(true);
        properties.setAllowedDomains(List.of("example.com"));
        ScanTargetSecurityPolicy policy = new ScanTargetSecurityPolicy(properties);

        assertThrows(UnsafeScanTargetException.class, () -> policy.validateTarget("https://google.com"));
    }

    @Test
    void shouldAllowWhitelistedDomainWhenWhitelistIsEnforced() {
        ScannerSecurityProperties properties = new ScannerSecurityProperties();
        properties.setEnforceWhitelist(true);
        properties.setAllowedDomains(List.of("example.com"));
        properties.setBlockPrivateNetworks(false);
        ScanTargetSecurityPolicy policy = new ScanTargetSecurityPolicy(properties);

        assertDoesNotThrow(() -> policy.validateTarget("https://www.example.com/path"));
    }
}

