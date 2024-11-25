package com.iwa.userservice.controller;
import org.springframework.security.core.Authentication;
import com.iwa.userservice.model.User;
import com.iwa.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

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