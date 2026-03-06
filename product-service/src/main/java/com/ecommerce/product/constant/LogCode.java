package com.ecommerce.product.constant;

/**
 * Structured log codes for observability and monitoring.
 * <p>
 * Convention: {@code PRD-XXX} where:
 * <ul>
 * <li>{@code PRD} = product-service domain prefix</li>
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
    CATEGORY_NOT_FOUND("PRD-001", "Category not found"),
    PRODUCT_NOT_FOUND("PRD-002", "Product not found"),

    // --- 1xx: Conflict / uniqueness ---
    CATEGORY_NAME_ALREADY_EXISTS("PRD-100", "Category name already exists"),
    PRODUCT_NAME_ALREADY_EXISTS("PRD-101", "Product name already exists"),

    // --- 2xx: Validation ---
    VALIDATION_FAILED("PRD-200", "Validation failed"),

    // --- 9xx: Unexpected / internal ---
    INTERNAL_ERROR("PRD-900", "Unhandled internal error");

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
