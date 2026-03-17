package com.inigosanz.backend.infrastructure.adapter.out.scanner;

import com.inigosanz.backend.shared.exception.UnsafeScanTargetException;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
public class ScanTargetSecurityPolicy {

    private final ScannerSecurityProperties properties;

    public ScanTargetSecurityPolicy(ScannerSecurityProperties properties) {
        this.properties = properties;
    }

    public void validateTarget(String rawUrl) {
        Objects.requireNonNull(rawUrl, "pageUrl is required");

        URI uri = parseUri(rawUrl);
        String host = normalizeHost(uri);

        if (properties.isEnforceWhitelist() && !isAllowedByWhitelist(host)) {
            throw new UnsafeScanTargetException("Target host is not in whitelist: " + host);
        }

        if (!properties.isBlockPrivateNetworks()) {
            return;
        }

        if ("localhost".equals(host)) {
            throw new UnsafeScanTargetException("Target host resolves to a blocked private network: " + host);
        }

        InetAddress[] addresses = resolveAddresses(host);
        for (InetAddress address : addresses) {
            if (isBlockedAddress(address)) {
                throw new UnsafeScanTargetException("Target host resolves to a blocked private network: " + host);
            }
        }
    }

    private URI parseUri(String rawUrl) {
        try {
            URI uri = new URI(rawUrl);
            String scheme = uri.getScheme();
            if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
                throw new UnsafeScanTargetException("Only http/https URLs are allowed");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new UnsafeScanTargetException("URL host is required");
            }
            return uri;
        } catch (URISyntaxException exception) {
            throw new UnsafeScanTargetException("Invalid target URL format");
        }
    }

    private String normalizeHost(URI uri) {
        return uri.getHost().trim().toLowerCase(Locale.ROOT);
    }

    private boolean isAllowedByWhitelist(String host) {
        List<String> allowedDomains = properties.getAllowedDomains();
        if (allowedDomains == null || allowedDomains.isEmpty()) {
            return false;
        }
        return allowedDomains.stream()
                .filter(domain -> domain != null && !domain.isBlank())
                .map(domain -> domain.trim().toLowerCase(Locale.ROOT))
                .anyMatch(allowed -> host.equals(allowed) || host.endsWith("." + allowed));
    }

    private InetAddress[] resolveAddresses(String host) {
        try {
            return InetAddress.getAllByName(host);
        } catch (UnknownHostException exception) {
            throw new UnsafeScanTargetException("Target host could not be resolved: " + host);
        }
    }

    private boolean isBlockedAddress(InetAddress address) {
        if (address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isLinkLocalAddress()
                || address.isSiteLocalAddress()
                || address.isMulticastAddress()) {
            return true;
        }

        if (address instanceof Inet4Address inet4Address) {
            return isPrivateIpv4(inet4Address.getAddress());
        }

        if (address instanceof Inet6Address inet6Address) {
            return isUniqueLocalIpv6(inet6Address.getAddress());
        }

        return false;
    }

    private boolean isPrivateIpv4(byte[] bytes) {
        int first = bytes[0] & 0xFF;
        int second = bytes[1] & 0xFF;

        return first == 10
                || first == 127
                || (first == 172 && second >= 16 && second <= 31)
                || (first == 192 && second == 168)
                || (first == 169 && second == 254);
    }

    private boolean isUniqueLocalIpv6(byte[] bytes) {
        int first = bytes[0] & 0xFF;
        return (first & 0xFE) == 0xFC;
    }
}


