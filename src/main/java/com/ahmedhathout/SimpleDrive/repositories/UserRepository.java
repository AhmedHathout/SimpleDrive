package com.ahmedhathout.SimpleDrive.repositories;

import com.ahmedhathout.SimpleDrive.entities.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String>, UserDetailsService {

    Optional<User> findUserByEmail(String email);
    void deleteUserByEmail(String email);

    // By Convention, it should be a different class but I do not think that there is a need for that now at least.
    @Override
    default UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findById(username.toLowerCase()).orElseThrow(() -> new UsernameNotFoundException(username));
    }
}
