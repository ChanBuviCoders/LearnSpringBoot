package config.financial.settings;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessSettingRepository extends JpaRepository<BusinessSetting, Long> {

	Optional<BusinessSetting> findByKey(String key);

	Optional<BusinessSetting> findByKeyAndActiveTrue(String key);

	List<BusinessSetting> findAllByOrderByCategoryAscKeyAsc();
}
