package com.undoschool.cousesearch.service;

import com.undoschool.cousesearch.document.CourseDocument;
import com.undoschool.cousesearch.dto.SearchRequestDto;
import com.undoschool.cousesearch.dto.SearchResponseDto;
import com.undoschool.cousesearch.repository.CourseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class CourseSearchServiceTest {

    @Container
    static ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchContainer("docker.elastic.co/elasticsearch/elasticsearch:8.11.0")
                    .withEnv("discovery.type", "single-node")
                    .withEnv("xpack.security.enabled", "false")
                    .withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.elasticsearch.uris", elasticsearchContainer::getHttpHostAddress);
        registry.add("app.data.initialization.enabled", () -> "false");
    }

    @Autowired
    private CourseSearchService courseSearchService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    @BeforeEach
    void setUp() {
        courseRepository.deleteAll();

        // Create test data
        CourseDocument course1 = CourseDocument.builder()
                .id("1")
                .title("Math Basics")
                .description("Learn basic mathematics")
                .category("Math")
                .type(CourseDocument.CourseType.COURSE)
                .gradeRange("1st-3rd")
                .minAge(6)
                .maxAge(9)
                .price(100.0)
                .nextSessionDate(LocalDateTime.now().plusDays(7).atZone(ZoneOffset.UTC).toInstant())
                .suggest(new Completion(new String[]{"Math Basics"}))
                .build();

        CourseDocument course2 = CourseDocument.builder()
                .id("2")
                .title("Science Fun")
                .description("Exciting science experiments")
                .category("Science")
                .type(CourseDocument.CourseType.ONE_TIME)
                .gradeRange("4th-6th")
                .minAge(9)
                .maxAge(12)
                .price(75.0)
                .nextSessionDate(LocalDateTime.now().plusDays(14).atZone(ZoneOffset.UTC).toInstant())
                .suggest(new Completion(new String[]{"Science Fun"}))
                .build();

        courseRepository.saveAll(List.of(course1, course2));

        // Wait for indexing to complete
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    void testSearchWithKeyword() {
        SearchRequestDto request = SearchRequestDto.builder()
                .q("math")
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(1, response.getTotal());
        assertEquals("Math Basics", response.getCourses().get(0).getTitle());
    }

    @Test
    void testSearchWithCategoryFilter() {
        SearchRequestDto request = SearchRequestDto.builder()
                .category("Science")
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(1, response.getTotal());
        assertEquals("Science Fun", response.getCourses().get(0).getTitle());
    }

    @Test
    void testSearchWithPriceRange() {
        SearchRequestDto request = SearchRequestDto.builder()
                .minPrice(50.0)
                .maxPrice(80.0)
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(1, response.getTotal());
        assertEquals("Science Fun", response.getCourses().get(0).getTitle());
    }

    @Test
    void testSearchWithAgeRange() {
        SearchRequestDto request = SearchRequestDto.builder()
                .minAge(8)
                .maxAge(10)
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(2, response.getTotal()); // Both courses should match age range overlap
    }

    @Test
    void testSearchWithTypeFilter() {
        SearchRequestDto request = SearchRequestDto.builder()
                .type(CourseDocument.CourseType.COURSE)
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(1, response.getTotal());
        assertEquals("Math Basics", response.getCourses().get(0).getTitle());
    }

    @Test
    void testSearchWithPagination() {
        SearchRequestDto request = SearchRequestDto.builder()
                .page(0)
                .size(1)
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(2, response.getTotal());
        assertEquals(1, response.getCourses().size());
        assertEquals(2, response.getTotalPages());
    }

    @Test
    void testSearchWithSorting() {
        SearchRequestDto request = SearchRequestDto.builder()
                .sort("priceDesc")
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertEquals(2, response.getTotal());
        assertEquals("Math Basics", response.getCourses().get(0).getTitle()); // Higher price first
    }

    @Test
    void testFuzzySearch() {
        SearchRequestDto request = SearchRequestDto.builder()
                .q("matg") // Typo in "math"
                .build();

        SearchResponseDto response = courseSearchService.searchCourses(request);

        assertTrue(response.getTotal() > 0);
    }

    @Test
    void testAutocompleteSuggestions() {
        List<String> suggestions = courseSearchService.getAutocompleteSuggestions("Ma");

        assertFalse(suggestions.isEmpty());
        assertTrue(suggestions.stream().anyMatch(s -> s.contains("Math")));
    }
}