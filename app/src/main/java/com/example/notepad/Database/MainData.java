package com.example.notepad.Database;

import static androidx.room.OnConflictStrategy.REPLACE;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;
import com.example.notepad.Models.Notes;

@Dao
public interface MainData {
    @Insert(onConflict = REPLACE)
    void insert(Notes notes);

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Notes> getAllNotes();

//    @Query("UPDATE notes SET title=:title, write=:write WHERE id=:id")
    @Update
    void update(Notes notes);
    @Delete
    void delete(Notes notes);

    @Query("SELECT * FROM notes WHERE id = :id")
    Notes getNoteById(int id);

}

