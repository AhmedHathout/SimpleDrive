package com.ahmedhathout.SimpleDrive.controller;

import com.ahmedhathout.SimpleDrive.entities.ConfirmationToken;
import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.exceptions.UserAlreadyExistsException;
import com.ahmedhathout.SimpleDrive.repositories.ConfirmationTokenRepository;
import com.ahmedhathout.SimpleDrive.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.validation.BindException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class UserControllerIT {

    private static final String CONTENT_TYPE = "application/x-www-form-urlencoded";

    public static final String VALID_EMAIL = "valid.user@SimpleDrive.com".toLowerCase().trim();
    public static final String MATCHING_PASSWORD = "123456789";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    public UserRepository userRepository;

    @Autowired
    private ConfirmationTokenRepository confirmationTokenRepository;


    @BeforeAll
    static void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
    }

    @Test
    public void givenValidUserData_whenSignup_thenCreateConfirmationToken_whenConfirmed_thenSaveUserToDBAndDeleteToken() throws Exception {

        // Given
        String email = "valid.user@SimpleDrive.com".toLowerCase().trim();
        User user = User.builder()
                .firstName("Valid")
                .lastName("User")
                .email(email)
                .password("123456789").build();

        // When
        signUp(user, true)
        // Then
                .andExpect(status().isOk());

        ConfirmationToken confirmationToken = confirmationTokenRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new AssertionError("Could not find a token with the email " + user.getEmail()));

        // When
        confirmSignup(confirmationToken, user);

        // Then
        assertThat(confirmationTokenRepository.findAll()).isEmpty();
        assertThat(userRepository.loadUserByUsername(email)).isNotNull();
    }

    @Test
    public void createAnotherValidUser() throws Exception {
        String email = "valid.user2@simpledrive.com";
        createUser(User.builder()
                .firstName("Valid")
                .lastName("User2")
                .email(email)
                .password("123456789").build());

        assertThat(userRepository.findUserByEmail(email).isPresent());
    }

    @Test
    public void givenAlreadyExistingUser_whenSignup_thenExpectUserAlreadyExistsException() throws Exception {
        String email = "valid.user@SimpleDrive.com".toLowerCase().trim();

        // Given
        User user = User.builder()
                .firstName("Valid")
                .lastName("User")
                .email(email)
                .password("123456789").build();

        // When
        mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("user", user))
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(UserAlreadyExistsException.class));

        // Check if there is just one instance of that user.
        assertThat(userRepository.findUserByEmail(email));
    }

    @Test
    public void givenUserWithNoEmail_whenSignup_thenExpectException() throws Exception {
        String email = "";

        // Given
        User user = User.builder()
                .firstName("Invalid")
                .lastName("User")
                .email(email)
                .password("123456789").build();

        // When
        signUp(user, false)
                .andExpect(result -> assertThat(result.getResolvedException()).isNotNull());
    }

    @Test
    public void givenUserWithNoFirstName_whenSignup_thenExpectException() throws Exception {
        String email = "invalid.user@simpledrive.com".toLowerCase().trim();

        // Given
        User user = User.builder()
                .firstName("")
                .lastName("User")
                .email(email)
                .password("123456789").build();

        // When
        signUp(user, false)
        // Then
                .andExpect(result -> assertThat(result.getResolvedException()).isInstanceOf(BindException.class));

        assertThat(userRepository.findUserByEmail(email)).isEqualTo(Optional.ofNullable(null));
    }

    public ResultActions signUp(User user, boolean deleteExistingFirst) throws Exception {

        if (deleteExistingFirst) {
            userRepository.deleteUserByEmail(user.getEmail());
            confirmationTokenRepository.deleteByUserEmail(user.getEmail());
        }

        return mockMvc.perform(post("/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .flashAttr("user", user));
    }

    public ResultActions confirmSignup(ConfirmationToken confirmationToken, User user) throws Exception {
        return mockMvc.perform(get("/signup/confirm-account")
                .param("token", confirmationToken.getConfirmationToken())
                .param("email", user.getEmail()));
    }

    public void createUser(User user) throws Exception {
        signUp(user, true);

        ConfirmationToken confirmationToken = confirmationTokenRepository.findByUserEmail(user.getEmail())
                .orElseThrow(() -> new AssertionError("Could not find a token with the email " + user.getEmail()));

        confirmSignup(confirmationToken, user);
    }

//    @Test
//    public void givenValidUser_whenLogin_thenLoginToHomepage() throws Exception {
//        // When
//        mockMvc.perform(post("/login").with(SecurityMockMvcRequestPostProcessors.csrf())
//                .contentType(MediaType.APPLICATION_JSON)
//                .flashAttr("email", VALID_EMAIL).flashAttr("password", MATCHING_PASSWORD))
//        // Then
////                .andExpect(view().name("homepage"));
//        .andExpect(status().is3xxRedirection());
//    }
//
//    @Test
//    public void givenIncorrectPassword_whenLogin_thenStayAtLoginAndAddMessageToModel() throws Exception {
//        // When
//        mockMvc.perform(post("/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .flashAttr("email", VALID_EMAIL).flashAttr("password", "Incorrect Password"))
//        // Then
//                .andExpect(view().name("login"))
//                .andExpect(result -> assertThat(result.getResponse().getForwardedUrl()).contains("?error="))
//                .andExpect(model().attributeExists("error")).andReturn().getModelAndView();
//    }
//
//    @Test
//    public void givenEmptyEmail_whenLogin_thenStayAtLoginAndAddMessageToModel() throws Exception {
//        // When
//        mockMvc.perform(post("/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .flashAttr("email", "").flashAttr("password", "Any Password"))
//        // Then
////                .andExpect(model().attributeExists("error"))
//                .andExpect(view().name("login"));
////                .andExpect(result -> {
//////                    assertThat(result.getResponse().getForwardedUrl()).contains("?error=");
////                    assertThat(result.getModelAndView().getViewName().equals("login"));
////                });
//    }
}
