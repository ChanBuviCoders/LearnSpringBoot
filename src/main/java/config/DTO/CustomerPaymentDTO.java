package config.DTO;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomerPaymentDTO {
    private String firstName;
    private String lastName;
    private Long loanAmount;
    private Long amount;
    private Boolean paymentStatus;
    private String paymentMode;
    private Long paymentId;
    private Date date;
    private String loanType;
    private Long totalPayable;
    private Long totalPaid;
    private Long balanceAmount;

}
