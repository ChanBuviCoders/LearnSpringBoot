package config.Entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Data;

@Data
@Entity
public class Payments {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId;
	private Date date;
	private Long amount;
	private Boolean paymentStatus=false;
	private String paymentMode="offline";
	private Long customerId;
	private String loanType;
	
	@ManyToOne
	@JoinColumn(name="customerId",insertable = false,updatable = false)
	private CustomerList customerList;
}
