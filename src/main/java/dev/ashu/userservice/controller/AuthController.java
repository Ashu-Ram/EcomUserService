package dev.ashu.userservice.controller;

import dev.ashu.userservice.dto.*;
import dev.ashu.userservice.model.Session;
import dev.ashu.userservice.model.SessionStatus;
import dev.ashu.userservice.model.User;
import dev.ashu.userservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

//    @PostMapping("/signup")
//    public ResponseEntity<UserDto> signUp(@RequestBody SignUpRequestDto request) {
//        UserDto userDto = authService.signUp(request.getEmail(), request.getPassword());
//        return new ResponseEntity<>(userDto, HttpStatus.OK);
//    }
@PostMapping("/signup")
public ResponseEntity<Map<String, Object>> signUp(@RequestBody SignUpRequestDto request) {
    UserDto userDto = authService.signUp(request.getEmail(), request.getPassword());

    // Build success response
    Map<String, Object> response = new HashMap<>();
    response.put("message", "User registered successfully.");
    response.put("user", userDto);
    response.put("status", HttpStatus.CREATED.value()); // 201

    return new ResponseEntity<>(response, HttpStatus.CREATED);
}



    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto request) {
        return authService.login(request.getEmail(), request.getPassword());
    }

    @PostMapping("/logout/{id}")
    public ResponseEntity<Map<String, String>> logout(@PathVariable("id") Long userId, @RequestHeader("token") String token) {
        return authService.logout(token, userId);
    }
//    @PostMapping("/logout/{id}")
//    public ResponseEntity<Void> logout (@PathVariable("id") Long userId,@RequestHeader("token") String token) {
//        return authService.logout(token,userId);
//    }

//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(@RequestBody LogoutRequestDto request) {
//        return authService.logout(request.getToken(), request.getUserId());
//    }

    @PostMapping("/validate")
    public ResponseEntity<SessionStatus> validateToken(@RequestBody ValidateTokenRequestDto request) {
        SessionStatus sessionStatus = authService.validate(request.getToken(), request.getUserId());
        return new ResponseEntity<>(sessionStatus, HttpStatus.OK);

    }


    //below APIs are only for learning purposes, should not be present in actual systems
    @GetMapping("/session")
    public ResponseEntity<List<Session>> getAllSessions() {
        return authService.getAllSession();
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return authService.getAllUsers();
    }


}
