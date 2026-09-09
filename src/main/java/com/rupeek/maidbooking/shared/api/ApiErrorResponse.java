package com.rupeek.maidbooking.shared.api;

import java.time.Instant;
import java.util.Map;

public record ApiErrorResponse(Instant timestamp, int status, String code, String message,
                               Map<String, String> fieldErrors, String path) {
    public static ApiErrorResponse of(int status, String code, String message,
                                      Map<String, String> fieldErrors, String path) {
        return new ApiErrorResponse(Instant.now(), status, code, message, fieldErrors, path);
    }
}
