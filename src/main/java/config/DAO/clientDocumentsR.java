package config.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import config.Entity.ClientDocuments;

@Repository
public interface ClientDocumentsR extends JpaRepository<ClientDocuments, Long> {

	List<ClientDocuments> findAllByUserAccountId(long userAccountId);

}
