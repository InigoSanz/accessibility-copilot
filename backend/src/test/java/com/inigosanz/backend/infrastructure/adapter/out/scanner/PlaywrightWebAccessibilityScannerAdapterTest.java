package com.inigosanz.backend.infrastructure.adapter.out.scanner;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaywrightWebAccessibilityScannerAdapterTest {

    @Test
    void shouldLoadLocalAxeScriptFromClasspathOnAdapterCreation() throws Exception {
        PlaywrightWebAccessibilityScannerAdapter adapter = new PlaywrightWebAccessibilityScannerAdapter(new ObjectMapper());

        Field axeScriptField = PlaywrightWebAccessibilityScannerAdapter.class.getDeclaredField("axeScriptContent");
        axeScriptField.setAccessible(true);

        String axeScriptContent = (String) axeScriptField.get(adapter);
        assertNotNull(axeScriptContent);
        assertFalse(axeScriptContent.isBlank());
        assertTrue(axeScriptContent.length() > 1_000);
        assertTrue(axeScriptContent.toLowerCase().contains("axe"));
    }

    @Test
    void shouldHaveVendoredAxeScriptInExpectedResourcePath() throws Exception {
        ClassPathResource resource = new ClassPathResource("scanner/axe.min.js");

        assertTrue(resource.exists());
        String content;
        try (InputStream inputStream = resource.getInputStream()) {
            content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
        assertFalse(content.isBlank());
        assertTrue(content.length() > 1_000);
    }
}


