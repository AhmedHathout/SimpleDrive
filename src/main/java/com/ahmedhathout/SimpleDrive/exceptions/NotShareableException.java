package com.ahmedhathout.SimpleDrive.exceptions;

public class NotShareableException extends IllegalFileAccessException{

    public NotShareableException(String shareableLink) {
        super("This file is not accessible to everyone. Either you need to log in or the owner of the file needs" +
                " to share it with you or share it with everybody", shareableLink);
    }
}
