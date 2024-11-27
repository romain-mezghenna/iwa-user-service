package com.iwa.userservice.controller;
import com.iwa.userservice.security.JwtTokenUtil;
import com.netflix.discovery.converters.Auto;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import com.iwa.userservice.model.User;
import com.iwa.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;



    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    @GetMapping
    public List<User> getAllUsers() {
        // TODO VÉRIFICATION DES DROITS
        return userService.getAllUsers();
    }

    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        // TODO VÉRIFICATION DES DROITS
        return userService.getUserById(id).orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @PutMapping("/{id}")
    public User updateUser(@PathVariable Long id, @RequestBody User userDetails) {
        return userService.updateUser(id, userDetails);
    }

    @GetMapping("/profile")
    public User getUserProfile(@RequestHeader("Authorization") String token) {
        // Extraire le JWT (supprimer le préfixe "Bearer ")
        String jwt = token.replace("Bearer ", "");

        // Décoder le JWT pour obtenir l'ID utilisateur
        Long userId = jwtTokenUtil.getUserId(jwt);

        // Rechercher l'utilisateur avec l'ID extrait
        return userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<?> getUserDetails(@PathVariable Long id) {
        // Rechercher l'utilisateur avec l'ID fourni
        return userService.getUserById(id)
                .map(user -> ResponseEntity.ok(Map.of(
                        "nom", user.getNom(),
                        "prenom", user.getPrenom(),
                        "photo", user.getPhoto()
                )))
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("message","Utilisateur non trouvé")));
    }

    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id,Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(id) && !userService.isAdmin(currentUser.getId())) {
            throw new RuntimeException("Accès refusé");
        }
        userService.deleteUser(id);
        // Ici, vous pouvez implémenter la notification à l'admin via Kafka
        return "Utilisateur supprimé avec succès";
    }
}