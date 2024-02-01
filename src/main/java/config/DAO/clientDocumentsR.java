package config.DAO;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import config.Entity.clientDocuments;

@Repository
public interface clientDocumentsR extends JpaRepository<clientDocuments, Long> {

	List<clientDocuments> findAllByUserAccountId(long userAccountId);

}
