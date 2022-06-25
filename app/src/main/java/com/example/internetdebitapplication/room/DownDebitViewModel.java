package com.example.internetdebitapplication.room;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.internetdebitapplication.model.DownDebit;

import java.util.List;

public class DownDebitViewModel extends AndroidViewModel {
    private final DownDebitRepository downDebitDataSource;

    public DownDebitViewModel(Application application) {
        super(application);
        downDebitDataSource = new DownDebitRepository(application);
    }

    public LiveData<List<DownDebit>> getAll() {
        return downDebitDataSource.getAll();
    }

    public void insert(DownDebit downDebit) {
        downDebitDataSource.insert(downDebit);
    }
}
