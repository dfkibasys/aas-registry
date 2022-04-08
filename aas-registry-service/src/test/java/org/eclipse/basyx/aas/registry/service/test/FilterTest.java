package org.eclipse.basyx.aas.registry.service.test;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.basyx.aas.registry.service.configuration.ServletHeaderConfiguration.HeaderDefinition;
import org.eclipse.basyx.aas.registry.service.configuration.ServletHeaderConfiguration.MappingsHeaderApplier;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class FilterTest {

	private final FilterConfig filterConfig = Mockito.mock(FilterConfig.class);
	private final HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
	private final HttpServletResponse response = Mockito.mock(HttpServletResponse.class);
	private final FilterChain chain = Mockito.mock(FilterChain.class);

	@Test
	public void whenFilterIsApplied_thenOtherFiltersAreAlsoInvoked() throws ServletException, IOException {
		Mockito.when(request.getServletPath()).thenReturn("/registry/shell-descriptors");
		Mockito.when(request.getMethod()).thenReturn("GET");

		MappingsHeaderApplier headers = new MappingsHeaderApplier(List.of());
		headers.init(filterConfig);
		headers.doFilter(request, response, chain);
		Mockito.verify(chain, Mockito.times(1)).doFilter(request, response);
	}

	@Test
	public void whenInitIsCalled_thenHeaderDefsAreAdjusted() throws ServletException, IOException {
		Mockito.when(request.getServletPath()).thenReturn("/registry/shell-descriptors");
		Mockito.when(request.getMethod()).thenReturn("GET");

		HeaderDefinition def1 = newDef1();
		HeaderDefinition def2 = newDef2();
		MappingsHeaderApplier headers = new MappingsHeaderApplier(List.of(def1, def2));
		headers.init(filterConfig);

		assertThat(def1.getPath()).isEqualTo("/registry/shell-descriptors");
		assertThat(def2.getPath()).isEqualTo("/registry/shell-descriptors");
		assertThat(def1.getMethods()).isEqualTo(new String[] { "GET", "OPTIONS" });
		assertThat(def2.getMethods()).isEqualTo(new String[] { "OPTIONS", "POST" });
	}

	@Test
	public void whenFiltering_thenValuesAreApplied() throws ServletException, IOException {
		Mockito.when(request.getServletPath()).thenReturn("/registry/shell-descriptors");
		Mockito.when(request.getMethod()).thenReturn("OPTIONS");

		HeaderDefinition def1 = newDef1();
		HeaderDefinition def2 = newDef2();
		MappingsHeaderApplier headers = new MappingsHeaderApplier(List.of(def1, def2));
		headers.init(filterConfig);
		headers.doFilter(request, response, chain);

		Mockito.verify(response, Mockito.times(1)).addHeader("Header1", "Value1");
		Mockito.verify(response, Mockito.times(1)).addHeader("Header2", "Value2");
		Mockito.verify(response, Mockito.times(2)).addHeader(Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void whenFilteringWithUndefinedHeaders_thenNoAdditionalHeadersAreApplied() throws ServletException, IOException {
		Mockito.when(request.getServletPath()).thenReturn("/registry/shell-descriptors");
		Mockito.when(request.getMethod()).thenReturn("DELETE");

		HeaderDefinition def1 = newDef1();
		HeaderDefinition def2 = newDef2();
		MappingsHeaderApplier headers = new MappingsHeaderApplier(List.of(def1, def2));
		headers.init(filterConfig);
		headers.doFilter(request, response, chain);

		Mockito.verify(response, Mockito.never()).addHeader(Mockito.anyString(), Mockito.anyString());
	}

	@Test
	public void whenMethodsNotDefined_thenAllAreMatching() throws IOException, ServletException {
		Mockito.when(request.getServletPath()).thenReturn("/registry/shell-descriptors");
		Mockito.when(request.getMethod()).thenReturn("GET");

		HeaderDefinition def1 = newDef("/registry/shell-descriptors", null, Map.of("Header1", "Value1"));
		HeaderDefinition def2 = newDef("/registry/shell-descriptors", new String[0], Map.of("Header2", "Value2"));
		MappingsHeaderApplier headers = new MappingsHeaderApplier(List.of(def1, def2));
		headers.init(filterConfig);
		headers.doFilter(request, response, chain);

		Mockito.verify(response, Mockito.times(1)).addHeader("Header1", "Value1");
		Mockito.verify(response, Mockito.times(1)).addHeader("Header2", "Value2");
		Mockito.verify(response, Mockito.times(2)).addHeader(Mockito.anyString(), Mockito.anyString());
	}

	private HeaderDefinition newDef2() {
		return newDef("/registry/shell-descriptors", new String[] { "POST", "OPTIONS" }, Map.of("Header2", "Value2"));
	}

	private HeaderDefinition newDef1() {
		return newDef("/registry/shell-descriptors/", new String[] { "OPTIONS", "GET" }, Map.of("Header1", "Value1"));
	}

	private HeaderDefinition newDef(String path, String[] methods, java.util.Map<String, String> headers) {
		HeaderDefinition def = new HeaderDefinition();
		def.setPath(path);
		def.setMethods(methods);
		def.setValues(headers);
		return def;
	}

}
