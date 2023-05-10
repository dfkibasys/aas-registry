package org.eclipse.digitaltwin.basyx.aasregistry.service.configuration;

import org.eclipse.digitaltwin.basyx.aasregistry.model.AssetKind;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


@Configuration
public class WebConfig implements WebMvcConfigurer {
	@Override
	public void addFormatters(FormatterRegistry registry) {
		registry.addConverter(new StringToEnumConverter());
	}

	public static class StringToEnumConverter implements Converter<String, AssetKind> {
		@Override
		public AssetKind convert(String source) {
			return AssetKind.fromValue(source);
		}
	}
}
