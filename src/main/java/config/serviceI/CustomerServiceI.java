package config.serviceI;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import config.DAO.CustomerListR;
import config.DAO.PaymentsR;
import config.DTO.CustomerPaymentDTO;
import config.DTO.PaymentInput;
import config.DTO.Response;
import config.Entity.CustomerList;
import config.Entity.Payments;
import config.Service.CustomerService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomerServiceI implements CustomerService {

	private static final Logger logger = LoggerFactory.getLogger(CustomerServiceI.class);

	private final CustomerListR customerListR;
	private final PaymentsR paymentR;

	@Value("${twilio.account-sid:}")
	private String twilioAccountSid;

	@Value("${twilio.auth-token:}")
	private String twilioAuthToken;

	@Value("${twilio.from-number:}")
	private String twilioFromNumber;

	@Value("${twilio.to-number:}")
	private String twilioToNumber;

	@Value("${twilio.message-body:Hello from Learn Spring Boot}")
	private String twilioMessageBody;

	@Override
	public Response addCustomer(CustomerList customerList) {
		Response response = new Response();
		try {
			if (customerList.getLoanType() != null && customerList.getLoanType().equalsIgnoreCase("weekly")) {
				Long totalPayable = customerList.getLoanAmount() + (20 * customerList.getLoanAmount()) / 100;
				customerList.setTotalPayable(totalPayable);
			} else {
				customerList.setTotalPayable(customerList.getTotalPayable());
			}
			customerListR.save(customerList);
			response.setMessage("Customer Added Successfully");
			response.setStatus(true);
			logger.info("Customer added: {}", customerList.getCustomerId());
			return response;
		} catch (Exception e) {
			logger.error("addCustomer failed", e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public Response getCustomerDetailsById(Long customerId) {
		Response response = new Response();
		try {
			CustomerList customer = customerListR.findByCustomerId(customerId);
			if (customer == null) {
				response.setStatus(false);
				response.setMessage("Customer not found");
				return response;
			}
			response.setData(customer);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getCustomerDetailsById failed for id {}", customerId, e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	public Response updateCustomer(CustomerList customerList) {
		Response response = new Response();
		try {
			if (customerList != null && customerList.getCustomerId() != null) {
				CustomerList customerListFr = customerListR.findByCustomerId(customerList.getCustomerId());
				customerListFr.setFirstName(customerList.getFirstName());
				customerListFr.setLastName(customerList.getLastName());
				customerListFr.setGender(customerList.getGender());
				customerListFr.setLoanAmount(customerList.getLoanAmount());
				customerListFr.setLoanType(customerList.getLoanType());
				customerListFr.setMobileNumber(customerList.getMobileNumber());
				customerListR.save(customerListFr);
				response.setMessage("Customer Details Updated Successfully");
				response.setStatus(true);
				return response;
			}
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		} catch (Exception e) {
			logger.error("updateCustomer failed", e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public Response deleteCustomerByCustomerId(long customerId) {
		Response response = new Response();
		try {
			customerListR.deleteById(customerId);
			response.setMessage("Deleted Successfully");
			response.setStatus(true);
			logger.info("Customer deleted: {}", customerId);
			return response;
		} catch (Exception e) {
			logger.error("deleteCustomer failed for id {}", customerId, e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public Response getAllCustomerListByUserAccountId(CustomerList customerFilter) {
		Response response = new Response();
		try {
			List<CustomerList> acList;
			if (customerFilter.getLoanType() != null && customerFilter.getLoanType().equalsIgnoreCase("all")) {
				acList = customerListR.findAllByUserAccountId(customerFilter.getUserAccountId());
			} else {
				acList = customerListR.findAllByUserAccountIdAndLoanType(customerFilter.getUserAccountId(),
						customerFilter.getLoanType());
			}
			response.setData(acList);
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			logger.error("getAllCustomerListByUserAccountId failed", e);
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public ResponseEntity<String> sendSmsToMobileNumber() throws ParseException {
		if (twilioAccountSid == null || twilioAccountSid.isBlank() || twilioAuthToken == null
				|| twilioAuthToken.isBlank()) {
			logger.warn("Twilio credentials are not configured");
			return new ResponseEntity<>("Twilio is not configured", HttpStatus.SERVICE_UNAVAILABLE);
		}
		Twilio.init(twilioAccountSid, twilioAuthToken);
		Message.creator(new PhoneNumber(twilioToNumber), new PhoneNumber(twilioFromNumber), twilioMessageBody).create();
		logger.info("SMS sent successfully via Twilio");
		return new ResponseEntity<>("Message sent successfully", HttpStatus.OK);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response getchartDetails(Long userAccountId, Byte type) {
		Response response = new Response();
		try {
			JSONObject json = new JSONObject();
			json.put("chartData", getChartData(userAccountId, type));

			String[] monthLists = customerListR.getMonthLists(userAccountId);
			JSONArray ja = new JSONArray();
			for (String month : monthLists) {
				ja.add(month);
			}
			json.put("chartLabels", ja);

			response.setStatus(true);
			response.setData(json);
			return response;
		} catch (Exception e) {
			logger.error("getchartDetails failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray getChartData(Long userAccountId, Byte type) {
		JSONArray json = new JSONArray();
		try {
			List<String> loanTypes = customerListR.findDistinctLoanTypeByUserAccountId(userAccountId);
			for (String loanType : loanTypes) {
				JSONArray jsonArray = new JSONArray();
				String[] monthLists = customerListR.getMonthLists(userAccountId);
				for (String month : monthLists) {
					if (type == 1) {
						jsonArray.add(customerListR.getCustomerCountByMonth(month, loanType, userAccountId));
					} else {
						Long sum = customerListR.getSumOfLoanAmountByLoanType(month, loanType, userAccountId);
						jsonArray.add(sum != null ? sum : 0);
					}
				}
				JSONObject inner = new JSONObject();
				inner.put("label", loanType);
				inner.put("data", jsonArray);
				json.add(inner);
			}
		} catch (Exception e) {
			logger.error("getChartData failed", e);
		}
		return json;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Response getPaymentList(PaymentInput paymentInput) {
		Response response = new Response();
		try {
			List<Payments> paymentList = paymentR.findAllByLoanTypeAndDate(paymentInput.getLoanType(),
					paymentInput.getDate());
			List<CustomerList> customerList = customerListR.findByLoanTypeAndStartDateLessThanEqualAndUserAccountId(
					paymentInput.getLoanType(), paymentInput.getDate(), paymentInput.getUserAccountId());

			if (paymentList.isEmpty()) {
				customerList.forEach(customer -> createPaymentList(customer, paymentInput));
			} else if (paymentList.size() != customerList.size()) {
				List<Long> customerIdList = paymentList.stream().map(Payments::getCustomerId)
						.collect(Collectors.toList());
				customerList.stream().filter(c -> !customerIdList.contains(c.getCustomerId()))
						.forEach(customer -> createPaymentList(customer, paymentInput));
			}

			Long todayCredit = paymentR.findTodayCredit(paymentInput.getDate(), paymentInput.getLoanType());
			JSONObject json = new JSONObject();
			json.put("todayCredit", todayCredit);
			json.put("paymentList", getCustomerPaymentList(paymentInput));
			response.setData(json);
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			logger.error("getPaymentList failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	public void createPaymentList(CustomerList customer, PaymentInput paymentInput) {
		try {
			Payments payment = new Payments();
			payment.setCustomerId(customer.getCustomerId());
			if (paymentInput.getLoanType().equalsIgnoreCase("Daily")) {
				payment.setAmount(customer.getLoanAmount() / 100);
			} else if (paymentInput.getLoanType().equalsIgnoreCase("Weekly")) {
				payment.setAmount(customer.getLoanAmount() * 10 / 100);
			} else {
				payment.setAmount(customer.getLoanAmount());
			}
			payment.setDate(paymentInput.getDate());
			payment.setLoanType(customer.getLoanType());
			paymentR.save(payment);
		} catch (Exception e) {
			logger.error("createPaymentList failed for customer {}", customer.getCustomerId(), e);
		}
	}

	public List<CustomerPaymentDTO> getCustomerPaymentList(PaymentInput paymentInput) {
		List<CustomerPaymentDTO> result = new ArrayList<>();
		List<Payments> paymentList = paymentR.findAllByLoanTypeAndDate(paymentInput.getLoanType(),
				paymentInput.getDate());
		for (Payments payment : paymentList) {
			CustomerPaymentDTO dto = new CustomerPaymentDTO();
			dto.setAmount(payment.getAmount());
			dto.setDate(payment.getDate());
			dto.setPaymentId(payment.getPaymentId());
			dto.setPaymentStatus(payment.getPaymentStatus());
			dto.setPaymentMode(payment.getPaymentMode());
			if (payment.getCustomerList() != null) {
				dto.setFirstName(payment.getCustomerList().getFirstName());
				dto.setLastName(payment.getCustomerList().getLastName());
				dto.setLoanAmount(payment.getCustomerList().getLoanAmount());
				dto.setLoanType(payment.getCustomerList().getLoanType());
				dto.setTotalPayable(payment.getCustomerList().getTotalPayable());
				dto.setTotalPaid(payment.getCustomerList().getTotalPaid());
				Long totalPayable = payment.getCustomerList().getTotalPayable() != null
						? payment.getCustomerList().getTotalPayable()
						: 0L;
				Long totalPaid = payment.getCustomerList().getTotalPaid() != null
						? payment.getCustomerList().getTotalPaid()
						: 0L;
				dto.setBalanceAmount(totalPayable - totalPaid);
			}
			result.add(dto);
		}
		return result;
	}

	@Override
	@Transactional
	public Response changePaymentStatus(CustomerPaymentDTO customerPaymentDTO) {
		Response response = new Response();
		try {
			Payments payment = paymentR.findByPaymentId(customerPaymentDTO.getPaymentId());
			if (payment == null) {
				response.setStatus(false);
				response.setMessage("Payment not found");
				return response;
			}
			payment.setPaymentStatus(customerPaymentDTO.getPaymentStatus());
			payment.setAmount(customerPaymentDTO.getAmount());
			payment.setPaymentMode(customerPaymentDTO.getPaymentMode());
			paymentR.save(payment);
			setTotalPaidAmountByCustomerId(payment.getCustomerId());

			Map<String, Long> todayCreditMap = new HashMap<>();
			todayCreditMap.put("todayCredit",
					getTodayCreditAmount(customerPaymentDTO.getDate(), customerPaymentDTO.getLoanType()));
			response.setData(todayCreditMap);
			response.setStatus(true);
			response.setMessage("Payment status updated");
			logger.info("Payment status updated for paymentId={}", customerPaymentDTO.getPaymentId());
			return response;
		} catch (Exception e) {
			logger.error("changePaymentStatus failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	public Long getTodayCreditAmount(Date date, String loanType) {
		try {
			return paymentR.findTodayCredit(date, loanType);
		} catch (Exception e) {
			logger.error("getTodayCreditAmount failed", e);
			return null;
		}
	}

	public void setTotalPaidAmountByCustomerId(Long customerId) {
		try {
			CustomerList customerList = customerListR.findByCustomerId(customerId);
			Long totalPaidAmount = paymentR.findTotalPaidAmountByCustomerId(customerId);
			customerList.setTotalPaid(totalPaidAmount != null ? totalPaidAmount : 0L);
			customerListR.save(customerList);
		} catch (Exception e) {
			logger.error("setTotalPaidAmountByCustomerId failed for {}", customerId, e);
		}
	}

	@Override
	public Response getPaymentListByCustomerId(Long customerId) {
		Response response = new Response();
		try {
			List<Payments> paymentList = paymentR.findAllByCustomerIdAndPaymentStatusTrue(customerId);
			response.setData(paymentList);
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			logger.error("getPaymentListByCustomerId failed", e);
			response.setMessage("Something Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public Response getPaymentById(Long paymentId) {
		Response response = new Response();
		try {
			Payments payment = paymentR.findByPaymentId(paymentId);
			if (payment == null) {
				response.setStatus(false);
				response.setMessage("Payment not found");
				return response;
			}
			response.setData(payment);
			response.setStatus(true);
			response.setMessage("Success");
			return response;
		} catch (Exception e) {
			logger.error("getPaymentById failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	@Transactional
	public Response deletePayment(Long paymentId) {
		Response response = new Response();
		try {
			Payments payment = paymentR.findByPaymentId(paymentId);
			if (payment == null) {
				response.setStatus(false);
				response.setMessage("Payment not found");
				return response;
			}
			Long customerId = payment.getCustomerId();
			paymentR.deleteById(paymentId);
			setTotalPaidAmountByCustomerId(customerId);
			response.setStatus(true);
			response.setMessage("Payment deleted successfully");
			logger.info("Payment deleted: {}", paymentId);
			return response;
		} catch (Exception e) {
			logger.error("deletePayment failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}

	@Override
	@Transactional
	public Response updatePayment(Payments paymentRequest) {
		Response response = new Response();
		try {
			Payments payment = paymentR.findByPaymentId(paymentRequest.getPaymentId());
			if (payment == null) {
				response.setStatus(false);
				response.setMessage("Payment not found");
				return response;
			}
			if (paymentRequest.getAmount() != null) {
				payment.setAmount(paymentRequest.getAmount());
			}
			if (paymentRequest.getPaymentMode() != null) {
				payment.setPaymentMode(paymentRequest.getPaymentMode());
			}
			if (paymentRequest.getPaymentStatus() != null) {
				payment.setPaymentStatus(paymentRequest.getPaymentStatus());
			}
			if (paymentRequest.getDate() != null) {
				payment.setDate(paymentRequest.getDate());
			}
			paymentR.save(payment);
			setTotalPaidAmountByCustomerId(payment.getCustomerId());
			response.setData(payment);
			response.setStatus(true);
			response.setMessage("Payment updated successfully");
			return response;
		} catch (Exception e) {
			logger.error("updatePayment failed", e);
			response.setStatus(false);
			response.setMessage("Something Went Wrong");
			return response;
		}
	}
}
