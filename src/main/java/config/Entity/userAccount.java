package config.Entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
public class userAccount {

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
	private boolean isActive;
	
	public Long getUserAccountId() {
		return userAccountId;
	}
	public void setUserAccountId(Long userAccountId) {
		this.userAccountId = userAccountId;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getFathersName() {
		return fathersName;
	}
	public void setFathersName(String fathersName) {
		this.fathersName = fathersName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getMarriedStatus() {
		return marriedStatus;
	}
	public void setMarriedStatus(String marriedStatus) {
		this.marriedStatus = marriedStatus;
	}
	public String getOccupation() {
		return occupation;
	}
	public void setOccupation(String occupation) {
		this.occupation = occupation;
	}
	public String getQualification() {
		return qualification;
	}
	public void setQualification(String qualification) {
		this.qualification = qualification;
	}
	public String getPanNumber() {
		return panNumber;
	}
	public void setPanNumber(String panNumber) {
		this.panNumber = panNumber;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getAdharImagePath() {
		return adharImagePath;
	}
	public void setAdharImagePath(String adharImagePath) {
		this.adharImagePath = adharImagePath;
	}
	public String getPanImagePath() {
		return panImagePath;
	}
	public void setPanImagePath(String panImagePath) {
		this.panImagePath = panImagePath;
	}
	public String getZipcode() {
		return zipcode;
	}
	public void setZipcode(String zipcode) {
		this.zipcode = zipcode;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public Date getDob() {
		return dob;
	}
	public void setDob(Date dob) {
		this.dob = dob;
	}
	public Long getMobileNumber() {
		return mobileNumber;
	}
	public void setMobileNumber(Long mobileNumber) {
		this.mobileNumber = mobileNumber;
	}
	public Long getAltMobileNumber() {
		return altMobileNumber;
	}
	public void setAltMobileNumber(Long altMobileNumber) {
		this.altMobileNumber = altMobileNumber;
	}
	public Long getAnnualIncome() {
		return annualIncome;
	}
	public void setAnnualIncome(Long annualIncome) {
		this.annualIncome = annualIncome;
	}
	public Date getLastLoginDate() {
		return lastLoginDate;
	}
	public void setLastLoginDate(Date lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}
	public Date getCurrentLoginDate() {
		return currentLoginDate;
	}
	public void setCurrentLoginDate(Date currentLoginDate) {
		this.currentLoginDate = currentLoginDate;
	}
	public Long getAdharNumber() {
		return adharNumber;
	}
	public void setAdharNumber(Long adharNumber) {
		this.adharNumber = adharNumber;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	
//	@OneToMany(mappedBy = "userAccount",cascade = CascadeType.ALL)
//	private List<clientDocuments> clientDocuments;
//	
//	@OneToMany(mappedBy = "userAccount",cascade = CascadeType.ALL)
//	private List<customerList> customerList;

}
