package config.financial.settings;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BusinessSettingRequest(
		@NotBlank @Pattern(regexp = "^[A-Z][A-Z0-9_]*$") @Size(max = 120) String key,
		@NotBlank @Size(max = 1000) String value,
		@NotNull SettingValueType valueType,
		@NotBlank @Size(max = 50) String category,
		@Size(max = 500) String description,
		boolean active) {
}
