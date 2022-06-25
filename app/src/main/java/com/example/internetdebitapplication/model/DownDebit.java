package com.example.internetdebitapplication.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class DownDebit {
    /**
     * Value of the down debit
     */
    @PrimaryKey
    private double downValue;

    /**
     * String with the value of the down debit
     */
    @ColumnInfo(name = "down_string")
    private String downString;

    public DownDebit(double downValue, String downString) {
        this.downValue = downValue;
        this.downString = downString;
    }

    public double getDownValue() {
        return downValue;
    }

    public void setDownValue(double downValue) {
        this.downValue = downValue;
    }

    public String getDownString() {
        return downString;
    }

    public void setDownString(String downString) {
        this.downString = downString;
    }
}
