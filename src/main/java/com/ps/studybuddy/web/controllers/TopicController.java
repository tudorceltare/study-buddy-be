package com.ps.studybuddy.web.controllers;

import com.ps.studybuddy.domain.dtos.TopicDTO;
import com.ps.studybuddy.exception.domain.TopicExistException;
import com.ps.studybuddy.exception.domain.TopicNotFoundException;
import com.ps.studybuddy.services.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = {"/topics"})
public class TopicController {
    private final TopicService topicService;

    @Autowired
    public TopicController(TopicService topicService) {
        this.topicService = topicService;
    }

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('topic:read')")
    public List<TopicDTO> getTopics() {
        return this.topicService.getAllTopics();
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyAuthority('topic:create')")
    public ResponseEntity<String> createTopic(TopicDTO topicDTO) throws TopicExistException {
        this.topicService.createTopic(topicDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('topic:update')")
    public ResponseEntity<String> updateTopic(TopicDTO topicDTO) throws TopicNotFoundException {
        this.topicService.updateTopic(topicDTO);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('topic:delete')")
    public ResponseEntity<String> deleteTopic(@PathVariable("id") UUID id) throws TopicNotFoundException {
        this.topicService.deleteTopic(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
