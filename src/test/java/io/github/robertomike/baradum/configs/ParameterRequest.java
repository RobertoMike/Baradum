package io.github.robertomike.baradum.configs;

import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ParametersRequest.class)
@Test
public @interface ParameterRequest {
    String key();
    String value();
}

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@interface ParametersRequest {
    ParameterRequest[] value();
}
