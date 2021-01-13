package net.intensecorp.notesy.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import net.intensecorp.notesy.dao.NoteDao;
import net.intensecorp.notesy.entities.Note;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NotesDatabase extends RoomDatabase {
    private static NotesDatabase sNotesDatabase;

    public static synchronized NotesDatabase getDatabase(Context context) {
        if (sNotesDatabase == null) {
            sNotesDatabase = Room.databaseBuilder(context, NotesDatabase.class, "notes_db").build();
        }
        return sNotesDatabase;
    }

    public abstract NoteDao noteDao();
}
