package com.brorental.bropartner.localdb;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "states")
public class StateEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;
    @ColumnInfo(name = "state")
    private String state;
    public StateEntity(String state) {
        this.state = state;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
