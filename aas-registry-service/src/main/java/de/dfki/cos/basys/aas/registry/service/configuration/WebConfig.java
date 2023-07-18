package de.dfki.cos.basys.aas.registry.service.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import de.dfki.cos.basys.aas.registry.model.AssetKind;


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
