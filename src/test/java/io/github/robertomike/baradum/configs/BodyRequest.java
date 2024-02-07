package io.github.robertomike.baradum.configs;

import org.junit.jupiter.api.Test;

import java.lang.annotation.*;

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(BodyRequests.class)
@Test
public @interface BodyRequest {
    String value();
}

@Target({ ElementType.TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@interface BodyRequests {
    BodyRequest[] value();
}
