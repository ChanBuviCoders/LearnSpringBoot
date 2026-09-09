package config.controller;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import config.DAO.TestPageable;
import config.DTO.CustomerPaymentDTO;
import config.DTO.PaymentInput;
import config.DTO.Response;
import config.Entity.Payments;
import config.Entity.CustomerList;
import config.Entity.UserAccount;
import config.Service.CustomerService;

@RestController()
@CrossOrigin(origins = "*")
@RequestMapping(path = "/api")
public class CustomerController {

	@Autowired
	CustomerService customerS;

	@Autowired
	TestPageable pagingDao;

	/* ****************************************************************************************/
	@RequestMapping(value = "/addCustomer", method = RequestMethod.POST, produces = "application/Json")
	public Response addCustomer(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerS.addCustomer(customerList);
		} catch (Exception e) {
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@RequestMapping(value = "/getCustomerDetailsById", method = RequestMethod.POST, produces = "application/Json")
	public Optional<CustomerList> getCustomerDetailsById(@RequestBody CustomerList customerList) {
		Optional<CustomerList> customerDetails = null;
		try {
			return customerDetails;
		} catch (Exception e) {
			return customerDetails;
		}
	}

	/*--------------------------------------update customer-------------------------------------*/
	@RequestMapping(value = "/updateCustomer", method = RequestMethod.POST, produces = "application/Json")
	public Response updateCustomer(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerS.updateCustomer(customerList);
		} catch (Exception e) {

			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	/*-------------------------------------------delete customer----------------------------------------*/
	@RequestMapping(value = "/deleteCustomer", method = RequestMethod.POST, produces = "application/Json")
	public Response deleteCustomer(@RequestBody CustomerList customerList) {
		Response response = new Response();
		try {
			return customerS.deleteCustomerByCustomerId(customerList.getCustomerId());
		} catch (Exception e) {
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@RequestMapping(value = "/getAllCustomerList", method = RequestMethod.POST, produces = "application/Json")
	public Response getAllCustomerList(@RequestBody CustomerList customerList) {
		Response response = new Response();

		try {
			return customerS.getAllCustomerListByUserAccountId(customerList);
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@RequestMapping(value = "/getAllCustomerList1", method = RequestMethod.GET, produces = "application/Json")
	public ResponseEntity<List<CustomerList>> getAllCustomerList1(Pageable pageable) {
		Page<CustomerList> acList = null;
		try {
			acList = pagingDao.findAll(pageable);
			acList.getContent();
			return ResponseEntity.status(HttpStatus.OK).body(acList.getContent());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.OK).body(acList.getContent());
		}
	}

	@RequestMapping(value = "/sendSmsToMobileNumber", method = RequestMethod.POST, produces = "application/Json")
	public ResponseEntity<String> sendSmsToMobileNumber() throws ParseException {
		return customerS.sendSmsToMobileNumber();
	}

	@RequestMapping(value = "/getChartDetails/{userAccountId}/{type}", method = RequestMethod.POST, produces = "application/Json")
	public Response getChartDetails(@PathVariable("userAccountId") long userAccountId,
			@PathVariable("type") Byte type) {
		Response response = new Response();

		try {
			return customerS.getchartDetails(userAccountId, type);
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@PostMapping("/getPaymentList")
	public Response getPaymentList(@RequestBody PaymentInput pi) {
		Response response = new Response();
		try {
			return customerS.getPaymentList(pi);
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@PostMapping("/changePaymentStatus")
	public Response changePaymentStatus(@RequestBody CustomerPaymentDTO cpd) {
		Response response = new Response();
		try {
			return customerS.changePaymentStatus(cpd);
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@GetMapping("/getPaymentListByCustomerId/{customerId}")
	public Response getPaymentListByCustomerId(@PathVariable("customerId") Long customerId) {
		Response response = new Response();
		try {
			return customerS.getPaymentListByCustomerId(customerId);
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

}
