package config.controller;

import java.text.ParseException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import config.DAO.TestPageable;
import config.DTO.CustomerPaymentDTO;
import config.DTO.PaymentInput;
import config.DTO.Response;
import config.Entity.CustomerList;
import config.Entity.Payments;
import config.Service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(path = "/api")
@Tag(name = "Customer & Payments", description = "Customer and payment CRUD APIs")
public class CustomerController {

	private static final Logger logger = LoggerFactory.getLogger(CustomerController.class);

	@Autowired
	private CustomerService customerService;

	@Autowired
	private TestPageable pagingDao;

	@Operation(summary = "Create customer")
	@PostMapping(value = "/addCustomer", produces = "application/json")
	public Response addCustomer(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerService.addCustomer(customerList);
		} catch (Exception e) {
			logger.error("addCustomer failed", e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Get customer by id")
	@PostMapping(value = "/getCustomerDetailsById", produces = "application/json")
	public Response getCustomerDetailsById(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerService.getCustomerDetailsById(customerList.getCustomerId());
		} catch (Exception e) {
			logger.error("getCustomerDetailsById failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Operation(summary = "Get customer by id (REST path)")
	@GetMapping(value = "/customers/{customerId}", produces = "application/json")
	public Response getCustomerById(@PathVariable Long customerId) {
		return customerService.getCustomerDetailsById(customerId);
	}

	@Operation(summary = "Update customer")
	@PostMapping(value = "/updateCustomer", produces = "application/json")
	public Response updateCustomer(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerService.updateCustomer(customerList);
		} catch (Exception e) {
			logger.error("updateCustomer failed", e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Delete customer")
	@PostMapping(value = "/deleteCustomer", produces = "application/json")
	public Response deleteCustomer(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerService.deleteCustomerByCustomerId(customerList.getCustomerId());
		} catch (Exception e) {
			logger.error("deleteCustomer failed", e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "List customers by user account")
	@PostMapping(value = "/getAllCustomerList", produces = "application/json")
	public Response getAllCustomerList(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerService.getAllCustomerListByUserAccountId(customerList);
		} catch (Exception e) {
			logger.error("getAllCustomerList failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Paged customer list")
	@GetMapping(value = "/getAllCustomerList1", produces = "application/json")
	public ResponseEntity<List<CustomerList>> getAllCustomerList1(Pageable pageable) {
		try {
			Page<CustomerList> page = pagingDao.findAll(pageable);
			return ResponseEntity.status(HttpStatus.OK).body(page.getContent());
		} catch (Exception e) {
			logger.error("getAllCustomerList1 failed", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
		}
	}

	@Operation(summary = "Send SMS via Twilio")
	@PostMapping(value = "/sendSmsToMobileNumber", produces = "application/json")
	public ResponseEntity<String> sendSmsToMobileNumber() throws ParseException {
		return customerService.sendSmsToMobileNumber();
	}

	@Operation(summary = "Chart details for dashboard")
	@PostMapping(value = "/getChartDetails/{userAccountId}/{type}", produces = "application/json")
	public Response getChartDetails(@PathVariable("userAccountId") long userAccountId,
			@PathVariable("type") Byte type) {
		Response response = new Response();
		try {
			return customerService.getchartDetails(userAccountId, type);
		} catch (Exception e) {
			logger.error("getChartDetails failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Get or generate daily payment list")
	@PostMapping("/getPaymentList")
	public Response getPaymentList(@RequestBody PaymentInput paymentInput) {
		Response response = new Response();
		try {
			return customerService.getPaymentList(paymentInput);
		} catch (Exception e) {
			logger.error("getPaymentList failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Change payment status")
	@PostMapping("/changePaymentStatus")
	public Response changePaymentStatus(@RequestBody CustomerPaymentDTO customerPaymentDTO) {
		Response response = new Response();
		try {
			return customerService.changePaymentStatus(customerPaymentDTO);
		} catch (Exception e) {
			logger.error("changePaymentStatus failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Payments by customer id")
	@GetMapping("/getPaymentListByCustomerId/{customerId}")
	public Response getPaymentListByCustomerId(@PathVariable("customerId") Long customerId) {
		Response response = new Response();
		try {
			return customerService.getPaymentListByCustomerId(customerId);
		} catch (Exception e) {
			logger.error("getPaymentListByCustomerId failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Operation(summary = "Get payment by id")
	@GetMapping("/payments/{paymentId}")
	public Response getPaymentById(@PathVariable Long paymentId) {
		return customerService.getPaymentById(paymentId);
	}

	@Operation(summary = "Update payment")
	@PostMapping("/updatePayment")
	public Response updatePayment(@RequestBody Payments payment) {
		return customerService.updatePayment(payment);
	}

	@Operation(summary = "Delete payment")
	@PostMapping("/deletePayment")
	public Response deletePayment(@RequestBody Payments payment) {
		return customerService.deletePayment(payment.getPaymentId());
	}
}
