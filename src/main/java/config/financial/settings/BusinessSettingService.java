package config.financial.settings;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import config.financial.audit.AuditAction;
import config.financial.audit.AuditService;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessSettingService {

	private final BusinessSettingRepository repository;
	private final AuditService auditService;

	public List<BusinessSetting> findAll() {
		return repository.findAllByOrderByCategoryAscKeyAsc();
	}

	public String getRequiredValue(String key) {
		return repository.findByKeyAndActiveTrue(key)
				.map(BusinessSetting::getValue)
				.orElseThrow(() -> new IllegalStateException("Active business setting not found: " + key));
	}

	public int getInteger(String key) {
		return Integer.parseInt(getRequiredValue(key));
	}

	public BigDecimal getDecimal(String key) {
		return new BigDecimal(getRequiredValue(key));
	}

	public boolean getBoolean(String key) {
		return Boolean.parseBoolean(getRequiredValue(key));
	}

	public BusinessSettingsResponse getAggregate() {
		return new BusinessSettingsResponse(
				value("DEFAULT_CURRENCY"), value("TIMEZONE"),
				decimal("DAILY_DEFAULT_INTEREST_RATE"), decimal("WEEKLY_DEFAULT_INTEREST_RATE"),
				decimal("MONTHLY_DEFAULT_INTEREST_RATE"), integer("GRACE_PERIOD_DAYS"),
				integer("DEFAULT_LOAN_DURATION"), decimal("DEFAULT_CHIT_AMOUNT"),
				integer("DEFAULT_CHIT_MEMBERS"), value("RECEIPT_PREFIX"));
	}

	@Transactional
	public BusinessSettingsResponse updateAggregate(BusinessSettingsRequest request) {
		BusinessSettingsResponse before = getAggregate();
		upsert("DEFAULT_CURRENCY", request.defaultCurrency(), SettingValueType.STRING, "ORGANISATION");
		upsert("TIMEZONE", request.timezone(), SettingValueType.STRING, "ORGANISATION");
		upsert("DAILY_DEFAULT_INTEREST_RATE", request.dailyInterestRate().toPlainString(),
				SettingValueType.DECIMAL, "LOAN");
		upsert("WEEKLY_DEFAULT_INTEREST_RATE", request.weeklyInterestRate().toPlainString(),
				SettingValueType.DECIMAL, "LOAN");
		upsert("MONTHLY_DEFAULT_INTEREST_RATE", request.monthlyInterestRate().toPlainString(),
				SettingValueType.DECIMAL, "LOAN");
		upsert("GRACE_PERIOD_DAYS", String.valueOf(request.gracePeriodDays()), SettingValueType.INTEGER, "LOAN");
		upsert("DEFAULT_LOAN_DURATION", String.valueOf(request.defaultLoanDuration()),
				SettingValueType.INTEGER, "LOAN");
		upsert("DEFAULT_CHIT_AMOUNT", request.defaultChitAmount().toPlainString(),
				SettingValueType.DECIMAL, "CHIT");
		upsert("DEFAULT_CHIT_MEMBERS", String.valueOf(request.defaultChitMembers()),
				SettingValueType.INTEGER, "CHIT");
		upsert("RECEIPT_PREFIX", request.receiptPrefix(), SettingValueType.STRING, "ORGANISATION");
		BusinessSettingsResponse result = getAggregate();
		auditService.record("BusinessSettings", "aggregate", AuditAction.UPDATE, before, result);
		return result;
	}

	@Transactional
	public BusinessSetting save(BusinessSettingRequest request) {
		BusinessSetting setting = repository.findByKey(request.key())
				.orElseGet(BusinessSetting::new);
		setting.setKey(request.key());
		setting.setValue(validateValue(request.value(), request.valueType()));
		setting.setValueType(request.valueType());
		setting.setCategory(request.category().toUpperCase());
		setting.setDescription(request.description());
		setting.setActive(request.active());
		return repository.save(setting);
	}

	@Transactional
	public BusinessSetting update(Long id, BusinessSettingRequest request) {
		BusinessSetting setting = repository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Business setting not found: " + id));
		if (!setting.getKey().equals(request.key())) {
			throw new IllegalArgumentException("The setting key cannot be changed");
		}
		setting.setValue(validateValue(request.value(), request.valueType()));
		setting.setValueType(request.valueType());
		setting.setCategory(request.category().toUpperCase());
		setting.setDescription(request.description());
		setting.setActive(request.active());
		return repository.save(setting);
	}

	private String validateValue(String value, SettingValueType type) {
		switch (type) {
		case INTEGER -> Integer.parseInt(value);
		case DECIMAL -> new BigDecimal(value);
		case BOOLEAN -> {
			if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
				throw new IllegalArgumentException("Boolean settings must be true or false");
			}
		}
		case STRING -> {
			// No additional conversion.
		}
		default -> throw new IllegalArgumentException("Unsupported setting value type");
		}
		return value;
	}

	private String value(String key) {
		return getRequiredValue(key);
	}

	private int integer(String key) {
		return Integer.parseInt(value(key));
	}

	private BigDecimal decimal(String key) {
		return new BigDecimal(value(key));
	}

	private void upsert(String key, String value, SettingValueType type, String category) {
		BusinessSetting setting = repository.findByKey(key).orElseGet(BusinessSetting::new);
		setting.setKey(key);
		setting.setValue(validateValue(value, type));
		setting.setValueType(type);
		setting.setCategory(category);
		setting.setDescription("Angular aggregate business setting: " + key);
		setting.setActive(true);
		repository.save(setting);
	}
}

record BusinessSettingsRequest(
		@NotBlank String defaultCurrency,
		@NotBlank String timezone,
		@NotNull @DecimalMin("0.0") BigDecimal dailyInterestRate,
		@NotNull @DecimalMin("0.0") BigDecimal weeklyInterestRate,
		@NotNull @DecimalMin("0.0") BigDecimal monthlyInterestRate,
		@NotNull @Min(0) Integer gracePeriodDays,
		@NotNull @Min(1) Integer defaultLoanDuration,
		@NotNull @DecimalMin("0.01") BigDecimal defaultChitAmount,
		@NotNull @Min(2) Integer defaultChitMembers,
		@NotBlank String receiptPrefix) {
}

record BusinessSettingsResponse(String defaultCurrency, String timezone,
		BigDecimal dailyInterestRate, BigDecimal weeklyInterestRate,
		BigDecimal monthlyInterestRate, Integer gracePeriodDays,
		Integer defaultLoanDuration, BigDecimal defaultChitAmount,
		Integer defaultChitMembers, String receiptPrefix) {
}
