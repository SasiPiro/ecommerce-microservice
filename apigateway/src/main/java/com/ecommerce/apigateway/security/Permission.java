package com.ecommerce.apigateway.security;

public class Permission {

    private Permission(){}

    public static final String USER_READ = "user.read";
    public static final String USER_WRITE = "user.write";
    public static final String USER_DELETE = "user.delete";

    public static final String PRODUCT_WRITE = "product.write";
    public static final String PRODUCT_DELETE = "product.delete";
    public static final String PRODUCT_READ = "product.read";

    public static final String CATEGORY_WRITE = "category.write";
    public static final String CATEGORY_DELETE = "category.delete";
    public static final String CATEGORY_READ = "category.read";

}
