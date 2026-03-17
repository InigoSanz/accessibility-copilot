package com.inigosanz.backend.infrastructure.adapter.out.scanner;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "scanner.security")
public class ScannerSecurityProperties {

    private boolean enforceWhitelist = false;
    private List<String> allowedDomains = new ArrayList<>();
    private boolean blockPrivateNetworks = true;

    public boolean isEnforceWhitelist() {
        return enforceWhitelist;
    }

    public void setEnforceWhitelist(boolean enforceWhitelist) {
        this.enforceWhitelist = enforceWhitelist;
    }

    public List<String> getAllowedDomains() {
        return allowedDomains;
    }

    public void setAllowedDomains(List<String> allowedDomains) {
        this.allowedDomains = allowedDomains;
    }

    public boolean isBlockPrivateNetworks() {
        return blockPrivateNetworks;
    }

    public void setBlockPrivateNetworks(boolean blockPrivateNetworks) {
        this.blockPrivateNetworks = blockPrivateNetworks;
    }
}

