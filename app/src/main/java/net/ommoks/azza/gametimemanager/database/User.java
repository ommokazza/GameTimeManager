package net.ommoks.azza.gametimemanager.database;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "user")
    public String name;

    public User(String name) {
        this.name = name;
    }
}
