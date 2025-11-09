package io.github.mfthfzn.util;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Collection;
import java.util.Set;

public class ValidatorUtil {

  private static ValidatorFactory validatorFactory;

  private static Validator validator;

  static {
    validatorFactory = Validation.buildDefaultValidatorFactory();
    validator = validatorFactory.getValidator();
  }

  public static Set<ConstraintViolation<Object>> validate(Object object) {
    return validator.validate(object);
  }

}
