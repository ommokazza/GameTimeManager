package net.ommoks.azza.gametimemanager.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface RecordDao {
    @Query("SELECT * FROM records")
    List<Record> getAll();

    @Query("SELECT * FROM records WHERE week_index = :weekIndex")
    List<Record> getAllWithWeekIndex(int weekIndex);

    @Query("SELECT * FROM records WHERE id = (SELECT MAX(id) FROM records)")
    Record getLastOne();

    @Insert
    long insert(Record record);
}
