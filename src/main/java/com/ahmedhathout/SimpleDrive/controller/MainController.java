package com.ahmedhathout.SimpleDrive.controller;

import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.entities.UserFile;
import com.ahmedhathout.SimpleDrive.exceptions.NoSuchFileException;
import com.ahmedhathout.SimpleDrive.exceptions.NotShareableException;
import com.ahmedhathout.SimpleDrive.repositories.UserRepository;
import com.ahmedhathout.SimpleDrive.services.UserFileService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.cassandra.CassandraReactiveRepositoriesAutoConfiguration;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * For displaying the user files.
 */

@Log4j2
@Data
@AllArgsConstructor
@Controller
@Validated
public class MainController {

    // TODO To be replaced with a user service
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserFileService userFileService;

    @GetMapping("/")
    public String listAllFilesOwnedByUser(Model model,
                                          String fileNameAndTags,
                                          @AuthenticationPrincipal @NonNull User user) {

        // Had to call the DB because the user's files might have changed.
        // I do not think that this is the most efficient way to get the files but I could not find anything else
        // Will look again for a better way later.
        user = userRepository.findById(user.getUsername()).get();

        if (fileNameAndTags == null || fileNameAndTags.isEmpty()) {
            model.addAttribute("ownedFiles", user.getOwnedFiles());
            model.addAttribute("sharedFiles", user.getFilesWithAccess());
        }

        else {
            List<String> keywords = Arrays.asList(fileNameAndTags.trim().replaceAll(" +", "").replaceAll("#+", "").split("\\s*,\\s*"));
            model.addAttribute("ownedFiles", user.getOwnedFiles()
                    .stream()
                    .filter(userFile -> userFile.containsKeywords(keywords))
                    .collect(Collectors.toList()));

            model.addAttribute("sharedFiles", user.getFilesWithAccess()
                    .stream()
                    .filter(userFile -> userFile.containsKeywords(keywords))
                    .collect(Collectors.toList()));

            model.addAttribute("searchString", fileNameAndTags);
        }

        return "homepage";
    }

    @PostMapping("/")
    public String createUserFile(@RequestParam("file") MultipartFile file,
                                 RedirectAttributes redirectAttributes,
                                 @AuthenticationPrincipal User user) throws IOException, FileSizeLimitExceededException {


        if (file == null || file.isEmpty()) {
            redirectAttributes.addFlashAttribute("failureMessage", "No file was provided!");
        }
        else {
            userFileService.createUserFile(file, user);
            redirectAttributes.addFlashAttribute("successMessage", file.getOriginalFilename() + " was saved!");
        }
        return "redirect:/";
    }

    @GetMapping("/files/get")
    @ResponseBody
    public ResponseEntity<Resource> downloadFile(@AuthenticationPrincipal @Nullable User user,
                                                 @NotEmpty String shareableLink) throws IOException {

        // TODO Send the optional user without getting it.
        if (user != null) {
            user = userRepository.findById(user.getUsername()).get();
        }

        Optional<UserFile> optionalUserFile = userFileService.getUserFileByShareableLink(user, shareableLink, false);

        if (optionalUserFile.isEmpty()) {
            if (user == null) {
                // TODO Should redirect to the login page instead. Throwing an error then catching it should probably solve the problem
                return null;
            }

            else {
                throw new NoSuchFileException(shareableLink);
            }
        }

        GridFsResource gridFsResource = userFileService.getGridFsResource(optionalUserFile).get();

        // Update lastDownloadDate
        UserFile userFile = optionalUserFile.get();
        userFile.setLastDownloadDate(Instant.now());
        userFileService.saveUserFile(userFile);

        return ResponseEntity.ok()
                .contentLength(gridFsResource.contentLength())
                .contentType(MediaType.valueOf("application/x-download"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=\"" + gridFsResource.getFilename() + "\"")
                .body(gridFsResource);
    }

    @PostMapping("/files/toggle-sharing")
    public String toggleSharing(@AuthenticationPrincipal @NonNull User user,
                                @RequestParam @NotEmpty String shareableLink) throws NoSuchFileException, NotShareableException {

        System.out.println(user);
        userFileService.toggleShareableWithLink(user, shareableLink);
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return "redirect:/";
    }

    // Had to use POST here since DELETE is not supported by HTML forms
    @RequestMapping(value = "/files/delete", method = {RequestMethod.DELETE, RequestMethod.POST})
    public String deleteFile(@AuthenticationPrincipal @NonNull User user,
                             @NotEmpty String shareableLink) throws NoSuchFileException, NotShareableException {

        userFileService.deleteUserFile(user, shareableLink);
        return "redirect:/";
    }

    @PostMapping("/files/share-with-users")
    public String shareWithUsers(@AuthenticationPrincipal @NonNull User user,
                                 @NotEmpty String shareableLink,
                                 String userEmailsToAdd,
                                 String userEmailsToRemove,
                                 RedirectAttributes redirectAttributes) throws NoSuchFileException, NotShareableException {

        Map<String, List<String>> usersAddedAndRemoved = userFileService.shareWithUsers(user, shareableLink, userEmailsToAdd, userEmailsToRemove);
        redirectAttributes.addAllAttributes(usersAddedAndRemoved);
        return "redirect:/";
    }

    @PostMapping("/files/tags")
    public String changeTags(@AuthenticationPrincipal @NonNull User user,
                             @NotEmpty String shareableLink,
                             String tagsToAdd,
                             String tagsToRemove,
                             RedirectAttributes redirectAttributes) throws NoSuchFileException, NotShareableException {

        Map<String, List<String>> tagsAddedAndRemoved = userFileService.changeTags(user, shareableLink, tagsToAdd, tagsToRemove);
        redirectAttributes.addAllAttributes(tagsAddedAndRemoved);
        return "redirect:/";
    }
}
