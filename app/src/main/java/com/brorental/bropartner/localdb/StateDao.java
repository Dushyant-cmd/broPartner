package com.brorental.bropartner.localdb;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface StateDao {
    @Insert
    void insertState(StateEntity state);

    @Update
    void update(StateEntity state);

    @Query("SELECT * FROM states")
    List<StateEntity> getStates();

    @Delete
    void deleteState(StateEntity state);

    @Query("DELETE FROM states")
    void deleteStates();
}
