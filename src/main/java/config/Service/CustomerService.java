package config.Service;

import java.text.ParseException;

import org.springframework.http.ResponseEntity;

import config.DTO.CustomerPaymentDTO;
import config.DTO.PaymentInput;
import config.DTO.Response;
import config.Entity.CustomerList;
import config.Entity.Payments;

public interface CustomerService {

	Response addCustomer(CustomerList customerList);

	Response getCustomerDetailsById(Long customerId);

	Response getchartDetails(Long userAccountId, Byte type);

	Response getAllCustomerListByUserAccountId(CustomerList customerList);

	Response deleteCustomerByCustomerId(long customerId);

	Response updateCustomer(CustomerList customerList);

	ResponseEntity<String> sendSmsToMobileNumber() throws ParseException;

	Response getPaymentList(PaymentInput pi);

	Response changePaymentStatus(CustomerPaymentDTO cpd);

	Response getPaymentListByCustomerId(Long customerId);

	Response getPaymentById(Long paymentId);

	Response deletePayment(Long paymentId);

	Response updatePayment(Payments payment);
}
