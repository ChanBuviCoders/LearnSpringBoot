package config.financial.settings;

import config.financial.common.BaseAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "business_settings", schema = "finance")
public class BusinessSetting extends BaseAuditableEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "setting_key", nullable = false, unique = true, length = 120)
	private String key;

	@Column(name = "setting_value", nullable = false, length = 1000)
	private String value;

	@Enumerated(EnumType.STRING)
	@Column(name = "value_type", nullable = false, length = 30)
	private SettingValueType valueType;

	@Column(nullable = false, length = 50)
	private String category;

	@Column(length = 500)
	private String description;

	@Column(nullable = false)
	private boolean active = true;
}
