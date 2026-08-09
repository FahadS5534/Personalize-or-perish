package com.personalize.service;

import com.personalize.dto.topic.TopicRequest;
import com.personalize.dto.topic.TopicResponse;
import com.personalize.entity.Topic;
import com.personalize.exception.BadRequestException;
import com.personalize.exception.ResourceNotFoundException;
import com.personalize.repository.TopicRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TopicService {

    private final TopicRepository topicRepository;

    @PostConstruct
    @Transactional
    public void seedTopics() {
        if (topicRepository.count() == 0) {
            List<Topic> defaultTopics = Arrays.asList(
                Topic.builder().name("OOP").description("Object-Oriented Programming principles: Encapsulation, Inheritance, Polymorphism, Abstraction").build(),
                Topic.builder().name("Collections").description("Java Collections Framework: Lists, Sets, Maps, Queues").build(),
                Topic.builder().name("Exception Handling").description("Java exception model: Checked and Unchecked exceptions, try-catch-finally, custom exceptions").build(),
                Topic.builder().name("Multithreading").description("Concurrency in Java: Threads, Runnables, ExecutorService, synchronization, locks").build(),
                Topic.builder().name("JDBC").description("Java Database Connectivity: Connections, Statements, ResultSet, transactions").build(),
                Topic.builder().name("Generics").description("Type safety in Java: Generic classes, methods, wildcards, type erasure").build()
            );
            topicRepository.saveAll(defaultTopics);
        }
    }

    @Transactional
    public TopicResponse createTopic(TopicRequest request) {
        if (topicRepository.findByName(request.getName()).isPresent()) {
            throw new BadRequestException("Topic with name '" + request.getName() + "' already exists");
        }
        Topic topic = Topic.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        topic = topicRepository.save(topic);
        return mapToTopicResponse(topic);
    }

    public List<TopicResponse> getAllTopics() {
        return topicRepository.findAll().stream()
                .map(this::mapToTopicResponse)
                .collect(Collectors.toList());
    }

    public TopicResponse getTopicResponseById(Long id) {
        Topic topic = topicRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found with id: " + id));
        return mapToTopicResponse(topic);
    }

    private TopicResponse mapToTopicResponse(Topic topic) {
        return TopicResponse.builder()
                .id(topic.getId())
                .name(topic.getName())
                .description(topic.getDescription())
                .build();
    }
}
