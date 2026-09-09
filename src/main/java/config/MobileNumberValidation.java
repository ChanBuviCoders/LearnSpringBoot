package config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

@Target({ ElementType.FIELD, ElementType.METHOD, ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = { UniqueMobileNumberValidation.class })
public @interface MobileNumberValidation {

	public String message() default "Mobile number already exist";

	public Class<?>[] groups() default {};

	public Class<? extends Payload>[] payload() default {};

}
