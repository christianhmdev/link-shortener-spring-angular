package com.linkshortener.controller;

import com.linkshortener.dto.UserDto;
import com.linkshortener.entity.User;
import com.linkshortener.enums.UserGroup;
import com.linkshortener.security.AuthenticationResponse;
import com.linkshortener.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Validated
@RestController
@RequestMapping("/api")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Returns all users",
            description = "Returns all users. If user has ADMIN authorities than it wil return all users. If user not authenticated than the result will be empty array.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "users returned",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserDto.class))}),
            @ApiResponse(responseCode = "403", description = "forbidden for user without ADMIN authority")
    })
    @GetMapping("/users")
    public List<UserDto> getUsers() {
        return userService.getAllUsers().stream().map(this::convertToUserDto).collect(Collectors.toList());
    }

    @Operation(summary = "Creates user",
            description = "Creates user with USER authorities, fails if email is already used.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user added",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))}),
            @ApiResponse(responseCode = "400", description = "not valid user"),
            @ApiResponse(responseCode = "409", description = "email already exist")
    })
    @PostMapping("/users")
    public AuthenticationResponse register(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(convertToUser(userDto), UserGroup.USER);
    }

    @Operation(summary = "Creates user",
            description = "Creates user with ADMIN authorities, fails if email is already used and user doesn't have ADMIN authorities.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "user added",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))}),
            @ApiResponse(responseCode = "400", description = "not valid user"),
            @ApiResponse(responseCode = "403", description = "forbidden for user without ADMIN authority"),
            @ApiResponse(responseCode = "409", description = "email already exist")
    })
    @PostMapping("/users/admin")
    public AuthenticationResponse registerAdmin(@Valid @RequestBody UserDto userDto) {
        return userService.addUser(convertToUser(userDto), UserGroup.ADMIN);
    }

    @Operation(summary = "Logins user",
            description = "Logins and returns jwt token for authorization")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "login was successful",
                    content = {@Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthenticationResponse.class))}),
            @ApiResponse(responseCode = "400", description = "not valid user"),
            @ApiResponse(responseCode = "403", description = "user or password incorrect")
    })
    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody UserDto userDto) {
        return userService.getAuthenticationResponse(convertToUser(userDto));
    }

    @Operation(summary = "Delete all users",
            description = "Delete all users if signed as admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "removed all users"),
            @ApiResponse(responseCode = "403", description = "forbidden for user without ADMIN authority")
    })
    @DeleteMapping("/users")
    public void deleteUsers() {
        userService.removeAllUsers();
    }

    private UserDto convertToUserDto(User user) {
        return new UserDto(user.getEmail(), user.getPassword());
    }

    private User convertToUser(UserDto userDto) {
        return new User(userDto.getEmail(), userDto.getPassword());
    }
}
