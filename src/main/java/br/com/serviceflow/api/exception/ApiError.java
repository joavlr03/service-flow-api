package br.com.serviceflow.api.exception;

import java.time.Instant;
import java.util.Map;

public record ApiError(
        int status,
        String code,
        String message,
        Map<String, String> fields,
        Instant timestamp) {}
