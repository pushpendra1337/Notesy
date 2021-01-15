package net.intensecorp.notesy.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import net.intensecorp.notesy.R;
import net.intensecorp.notesy.entities.Note;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> mNoteList;

    public NotesAdapter(List<Note> mNoteList) {
        this.mNoteList = mNoteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        holder.setNote(mNoteList.get(position));
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView mNoteTitle, mNoteSubtitle, mTimestamp;

        public NoteViewHolder(@NonNull View view) {
            super(view);
            mNoteTitle = view.findViewById(R.id.textView_note_title);
            mNoteSubtitle = view.findViewById(R.id.textView_note_subtitle);
            mTimestamp = view.findViewById(R.id.textView_timestamp);
        }

        void setNote(Note note) {
            mNoteTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                mNoteSubtitle.setVisibility(View.GONE);
            } else {
                mNoteSubtitle.setText(note.getSubtitle());
            }
            mTimestamp.setText(note.getTimestamp());
        }
    }
}
