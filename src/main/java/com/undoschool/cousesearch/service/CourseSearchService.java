package com.undoschool.elastic.service;

import com.undoschool.elastic.document.CourseDocument;
import com.undoschool.elastic.dto.SearchRequestDto;
import com.undoschool.elastic.dto.SearchResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch._types.query_dsl.RangeQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.TermQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MultiMatchQuery;
import co.elastic.clients.json.JsonData;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchResponseDto searchCourses(SearchRequestDto request) {
        log.info("Searching courses with request: {}", request);

        NativeQueryBuilder queryBuilder = new NativeQueryBuilder();

        // Build query
        Query query = buildQuery(request);
        queryBuilder.withQuery(query);

        // Add sorting
        addSorting(queryBuilder, request.getSort());

        // Add pagination
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        queryBuilder.withPageable(pageable);

        NativeQuery nativeQuery = queryBuilder.build();

        SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(nativeQuery, CourseDocument.class);

        List<CourseDocument> courses = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        int totalPages = (int) Math.ceil((double) searchHits.getTotalHits() / request.getSize());

        return SearchResponseDto.builder()
                .total(searchHits.getTotalHits())
                .courses(courses)
                .page(request.getPage())
                .size(request.getSize())
                .totalPages(totalPages)
                .build();
    }

    private Query buildQuery(SearchRequestDto request) {
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // Full-text search on title and description
        if (request.getQ() != null && !request.getQ().trim().isEmpty()) {
            // Multi-match query with fuzziness for Assignment B
            MultiMatchQuery multiMatchQuery = MultiMatchQuery.of(m -> m
                    .fields("title^2", "description") // Boost title relevance
                    .query(request.getQ())
                    .fuzziness("AUTO") // Enable fuzzy matching
                    .prefixLength(1)
                    .maxExpansions(10)
            );
            boolQueryBuilder.must(multiMatchQuery._toQuery());
        }

        // Category filter
        if (request.getCategory() != null && !request.getCategory().trim().isEmpty()) {
            TermQuery categoryQuery = TermQuery.of(t -> t
                    .field("category")
                    .value(request.getCategory())
            );
            boolQueryBuilder.filter(categoryQuery._toQuery());
        }

        // Type filter
        if (request.getType() != null) {
            TermQuery typeQuery = TermQuery.of(t -> t
                    .field("type")
                    .value(String.valueOf(request.getType()))
            );
            boolQueryBuilder.filter(typeQuery._toQuery());
        }

        // Age range filter
        if (request.getMinAge() != null || request.getMaxAge() != null) {
            if (request.getMinAge() != null) {
                RangeQuery minAgeQuery = RangeQuery.of(r -> r
                        .field("maxAge")
                        .gte(JsonData.of(request.getMinAge()))
                );
                boolQueryBuilder.filter(minAgeQuery._toQuery());
            }
            if (request.getMaxAge() != null) {
                RangeQuery maxAgeQuery = RangeQuery.of(r -> r
                        .field("minAge")
                        .lte(JsonData.of(request.getMaxAge()))
                );
                boolQueryBuilder.filter(maxAgeQuery._toQuery());
            }
        }

        // Price range filter
        if (request.getMinPrice() != null || request.getMaxPrice() != null) {
            RangeQuery.Builder priceRangeBuilder = new RangeQuery.Builder().field("price");

            if (request.getMinPrice() != null) {
                priceRangeBuilder.gte(JsonData.of(request.getMinPrice()));
            }
            if (request.getMaxPrice() != null) {
                priceRangeBuilder.lte(JsonData.of(request.getMaxPrice()));
            }

            boolQueryBuilder.filter(priceRangeBuilder.build()._toQuery());
        }

        // Date filter (show only courses on or after given date)
        if (request.getStartDate() != null) {
            String dateString = request.getStartDate().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            RangeQuery dateQuery = RangeQuery.of(r -> r
                    .field("nextSessionDate")
                    .gte(JsonData.of(dateString))
            );
            boolQueryBuilder.filter(dateQuery._toQuery());
        }

        BoolQuery boolQuery = boolQueryBuilder.build();

        // If no conditions, return match_all
        if (boolQuery.must().isEmpty() && boolQuery.filter().isEmpty()) {
            return Query.of(q -> q.matchAll(m -> m));
        }

        return boolQuery._toQuery();
    }

    private void addSorting(NativeQueryBuilder queryBuilder, String sortParam) {
        if (sortParam == null || sortParam.trim().isEmpty() || "upcoming".equals(sortParam)) {
            // Default sort: ascending by nextSessionDate
            queryBuilder.withSort(Sort.by(Sort.Direction.ASC, "nextSessionDate"));
        } else if ("priceAsc".equals(sortParam)) {
            queryBuilder.withSort(Sort.by(Sort.Direction.ASC, "price"));
        } else if ("priceDesc".equals(sortParam)) {
            queryBuilder.withSort(Sort.by(Sort.Direction.DESC, "price"));
        } else {
            // Default to upcoming if unknown sort parameter
            queryBuilder.withSort(Sort.by(Sort.Direction.ASC, "nextSessionDate"));
        }
    }

    public List<String> getAutocompleteSuggestions(String partialTitle) {
        log.info("Getting autocomplete suggestions for: {}", partialTitle);

        if (partialTitle == null || partialTitle.trim().isEmpty()) {
            return List.of();
        }

        // Use a prefix query instead of completion suggester for better compatibility
        Query prefixQuery = Query.of(q -> q
                .prefix(p -> p
                        .field("title")
                        .value(partialTitle)
                )
        );

        NativeQuery query = new NativeQueryBuilder()
                .withQuery(prefixQuery)
                .withMaxResults(10)
                .build();

        SearchHits<CourseDocument> searchHits = elasticsearchOperations.search(query, CourseDocument.class);

        List<String> suggestions = searchHits.stream()
                .map(SearchHit::getContent)
                .map(CourseDocument::getTitle)
                .distinct()
                .collect(Collectors.toList());

        return suggestions;
    }
}