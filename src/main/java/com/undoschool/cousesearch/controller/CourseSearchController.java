package com.undoschool.elastic.controller;

import com.undoschool.elastic.document.CourseDocument;
import com.undoschool.elastic.dto.SearchRequestDto;
import com.undoschool.elastic.dto.SearchResponseDto;
import com.undoschool.elastic.service.CourseSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List; // Add this import for List

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CourseSearchController {

    private final CourseSearchService courseSearchService;

    @GetMapping
    public ResponseEntity<SearchResponseDto> searchCourses(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer minAge,
            @RequestParam(required = false) Integer maxAge,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) CourseDocument.CourseType type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false, defaultValue = "upcoming") String sort,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "10") Integer size
    ) {
        log.info("Received search request - q: {}, minAge: {}, maxAge: {}, category: {}, type: {}, minPrice: {}, maxPrice: {}, startDate: {}, sort: {}, page: {}, size: {}",
                q, minAge, maxAge, category, type, minPrice, maxPrice, startDate, sort, page, size);

        SearchRequestDto request = SearchRequestDto.builder()
                .q(q)
                .minAge(minAge)
                .maxAge(maxAge)
                .category(category)
                .type(type)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .startDate(startDate)
                .sort(sort)
                .page(page)
                .size(size)
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        return ResponseEntity.ok(response);
    }

    // Assignment B - Autocomplete endpoint
    @GetMapping("/suggest")
    public ResponseEntity<List<String>> getAutocompleteSuggestions(
            @RequestParam String q
    ) {
        log.info("Received autocomplete request for: {}", q);

        List<String> suggestions = courseSearchService.getAutocompleteSuggestions(q);

        return ResponseEntity.ok(suggestions);
    }
}