package com.ps.studybuddy.web.controllers;

import com.ps.studybuddy.domain.dtos.UserDTO;
import com.ps.studybuddy.domain.dtos.UserLoginDTO;
import com.ps.studybuddy.domain.entities.User;
import com.ps.studybuddy.domain.entities.UserPrincipal;
import com.ps.studybuddy.security.utility.JWTTokenProvider;
import com.ps.studybuddy.services.UserService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ps.studybuddy.security.constant.SecurityConstant.JWT_TOKEN_HEADER;

@RestController
@RequestMapping(value = "/login")
public class LoginController {
    private UserService userService;
    private ModelMapper modelMapper;
    private AuthenticationManager authenticationManager;
    private JWTTokenProvider jwtTokenProvider;

    @Autowired
    public LoginController(UserService userService, ModelMapper modelMapper, AuthenticationManager authenticationManager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.modelMapper = modelMapper;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping()
    public ResponseEntity<UserDTO> login(@RequestBody UserLoginDTO dto) {
        authenticate(dto.getUsername(), dto.getPassword());
        User loginUser = this.userService.findEntityByUsername(dto.getUsername());
        UserPrincipal userPrincipal = new UserPrincipal(loginUser);
        HttpHeaders jwtHeader = getJwtHeader(userPrincipal);
        return ResponseEntity.ok()
                .headers(jwtHeader)
                .body(this.modelMapper.map(loginUser, UserDTO.class));
    }

    private HttpHeaders getJwtHeader(UserPrincipal userPrincipal) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(JWT_TOKEN_HEADER, this.jwtTokenProvider.generateJwtToken(userPrincipal));
        return headers;
    }

    private void authenticate(String username, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
    }
}
