package com.ps.studybuddy.services;

import com.ps.studybuddy.domain.dtos.TopicDTO;
import com.ps.studybuddy.domain.entities.Group;
import com.ps.studybuddy.domain.entities.Topic;
import com.ps.studybuddy.domain.repositories.GroupRepository;
import com.ps.studybuddy.domain.repositories.TopicRepository;
import com.ps.studybuddy.exception.domain.TopicExistException;
import com.ps.studybuddy.exception.domain.TopicNotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional
public class TopicService {
    private final TopicRepository topicRepository;
    private final GroupRepository groupRepository;
    private final ModelMapper modelMapper;

    @Autowired
    public TopicService(TopicRepository topicRepository, GroupRepository groupRepository, ModelMapper modelMapper) {
        this.topicRepository = topicRepository;
        this.groupRepository = groupRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * First it checks if the topic exists in the database. If it does, it adds the group to the topic's groups list,
     * else it creates a new topic with a default description and adds the group to the topic's groups list.
     * All topic's names are saved in lowercase and are unique.
     * @param dto The topic to be created, only the name is required to successfully create a topic.
     * @param group The group to be added to the topic's groups list.
     * @return The created topic.
     */
    public Topic createTopic(TopicDTO dto, Group group) {
        Optional<Topic> topicOptional = this.topicRepository.findTopicByName(dto.getName().toLowerCase());
        Topic topic;
        if(topicOptional.isPresent()) {
            topic = topicOptional.get();
            topic.getGroups().add(group);
        } else {
            topic = Topic.builder()
                    .name(dto.getName().toLowerCase())
                    .groups(new ArrayList<>())
                    .createdDate(new Date())
                    .description(this.generateDefaultTopicDescription(dto.getName()))
                    .build();
            topic.getGroups().add(group);
        }
        return this.topicRepository.save(topic);
    }

    /**
     * Creates a new topic with a default description. It checks if the topic already exists in the database, if so,
     * it throws a TopicExistException. All topic's names are saved in lowercase and are unique.
     * @param dto The topic to be created, only the name is required to successfully create a topic.
     * @throws TopicExistException If the topic already exists in the database.
     */
    public void createTopic(TopicDTO dto) throws TopicExistException {
        Optional<Topic> topicOptional = this.topicRepository.findTopicByName(dto.getName().toLowerCase());
        if(topicOptional.isPresent()) {
            throw new TopicExistException("Topic already exists");
        }
        Topic topic = Topic.builder()
                .name(dto.getName().toLowerCase())
                .createdDate(new Date())
                .groups(new ArrayList<>())
                .build();
        if(dto.getDescription().isEmpty()) {
            topic.setDescription(this.generateDefaultTopicDescription(topic.getName()));
        }
        this.topicRepository.save(topic);
    }

    public void updateTopic(TopicDTO dto) throws TopicNotFoundException {
        Optional<Topic> topicOptional = this.topicRepository.findTopicByName(dto.getName().toLowerCase());
        if(!topicOptional.isPresent()) {
            throw new TopicNotFoundException("Topic not found");
        }
        Topic topic = topicOptional.get();
        topic.setName(dto.getName().toLowerCase());
        if (!dto.getDescription().isEmpty()) {
            topic.setDescription(dto.getDescription());
        } else {
            topic.setDescription(this.generateDefaultTopicDescription(topic.getName()));
        }
        this.topicRepository.save(topic);
    }

    public void deleteTopic(UUID topicId) throws TopicNotFoundException {
        Optional<Topic> topicOptional = this.topicRepository.findById(topicId);
        if(!topicOptional.isPresent()) {
            throw new TopicNotFoundException("Topic not found");
        }
        for (Group group : topicOptional.get().getGroups()) {
            group.getTopics().remove(topicOptional.get());
            this.groupRepository.save(group);
        }
        this.topicRepository.deleteById(topicId);
    }

    public List<TopicDTO> getAllTopics() {
        List<Topic> topics = this.topicRepository.findAll();
        List<TopicDTO> topicDTOS = new ArrayList<>();
        for(Topic topic : topics) {
            TopicDTO dto = this.modelMapper.map(topic, TopicDTO.class);
            dto.setName(StringUtils.capitalize(dto.getName()));
            topicDTOS.add(dto);
        }
        return topicDTOS;
    }

    private String generateDefaultTopicDescription(String name) {
        return "This is the default description for the topic " + StringUtils.capitalize(name) + ".";
    }
}
