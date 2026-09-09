
package config.DAO;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import config.Entity.customerList;

@Repository
public interface customerListR extends JpaRepository<customerList, Long> {

	List<customerList> findAllByUserAccountId(Long userAccountId);

	List<customerList> findAllByMobileNumber(Long mobileNumber);

	List<customerList> findAllByUserAccountIdAndLoanType(long userAccountId, String loanType);

	List<customerList> findByLoanType(String loanType);

	@Query(value = "select distinct (loanType) from customerList where userAccountId=:userAccountId  ", nativeQuery = true)
	public List<String> findDistinctLoanTypeByUserAccountId(@Param("userAccountId") Long UserAccountId);

	@Query(value = "SELECT distinct loanType  FROM customerList where userAccountId=:userAccountId;", nativeQuery = true)
	public String[] getLoanTypes(@Param("userAccountId") long userAccountId);

	@Query(value = "SELECT distinct datename(month,startDate) FROM customerList where userAccountId=:userAccountId  order by  datename(month,startDate) desc ;", nativeQuery = true)
	public String[] getMonthLists(@Param("userAccountId") long userAccountId);

	@Query(value = "SELECT count (startDate) FROM customerList  where  datename(month,startDate)=:month and  loanType=:loanType  and userAccountId=:userAccountId ;", nativeQuery = true)
	public int getCustomerCountByMonth(@Param("month") String month, @Param("loanType") String loanType,
			@Param("userAccountId") long userAccountId);

	@Query(value = "select  sum(loanAmount) as sum_value from customerList  where userAccountId=:userAccountId and datename(month,startdate)=:month and loanType=:loanType ;", nativeQuery = true)
	public Long getSumOfLoanAmountByLoanType(@Param("month") String month, @Param("loanType") String loanType,
			@Param("userAccountId") long userAccountId);

	customerList findByCustomerId(long customerId);

	List<customerList> findByLoanTypeAndUserAccountId(String loanType, Long userAccountId);

	List<customerList> findByLoanTypeAndStartDateLessThanEqualAndUserAccountId(String loanType, Date date,
			Long userAccountId);

}
