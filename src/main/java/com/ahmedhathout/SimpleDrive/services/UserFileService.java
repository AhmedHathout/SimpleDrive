package com.ahmedhathout.SimpleDrive.services;

import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.entities.UserFile;
import com.ahmedhathout.SimpleDrive.exceptions.GridFsFileNotFoundException;
import com.ahmedhathout.SimpleDrive.exceptions.NoSuchFileException;
import com.ahmedhathout.SimpleDrive.exceptions.NotShareableException;
import com.ahmedhathout.SimpleDrive.repositories.UserFileRepository;
import com.ahmedhathout.SimpleDrive.repositories.UserRepository;
import com.mongodb.BasicDBObject;
import com.mongodb.client.gridfs.model.GridFSFile;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.io.IOException;
import java.util.*;

@Log4j2
@Service
@AllArgsConstructor
@Validated
public class UserFileService {

    public static final int MAX_FILE_SIZE = 4 * 1024 * 1024; // 4 MB

    @Autowired
    private UserFileRepository userFileRepository;

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    private UserRepository userRepository;

    public UserFile createUserFile(@NonNull MultipartFile file,
                                   @NonNull User user) throws IOException, FileSizeLimitExceededException {

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeLimitExceededException(
                    "Sorry. Maximum file size is " + (MAX_FILE_SIZE / 1024 / 1024)
                            + " MB but you tried to upload "
                            + (file.getSize() / 1024.0 / 1024)
                            + " MB", file.getSize(), MAX_FILE_SIZE);
        }
        BasicDBObject metaData = new BasicDBObject("owner_username", user.getUsername());

        ObjectId gridFsFileId = gridFsTemplate.store(file.getInputStream(),
                StringUtils.cleanPath(file.getOriginalFilename()),
                metaData);

        UserFile userFile = UserFile.builder()
                .fileName(StringUtils.cleanPath(file.getOriginalFilename()))
                .owner(user)
                .gridFsFileId(gridFsFileId)
                .createdBy(user)
                .size(file.getSize())
                .build();

        user.getOwnedFiles().add(userFile);
        userRepository.save(user);
        return userFileRepository.save(userFile);
    }

    public UserFile saveUserFile(UserFile userFile) {
        return userFileRepository.save(userFile);
    }

    public Optional<GridFsResource> getGridFsResource(@Nullable User user,
                                                      @NotEmpty String shareableLinkWithPrefix,
                                                      boolean mustBeOwner) throws NoSuchFileException, GridFsFileNotFoundException, NotShareableException {

        Optional<UserFile> optionalUserFile = getUserFileByShareableLink(user, shareableLinkWithPrefix,mustBeOwner);

        return getGridFsResource(optionalUserFile);
    }

    public Optional<GridFsResource> getGridFsResource(@NonNull @Valid Optional<UserFile> optionalUserFile) throws NoSuchFileException, GridFsFileNotFoundException {

        if (optionalUserFile.isEmpty()) {
            return Optional.empty();
        }

        UserFile userFile = optionalUserFile.get();
        GridFSFile gridFSFile = gridFsTemplate.findOne(new Query(Criteria
                .where("_id").is(userFile.getGridFsFileId())));

        if (gridFSFile == null) {
            throw new GridFsFileNotFoundException(userFile.getGridFsFileId(), userFile.getFileName());
        }

        return Optional.of(gridFsTemplate.getResource(gridFSFile));
    }

    public void toggleShareableWithLink(@Valid @NonNull User user,
                                        @NotEmpty String shareableLink) throws NoSuchFileException, NotShareableException {

        Optional<UserFile> optionalUserFile = getUserFileByShareableLink(user, shareableLink, true);

        System.out.println(optionalUserFile.get());
        optionalUserFile.ifPresent(userFile -> {
            if (userFile.getOwner().equals(user)) {
                userFile.setShareableWithLink(!userFile.isShareableWithLink());
                userFileRepository.save(userFile);
            }
        });
    }

    public Optional<UserFile> getUserFileByShareableLink(@Valid @Nullable User user,
                                                         @NotEmpty String shareableLink,
                                                         boolean mustBeOwner) throws NoSuchFileException, NotShareableException {

        // TODO Use optional instead of sending a null user.
        // TODO This method should throw declarative errors. An error for each case.
//        String[] shareableLinkSplit = shareableLinkWithPrefix.split("/");
//        String shareableLink = shareableLinkSplit[shareableLinkSplit.length - 1];

        UserFile userFile = userFileRepository
                .findById(shareableLink)
                .orElseThrow(() -> new NoSuchFileException(shareableLink));

        // Return the file only if it is shareable, the user is the owner of the file or the file was shared with that user.
        if (userFile.isShareableWithLink() || // The file is shareable with anyone with the link
                (user != null &&
                        (
                        user.equals(userFile.getOwner()) || // user is owner
                                (user.getFilesWithAccess().contains(userFile) && !mustBeOwner) // user has access
                        )
                )

        ) {
            return Optional.of(userFile);
        }


        throw new NotShareableException(shareableLink);
    }

    public void deleteUserFile(@Valid @NonNull User user,
                               @NotEmpty String shareableLink) throws NoSuchFileException, NotShareableException {

        Optional<UserFile> optionalUserFile = getUserFileByShareableLink(user, shareableLink, true);

        if (optionalUserFile.isEmpty()) {
            return;
        }

        UserFile userFile = optionalUserFile.get();

        // TODO May need to do the same for FilesWithAccess to everybody else.
        user.getOwnedFiles().remove(userFile);


        // Update owner
        userRepository.save(user);

        // Delete file
        gridFsTemplate.delete(new Query(Criteria.where("_id").is(userFile.getGridFsFileId())));
        userFileRepository.delete(userFile);
    }

    public Map<String, List<String>> shareWithUsers(@NonNull User user,
                                                        @NotEmpty String shareableLink,
                                                        String userEmailsToAdd,
                                                        String userEmailsToRemove) throws NoSuchFileException, NotShareableException {

        Optional<UserFile> optionalUserFile = getUserFileByShareableLink(user, shareableLink, true);

        if (optionalUserFile.isEmpty()) {
            return new HashMap<String, List<String>>();
        }

        UserFile userFile = optionalUserFile.get();
        Map<String, List<String>> usersAddedAndRemovedMap = new HashMap<>();
        final String USERS_ADDED = "Users Added";
        final String USERS_REMOVED = "Users Removed";

        List<String> userEmailsToAddSplit = Arrays.asList(userEmailsToAdd.split("\\s*,\\s*"));

        Iterable<User> usersToAdd = userRepository.findAllById(userEmailsToAddSplit);
        usersToAdd.forEach(aUserToAdd -> {
            if (user.equals(aUserToAdd)) {
                return;
            }
            userFile.getPeopleWithAccess().add(aUserToAdd);
            aUserToAdd.getFilesWithAccess().add(userFile);
            usersAddedAndRemovedMap.putIfAbsent(USERS_ADDED, new ArrayList<>());
            usersAddedAndRemovedMap.get(USERS_ADDED).add(aUserToAdd.getEmail());
            userRepository.save(aUserToAdd);
        });

        List<String> userEmailsToRemoveSplit = Arrays.asList(userEmailsToRemove.split("\\s*,\\s*"));

        Iterable<User> usersToRemove = userRepository.findAllById(userEmailsToRemoveSplit);
        usersToRemove.forEach(aUserToRemove -> {
            userFile.getPeopleWithAccess().remove(aUserToRemove);
            aUserToRemove.getFilesWithAccess().remove(userFile);
            usersAddedAndRemovedMap.putIfAbsent(USERS_REMOVED, new ArrayList<>());
            usersAddedAndRemovedMap.get(USERS_REMOVED).add(aUserToRemove.getEmail());
            userRepository.save(aUserToRemove);
        });

        userFileRepository.save(userFile);
        return usersAddedAndRemovedMap;
    }

    public Map<String, List<String>> changeTags(@NonNull User user,
                                                @NotEmpty String shareableLink,
                                                String tagsToAdd,
                                                String tagsToRemove) throws NoSuchFileException, NotShareableException {

        final String TAGS_ADDED = "Tags Added";
        final String TAGS_REMOVED = "Tags Removed";

        Optional<UserFile> optionalUserFile = getUserFileByShareableLink(user, shareableLink, true);

        if (optionalUserFile.isEmpty()) {
            return new HashMap<String, List<String>>();
        }

        UserFile userFile = optionalUserFile.get();
        List<String> addedTags = userFile.addOrRemoveTags(tagsToAdd, UserFile.AddORRemoveTag.ADD_TAG);
        List<String> removedTags = userFile.addOrRemoveTags(tagsToRemove, UserFile.AddORRemoveTag.REMOVE_TAG);

        Map<String, List<String>> tagsAddedAndRemoved = new HashMap<>();
        tagsAddedAndRemoved.put(TAGS_ADDED, addedTags);
        tagsAddedAndRemoved.put(TAGS_REMOVED, removedTags);

        userFileRepository.save(userFile);
        return tagsAddedAndRemoved;
    }
}
