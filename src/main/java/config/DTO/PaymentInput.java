package config.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentInput {
	private Long userAccountId;
	private Date date;
	private String loanType;
}
