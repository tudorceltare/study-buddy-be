package com.ps.studybuddy.services;

import com.ps.studybuddy.domain.dtos.*;
import com.ps.studybuddy.domain.entities.Group;
import com.ps.studybuddy.domain.entities.User;
import com.ps.studybuddy.domain.entities.UserPrincipal;
import com.ps.studybuddy.domain.enumeration.Role;
import com.ps.studybuddy.domain.repositories.UserRepository;
import com.ps.studybuddy.exception.domain.AnonymousUserException;
import com.ps.studybuddy.exception.domain.EmailExistException;
import com.ps.studybuddy.exception.domain.UserNotFoundException;
import com.ps.studybuddy.exception.domain.UsernameExistException;
import org.apache.commons.lang3.StringUtils;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@Qualifier("userDetailsService")
public class UserService implements UserDetailsService {
    public static final String NO_PERSON_FOUND_BY_USERNAME = "No person found by username: ";
    public static final String USERNAME_ALREADY_EXISTS = "Username already exists";
    public static final String EMAIL_ALREADY_EXISTS = "Email already exists";

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Autowired
    public UserService(
            UserRepository userRepository,
            ModelMapper modelMapper,
            BCryptPasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> userOptional = this.userRepository.findUserByUsername(username);
        if(userOptional.isEmpty()) {
            LOGGER.error(NO_PERSON_FOUND_BY_USERNAME + username);
            throw new UsernameNotFoundException(NO_PERSON_FOUND_BY_USERNAME + username);
        } else {
            return new UserPrincipal(userOptional.get());
        }
    }

    public List<UserDTO> findAll() {
        return this.userRepository.findAll().stream()
                .map(user -> this.modelMapper.map(user, UserDTO.class))
                .collect(Collectors.toList());
    }

    public UserDTO findById(UUID id) throws EntityNotFoundException {
        Optional<User> personOptional = this.userRepository.findById(id);
        if(personOptional.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        return this.modelMapper.map(personOptional.get(), UserDTO.class);
    }

    public String register(UserCreateDTO dto) throws UserNotFoundException, EmailExistException, UsernameExistException {
        validateNewUsernameAndEmail(StringUtils.EMPTY, dto.getUsername(), dto.getEmail());
        User user = this.modelMapper.map(dto, User.class);
        user.setCreatedDate(new Date(System.currentTimeMillis()));
        String encodedPassword = encodePassword(user.getPassword());
        user.setPassword(encodedPassword);
        user.setAvatarColor(this.stringToColour(user.getUsername()));
        user.setActive(true);
        user.setNotLocked(true);
        user.setRole(dto.getRole());
        user.setAuthorities(getRoleEnumName(dto.getRole()).getAuthorities());
        user.setGroupsWhereMember(new ArrayList<>());
        user.setGroupsWhereAdmin(new ArrayList<>());
        this.userRepository.save(user);
        LOGGER.info("New user password: " + dto.getPassword());
        return user.getId().toString();
    }

    public UserDTO updateUser(UserUpdateDTO dto) throws UserNotFoundException, EmailExistException, UsernameExistException, EntityNotFoundException {
        validateNewUsernameAndEmail(dto.getUsername(), dto.getNewUsername(), dto.getNewEmail());
        Optional<User> userOptional = this.userRepository.findById(dto.getId());
        if(userOptional.isEmpty()){
            throw new EntityNotFoundException();
        }
        User user = User.builder()
                .id(userOptional.get().getId())
                .username(dto.getNewUsername())
                .email(dto.getNewEmail())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .build();
        user.setActive(dto.isActive());
        user.setNotLocked(dto.isNotLocked());
        user.setId(userOptional.get().getId());
        user.setCreatedDate(userOptional.get().getCreatedDate());
        user.setPassword(userOptional.get().getPassword());
        user.setAvatarColor(this.stringToColour(dto.getUsername()));
        user.setRole(userOptional.get().getRole());
        user.setAuthorities(userOptional.get().getAuthorities());
        user.setGroupsWhereMember(userOptional.get().getGroupsWhereMember());
        user.setGroupsWhereAdmin(userOptional.get().getGroupsWhereAdmin());
        this.userRepository.save(user);
        return this.modelMapper.map(user, UserDTO.class);
    }

    public void deleteById(UUID id) throws EntityNotFoundException {
        Optional<User> userOptional = this.userRepository.findById(id);
        if (userOptional.isEmpty()) {
            throw new EntityNotFoundException(User.class.getSimpleName() + " with id: " + id);
        }
        // delete all groups that the user is an admin of
        for(Group group : userOptional.get().getGroupsWhereAdmin()) {
            // this.groupService.delete(group);
        }

        // delete all instances of the user in groups where they are a member
        for(Group group : userOptional.get().getGroupsWhereMember()) {
            if(group.getAdmin().getId().equals(id)) {
                // this.groupService.removeMember(group, userOptional.get());
            }
        }
        this.userRepository.deleteById(id);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private UserDTO validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UserNotFoundException, EmailExistException, UsernameExistException {
        UserDTO userByUsername = findByUsername(newUsername);
        UserDTO userByNewEmail = findByEmail(newEmail);
        if (StringUtils.isNotBlank(currentUsername)) {
            UserDTO currentPerson = findByUsername(currentUsername);
            if (currentPerson == null) {
                throw new UserNotFoundException(NO_PERSON_FOUND_BY_USERNAME + currentUsername);
            }
            if (userByUsername != null && !currentPerson.getId().equals(userByUsername.getId())) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null && !currentPerson.getId().equals(userByNewEmail.getId())) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return currentPerson;
        } else {
            if (userByUsername != null) {
                throw new UsernameExistException(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewEmail != null) {
                throw new EmailExistException(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    public UserDTO findByUsername(String username) {
        Optional<User> optional = this.userRepository.findUserByUsername(username);
        return optional.map(user -> this.modelMapper.map(user, UserDTO.class)).orElse(null);
    }

    public UserDTO findByEmail(String email) {
        Optional<User> optional = this.userRepository.findUserByEmail(email);
        return optional.map(user -> this.modelMapper.map(user, UserDTO.class)).orElse(null);
    }

    private Role getRoleEnumName(String role) {
        return Role.valueOf(role);
    }

    /**
     * A function that generates a random color hex code from a user's username
     * @param username a string of variable length
     * @return a string of a hex code
     */
    public String stringToColour(String username) {
        int hash = 0;
        for (int i = 0; i < username.length(); i++) {
            hash = username.charAt(i) + ((hash << 5) - hash);
        }
        StringBuilder colour = new StringBuilder("#");
        for (int i = 0; i < 3; i++) {
            int value = (hash >> (i * 8)) & 0xFF;
            colour.append(String.format("%02x", value));
        }
        return colour.toString();
    }

    public User findUserByUsername(String username) {
        Optional<User> optional = this.userRepository.findUserByUsername(username);
        if (optional.isEmpty()) {
            throw new EntityNotFoundException("User with username: " + username + " not found");
        }
        return optional.get();
    }

    public List<MeetingDTO> findAllMeetingDatesOfUserWhereMember(Authentication authentication) throws AnonymousUserException {
        User user = checkAuthenticationAndGetUser(authentication);
        return findAllMeetingDatesFromGroups(user.getGroupsWhereMember());
    }

    public List<MeetingDTO> findAllMeetingDatesOfUserWhereAdmin(Authentication authentication) throws AnonymousUserException {
        User user = checkAuthenticationAndGetUser(authentication);
        return findAllMeetingDatesFromGroups(user.getGroupsWhereAdmin());
    }

    private List<MeetingDTO> findAllMeetingDatesFromGroups(List<Group> groups) {
        List<MeetingDTO> meetings = new ArrayList<>();
        for(Group group : groups) {
            for(Date meetingDate : group.getMeetingDates()) {
                MeetingDTO meeting = MeetingDTO.builder()
                        .meetingDate(meetingDate)
                        .location(this.modelMapper.map(group.getLocation(), LocationDTO.class))
                        .groupName(group.getName())
                        .groupId(group.getId())
                        .build();
                meetings.add(meeting);
            }
        }
        return meetings;
    }

    public List<LocationDTO> findAllMeetingLocationsOfUserWhereMember(Authentication authentication) throws AnonymousUserException {
        User user = checkAuthenticationAndGetUser(authentication);
        return findAllMeetingLocationsOfGroups(user.getGroupsWhereMember());
    }

    public List<LocationDTO> findAllMeetingLocationsOfUserWhereAdmin(Authentication authentication) throws AnonymousUserException {
        User user = checkAuthenticationAndGetUser(authentication);
        return findAllMeetingLocationsOfGroups(user.getGroupsWhereAdmin());
    }

    private List<LocationDTO> findAllMeetingLocationsOfGroups(List<Group> groups) {
        List<LocationDTO> locations = new ArrayList<>();
        for(Group group : groups) {
            locations.add(this.modelMapper.map(group.getLocation(), LocationDTO.class));
        }
        return locations;
    }

    private User checkAuthenticationAndGetUser(Authentication authentication) throws AnonymousUserException {
        if (authentication instanceof AnonymousAuthenticationToken) {
            throw new AnonymousUserException("Anonymous user cannot create a group");
        }
        String username = authentication.getName();
        return this.findUserByUsername(username);
    }
}
