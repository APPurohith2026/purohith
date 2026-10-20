package com.purohitcommunity.portal.web.validation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;
public class BirthDateValidator implements ConstraintValidator<BirthDate,LocalDate> {
    @Override public boolean isValid(LocalDate value,ConstraintValidatorContext context){
        return value==null || (value.getYear()>=1000 && value.getYear()<=9999 && !value.isAfter(LocalDate.now()));
    }
}
