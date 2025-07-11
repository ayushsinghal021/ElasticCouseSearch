package com.undoschool.cousesearch;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.undoschool.cousesearch.document.CourseDocument;
import com.undoschool.cousesearch.repository.CourseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

@SpringBootApplication
@Slf4j
public class CouseSearchApplication {

	public static void main(String[] args) {
		SpringApplication.run(CouseSearchApplication.class, args);
	}

	@Bean
	public CommandLineRunner loadData(CourseRepository courseRepository, ObjectMapper objectMapper) {
		return args -> {
			log.info("Starting data initialization from sample-courses.json...");
			courseRepository.deleteAll();
			try {
				List<CourseDocument> courses = objectMapper.readValue(
						new ClassPathResource("sample-courses.json").getInputStream(),
						new TypeReference<List<CourseDocument>>() {
						});
				courseRepository.saveAll(courses);
				log.info("Data initialization completed. Indexed {} courses from sample-courses.json", courses.size());
			} catch (IOException e) {
				log.error("Failed to load sample-courses.json", e);
			}
		};
	}

}
