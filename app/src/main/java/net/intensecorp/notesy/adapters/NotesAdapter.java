package net.intensecorp.notesy.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.makeramen.roundedimageview.RoundedImageView;

import net.intensecorp.notesy.R;
import net.intensecorp.notesy.entities.Note;
import net.intensecorp.notesy.listeners.NotesListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {
    private List<Note> mNoteList;
    private NotesListener mNotesListener;
    private Timer mTimer;
    private List<Note> mNotesSource;

    public NotesAdapter(List<Note> noteList, NotesListener notesListener) {
        this.mNoteList = noteList;
        this.mNotesListener = notesListener;
        mNotesSource = noteList;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, final int position) {
        holder.setNote(mNoteList.get(position));
        holder.mNoteLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNotesListener.onNoteClicked(mNoteList.get(position), position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mNoteList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public void searchNotes(final String searchKeyword) {
        mTimer = new Timer();
        mTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyword.trim().isEmpty()) {
                    mNoteList = mNotesSource;
                } else {
                    ArrayList<Note> tempList = new ArrayList<>();
                    for (Note note : mNotesSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getSubtitle().toLowerCase().contains(searchKeyword.toLowerCase())
                                || note.getNoteContent().toLowerCase().contains(searchKeyword.toLowerCase())) {
                            tempList.add(note);
                        }
                    }
                    mNoteList = tempList;
                }

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                });
            }
        }, 500);
    }

    public void cancelTimer() {
        if (mTimer != null) {
            mTimer.cancel();
        }
    }

    static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView mNoteTitle, mNoteSubtitle, mTimestamp;
        LinearLayout mNoteLayout;
        RoundedImageView mNoteImage;

        public NoteViewHolder(@NonNull View view) {
            super(view);
            mNoteTitle = view.findViewById(R.id.textView_note_title);
            mNoteSubtitle = view.findViewById(R.id.textView_note_subtitle);
            mTimestamp = view.findViewById(R.id.textView_timestamp);
            mNoteLayout = view.findViewById(R.id.linearLayout_note);
            mNoteImage = view.findViewById(R.id.roundedImageView_note_image);
        }

        void setNote(Note note) {
            mNoteTitle.setText(note.getTitle());
            if (note.getSubtitle().trim().isEmpty()) {
                mNoteSubtitle.setVisibility(View.GONE);
            } else {
                mNoteSubtitle.setText(note.getSubtitle());
            }

            mTimestamp.setText(note.getTimestamp());

            GradientDrawable gradientDrawable = (GradientDrawable) mNoteLayout.getBackground();

            if (note.getNoteColor() != null) {
                gradientDrawable.setColor(Color.parseColor(note.getNoteColor()));
            } else {
                gradientDrawable.setColor(Color.parseColor("#333333"));
            }

            if (note.getImagePath() != null) {
                mNoteImage.setImageBitmap(BitmapFactory.decodeFile(note.getImagePath()));
                mNoteImage.setVisibility(View.VISIBLE);
            } else {
                mNoteImage.setVisibility(View.GONE);
            }
        }
    }
}
