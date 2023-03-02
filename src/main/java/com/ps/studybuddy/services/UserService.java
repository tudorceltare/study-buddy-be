package com.ps.studybuddy.services;

import com.ps.studybuddy.domain.entities.User;
import com.ps.studybuddy.domain.entities.UserPrincipal;
import com.ps.studybuddy.domain.repositories.UserRepository;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

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
}
