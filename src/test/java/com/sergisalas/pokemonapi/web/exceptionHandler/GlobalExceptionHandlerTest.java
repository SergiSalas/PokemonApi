package com.sergisalas.pokemonapi.web.exceptionHandler;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;


import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntimeException_withIllegalArgumentCause_shouldReturnBadRequest() {
        // Given
        IllegalArgumentException cause = new IllegalArgumentException("Invalid parameter");
        RuntimeException ex = new RuntimeException("Wrapper exception", cause);

        // When
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);

        // Then
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid parameter", response.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_withoutIllegalArgumentCause_shouldReturnInternalServerError() {
        // Given
        RuntimeException ex = new RuntimeException("Generic error");

        // When
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("error"));
    }

    @Test
    void handleRuntimeException_withDifferentCause_shouldReturnInternalServerError() {
        // Given
        NullPointerException cause = new NullPointerException("Null value");
        RuntimeException ex = new RuntimeException("Wrapper", cause);

        // When
        ResponseEntity<Map<String, String>> response = handler.handleRuntimeException(ex);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Internal server error", response.getBody().get("error"));
    }
}
