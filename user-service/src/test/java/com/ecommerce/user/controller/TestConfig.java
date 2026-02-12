package com.ecommerce.user.controller;

import com.ecommerce.user.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

@SpringBootConfiguration
@Import({UserController.class, GlobalExceptionHandler.class})
class TestConfig {}