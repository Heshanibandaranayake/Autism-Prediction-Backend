package com.research.backend.controllers;

import com.research.backend.dto.UserDto;
import com.research.backend.entities.User;
import com.research.backend.enums.UserTypes;
import com.research.backend.exception.ResourceNotFoundException;
import com.research.backend.repositories.UserRepository;
import com.research.backend.security.AuthenticationRequest;
import com.research.backend.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostMapping("/signin")
    public ResponseEntity signin(@RequestBody AuthenticationRequest data) {
        try {
            String username = data.getUsername();
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, data.getPassword()));
            String token = jwtTokenProvider.createToken(username, userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "Username", username)).getRoles());

            User user = userRepository.findByUsername(username).orElseThrow(() -> new ResourceNotFoundException("User", "Username", username));
            Map<Object, Object> model = new HashMap<>();
            model.put("username", username);
            model.put("token", token);
            model.put("userType",user.getUserType());
            return ResponseEntity.ok(model);
        }catch (AuthenticationException e){
            throw new BadCredentialsException("Invalid username/password supplied");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity signup(@RequestBody UserDto userDto){
        User user = new User(userDto.getUsername(), passwordEncoder.encode(userDto.getPassword()), userDto.getName());
        user.setUserType(userDto.getUserType());
        user.setRoles(Arrays.asList( "ROLE_USER"));
        user = userRepository.save(user);
        return ResponseEntity.ok(userDto);
    }

}
