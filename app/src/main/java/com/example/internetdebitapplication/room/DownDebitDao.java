package com.example.internetdebitapplication.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.internetdebitapplication.model.DownDebit;

import java.util.List;

@Dao
public interface DownDebitDao {
    @Query("SELECT * FROM downdebit")
    LiveData<List<DownDebit>> getAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DownDebit downDebit);
}
