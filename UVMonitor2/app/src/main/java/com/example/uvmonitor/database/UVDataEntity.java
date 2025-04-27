package com.example.uvmonitor.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "uv_data")
public class UVDataEntity {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String date;
    public int uvValue;

    public UVDataEntity(String date, int uvValue) {
        this.date = date;
        this.uvValue = uvValue;
    }
}
