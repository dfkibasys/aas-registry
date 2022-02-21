//package org.eclipse.basyx.aas.registry.configuration;
//
//import java.io.IOException;
//import java.util.Arrays;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.servlet.Filter;
//import javax.servlet.FilterChain;
//import javax.servlet.FilterConfig;
//import javax.servlet.ServletException;
//import javax.servlet.ServletRequest;
//import javax.servlet.ServletResponse;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//
//import org.springframework.boot.context.properties.ConfigurationProperties;
//import org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.util.AntPathMatcher;
//
//import lombok.Data;
//
//@Configuration
//@lombok.Data
//@ConfigurationProperties(prefix = "servlet")
//public class ServletHeaderConfiguration {
//
//	private List<HeaderDefinition> headers;
//
//	@Bean
//	public Filter headerFilter(AnnotationConfigServletWebServerApplicationContext context) {
//		return new AllMappingsHeaders(headers);
//	}
//
//	private static final class AllMappingsHeaders implements Filter {
//
//		private AntPathMatcher matcher = new AntPathMatcher();
//
//		private final List<HeaderDefinition> headerDefs;
//
//		private final Map<String, Map<String, String>> resolvedHeadersByPath = new HashMap<>();
//
//		public AllMappingsHeaders(List<HeaderDefinition> headerDefs) {
//			this.headerDefs = headerDefs;
//		}
//
//		@Override
//		public void init(FilterConfig filterConfig) throws ServletException {
//			for (HeaderDefinition eachDef : headerDefs) {
//				prepareHeaderDefinition(eachDef);
//			}
//		}
//
//		private void prepareHeaderDefinition(HeaderDefinition eachDef) {
//			String pattern = eachDef.getPath();
//			pattern = removeTrailingSlash(pattern);
//			eachDef.setPath(pattern);
//			if (eachDef.getMethods() != null) {
//				Arrays.sort(eachDef.getMethods());
//			}
//		}
//
//		@Override
//		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
//				throws IOException, ServletException {
//			HttpServletRequest httpRequest = (HttpServletRequest) request;
//			HttpServletResponse httpResponse = (HttpServletResponse) response;
//			String path = httpRequest.getServletPath();
//			String method = httpRequest.getMethod();
//			Map<String, String> resolved = resolvedHeadersByPath.computeIfAbsent(path,
//					p -> resolveHeaders(p, method));
//
//		//	resolved.forEach(httpResponse::addHeader);
//			chain.doFilter(request, httpResponse);
//		}
//
//
//		private Map<String, String> resolveHeaders(String path, String method) {
//			path = removeTrailingSlash(path);
//			Map<String, String> headers = new HashMap<>();
//			for (HeaderDefinition eachDef : headerDefs) {
//				String[] methods = eachDef.getMethods();
//				if (areMethodMatching(methods, method)) {
//					applyHeadersIfMatching(eachDef, path, headers);
//				}
//			}
//			return headers;
//		}
//
//		private boolean areMethodMatching(String[] methods, String method) {
//			boolean applyForAllMethods = methods == null || methods.length == 0;
//			return applyForAllMethods || Arrays.binarySearch(methods, method) != -1;
//		}
//
//		private void applyHeadersIfMatching(HeaderDefinition eachDef, String path, Map<String, String> headers) {
//			String pattern = eachDef.getPath();
//			if (matcher.match(pattern, path)) {
//				// next defs will override previous definitions if multiple match
//				headers.putAll(eachDef.getValues());
//			}
//		}
//
//		private String removeTrailingSlash(String path) {
//			if (path.endsWith("/")) {
//				return path.substring(0, path.length() - 1);
//			}
//			return path;
//		}
//	}
//
//	@Data
//	private static final class HeaderDefinition {
//
//		private String path;
//
//		private Map<String, String> values;
//
//		private String[] methods;
//
//	}
//
//}