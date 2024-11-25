package com.iwa.userservice.service;

import com.iwa.userservice.model.User;
import com.iwa.userservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialisation d'un utilisateur fictif
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPrenom("John");
        user.setNom("Doe");
        user.setAdresse("123 Rue Exemple");
        user.setTelephone("1234567890");
    }

    @Test
    void testCreateUserSuccess() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User createdUser = userService.createUser(user);

        assertNotNull(createdUser);
        assertEquals(user.getEmail(), createdUser.getEmail());
        verify(userRepository).save(user);
    }

    @Test
    void testCreateUserThrowsExceptionWhenEmailExists() {
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.createUser(user));

        assertEquals("Email déjà utilisé", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testIsAdmin() {
        String adminIds = "1,2,3";
        userService = new UserService();
        userService.adminIds = adminIds;

        assertTrue(userService.isAdmin(1L));
        assertFalse(userService.isAdmin(4L));
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));

        List<User> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(1, users.size());
        assertEquals(user.getEmail(), users.get(0).getEmail());
        verify(userRepository).findAll();
    }

    @Test
    void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> retrievedUser = userService.getUserById(1L);

        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getEmail(), retrievedUser.get().getEmail());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserByIdNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> retrievedUser = userService.getUserById(1L);

        assertFalse(retrievedUser.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void testGetUserByEmail() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        Optional<User> retrievedUser = userService.getUserByEmail("test@example.com");

        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getEmail(), retrievedUser.get().getEmail());
        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void testUpdateUser() {
        User updatedUser = new User();
        updatedUser.setPrenom("Jane");
        updatedUser.setNom("Doe");
        updatedUser.setEmail("updated@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        User result = userService.updateUser(1L, updatedUser);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        assertEquals(user.getPrenom(), result.getPrenom());
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUserThrowsExceptionWhenNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> userService.updateUser(1L, user));

        assertEquals("Utilisateur non trouvé", exception.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testDeleteUser() {
        // Données de test
        Long userId = 1L;

        // Simuler le comportement de KafkaTemplate pour renvoyer un ListenableFuture simulé
        when(kafkaTemplate.send(eq("user-deletion"), eq(String.valueOf(userId)))).thenReturn(null);

        // Appeler la méthode de suppression
        userService.deleteUser(userId);

        // Vérifier que la méthode send de KafkaTemplate a été appelée avec les bons arguments
        verify(kafkaTemplate).send(eq("user-deletion"), eq(String.valueOf(userId)));
    }
}