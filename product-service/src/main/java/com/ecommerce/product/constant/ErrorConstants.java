package com.ecommerce.product.constant;

import java.net.URI;

public final class ErrorConstants {
    private ErrorConstants() {
    }

    public static final String BASE_URL = "https://api.ecommerce.it/errors/";
    public static final URI TYPE_CATEGORY_NOT_FOUND = URI.create(BASE_URL + "category-not-found");
    public static final URI TYPE_CATEGORY_CONFLICT = URI.create(BASE_URL + "category-already-exists");
    public static final URI TYPE_PRODUCT_NOT_FOUND = URI.create(BASE_URL + "product-not-found");
    public static final URI TYPE_PRODUCT_CONFLICT = URI.create(BASE_URL + "product-already-exists");
    public static final URI TYPE_VALIDATION_ERROR = URI.create(BASE_URL + "validation-error");
    public static final URI TYPE_GENERIC_ERROR = URI.create(BASE_URL + "internal-server-error");
}
