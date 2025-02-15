package config.DAO;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import config.Entity.Payments;

@Repository
public interface PaymentsR extends JpaRepository<Payments, Long> {

	List<Payments> findAllByLoanTypeAndDate(String loanType, Date date);

	Payments findByPaymentId(Long paymentId);

	List<Payments> findAllByCustomerIdAndPaymentStatusTrue(Long customerId);

	@Query(value = "SELECT SUM(amount) FROM payments WHERE date =:date AND paymentStatus =1 AND loanType = :loanType", nativeQuery = true)
	Long findTodayCredit(@Param("date") Date date, @Param("loanType") String loanType);

	@Query(value="SELECT SUM(amount) FROM payments WHERE customerId = :customerId AND paymentStatus = 1",nativeQuery = true)
	Long findTotalPaidAmountByCustomerId(@Param("customerId") Long customerId);

}
