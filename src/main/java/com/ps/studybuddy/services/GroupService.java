package com.ps.studybuddy.services;

import com.ps.studybuddy.domain.dtos.*;
import com.ps.studybuddy.domain.entities.Group;
import com.ps.studybuddy.domain.entities.Topic;
import com.ps.studybuddy.domain.entities.User;
import com.ps.studybuddy.domain.repositories.GroupRepository;
import com.ps.studybuddy.domain.repositories.TopicRepository;
import com.ps.studybuddy.domain.repositories.UserRepository;
import com.ps.studybuddy.exception.domain.*;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;
    private final TopicRepository topicRepository;
    private final ModelMapper modelMapper;
    private final UserService userService;
    private final TopicService topicService;

    @Autowired
    public GroupService(GroupRepository groupRepository, UserRepository userRepository, TopicRepository topicRepository, ModelMapper modelMapper, UserService userService, TopicService topicService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.topicRepository = topicRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
        this.topicService = topicService;
    }

    public void createGroup(GroupCreateDTO dto, Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        Group group = modelMapper.map(dto, Group.class);
        User admin = this.userService.findUserByUsername(adminUsername);
        List<Topic> topics = createListOfTopics(dto.getTopics(), group);
        group.setTopics(topics);
        group.setAdmin(admin);
        group.setMembers(new ArrayList<>());
        group.getMembers().add(admin);
        group.setCreatedDate(new Date());
        group.setMeetingDates(new ArrayList<>());
        groupRepository.save(group);
    }

    /**
     * Checks if the user is anonymous, checks if the group exists, checks if the user is the admin of the group,
     * and updates the group
     * @param dto GroupUpdateDTO
     * @param authentication Authentication
     * @throws AnonymousUserException if the user is anonymous
     * @throws NotAdminOfGroupException if the user is not the admin of the group
     */
    public void updateGroup(GroupUpdateDTO dto, Authentication authentication) throws AnonymousUserException, NotAdminOfGroupException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        Optional<Group> groupOptional = this.groupRepository.findById(dto.getId());
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + dto.getId() + " not found");
        }
        Group group = groupOptional.get();
        if (!group.getAdmin().getUsername().equals(adminUsername)) {
            throw new NotAdminOfGroupException("Only the admin of the group can update the group");
        }
        List<Topic> topics = createListOfTopics(dto.getTopics(), group);
        group.setTopics(topics);
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setLocation(dto.getLocation());
        groupRepository.save(group);
    }

    private List<Topic> createListOfTopics(List<TopicDTO> topicDTOs, Group group) {
        List<Topic> topics = new ArrayList<>();
        if (topicDTOs != null) {
            Set<String> topicNames = new HashSet<>();
            for(TopicDTO topicDTO : topicDTOs) {
                String topicName = topicDTO.getName().toLowerCase();
                if (topicNames.contains(topicName)) {
                    System.out.println("Duplicate topic found: " + topicName);
                } else {
                    topicNames.add(topicName);
                    topics.add(this.topicService.createTopic(topicDTO, group));
                }
            }
        }
        return topics;
    }

    /**
     * Checks if the user is the admin of the group, removes the group from all associated users, removes the group
     * from all associated topics and finally deletes the group
     * @param groupId id of the group to be deleted
     * @param authentication the user who is trying to delete the group from ContextHolder
     * @throws AnonymousUserException if the user is anonymous/has no authentication
     */
    public void deleteGroup(UUID groupId, Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        User admin = this.userService.findUserByUsername(adminUsername);
        Optional<Group> groupOptional = this.groupRepository.findById(groupId);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + groupId + " not found");
        }
        if(!groupOptional.get().getAdmin().getId().equals(admin.getId())) {
            throw new EntityNotFoundException("User is not the admin of the group");
        }
        Group group = groupOptional.get();
        List<User> members = group.getMembers();
        for (User member : members) {
            member.getGroupsWhereMember().remove(group);
            this.userRepository.save(member);
        }
        List<Topic> topics = group.getTopics();
        for (Topic topic : topics) {
            topic.getGroups().remove(group);
            this.topicRepository.save(topic);
        }
        groupRepository.delete(group);
    }

    /**
     * A function that maps a group to a GroupDTO and gets the next meeting date in relation to the current date
     * @param group the group to be mapped
     * @return GroupDTO
     */
    private GroupDTO mapGroupToGroupDTO(Group group) {
        GroupDTO dto = GroupDTO.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .location(group.getLocation())
                .topics(group.getTopics().stream()
                        .map(topic -> this.modelMapper.map(topic, TopicDTO.class))
                        .collect(Collectors.toList()))
                .build();
        if (group.getMeetingDates().isEmpty()){
            dto.setNextMeetingDate(null);
        } else {
            Date nextMeetingDate = group.getMeetingDates().stream()
                    .filter((date) -> date.after(new Date()))
                    .min(Date::compareTo)
                    .orElse(null);
            dto.setNextMeetingDate(nextMeetingDate);
        }
        return dto;
    }

    public List<GroupDTO> findAll() {
        return this.groupRepository.findAll().stream()
                .map(this::mapGroupToGroupDTO)
                .collect(Collectors.toList());
    }

    public GroupDetailsDTO findById(UUID id) throws EntityNotFoundException {
        Optional<Group> groupOptional = this.groupRepository.findByIdOrderByMeetingDatesAsc(id);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + id + " not found");
        }
        Group group = groupOptional.get();
        GroupDetailsDTO dto = GroupDetailsDTO.builder()
                .id(group.getId())
                .description(group.getDescription())
                .name(group.getName())
                .location(group.getLocation())
                .admin(this.modelMapper.map(group.getAdmin(), UserDTO.class))
                .members(this.findAllMembersOfGroup(group.getId()))
                .meetingDates(group.getMeetingDates())
                .topics(group.getTopics().stream()
                        .map(topic -> this.modelMapper.map(topic, TopicDTO.class))
                        .collect(Collectors.toList()))
                .build();
        return dto;
    }

    public List<GroupDTO> findGroupsWhereUserIsAdmin(Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        User admin = this.userService.findUserByUsername(adminUsername);
        return this.groupRepository.findGroupsByAdmin(admin).stream()
                .map(this::mapGroupToGroupDTO)
                .collect(Collectors.toList());
    }


    public List<GroupDTO> findGroupsWhereUserIsMember(UUID id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName() + " with id: " + id + " not found");
        }
        return this.groupRepository.findGroupsByMembersContaining(userOptional.get()).stream()
                .map(this::mapGroupToGroupDTO)
                .collect(Collectors.toList());
    }

    /**
     * Checks if the user is anonymous, checks if the group exists, checks if the user is the admin of the group,
     * @param authentication Authentication
     * @return List<GroupDTO> list of groups where the user is the admin
     * @throws AnonymousUserException if the user is anonymous
     */
    public List<GroupDTO> findGroupsWhereUserIsMember(Authentication authentication) throws AnonymousUserException {
        User authenticatedUser = this.checkAuthenticationAndGetUser(authentication);
        return this.groupRepository.findGroupsByMembersContaining(authenticatedUser).stream()
                .map(this::mapGroupToGroupDTO)
                .collect(Collectors.toList());
    }

    public void userJoinGroup(UUID groupId, Authentication authentication) throws AnonymousUserException, UserExistsInMemberListException {
        User authenticatedUser = this.checkAuthenticationAndGetUser(authentication);
        Optional<Group> groupOptional = this.groupRepository.findById(groupId);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + groupId + " not found");
        }
        Group group = groupOptional.get();
        if(group.getMembers().contains(authenticatedUser)) {
            throw new UserExistsInMemberListException("User already in group");
        }
        authenticatedUser.getGroupsWhereMember().add(group);
        group.getMembers().add(authenticatedUser);
        groupRepository.save(group);
    }

    public void userLeaveGroup(UUID groupId, Authentication authentication) throws AnonymousUserException, UserNotFoundInGroupException {
        User authenticatedUser = this.checkAuthenticationAndGetUser(authentication);
        Optional<Group> groupOptional = this.groupRepository.findById(groupId);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + groupId + " not found");
        }
        Group group = groupOptional.get();
        if(!group.getMembers().contains(authenticatedUser)) {
            throw new UserNotFoundInGroupException("User is not a member of the group");
        }
        int membersListSize = group.getMembers().size();
        // check if user is admin of the group
        if(group.getAdmin().getId().equals(authenticatedUser.getId())) {
            // if user is admin and there are other members, set the first member as admin
            if(membersListSize > 1) {
                group.setAdmin(group.getMembers().get(1));
                authenticatedUser.getGroupsWhereMember().remove(group);
                group.getMembers().remove(authenticatedUser);
                userRepository.save(authenticatedUser);
                groupRepository.save(group);
            } else {
                // if user is admin and there are no other members, delete the group
                this.deleteGroup(groupId, authentication);
            }
        } else {
            authenticatedUser.getGroupsWhereMember().remove(group);
            group.getMembers().remove(authenticatedUser);
            userRepository.save(authenticatedUser);
            groupRepository.save(group);
        }
    }

    public void kickUserFromGroup(UUID groupId, UUID userId, Authentication authentication) throws AnonymousUserException, NotAdminOfGroupException, IsAdminOfGroupException, UserNotFoundInGroupException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        Optional<Group> groupOptional = this.groupRepository.findById(groupId);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + groupId + " not found");
        }
        Group group = groupOptional.get();
        if (!group.getAdmin().getUsername().equals(adminUsername)) {
            throw new NotAdminOfGroupException("Only the admin of the group can kick somebody from the group");
        }
        Optional<User> userOptional = this.userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName() + " with id: " + userId + " not found");
        }
        User user = userOptional.get();
        if(!group.getMembers().contains(user)) {
            throw new UserNotFoundInGroupException("User is not a member of the group");
        }
        if(group.getAdmin().getId().equals(user.getId())) {
            throw new IsAdminOfGroupException("Admin cannot be kicked from the group");
        }
        user.getGroupsWhereMember().remove(group);
        group.getMembers().remove(user);
        userRepository.save(user);
        groupRepository.save(group);
    }

    public void promoteUserToAdmin(UUID groupId, UUID userId, Authentication authentication) throws AnonymousUserException, NotAdminOfGroupException, IsAdminOfGroupException, UserNotFoundInGroupException {
        User authenticatedUser = this.checkAuthenticationAndGetUser(authentication);
        Optional<Group> groupOptional = this.groupRepository.findById(groupId);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + groupId + " not found");
        }
        Group group = groupOptional.get();
        if (!group.getAdmin().getId().equals(authenticatedUser.getId())) {
            throw new NotAdminOfGroupException("Only the admin of the group can make somebody admin");
        }
        Optional<User> userOptional = this.userRepository.findById(userId);
        if(userOptional.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName() + " with id: " + userId + " not found");
        }
        User user = userOptional.get();
        if(!group.getMembers().contains(user)) {
            throw new UserNotFoundInGroupException("User is not a member of the group");
        }
        if(group.getAdmin().getId().equals(user.getId())) {
            throw new IsAdminOfGroupException("User is already admin of the group");
        }
        group.setAdmin(user);
        user.getGroupsWhereAdmin().add(group);
        authenticatedUser.getGroupsWhereAdmin().remove(group);
        this.userRepository.save(authenticatedUser);
        this.userRepository.save(user);
        groupRepository.save(group);
    }

    public List<UserDTO> findAllMembersOfGroup(UUID groupId) {
        Optional<Group> groupOptional = this.groupRepository.findById(groupId);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + groupId + " not found");
        }
        return groupOptional.get().getMembers().stream()
                .map(user -> this.modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    /**
     * Checks if the user is authenticated, checks if the user is admin of the group, checks if the meeting dates are
     * in the future, converts list to Set so that there are no duplicates and saves the new meeting dates to the group
     * @param dto the group meeting dates DTO with the group id and the list of meeting dates
     * @param authentication the authentication object
     * @throws AnonymousUserException if the user is anonymous/has no authentication
     * @throws NotAdminOfGroupException if the user is not admin of the group
     * @throws MeetingDateIsInThePastException if the meeting date is in the past
     */
    public void addMeetingDates(GroupMeetingDatesDTO dto, Authentication authentication) throws AnonymousUserException, NotAdminOfGroupException, MeetingDateIsInThePastException {
        User authenticatedUser = this.checkAuthenticationAndGetUser(authentication);
        Optional<Group> groupOptional = this.groupRepository.findById(dto.getGroupId());
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + dto.getGroupId() + " not found");
        }
        Group group = groupOptional.get();
        if (!group.getAdmin().getId().equals(authenticatedUser.getId())) {
            throw new NotAdminOfGroupException("Only the admin of the group can add meeting dates");
        }
        for(Date date : dto.getMeetingDates()) {
            if(date.before(new Date())) {
                throw new MeetingDateIsInThePastException("Meeting date cannot be in the past");
            }
        }
        Set<Date> existingMeetingDates = new HashSet<>(group.getMeetingDates());
        existingMeetingDates.addAll(dto.getMeetingDates());
        List<Date> sortedMeetingDates = new ArrayList<>(existingMeetingDates);
        sortedMeetingDates.sort(Collections.reverseOrder());
        group.setMeetingDates(sortedMeetingDates);
        groupRepository.save(group);
    }

    /**
     * Checks if the user is authenticated, checks if the user is admin of the group,
     * removes the meeting dates from the group
     * @param dto the group meeting dates DTO with the group id and the list of meeting dates
     * @param authentication the authentication object
     * @throws AnonymousUserException if the user is anonymous/has no authentication
     * @throws NotAdminOfGroupException if the user is not admin of the group
     */
    public void removeMeetingDates(GroupMeetingDatesDTO dto, Authentication authentication) throws AnonymousUserException, NotAdminOfGroupException {
        User authenticatedUser = this.checkAuthenticationAndGetUser(authentication);
        Optional<Group> groupOptional = this.groupRepository.findById(dto.getGroupId());
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + dto.getGroupId() + " not found");
        }
        Group group = groupOptional.get();
        if (!group.getAdmin().getId().equals(authenticatedUser.getId())) {
            throw new NotAdminOfGroupException("Only the admin of the group can remove meeting dates");
        }
        for (Date date : dto.getMeetingDates()) {
            group.getMeetingDates().remove(date);
        }

        groupRepository.save(group);
    }

    private User checkAuthenticationAndGetUser(Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        return this.userService.findUserByUsername(adminUsername);
    }
}
