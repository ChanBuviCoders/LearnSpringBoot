
package config.DAO;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import config.Entity.CustomerList;

@Repository
public interface CustomerListR extends JpaRepository<CustomerList, Long> {

	List<CustomerList> findAllByUserAccountId(Long userAccountId);

	List<CustomerList> findAllByMobileNumber(Long mobileNumber);

	List<CustomerList> findAllByUserAccountIdAndLoanType(long userAccountId, String loanType);

	List<CustomerList> findByLoanType(String loanType);

	@Query(value = "select distinct (loanType) from CustomerList where userAccountId=:userAccountId  ", nativeQuery = true)
	public List<String> findDistinctLoanTypeByUserAccountId(@Param("userAccountId") Long UserAccountId);

	@Query(value = "SELECT distinct loanType  FROM CustomerList where userAccountId=:userAccountId;", nativeQuery = true)
	public String[] getLoanTypes(@Param("userAccountId") long userAccountId);

	@Query(value = "SELECT distinct datename(month,startDate) FROM CustomerList where userAccountId=:userAccountId  order by  datename(month,startDate) desc ;", nativeQuery = true)
	public String[] getMonthLists(@Param("userAccountId") long userAccountId);

	@Query(value = "SELECT count (startDate) FROM CustomerList  where  datename(month,startDate)=:month and  loanType=:loanType  and userAccountId=:userAccountId ;", nativeQuery = true)
	public int getCustomerCountByMonth(@Param("month") String month, @Param("loanType") String loanType,
			@Param("userAccountId") long userAccountId);

	@Query(value = "select  sum(loanAmount) as sum_value from CustomerList  where userAccountId=:userAccountId and datename(month,startdate)=:month and loanType=:loanType ;", nativeQuery = true)
	public Long getSumOfLoanAmountByLoanType(@Param("month") String month, @Param("loanType") String loanType,
			@Param("userAccountId") long userAccountId);

	CustomerList findByCustomerId(long customerId);

	List<CustomerList> findByLoanTypeAndUserAccountId(String loanType, Long userAccountId);

	List<CustomerList> findByLoanTypeAndStartDateLessThanEqualAndUserAccountId(String loanType, Date date,
			Long userAccountId);

}
