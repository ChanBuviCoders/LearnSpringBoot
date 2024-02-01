package config.Service;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import config.DTO.response;
import config.Entity.customerList;

@Service
public interface customerService {

	response addCustomer(customerList customerList);

	response getchartDetails(Long userAccountId, Byte type);

	response getAllCustomerListByUserAccountId(long userAccountId);

	response deleteCustomerByCustomerId(long customerId);

	response updateCustomer(customerList customerList);

	ResponseEntity<String> sendSmsToMobileNumber() throws ParseException;

}
