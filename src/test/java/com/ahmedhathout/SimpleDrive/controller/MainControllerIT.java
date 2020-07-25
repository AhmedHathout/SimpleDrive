package com.ahmedhathout.SimpleDrive.controller;

import com.ahmedhathout.SimpleDrive.configurations.SecurityConfiguration;
import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.entities.UserFile;
import com.ahmedhathout.SimpleDrive.repositories.UserFileRepository;
import com.ahmedhathout.SimpleDrive.repositories.UserRepository;
import com.ahmedhathout.SimpleDrive.services.UserFileService;
import com.ahmedhathout.SimpleDrive.services.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * These tests are far from extensive. They are just a proof of work
 */

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@WithUserDetails(value = "valid.user@simpledrive.com", userDetailsServiceBeanName = "userRepository")
class MainControllerIT {

    @Value("${spring.mail.password}")
    private String password;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void givenValidUserAndEmptyFileNameAndTags_whenListAllFilesOwnedByUser_thenReturnHomepageAndModelWithFiles() throws Exception {

        mockMvc.perform(get("/"))
            .andExpect(result -> assertThat(result.getResolvedException()).isNull())
            .andExpect(view().name("homepage"))
            .andExpect(model().attributeExists("ownedFiles", "sharedFiles"));
    }

    @Test
    public void givenUserLoggedIn_whenCreateUserFile_thenAddFileToOwnedFiles() throws Exception {

        // Pre-condition
        String fileName = "Text 1.txt";
        userFileRepository.deleteByFileName(fileName);

        // When
        mockMvc.perform(multipart("/")
                .file(new MockMultipartFile("file",
                    fileName,
                        "text/plain",
                    new FileInputStream(new File("D:\\Git\\SimpleDrive\\Files to Upload\\" + fileName))))
        )
        // Then
                .andExpect(result -> assertThat(result.getResolvedException()).isNull())
                .andExpect(redirectedUrl("/"))
                .andReturn().getModelAndView().getModel().keySet().containsAll(List.of("ownedFiles", "sharedFiles"));

        assertThat(userFileRepository.findUserFileByFileNameContains(fileName)).isNotEmpty();
        assertThat(userFileRepository.findUserFileByFileNameContains(fileName).get(0).getOwner().getUsername()).contains("valid.user");
    }

    @Test
    public void givenUserOwnsAFile_whenToggleSharingThatFile_thenToggleSharing() throws Exception {

        // Given
        UserFile userFile = userFileRepository.findUserFileByFileNameContains("Text 1.txt").get(0);
        String shareableLink = userFile.getShareableLink();
        boolean isShareableWithLink = userFile.isShareableWithLink();

        // When
        mockMvc.perform(post("/files/toggle-sharing").param("shareableLink", shareableLink))
        // Then
                .andExpect(redirectedUrl("/"))
                .andExpect(result -> assertThat(result.getResolvedException()).isNull());

        assertThat(userFileRepository.findUserFileByFileNameContains("Text 1.txt").get(0).isShareableWithLink()).isNotEqualTo(isShareableWithLink);
    }

    @Test
    @WithUserDetails(value = "valid.user2@simpledrive.com", userDetailsServiceBeanName = "userRepository")
    public void givenUserDoesnotOwnTheFile_whenToggleSharing_thenDoNothing() throws Exception {

        // Given
        UserFile userFile = userFileRepository.findUserFileByFileNameContains("Text 1.txt").get(0);
        String shareableLink = userFile.getShareableLink();
        boolean isShareableWithLink = userFile.isShareableWithLink();

        // When
        mockMvc.perform(post("/files/toggle-sharing").param("shareableLink", shareableLink))
                // Then
                .andExpect(redirectedUrl("/"))
                .andExpect(result -> assertThat(result.getResolvedException()).isNull());

        assertThat(userFileRepository.findUserFileByFileNameContains("Text 1.txt").get(0).isShareableWithLink()).isEqualTo(isShareableWithLink);
    }
}