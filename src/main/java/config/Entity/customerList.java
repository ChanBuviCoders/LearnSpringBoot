package config.Entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class customerList {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long customerId;
	private String firstName;
	private String lastName;
	private String gender;
	private long loanAmount;
	private String loanType;
	private long mobileNumber;
	private Date startDate;
	private Date endDate;
	private long userAccountId;

	// @ManyToOne
//	@JoinColumn(name = "userAccountId",referencedColumnName = "userAccountId",updatable = false,nullable = false)
//	private userAccount userAccount;

}
