package org.eclipse.basyx.aas.registry.repository;

import org.apache.lucene.search.join.ScoreMode;
import org.eclipse.basyx.aas.registry.client.api.ShellDescriptorPaths;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchQuery;
import org.eclipse.basyx.aas.registry.model.SortDirection;
import org.eclipse.basyx.aas.registry.model.Sorting;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.InnerHitBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.NestedQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchRequestMapper {

	private static final int MAX_INNER_HITS = 100;

	public static NativeSearchQuery mapSearchQuery(ShellDescriptorSearchQuery query) {
		String key = query.getPath();
		Object value = query.getValue();

		BoolQueryBuilder bqBuilder = QueryBuilders.boolQuery();
		
		MatchQueryBuilder matchBuilder = QueryBuilders.matchQuery(key, value);
		bqBuilder.must(matchBuilder);

		NativeSearchQuery nQuery;
		if (doBuildSubmodelNestedQuqery(key)) {
			NestedQueryBuilder nestedBuilder = QueryBuilders.nestedQuery(ShellDescriptorPaths.submodelDescriptors().toString(),bqBuilder, ScoreMode.None);
			InnerHitBuilder innerHitBuilder = new InnerHitBuilder();
			innerHitBuilder.setSize(MAX_INNER_HITS);
			nestedBuilder.innerHit(innerHitBuilder);
			
			nQuery = new NativeSearchQuery(nestedBuilder);
		} else {
			nQuery = new NativeSearchQuery(bqBuilder);
		}
		nQuery.setMaxResults(10000);
		addSorting(query, nQuery);
		
		return nQuery;
	}

	private static boolean doBuildSubmodelNestedQuqery(String key) {
		return key.startsWith(ShellDescriptorPaths.submodelDescriptors().toString() + ".");
	}

	private static void addSorting(ShellDescriptorSearchQuery query, NativeSearchQuery nQuery) {
		Sorting sorting = query.getSorting();
		if (sorting != null) {
			SortDirection sortDirection = sorting.getDirection();
			Direction direction;
			if (sortDirection == null) {
				direction = Direction.ASC;
			} else {
				direction = Direction.fromString(sortDirection.name());
			}
			nQuery.addSort(Sort.by(direction, sorting.getProperty()));
		}
	}

}