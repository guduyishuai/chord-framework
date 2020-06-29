package com.chord.framework.boot.autoconfigure.common;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import java.util.Optional;
import java.util.Set;

/**
 * Created on 2020/6/28
 *
 * @author: wulinfeng
 */
public class ValidationUtils<T> {

    public void validate(T t) {

        Set<ConstraintViolation<T>> result = Validation.buildDefaultValidatorFactory().getValidator().validate(t);
        result.forEach(violation ->
                Optional.ofNullable(violation.getInvalidValue())
                .ifPresent(invalidField->{
                    throw new IllegalArgumentException(violation.getMessage());
                }));

    }

}
