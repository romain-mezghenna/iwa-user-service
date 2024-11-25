package com.iwa.userservice.controller;

import com.iwa.userservice.model.User;
import com.iwa.userservice.security.JwtTokenFilter;
import com.iwa.userservice.security.JwtTokenUtil;
import com.iwa.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@TestPropertySource(properties = {
        "security.jwt.secret=TestSecretKeyForJWT"
})
@Import({JwtTokenUtil.class, JwtTokenFilter.class})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private WebApplicationContext context;

    private User user;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPrenom("John");
        user.setNom("Doe");
        // Configurez d'autres propriétés de l'utilisateur si nécessaire
    }

    @Test
    @WithMockUser
    public void testCreateUser() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\",\"prenom\":\"John\",\"nom\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.prenom").value("John"))
                .andExpect(jsonPath("$.nom").value("Doe"));
    }

    @Test
    @WithMockUser
    public void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("test@example.com"))
                .andExpect(jsonPath("$[0].prenom").value("John"))
                .andExpect(jsonPath("$[0].nom").value("Doe"));
    }

    @Test
    @WithMockUser
    public void testGetUserById() throws Exception {
        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/users/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.prenom").value("John"))
                .andExpect(jsonPath("$.nom").value("Doe"));
    }

    @Test
    @WithMockUser
    public void testUpdateUser() throws Exception {
        when(userService.updateUser(any(Long.class), any(User.class))).thenReturn(user);

        mockMvc.perform(put("/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"updated@example.com\",\"prenom\":\"Jane\",\"nom\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.prenom").value("John"))
                .andExpect(jsonPath("$.nom").value("Doe"));
    }

    @Test
    void testDeleteUser() throws Exception {
        // Données de test
        Long userId = 1L;
        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@example.com");

        // Simuler les droits d'administrateur
        Mockito.when(userService.isAdmin(userId)).thenReturn(true);

        // Mock de l'utilisateur connecté
        Authentication mockAuthentication = Mockito.mock(Authentication.class);
        Mockito.when(mockAuthentication.getPrincipal()).thenReturn(mockUser);

        // Effectuer la requête DELETE
        mockMvc.perform(delete("/users/{id}", userId)
                        .principal(mockAuthentication)) // Injecter le mock d'`Authentication`
                .andExpect(status().isOk())
                .andExpect(content().string("Utilisateur supprimé avec succès"));

        // Vérifier que le service a été appelé avec les bons paramètres
        Mockito.verify(userService).deleteUser(userId);
    }
}