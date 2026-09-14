package config.Entity;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class UserAccount {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long userAccountId;
	private String userName;
	private String password;
	private String firstName;
	private String lastName;
	private String gender;
	private String fathersName;
	private String email;
	private String marriedStatus;
	private String occupation;
	private String qualification;
	private String panNumber;
	private String address;
	private String adharImagePath;
	private String panImagePath;
	private String zipcode;
	private String city;
	private String state;
	private Date dob;
	private Long mobileNumber;
	private Long altMobileNumber;
	private Long annualIncome;
	private Date lastLoginDate;
	private Date currentLoginDate;
	private Long adharNumber;
	private Long userGroupId;
	private Integer loginAttempt;
	private boolean isActive;

	@Transient
	private List<String> authorities = new ArrayList<>();

//	@OneToMany(mappedBy = "userAccount",cascade = CascadeType.ALL)
//	private List<clientDocuments> clientDocuments;
//	
//	@OneToMany(mappedBy = "userAccount",cascade = CascadeType.ALL)
//	private List<customerList> customerList;

}
