package com.ps.studybuddy.web.controllers;

import com.ps.studybuddy.domain.dtos.*;
import com.ps.studybuddy.exception.domain.*;
import com.ps.studybuddy.services.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @PostMapping("/kick")
    public ResponseEntity<String> kickUserFromGroup(@RequestParam("groupId") UUID groupID, @RequestParam("userId") UUID userID) throws AnonymousUserException, NotAdminOfGroupException, IsAdminOfGroupException, UserNotFoundInGroupException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.kickUserFromGroup(groupID, userID, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/promote")
    public ResponseEntity<String> promoteUserToAdmin(@RequestParam("groupId") UUID groupID, @RequestParam("userId") UUID userID) throws AnonymousUserException, NotAdminOfGroupException, IsAdminOfGroupException, UserNotFoundInGroupException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        this.groupService.promoteUserToAdmin(groupID, userID, authentication);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping()
    public ResponseEntity<List<GroupDTO>> getAllGroups() {
        List<GroupDTO> groups = this.groupService.findAll();
        return ResponseEntity.ok().body(groups);
    }
    @GetMapping(value = "/{id}")
    public ResponseEntity<GroupDetailsDTO> getGroupById(@PathVariable("id") UUID id) {
        GroupDetailsDTO dto = this.groupService.findById(id);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping(value = "/with-member/{id}")
    public ResponseEntity<List<GroupDTO>> getGroupsByMemberId(@PathVariable("id") UUID id) {
        List<GroupDTO> groups = this.groupService.findGroupsWhereUserIsMember(id);
        return ResponseEntity.ok().body(groups);
    }
    @GetMapping(value = "/members/{groupId}")
    public ResponseEntity<List<UserDTO>> getAllMembersOfGroup(@PathVariable("groupId") UUID groupId) {
        List<UserDTO> members = this.groupService.findAllMembersOfGroup(groupId);
        return ResponseEntity.ok().body(members);
    }

    @GetMapping(value = "/where-admin")
    public ResponseEntity<List<GroupDTO>> getGroupsWhereUserIsAdmin() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<GroupDTO> groups = this.groupService.findGroupsWhereUserIsAdmin(authentication);
        return ResponseEntity.ok().body(groups);
    }

    @GetMapping(value = "/where-member")
    public ResponseEntity<List<GroupDTO>> getGroupsWhereUserIsMember() throws AnonymousUserException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<GroupDTO> groups = this.groupService.findGroupsWhereUserIsMember(authentication);
        return ResponseEntity.ok().body(groups);
    }
}
