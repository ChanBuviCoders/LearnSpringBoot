package config.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NavigationMenuDTO {

	private Long menuId;
	private String menuName;
	private Boolean isView;
	private Boolean isEdit;
	private Boolean isDelete;
}
