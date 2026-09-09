package config.serviceI;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import config.DAO.PaymentsR;
import config.DAO.customerListR;
import config.DAO.testPageable;
import config.DTO.CustomerPaymentDTO;
import config.DTO.PaymentInput;
import config.DTO.response;
import config.Entity.Payments;
import config.Entity.customerList;
import config.Service.customerService;
import config.commonConfig.CustomException;
import jakarta.transaction.Transactional;

@Service
public class customerServiceI implements customerService {
	@Autowired
	customerListR customerListR;

	/* *************************************************************************/
	@Override
	public response addCustomer(customerList customerList) {
		response response = new response();
		try {
			customerList.getLoanAmount();
			if (customerList.getLoanType().equalsIgnoreCase("weekly")) {
				Long tP = customerList.getLoanAmount() + (20 * customerList.getLoanAmount()) / 100;
				customerList.setTotalPayable(tP);
			} else {
				customerList.setTotalPayable(customerList.getTotalPayable());
			}
			customerListR.save(customerList);
			response.setMessage("Customer Added Successfully");
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	/*---------------------------------------------update customer-------------------------------------*/
	@Override
	public response updateCustomer(customerList customerList) {
		response response = new response();
		try {

			if (customerList != null) {
				customerList customerListFr = customerListR.findByCustomerId(customerList.getCustomerId());
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
			} else {
				response.setMessage("invalid data");
				response.setStatus(false);
				return response;
			}
		} catch (Exception e) {

			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	// ---------------------------------------------delete
	// customer---------------------------------------->

	@Override
	public response deleteCustomerByCustomerId(long customerId) {
		response response = new response();
		try {
			customerListR.deleteById(customerId);
			response.setMessage("Deleted Successfully");
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Override
	public response getAllCustomerListByUserAccountId(customerList cL) {
		List<customerList> acList = null;
		response response = new response();
		try {
			if (cL.getLoanType().equalsIgnoreCase("all")) {
				acList = (List<customerList>) customerListR.findAllByUserAccountId(cL.getUserAccountId());
			} else {
				acList = (List<customerList>) customerListR.findAllByUserAccountIdAndLoanType(cL.getUserAccountId(),
						cL.getLoanType());
			}
			if (acList.size() > 0) {
				Map<String, List<customerList>> nl = acList.stream()
						.collect(Collectors.groupingBy(customerList::getLoanType));
				nl.forEach((loanType, customerList) -> {
					System.out.println("Department: " + loanType);
					customerList.forEach(user -> System.out.println(" - " + user.getFirstName()));
				});
				Map<String, Map<Long, List<customerList>>> map = acList.stream().collect(Collectors
						.groupingBy(customerList::getLoanType, Collectors.groupingBy(customerList::getLoanAmount)));
				map.forEach((loantype, values) -> {
					System.out.println("loanType" + loantype);
					values.forEach((loanAmount, customerL) -> {
						System.out.println("--loanAmount" + loanAmount);
						customerL.forEach(cus -> {
							System.out.println("======>" + cus.getFirstName());
						});
					});
				});
			}
			response.setData(acList);
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			response.setMessage("invalid data");
			response.setStatus(false);
			return response;
		}
	}

	@Autowired
	testPageable pagingDao;

	@RequestMapping(value = "/getAllCustomerList1", method = RequestMethod.GET, produces = "application/Json")
	public ResponseEntity<List<customerList>> getAllCustomerList1(Pageable pageable) {
		Page<customerList> acList = null;
		try {
			acList = pagingDao.findAll(pageable);
			acList.getContent();
			return ResponseEntity.status(HttpStatus.OK).body(acList.getContent());
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.OK).body(acList.getContent());
		}
	}

	public static final String ACCOUNT_SID = "AC085b4974038b667a53ff0fcaaf20e9c3";
	public static final String AUTH_TOKEN = "35e396c4ec0e5a17a2684970bbf3dbc6";

	@SuppressWarnings("unused")
	@Override
	public ResponseEntity<String> sendSmsToMobileNumber() throws ParseException {
		Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
		Message message = Message.creator(new PhoneNumber("+919965054383"), new PhoneNumber("+18064509304"),
				"Hello chandran this is your first sms  from java  please enjoy the end 📞 ,ABCDEFGHIJKLMNOPQRSTUVWXYZ,1234567890,இந்திய மொழிகளில் தமிழில், சமஸ்கிருதமற்ற இந்திய இலக்கியம் மிகவும் பழமையானது. அறிஞர்கள் ...")
				.create();
		return new ResponseEntity<String>("Message sent successfully", HttpStatus.OK);

	}

	@SuppressWarnings("unchecked")
	@Override
	public response getchartDetails(Long userAccountId, Byte type) {
		response response = new response();
		try {

			JSONObject json = new JSONObject();

			json.put("chartData", getchartData(userAccountId, type));

			String monthLists[] = customerListR.getMonthLists(userAccountId);

			json.put("chartLabels", monthLists);

			response.setStatus(true);
			response.setData(json);
			return response;
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	@SuppressWarnings("unchecked")
	public JSONArray getchartData(Long userAccountId, Byte type) {
		JSONArray json = new JSONArray();

		try {

			List<String> loanTypes = customerListR.findDistinctLoanTypeByUserAccountId(userAccountId);

			for (String loanType : loanTypes) {

				JSONArray jsonArray = new JSONArray();

				String monthLists[] = customerListR.getMonthLists(userAccountId);

				for (String month : monthLists) {
					if (type == 1)
						jsonArray.add(customerListR.getCustomerCountByMonth(month, loanType, userAccountId));
					else
						jsonArray.add(customerListR.getSumOfLoanAmountByLoanType(month, loanType, userAccountId) != null
								? customerListR.getSumOfLoanAmountByLoanType(month, loanType, userAccountId)
								: 0);
				}

				JSONObject jinner = new JSONObject();

				jinner.put("label", loanType);
				jinner.put("data", jsonArray);

				json.add(jinner);
			}

		} catch (Exception e) {
			// TODO: handle exception
			System.out.println("exception------>" + e);
		}
		return json;
	}

	@Autowired
	PaymentsR paymentR;

	@SuppressWarnings("unchecked")
	@Override
	public response getPaymentList(PaymentInput pi) {
		response response = new response();
		try {
			List<Payments> pl = paymentR.findAllByLoanTypeAndDate(pi.getLoanType(), pi.getDate());
			List<customerList> cl = customerListR.findByLoanTypeAndStartDateLessThanEqualAndUserAccountId(
					pi.getLoanType(), pi.getDate(), pi.getUserAccountId());
			if (pl.size() == 0) {
				cl.stream().forEach(m -> {
					createPaymentList(m, pi);
				});

			} else if (pl.size() != cl.size()) {
				List<Long> customerIdList = pl.stream().map(p -> p.getCustomerId()).collect(Collectors.toList());
				cl.stream().filter(c -> !customerIdList.contains(c.getCustomerId())).forEach(m -> {
					createPaymentList(m, pi);
				});
			}
			Long todayCredit = paymentR.findTodayCredit(pi.getDate(), pi.getLoanType());
			JSONObject json = new JSONObject();
			json.put("todayCredit", todayCredit);
			json.put("paymentList", getCustomerPaymentList(pi));
			response.setData(json);
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	public void createPaymentList(customerList c, PaymentInput pi) {
		try {

			Payments payment = new Payments();
			payment.setCustomerId(c.getCustomerId());
			if (pi.getLoanType().equalsIgnoreCase("Daily"))
				payment.setAmount(c.getLoanAmount() * 1 / 100);
			else if (pi.getLoanType().equalsIgnoreCase("Weekly"))
				payment.setAmount(c.getLoanAmount() * 10 / 100);
			else
				payment.setAmount(c.getLoanAmount());
			payment.setDate(pi.getDate());
			payment.setLoanType(c.getLoanType());
			paymentR.save(payment);

		} catch (Exception e) {
			System.out.println("Exception in CreatePayment Method");
		}
	}

	public List<CustomerPaymentDTO> getCustomerPaymentList(PaymentInput pi) {
		List<CustomerPaymentDTO> cpdl = new ArrayList<CustomerPaymentDTO>();
		List<Payments> pl = paymentR.findAllByLoanTypeAndDate(pi.getLoanType(), pi.getDate());
		if (pl.size() > 0) {
			for (int j = 0; j < pl.size(); j++) {
				Payments p = pl.get(j);
				CustomerPaymentDTO cpd = new CustomerPaymentDTO();
				cpd.setAmount(p.getAmount());
				cpd.setDate(p.getDate());
				cpd.setPaymentId(p.getPaymentId());
				cpd.setPaymentStatus(p.getPaymentStatus());
				cpd.setPaymentMode(p.getPaymentMode());
				cpd.setFirstName(p.getCustomerList().getFirstName());
				cpd.setLastName(p.getCustomerList().getLastName());
				cpd.setLoanAmount(p.getCustomerList().getLoanAmount());
				cpd.setLoanType(p.getCustomerList().getLoanType());
				cpd.setTotalPayable(p.getCustomerList().getTotalPayable());
				cpd.setTotalPaid(p.getCustomerList().getTotalPaid());
				cpd.setBalanceAmount(p.getCustomerList().getTotalPayable() - p.getCustomerList().getTotalPaid());
				cpdl.add(cpd);
			}

		}

		return cpdl;
	}

	@Override
	@Transactional
	public response changePaymentStatus(CustomerPaymentDTO cpd) {
		response response = new response();
//		try {
			Payments payment = paymentR.findByPaymentId(cpd.getPaymentId());
			payment.setPaymentStatus(cpd.getPaymentStatus());
			payment.setAmount(cpd.getAmount());
			payment.setPaymentMode(cpd.getPaymentMode());
			paymentR.save(payment);
			setTotalPaidAmountByCustomerId(payment.getCustomerId());
			Map<String, Long> tc = new HashMap<String, Long>();
			tc.put("todayCredit", getTodayCreditAmount(cpd.getDate(), cpd.getLoanType()));
			response.setData(tc);
			response.setStatus(true);
		    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
			return response;
//		} catch (Exception e) {
//			response.setMessage("Somthing Went Wrong");
//			response.setStatus(false);
//			return response;
//		}
	}

	public Long getTodayCreditAmount(Date date, String loanType) {
		try {
			return paymentR.findTodayCredit(date, loanType);
		} catch (Exception e) {
			System.out.println("exception in setTotalPaidAmountByCustomerId method" + e);
		}
		return null;
	}

	public void setTotalPaidAmountByCustomerId(Long customerId) {
		try {
			customerList customerList = customerListR.findByCustomerId(customerId);
			Long tpa = paymentR.findTotalPaidAmountByCustomerId(customerId);
			customerList.setTotalPaid(tpa != null ? tpa : 0L);
			customerListR.save(customerList);
		} catch (Exception e) {
			System.out.println("exception in setTotalPaidAmountByCustomerId method" + e);
		}
	}

	@Override
	public response getPaymentListByCustomerId(Long customerId) {
		response response = new response();
		try {
			List<Payments> pl = paymentR.findAllByCustomerIdAndPaymentStatusTrue(customerId);
			response.setData(pl);
			response.setStatus(true);
			return response;
		} catch (Exception e) {
			response.setMessage("Somthing Went Wrong");
			response.setStatus(false);
			return response;
		}
	}

	public void stringOccurenceProgram() {
		String inputStr = "hi this is chandran";
		char[] charArray = inputStr.toCharArray();
		Map<Character, Long> charOccurence = new LinkedHashMap<Character, Long>();
		for (char c : charArray) {
			if (charOccurence.containsKey(c)) {
				Long count = charOccurence.get(c);
				charOccurence.put(c, count + 1);
			} else {
				charOccurence.put(c, 1L);
			}
		}
		for (Map.Entry<Character, Long> entry : charOccurence.entrySet()) {
			System.out.println(entry.getKey() + " count " + entry.getValue());
		}
	}

	@Transactional(rollbackOn = CustomException.class)
	public void vowelsReplaceProgram() throws CustomException {
		String inputStr = "hi this is chandran".replaceAll("[aeiouAEIOU]", "@");
		throw new CustomException("custom Exception created success fully");
	}

}
