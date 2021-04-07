package net.intensecorp.notesy.listeners;

import net.intensecorp.notesy.entities.Note;

public interface NotesListener {
    void onNoteClicked(Note note, int position);
}
