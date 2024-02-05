package com.linkshortener.service;

import com.linkshortener.entity.Group;
import com.linkshortener.entity.User;
import com.linkshortener.enums.UserGroup;
import com.linkshortener.exception.UserAlreadyExistException;
import com.linkshortener.exception.UserNotFoundException;
import com.linkshortener.repository.GroupRepository;
import com.linkshortener.repository.UserRepository;
import com.linkshortener.security.AuthenticationResponse;
import com.linkshortener.security.CustomUserDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final static Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, GroupRepository groupRepository, JwtService jwtService, BCryptPasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.groupRepository = groupRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }


    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    public Optional<User> getUserByEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            return Optional.of(userRepository.findByEmail(email).get());
        }

        LOGGER.warn("User with email: {} was not found", email);

        return Optional.empty();
    }


    @Transactional
    public AuthenticationResponse addUser(User user, UserGroup userGroup) {
        Optional<User> foundedUser = userRepository.findByEmail(user.getEmail());

        if (foundedUser.isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            addGroupToUser(user, userGroup);
            userRepository.save(user);
            UserDetails userDetails = new CustomUserDetails(user.getEmail(), user.getPassword());
            String jwt = jwtService.generateJwt(new HashMap<>(), userDetails);

            return new AuthenticationResponse(jwt);
        }

        throw new UserAlreadyExistException(user.getEmail());
    }


    public void addGroupToUser(User user, UserGroup userGroup) {
        Set<Group> groups = new HashSet<>();

        if (user.getRoles() != null) {
            groups.addAll(user.getRoles());
        }

        String role = userGroup.toString();

        LOGGER.info("Adding user with groups :{} and Role :{}", groups, groupRepository.findByCode(role).orElse(new Group("Not found", "")));
        groupRepository.findByCode(role).ifPresent(groups::add);
        user.setRoles(groups);
    }


    public AuthenticationResponse getAuthenticationResponse(User user) {
        String jwt;

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        } catch (AuthenticationException e) {
            throw new UserNotFoundException(user.getEmail());
        }

        UserDetails userDetails = new CustomUserDetails(user.getEmail(), user.getPassword());
        jwt = jwtService.generateJwt(new HashMap<>(), userDetails);

        return new AuthenticationResponse(jwt);
    }


    @Transactional
    public void removeAllUsers() {
        userRepository.deleteAll();
    }

    @Transactional
    public void removeUser(Long id) {
        userRepository.findById(id).ifPresent(userRepository::delete);
    }
}
