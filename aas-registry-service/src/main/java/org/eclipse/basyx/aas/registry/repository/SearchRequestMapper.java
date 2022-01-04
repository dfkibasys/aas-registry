package org.eclipse.basyx.aas.registry.repository;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.basyx.aas.registry.model.TermQuery;
import org.eclipse.basyx.aas.registry.model.TermQueryContainer;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchRequestMapper {

	public static NativeSearchQuery mapTermQuery(TermQueryContainer request) {
		Map<String, TermQuery> query = request.getTerm();
		// we have exactly one entry
		Entry<String, TermQuery> entry = query.entrySet().iterator().next();
		String key = entry.getKey();
		TermQuery tValue = entry.getValue();
		Object value = tValue.getValue();
		TermQueryBuilder builder = QueryBuilders.termQuery(key, value);
		return new NativeSearchQuery(builder);
	}
}