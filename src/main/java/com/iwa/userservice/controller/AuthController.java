package com.iwa.userservice.controller;

import com.iwa.userservice.model.User;
import com.iwa.userservice.service.UserService;
import com.iwa.userservice.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userService.createUser(user);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User loginRequest) {
        // Chercher l'utilisateur par email
        Optional<User> user = userService.getUserByEmail(loginRequest.getEmail());

        // Vérifier si l'utilisateur existe
        if (user.isEmpty()) {
            // Notification d'erreur si l'utilisateur n'existe pas
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Utilisateur introuvable avec cet email."));
        }

        // Vérifier si le mot de passe correspond
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.get().getPassword())) {
            // Notification d'erreur pour mot de passe incorrect
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Mot de passe incorrect."));
        }

        // Si tout est valide, générer et retourner le token JWT
        String token = jwtTokenUtil.generateAccessToken(user.get().getId());
        return ResponseEntity.ok(Map.of("token", token));
    }

}