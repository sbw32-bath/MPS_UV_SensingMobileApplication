package com.example.uvmonitor.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {UVDataEntity.class}, version = 1, exportSchema = false)
public abstract class UVDatabase extends RoomDatabase {

    private static volatile UVDatabase INSTANCE;

    public abstract UVDataDao uvDataDao();

    public static UVDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (UVDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    UVDatabase.class, "uv_monitor_db")
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
