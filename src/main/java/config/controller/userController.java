package config.controller;

import java.util.List;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.JsonObject;

import config.DAO.UserAccountR;
import config.Entity.UserAccount;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping(path = "/api") // for nested api
public class UserController {

	@Autowired
	UserAccountR userAccountR;

	@RequestMapping(value = "/rgbToHexColor", method = RequestMethod.POST, produces = "application/Json")
	public String rgbToHexColor(@RequestBody int rgbArray[]) {
		String hexArray[] = new String[3];
		StringBuffer SB = new StringBuffer();
		JsonObject json = new JsonObject();
		try {
			char[] charArry = { 'A', 'B', 'C', 'D', 'E', 'F' };

			if (rgbArray.length > 0) {

				SB.append("#");
				for (int i = 0; i < rgbArray.length; i++) {

					int quotient = (rgbArray[i] + 1) / 16;
					int remainder = (rgbArray[i] + 1) % 16;

					String value = "";
					if (quotient > 0 && quotient < 16) {
						if (quotient < 10) {
							if (remainder <= 10) {
								String quo = String.valueOf((remainder == 0 ? quotient - 1 : quotient));
								String remain = String.valueOf((remainder == 0 ? "F" : (remainder - 1)));
								value = quo.concat(remain);
							} else {
								remainder = remainder - 10;
								String quo = String.valueOf(quotient);
								String remain = String.valueOf(charArry[(remainder == 0 ? 5 : (remainder - 1))]);
								value = quo.concat(remain);
							}
						} else {
							quotient = quotient - 10;
							if (remainder <= 10) {
								String quo = String
										.valueOf((quotient == 0 && remainder == 0) ? "9" : charArry[quotient]);
								String remain = String.valueOf((remainder == 0 ? "F" : (remainder - 1)));
								value = quo.concat(remain);
							} else {
								remainder = remainder - 10;
								String quo = String.valueOf(charArry[quotient]);
								String remain = String.valueOf(charArry[(remainder == 0 ? 5 : (remainder - 1))]);
								value = quo.concat(remain);
							}
						}
					} else if (quotient == 16 && remainder == 0) {
						value = "FF";
					} else if (quotient == 0 && remainder == 0) {
						value = "00";
					} else {
						if (remainder <= 10) {
							String quo = String.valueOf(0);
							String remain = String.valueOf((remainder == 0 ? "F" : (remainder - 1)));
							value = quo.concat(remain);
						} else {
							remainder = remainder - 10;
							String quo = String.valueOf(0);
							String remain = String.valueOf(charArry[(remainder == 0 ? 5 : (remainder - 1))]);
							value = quo.concat(remain);
						}
					}

					hexArray[i] = value;
					SB.append(value);
				}
				json.addProperty("hexValue", SB.toString());
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		return json.toString();
	}

	public String getMonthName(byte number) {
		String month = new String();
		if (number != 0) {
			if (number == 1)
				month = "January";
			else if (number == 2)
				month = "February";
			else if (number == 3)
				month = "March";
			else if (number == 4)
				month = "April";
			else if (number == 5)
				month = "May";
			else if (number == 6)
				month = "Jun";
			else if (number == 7)
				month = "July";
			else if (number == 8)
				month = "August";
			else if (number == 9)
				month = "September";
			else if (number == 10)
				month = "October";
			else if (number == 11)
				month = "November";
			else if (number == 12)
				month = "December";
		}

		return month;
	}

	@RequestMapping(value = "/getAllUser", method = RequestMethod.GET, produces = "application/json")
	public List<UserAccount> getAllUser() {

		List<UserAccount> list = (List<UserAccount>) userAccountR.findAll();
		return list;

	}
	/*
	 * ==================================jsp
	 * methods==================================
	 */

	@RequestMapping(value = "/sendMail", method = RequestMethod.POST, produces = "application/Json")
	public void sendMail() {

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
		mailSender.setHost("smtp.gmail.com");
		mailSender.setPort(587);
		mailSender.setUsername("chandran122250@gmail.com");
		mailSender.setPassword("Cnn@91%,");

		Properties properties = new Properties();
		properties.setProperty("mail.smtp.auth", "true");
		properties.setProperty("mail.smtp.starttls.enable", "true");

		mailSender.setJavaMailProperties(properties);

		String from = "chandran122250@gmail.com";
		String to = "nila122250@gmail.com";
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject("This is a plain text email");
		message.setText("Hello guys! This is a plain text email.");
		mailSender.send(message);

	}

}
