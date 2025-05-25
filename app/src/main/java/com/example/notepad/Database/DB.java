package com.example.notepad.Database;

import android.content.Context;

import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Entity;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.notepad.Models.Notes;

import java.util.ArrayList;
import java.util.List;

@Database(entities = Notes.class, version = 1, exportSchema = false)
public abstract class DB extends RoomDatabase {

    private static DB database;
    private static final String DB_NAME = "notepad.db";

    public abstract MainData mainData();

    public synchronized static DB getInstance(Context context) {
        if (database == null){
            database = Room.databaseBuilder(context.getApplicationContext(),DB.class, DB_NAME)
                    .fallbackToDestructiveMigration().build();
        }

        return database;
    }


}