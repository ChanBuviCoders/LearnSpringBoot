package config.DAO;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import config.Entity.customerList;

@Repository
public interface testPageable extends PagingAndSortingRepository<customerList, Long> {

}
