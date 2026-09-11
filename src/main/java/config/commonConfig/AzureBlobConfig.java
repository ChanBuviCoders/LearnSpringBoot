package config.commonConfig;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;

@Configuration
public class AzureBlobConfig {

	@Bean
	@ConditionalOnExpression("T(org.springframework.util.StringUtils).hasText('${azure.storage.connection-string:}')")
	public BlobServiceClient blobServiceClient(
			@Value("${azure.storage.connection-string}") String connectionString) {
		return new BlobServiceClientBuilder()
				.connectionString(connectionString)
				.buildClient();
	}
}
