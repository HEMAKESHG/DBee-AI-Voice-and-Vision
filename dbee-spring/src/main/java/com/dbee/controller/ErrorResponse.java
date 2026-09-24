package com.dbee.controller;

import java.time.Instant;

public record ErrorResponse(String error, Instant timestamp) {
    public ErrorResponse(String error) { this(error, Instant.now()); }
}
