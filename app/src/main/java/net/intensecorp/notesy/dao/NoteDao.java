package net.intensecorp.notesy.dao;

import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import net.intensecorp.notesy.entities.Note;

import java.util.List;

public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY id DESC")
    List<Note> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Void insertNote(Note note);

    @Delete
    Void deleteNote(Note note);
}
