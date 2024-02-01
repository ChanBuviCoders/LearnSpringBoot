
package config.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import config.Entity.customerList;

@Repository
public interface customerListR extends JpaRepository<customerList, Long> {

	List<customerList> findAllByUserAccountId(long userAccountId);

	@Query(value = "select distinct (loan_type) from customer_list where user_account_id=:userAccountId  ",nativeQuery = true)
	public List<String> findDistinctLoanTypeByUserAccountId(@Param("userAccountId") Long UserAccountId);

//	Long countByStartDateMonthAndLoanTypeAndUserAccountId(String month,String loanType,Long userAccountId);
//	
//	Long sumLoanAmountByMonthAndLoanTypeAndUserAccountId(String month,String loanType,Long userAccountId);
//	

	@Query(value = "SELECT distinct loan_type  FROM CUSTOMER_LIST where user_Account_Id=:userAccountId;", nativeQuery = true)
	public String[] getLoanTypes(@Param("userAccountId") long userAccountId);

	@Query(value = "SELECT distinct datename(month,start_date) FROM customer_List where user_Account_Id=:userAccountId  order by  datename(month,start_date) desc ;", nativeQuery = true)
	public String[] getMonthLists(@Param("userAccountId") long userAccountId);

	@Query(value = "SELECT count (start_date) FROM customer_List  where  datename(month,start_date)=:month and  loan_Type=:loanType  and user_Account_Id=:userAccountId ;", nativeQuery = true)
	public int getCustomerCountByMonth(@Param("month") String month, @Param("loanType") String loanType,
			@Param("userAccountId") long userAccountId);

	@Query(value = "select  sum(loan_amount) as sum_value from customer_List  where user_Account_Id=:userAccountId and datename(month,start_date)=:month and loan_Type=:loanType ;", nativeQuery = true)
	public Long getSumOfLoanAmountByLoanType(@Param("month") String month, @Param("loanType") String loanType,
			@Param("userAccountId") long userAccountId);

	customerList findByCustomerId(long customerId);

}
