package com.purohitcommunity.portal.web.validation;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;
@Target({ElementType.FIELD,ElementType.PARAMETER,ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=BirthDateValidator.class)
public @interface BirthDate {
    String message() default "Date of birth must have a four-digit year from 1000 through today";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
