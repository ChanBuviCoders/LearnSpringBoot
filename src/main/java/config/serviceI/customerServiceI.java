package config.serviceI;

import java.text.ParseException;
import java.util.List;
import java.util.Optional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import config.DAO.customerListR;
import config.DAO.testPageable;
import config.DTO.response;
import config.Entity.customerList;
import config.Service.customerService;

@Service
public class customerServiceI implements customerService {
	@Autowired
	customerListR customerListR;

	/* **************************************************************************************************************************/
	@RequestMapping(value = "/addCustomer", method = RequestMethod.POST, produces = "application/Json")
	public response addCustomer(@RequestBody customerList customerList) {
		response response = new response();
		try {
			customerList.getLoanAmount();
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

	@RequestMapping(value = "/getCustomerDetailsById", method = RequestMethod.POST, produces = "application/Json")
	public Optional<customerList> getCustomerDetailsById(@RequestBody customerList customerList) {
		Optional<customerList> customerDetails = null;
		try {
			customerDetails = customerListR.findById(customerList.getCustomerId());
			return customerDetails;
		} catch (Exception e) {
			return customerDetails;
		}
	}

//	---------------------------------------------update customer------------------------------------->
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

//	---------------------------------------------delete customer---------------------------------------->

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
	public response getAllCustomerListByUserAccountId(long userAccountId) {
		List<customerList> acList = null;
		response response = new response();
		try {
			acList = (List<customerList>) customerListR.findAllByUserAccountId(userAccountId);
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
}
