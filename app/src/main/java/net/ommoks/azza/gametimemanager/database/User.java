package net.ommoks.azza.gametimemanager.database;

import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class User {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @ColumnInfo(name = "user")
    public String name;

    public User(String name) {
        this.name = name;
    }

    @Ignore
    @Override
    public boolean equals(@Nullable Object other) {
        return other != null &&
                other instanceof User &&
                this.id == ((User)other).id
                && this.name.equals(((User)other).name);
    }

    @Override
    public int hashCode() {
        return (int) id;
    }
}
