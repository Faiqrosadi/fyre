package com.fyre.annotations;

import com.fyre.validators.UniqueEmailValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// anotasi validator untuk mengatasi jika melakukan registrasi dengan email yangs ama
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {UniqueEmailValidator.class})
public @interface UniqueEmail {
    String message();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
