package com.ps.studybuddy.services;

import com.ps.studybuddy.domain.dtos.*;
import com.ps.studybuddy.domain.entities.Group;
import com.ps.studybuddy.domain.entities.User;
import com.ps.studybuddy.domain.repositories.GroupRepository;
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
    private final ModelMapper modelMapper;
    private final UserService userService;

    @Autowired
    public GroupService(GroupRepository groupRepository,UserRepository userRepository , ModelMapper modelMapper, UserService userService) {
        this.groupRepository = groupRepository;
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.userService = userService;
    }

    public void createGroup(GroupCreateDTO dto, Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        Group group = modelMapper.map(dto, Group.class);
        User admin = this.userService.findUserByUsername(adminUsername);
        group.setAdmin(admin);
        group.setMembers(new ArrayList<>());
        group.getMembers().add(admin);
        group.setCreatedDate(new Date());
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
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        group.setLocation(dto.getLocation());
        groupRepository.save(group);
    }

    /**
     * Checks if the user is the admin of the group, removes all associated members and deletes the group
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
        }
        groupRepository.delete(group);
    }

    public List<GroupDTO> findAll() {
        return this.groupRepository.findAll().stream()
                .map(group -> this.modelMapper.map(group, GroupDTO.class))
                .collect(Collectors.toList());
    }

    public GroupDetailsDTO findById(UUID id) throws EntityNotFoundException {
        Optional<Group> groupOptional = this.groupRepository.findById(id);
        if(groupOptional.isEmpty()) {
            throw new EntityNotFoundException(Group.class.getSimpleName() + " with id: " + id + " not found");
        }
        return GroupDetailsDTO.builder()
                .id(groupOptional.get().getId())
                .description(groupOptional.get().getDescription())
                .name(groupOptional.get().getName())
                .location(groupOptional.get().getLocation())
                .admin(this.modelMapper.map(groupOptional.get().getAdmin(), UserDTO.class))
                .members(this.findAllMembersOfGroup(groupOptional.get().getId()))
                .build();
    }

    public List<GroupDTO> findGroupsWhereUserIsAdmin(Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        User admin = this.userService.findUserByUsername(adminUsername);
        return this.groupRepository.findGroupsByAdmin(admin).stream()
                .map(group -> this.modelMapper.map(group, GroupDTO.class))
                .collect(Collectors.toList());
    }

    public List<GroupDTO> findGroupsWhereUserIsMember(UUID id) {
        Optional<User> userOptional = this.userRepository.findById(id);
        if(userOptional.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName() + " with id: " + id + " not found");
        }
        return this.groupRepository.findGroupsByMembersContaining(userOptional.get()).stream()
                .map(group -> this.modelMapper.map(group, GroupDTO.class))
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
                .map(group -> this.modelMapper.map(group, GroupDTO.class))
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

    private User checkAuthenticationAndGetUser(Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String adminUsername = authentication.getName();
        return this.userService.findUserByUsername(adminUsername);
    }
}
