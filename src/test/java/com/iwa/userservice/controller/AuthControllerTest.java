package com.iwa.userservice.controller;

import com.iwa.userservice.config.TestSecurityConfig;
import com.iwa.userservice.model.User;
import com.iwa.userservice.security.JwtTokenUtil;
import com.iwa.userservice.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@TestPropertySource(properties = {
        "security.jwt.secret=TestSDSSSDFSecDFDFretKeyForJQSDWTPLUSL234:://ZEFNEZUFHUZQSESSAIREPOURLETYPEDENCODAZEKFNEZUFNEZUFEZOIFJGE",
        "spring.security.filter.csrf.enabled=false"
})
@Import({JwtTokenUtil.class, TestSecurityConfig.class})
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPassword("encodedPassword");
        user.setPrenom("John");
        user.setNom("Doe");

        when(passwordEncoder.encode(any(String.class))).thenReturn("encodedPassword");
        when(passwordEncoder.matches(any(String.class), any(String.class))).thenReturn(true);
    }

    @Test
    @WithMockUser
    void testRegisterSuccess() throws Exception {
        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password123\", \"prenom\":\"John\", \"nom\":\"Doe\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.prenom").value("John"))
                .andExpect(jsonPath("$.nom").value("Doe"));
    }

    @Test
    @WithMockUser
    void testLoginSuccess() throws Exception {
        // Simulez un mot de passe encodé pour l'utilisateur
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPassword = passwordEncoder.encode("password123");

        User mockUser = new User();
        mockUser.setEmail("test@example.com");
        mockUser.setPassword(encodedPassword);

        // Mock du service utilisateur
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(mockUser));

        // Effectuer la requête de connexion
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @WithMockUser
    void testLoginUserNotFound() throws Exception {
        when(userService.getUserByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nonexistent@example.com\", \"password\":\"password123\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Utilisateur introuvable avec cet email."));
    }

    @Test
    @WithMockUser
    void testLoginInvalidPassword() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@example.com\", \"password\":\"wrongPassword\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Mot de passe incorrect."));
    }
}