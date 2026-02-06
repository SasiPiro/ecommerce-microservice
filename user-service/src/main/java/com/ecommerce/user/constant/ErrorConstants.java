package com.ecommerce.user.constant;

import java.net.URI;

public final class ErrorConstants {
    private ErrorConstants() {} // Impedisce l'istanziazione

    public static final String BASE_URL = "https://api.tuo-progetto.it/errors/";
    public static final URI TYPE_USER_NOT_FOUND = URI.create(BASE_URL + "user-not-found");
    public static final URI TYPE_USER_CONFLICT = URI.create(BASE_URL + "user-already-exists");
    public static final URI TYPE_VALIDATION_ERROR = URI.create(BASE_URL + "validation-error");
    public static final URI TYPE_GENERIC_ERROR = URI.create(BASE_URL + "internal-server-error");
}