package config;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.springframework.beans.factory.annotation.Autowired;

import config.DAO.customerListR;

public class UniqueMobileNumberValidation implements ConstraintValidator<MobileNumberValidation, Long> {

	@Override
	public void initialize(MobileNumberValidation constraintAnnotation) {
		// TODO Auto-generated method stub

	}

	@Autowired
	customerListR customerList;

	@Override
	public boolean isValid(Long value, ConstraintValidatorContext context) {
		if (customerList.findAllByMobileNumber(value).size() == 0) {
			return true;
		}
		return false;
	}

}
