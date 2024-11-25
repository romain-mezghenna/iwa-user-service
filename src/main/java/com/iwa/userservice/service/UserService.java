package com.iwa.userservice.service;

import com.iwa.userservice.model.User;
import com.iwa.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Value("${admin.ids}")
    String adminIds;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public User createUser(User user) {
        // Vérifier si l'email existe déjà
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email déjà utilisé");
        }
        return userRepository.save(user);
    }

    public boolean isAdmin(Long userId) {
        List<String> adminIdList = Arrays.asList(adminIds.split(","));
        return adminIdList.contains(String.valueOf(userId));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByEmail(String email){
        return userRepository.findByEmail(email);
    }

    public User updateUser(Long userId, User updatedUser) {
        // Récupération de l'utilisateur existant
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mettre à jour uniquement les champs non-nuls
        if (updatedUser.getEmail() != null) {
            existingUser.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null) {
            existingUser.setPassword(updatedUser.getPassword());
        }
        if (updatedUser.getNom() != null) {
            existingUser.setNom(updatedUser.getNom());
        }
        if (updatedUser.getPrenom() != null) {
            existingUser.setPrenom(updatedUser.getPrenom());
        }
        if (updatedUser.getTelephone() != null) {
            existingUser.setTelephone(updatedUser.getTelephone());
        }
        if (updatedUser.getAdresse() != null) {
            existingUser.setAdresse(updatedUser.getAdresse());
        }
        if (updatedUser.getPhoto() != null) {
            existingUser.setPhoto(updatedUser.getPhoto());
        }

        // Sauvegarde dans la base de données
        return userRepository.save(existingUser);
    }

    public void deleteUser(Long id) {
        // Envoyer une notification à l'admin
        kafkaTemplate.send("user-deletion", String.valueOf(id));
        System.out.println("Demande de suppression à un administrateur de l'user : " + id.toString());
    }
}