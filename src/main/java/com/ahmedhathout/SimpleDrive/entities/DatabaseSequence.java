package com.ahmedhathout.SimpleDrive.entities;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collation = "database-sequences")
@Data
public class DatabaseSequence {

    @Id
    private String Id;
    private long sequence;
}
