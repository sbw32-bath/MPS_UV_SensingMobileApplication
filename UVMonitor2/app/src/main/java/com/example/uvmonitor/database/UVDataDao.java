package com.example.uvmonitor.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UVDataDao {

    @Insert
    void insert(UVDataEntity uvData);

    @Query("SELECT date, MAX(uvValue) AS maxUv, MIN(uvValue) AS minUv FROM uv_data GROUP BY date ORDER BY date DESC LIMIT 7")
    LiveData<List<UVSummary>> getSummarizedLast7Days();

    @Query("SELECT * FROM uv_data WHERE date = :date")
    LiveData<List<UVDataEntity>> getUVDataByDate(String date);
}
