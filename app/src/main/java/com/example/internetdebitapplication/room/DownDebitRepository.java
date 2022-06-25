package com.example.internetdebitapplication.room;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.internetdebitapplication.model.DownDebit;

import java.util.List;

public class DownDebitRepository {
    private final DownDebitDao downDebitDao;

    public DownDebitRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        downDebitDao = db.downDebitDao();
    }

    public LiveData<List<DownDebit>> getAll() {
        return downDebitDao.getAll();
    }

    public void insert(DownDebit downDebit) {
        AppDatabase.databaseWriteExecutor.execute(() -> {
            downDebitDao.insert(downDebit);
        });
    }
}
