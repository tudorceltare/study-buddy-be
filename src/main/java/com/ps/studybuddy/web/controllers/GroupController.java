package com.ps.studybuddy.web.controllers;

import com.ps.studybuddy.domain.dtos.*;
import com.ps.studybuddy.domain.entities.UserPrincipal;
import com.ps.studybuddy.exception.domain.AnonymousUserException;
import com.ps.studybuddy.exception.domain.NotAdminOfGroupException;
import com.ps.studybuddy.exception.domain.UserExistsInMemberListException;
import com.ps.studybuddy.exception.domain.UserNotFoundInGroupException;
import com.ps.studybuddy.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = {"/groups"})
public class GroupController {
    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    @PostMapping("/create")
    public ResponseEntity<String> createGroup(@RequestBody GroupCreateDTO dto) throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.createGroup(dto, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<String> updateGroup(@RequestBody GroupUpdateDTO dto) throws NotAdminOfGroupException, AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.updateGroup(dto, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<String> deleteGroup(@PathVariable("groupId") UUID groupId) throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.deleteGroup(groupId, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/join/{groupId}")
    public ResponseEntity<String> joinGroup(@PathVariable("groupId") UUID groupId) throws AnonymousUserException, UserExistsInMemberListException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.userJoinGroup(groupId, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/leave/{groupId}")
    public ResponseEntity<String> leaveGroup(@PathVariable("groupId") UUID groupId) throws UserNotFoundInGroupException, AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.userLeaveGroup(groupId, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = this.groupService.findAll();
        return ResponseEntity.ok().body(groups);
    }
    @GetMapping(value = "/{id}")
    public ResponseEntity<GroupDTO> getGroupById(@PathVariable("id") UUID id) {
        GroupDTO dto = this.groupService.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping(value = "/member/{id}")
    public ResponseEntity<List<GroupDTO>> getGroupsByMemberId(@PathVariable("id") UUID id) {
        List<GroupDTO> groups = this.groupService.findGroupsWhereUserIsMember(id);
        return ResponseEntity.ok().body(groups);
    }
    @GetMapping(value = "/members/{groupId}")
    public ResponseEntity<List<UserDTO>> getAllMembersOfGroup(@PathVariable("groupId") UUID groupId) {
        List<UserDTO> members = this.groupService.findAllMembersOfGroup(groupId);
        return ResponseEntity.ok().body(members);
    }

    @GetMapping(value = "/group-where-admin")
    public ResponseEntity<List<GroupDTO>> getGroupsByAdminId() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<GroupDTO> groups = this.groupService.findGroupsWhereUserIsAdmin(authentication);
        return ResponseEntity.ok().body(groups);
    }

    @PostMapping(value = "/test")
    @ResponseBody
    public ResponseEntity<String> test(Authentication authentication) {
//        System.out.println("Security Context Holder: " + SecurityContextHolder.getContext().toString());
//        System.out.println("Authentication: " + authentication.toString());
//        System.out.println("Principal: " + authentication.getPrincipal().toString());
//        System.out.println("Credentials: " + authentication.getCredentials().toString());
//        System.out.println("Details: " + authentication.getDetails().toString());
//        System.out.println("Authorities: " + authentication.getAuthorities().toString());
//        System.out.println("name: " + authentication.getName());
        System.out.println("Get user principal");
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            System.out.println("UserPrincipal: " + userPrincipal.toString());
        }
        return ResponseEntity.ok().body(authentication.getPrincipal().toString());
    }
}
