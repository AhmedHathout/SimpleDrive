package com.ahmedhathout.SimpleDrive.exceptions;

import lombok.Getter;
import lombok.NonNull;

public class UserAlreadyExistsException extends RuntimeException {

    @Getter
    private String message;

    public UserAlreadyExistsException(@NonNull String message) {
        super(message);
    }
}
