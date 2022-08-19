package net.ommoks.azza.gametimemanager.database;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {Record.class, User.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {

    public abstract RecordDao recordDao();

    public abstract UserDao userDao();
}
