package config.Entity;

import java.sql.Date;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class CustomerList {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long customerId;
	private String firstName;
	private String lastName;
	private String gender;
	private Long loanAmount;
	private String loanType;

	private Long mobileNumber;
	private Date startDate;
	private Date endDate;
	private Long userAccountId;
	private Long totalPayable;
	private Long totalPaid;

	@OneToMany(mappedBy = "customerList")
	private Set<Payments> payments;

}
