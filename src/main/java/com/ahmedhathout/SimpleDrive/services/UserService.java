package com.ahmedhathout.SimpleDrive.services;
import com.ahmedhathout.SimpleDrive.configurations.SecurityConfiguration;
import com.ahmedhathout.SimpleDrive.entities.User;
import com.ahmedhathout.SimpleDrive.exceptions.UserAlreadyExistsException;
import com.ahmedhathout.SimpleDrive.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

//
//import com.ahmedhathout.SimpleDrive.entities.User;
//import com.ahmedhathout.SimpleDrive.entities.files.DirectoryFile;
//import com.ahmedhathout.SimpleDrive.entities.UserFile;
//import com.ahmedhathout.SimpleDrive.repositories.DirectoryFileRepository;
//import com.ahmedhathout.SimpleDrive.repositories.UserRepository;
//import lombok.AllArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import reactor.core.publisher.Mono;
//
//@Service
//@AllArgsConstructor
//public class UserService {
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Autowired
//    private DirectoryFileRepository directoryFileRepository;
//
//    public DirectoryFile getUserHomeDirectory(User user) {
//        return directoryFileRepository
//                .findById(user.getHomeDirectoryId())
//                .switchIfEmpty(Mono.just(DirectoryFile.builder()
//                        .id(UserFile.Key.builder()
//                                .fileName(user.getFirstName() + "'s home directory")
//                                .parentDirectory(null)
//                                .build())
//                        .owner(user)
//                        .build()))
//                .block();
//    }
//}

@AllArgsConstructor
@Validated
@Service
public class UserService {

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    public SecurityConfiguration securityConfiguration;

    public User createNewUser(@NonNull @Valid User user) {
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setPassword(securityConfiguration.passwordEncoder().encode(user.getPassword()));
        user.setFirstName(user.getFirstName().trim());
        user.setLastName(user.getLastName().trim());

        if (userRepository.findById(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("There is an existing user with the same email address" +  user.getEmail());
        }

        return userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findById(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUser(User user) {
        userRepository.delete(user);
    }
}
