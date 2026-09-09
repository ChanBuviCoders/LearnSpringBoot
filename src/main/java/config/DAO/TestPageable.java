package config.DAO;

import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import config.Entity.CustomerList;

@Repository
public interface TestPageable extends PagingAndSortingRepository<CustomerList, Long> {

}
