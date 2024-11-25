package com.iwa.userservice.repository;

import com.iwa.userservice.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:testdb"
})
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        // Créer un utilisateur fictif
        user = new User();
        user.setPrenom("John");
        user.setNom("Doe");
        user.setEmail("test@example.com");
        user.setAdresse("123 Rue Exemple");
        user.setTelephone("1234567890");
        user.setPassword("password");
    }

    @Test
    void testSaveUser() {
        // Sauvegarder un utilisateur
        User savedUser = userRepository.save(user);

        // Vérifier que l'utilisateur est sauvegardé correctement
        assertNotNull(savedUser.getId());
        assertEquals("test@example.com", savedUser.getEmail());
    }

    @Test
    void testFindById() {
        // Sauvegarder un utilisateur
        User savedUser = userRepository.save(user);

        // Récupérer l'utilisateur par ID
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());

        // Vérifier que l'utilisateur est récupéré correctement
        assertTrue(retrievedUser.isPresent());
        assertEquals(savedUser.getEmail(), retrievedUser.get().getEmail());
    }

    @Test
    void testFindByEmail() {
        // Sauvegarder un utilisateur
        userRepository.save(user);

        // Récupérer l'utilisateur par email
        Optional<User> retrievedUser = userRepository.findByEmail("test@example.com");

        // Vérifier que l'utilisateur est récupéré correctement
        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getEmail(), retrievedUser.get().getEmail());
    }

    @Test
    void testDeleteUser() {
        // Sauvegarder un utilisateur
        User savedUser = userRepository.save(user);

        // Supprimer l'utilisateur
        userRepository.deleteById(savedUser.getId());

        // Vérifier que l'utilisateur est supprimé
        Optional<User> retrievedUser = userRepository.findById(savedUser.getId());
        assertFalse(retrievedUser.isPresent());
    }
}