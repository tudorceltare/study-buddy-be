package com.ps.studybuddy.web.controllers;

import com.ps.studybuddy.domain.dtos.*;
import com.ps.studybuddy.exception.domain.AnonymousUserException;
import com.ps.studybuddy.exception.domain.EmailExistException;
import com.ps.studybuddy.exception.handler.ExceptionHandling;
import com.ps.studybuddy.exception.domain.UserNotFoundException;
import com.ps.studybuddy.exception.domain.UsernameExistException;
import com.ps.studybuddy.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = {"/users"})
public class UserController extends ExceptionHandling {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    @PreAuthorize("hasAnyAuthority('user:read')")
    public List<UserDTO> getAll() {
        return this.userService.findAll();
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserCreateDTO dto) throws UserNotFoundException, EmailExistException, UsernameExistException {
        this.userService.register(dto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('user:read')")
    public ResponseEntity<UserDTO> getUserById(@PathVariable("id") UUID id) {
        UserDTO dto = this.userService.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @DeleteMapping(value = "/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public void delete(@PathVariable("id") UUID id) {
        this.userService.deleteById(id);
    }

    @PostMapping("/update")
    @PreAuthorize("hasAnyAuthority('user:update')")
    public ResponseEntity<UserDTO> update(@RequestBody UserUpdateDTO dto) throws UserNotFoundException, EmailExistException, UsernameExistException, EntityNotFoundException {
        UserDTO userDTO = this.userService.updateUser(dto);
        return ResponseEntity.ok().body(userDTO);
    }

    @GetMapping("meeting-locations-admin")
    public ResponseEntity<List<LocationDTO>> getAllMeetingLocationsWhereAdmin() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<LocationDTO> locationDTOS = this.userService.findAllMeetingLocationsOfUserWhereAdmin(authentication);
        return ResponseEntity.ok().body(locationDTOS);
    }

    @GetMapping("meeting-locations-member")
    public ResponseEntity<List<LocationDTO>> getAllMeetingLocationsWhereMember() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<LocationDTO> locationDTOS = this.userService.findAllMeetingLocationsOfUserWhereMember(authentication);
        return ResponseEntity.ok().body(locationDTOS);
    }

    @GetMapping("meetings-admin")
    public ResponseEntity<List<MeetingDTO>> getAllMeetingsWhereAdmin() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<MeetingDTO> meetingDTOS = this.userService.findAllMeetingDatesOfUserWhereAdmin(authentication);
        return ResponseEntity.ok().body(meetingDTOS);
    }

    @GetMapping("meetings-member")
    public ResponseEntity<List<MeetingDTO>> getAllMeetingsWhereMember() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<MeetingDTO> meetingDTOS = this.userService.findAllMeetingDatesOfUserWhereMember(authentication);
        return ResponseEntity.ok().body(meetingDTOS);
    }
}
