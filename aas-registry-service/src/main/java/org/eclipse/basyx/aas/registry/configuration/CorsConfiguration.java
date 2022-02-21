package org.eclipse.basyx.aas.registry.configuration;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import lombok.Data;

@Configuration
@lombok.Setter
@ConfigurationProperties(prefix = "cors")
public class CorsConfiguration implements WebMvcConfigurer {

	private List<CorsMapping> mappings;

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		for (CorsMapping eachMapping : mappings) {
			String path = eachMapping.getPath();
			CorsRegistration registration = registry.addMapping(path);
			
			Optional.ofNullable(eachMapping.getAllowCredentials()).ifPresent(registration::allowCredentials);
			Optional.ofNullable(eachMapping.getAllowedHeaders()).ifPresent(registration::allowedHeaders);
			Optional.ofNullable(eachMapping.getAllowedMethods()).ifPresent(registration::allowedMethods);
			Optional.ofNullable(eachMapping.getAllowedOriginPatterns()).ifPresent(registration::allowedOriginPatterns);
			Optional.ofNullable(eachMapping.getAllowedOrigins()).ifPresent(registration::allowedOrigins);
			Optional.ofNullable(eachMapping.getExposedHeaders()).ifPresent(registration::exposedHeaders);
			Optional.ofNullable(eachMapping.getMaxAge()).ifPresent(registration::maxAge);			
		}		
	}

	@Data
	private static final class CorsMapping {

		private String path;

		private Boolean allowCredentials;

		private String[] allowedHeaders;

		private String[] allowedMethods;

		private String[] allowedOriginPatterns;
		
		private String[] allowedOrigins;
		
		private String[] exposedHeaders;
		
		private Long maxAge;

	}

}
