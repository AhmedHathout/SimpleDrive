package com.ahmedhathout.SimpleDrive.listeners;

import com.ahmedhathout.SimpleDrive.entities.UserFile;

public interface UserFileListener {
    void onUserFileCreated(UserFile userFile);
    void onUserFileDeleted(UserFile userFile);
}
