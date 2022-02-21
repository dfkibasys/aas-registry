package org.eclipse.basyx.aas.registry.repository;

import javax.validation.constraints.NotNull;

import org.apache.lucene.search.join.ScoreMode;
import org.eclipse.basyx.aas.registry.client.api.AasRegistryPaths;
import org.eclipse.basyx.aas.registry.model.Match;
import org.eclipse.basyx.aas.registry.model.Page;
import org.eclipse.basyx.aas.registry.model.ShellDescriptorSearchQuery;
import org.eclipse.basyx.aas.registry.model.SortDirection;
import org.eclipse.basyx.aas.registry.model.Sorting;
import org.eclipse.basyx.aas.registry.model.SortingPath;
import org.elasticsearch.index.query.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SearchRequestMapper {

	private static final int MAX_INNER_HITS = 100;

	public static NativeSearchQuery mapSearchQuery(ShellDescriptorSearchQuery query) {
		NativeSearchQuery nQuery = createSearchQuery(query.getMatch());		
		Sort sort = getSort(query);
		if (sort != null) {
			nQuery.addSort(sort);
		}
		applyPageable(query, nQuery, sort);
		return nQuery;
	}

	private static NativeSearchQuery createSearchQuery(Match match) {
		if (match == null) {
			return createMatchAllQuery();
		} else {
			return createMatchByFilter(match.getPath(), match.getValue());
		}
	}
	
	private static NativeSearchQuery createMatchAllQuery() {
		MatchAllQueryBuilder matchAllBuilder = QueryBuilders.matchAllQuery();
		return new NativeSearchQuery(matchAllBuilder);
	}

	private static NativeSearchQuery createMatchByFilter(@NotNull String path, @NotNull String value) {
		BoolQueryBuilder bqBuilder = QueryBuilders.boolQuery();
		MatchQueryBuilder matchBuilder = QueryBuilders.matchQuery(path, value);
		matchBuilder.operator(Operator.AND);
		bqBuilder.must(matchBuilder);
		if (doBuildSubmodelNestedQuqery(path)) {
			NestedQueryBuilder nestedBuilder = QueryBuilders
					.nestedQuery(AasRegistryPaths.submodelDescriptors().toString(), bqBuilder, ScoreMode.None);
			InnerHitBuilder innerHitBuilder = new InnerHitBuilder();
			innerHitBuilder.setSize(MAX_INNER_HITS);
			nestedBuilder.innerHit(innerHitBuilder);
			return new NativeSearchQuery(nestedBuilder);
		} else {
			return new NativeSearchQuery(bqBuilder);
		}
	}

	private static Sort getSort(ShellDescriptorSearchQuery query) {
		Sorting sorting = query.getSortBy();
		if (sorting != null) {
			SortDirection sortDirection = sorting.getDirection();
			Direction direction;
			if (sortDirection == null) {
				direction = Direction.ASC;
			} else {
				direction = Direction.fromString(sortDirection.name());
			}
			String[] paths = sorting.getPath().stream().map(SortingPath::toString).toArray(String[]::new);
			return Sort.by(direction, paths);
		}
		return null;
	}

	private static void applyPageable(ShellDescriptorSearchQuery query, NativeSearchQuery nQuery, Sort sort) {
		Page page = query.getPage();
		if (page != null) {
			int idx = page.getIndex();
			int size = page.getSize();
			if (sort == null) {
				nQuery.setPageable(PageRequest.of(idx, size));
			} else {
				nQuery.setPageable(PageRequest.of(idx, size, sort));	
			}
			
		}
	}

	private static boolean doBuildSubmodelNestedQuqery(String key) {
		return key.startsWith(AasRegistryPaths.submodelDescriptors().toString() + ".");
	}

}