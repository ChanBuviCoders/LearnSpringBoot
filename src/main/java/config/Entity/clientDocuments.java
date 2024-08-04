package config.Entity;

import java.sql.Date;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class clientDocuments {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long fileId;
	private String fileName;
	private String fileSize;
	private String fileType;
	private String filePath;
	private String uploadedBy;
	private Date uploadedDate;
	private long userAccountId;

//    @ManyToOne(fetch = FetchType.EAGER)
//    @JoinColumn(name="userAccountId",nullable = false)
//    private userAccount userAccount;

}
