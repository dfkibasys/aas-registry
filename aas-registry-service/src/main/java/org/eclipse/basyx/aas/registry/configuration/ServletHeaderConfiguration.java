package org.eclipse.basyx.aas.registry.configuration;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@lombok.Data
@ConfigurationProperties(prefix = "servlet.headers")
public class ServletHeaderConfiguration {

	private Map<String, String> general;
	
	@Bean
	public Filter generalHeadersApplier() {
		return new AllMappingsHeaders(general);
	}

	private static final class AllMappingsHeaders implements Filter {

		private final Map<String, String> configuration;

		public AllMappingsHeaders( Map<String, String> configuration) {
			this.configuration = configuration;
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
				throws IOException, ServletException {
			HttpServletResponse httpResponse = (HttpServletResponse) response;
			configuration.forEach(httpResponse::addHeader);
			chain.doFilter(request, httpResponse);
		}
	}

}