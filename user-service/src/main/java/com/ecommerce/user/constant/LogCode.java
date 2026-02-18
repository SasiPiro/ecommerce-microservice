package com.ecommerce.user.constant;

/**
 * Structured log codes for observability and monitoring.
 * <p>
 * Convention: {@code USR-XXX} where:
 * <ul>
 * <li>{@code USR} = user-service domain prefix</li>
 * <li>{@code 0xx} = not-found errors</li>
 * <li>{@code 1xx} = conflict / uniqueness errors</li>
 * <li>{@code 2xx} = validation errors</li>
 * <li>{@code 9xx} = unexpected / internal errors</li>
 * </ul>
 * These codes are designed to be machine-parseable by ELK, Datadog, Grafana
 * Loki
 * and similar observability platforms for alert grouping and trend analysis.
 */
public enum LogCode {

    // --- 0xx: Resource not found ---
    USER_NOT_FOUND("USR-001", "User not found"),

    // --- 1xx: Conflict / uniqueness ---
    USERNAME_ALREADY_EXISTS("USR-100", "Username already exists"),
    EMAIL_ALREADY_EXISTS("USR-101", "Email already exists"),

    // --- 2xx: Validation ---
    VALIDATION_FAILED("USR-200", "Validation failed"),

    // --- 9xx: Unexpected / internal ---
    INTERNAL_ERROR("USR-900", "Unhandled internal error");

    private final String code;
    private final String description;

    LogCode(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String code() {
        return code;
    }

    public String description() {
        return description;
    }

    @Override
    public String toString() {
        return code;
    }
}
