package net.ommoks.azza.gametimemanager.database;

import android.os.Build;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

@Entity(tableName = "records")
public class Record {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "week_index")
    public int weekIndex;   // Just to identify same week records. Not week number in a year.

    @ColumnInfo(name = "type")
    public int type;    // 0: normal log record, 1: use record

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "use_time")
    public long useTime;    // time in minutes

    @ColumnInfo(name = "user")
    public String user;

    @ColumnInfo(name = "comment")
    public String comment;

    @NonNull
    @Override
    @Ignore
    public String toString() {
        return "id = " + id
                + ", weekIndex = " + weekIndex
                + ", type = " + type
                + ", timestamp = " + getDateTime()
                + ", useTime = " + useTime
                + ", user = " + user
                + ", comment = " + comment;
    }

    @Ignore
    private String getDateTime() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-mm-dd HH:mm"));
        } else {
            return new Date(timestamp).toString();
        }
    }
}
