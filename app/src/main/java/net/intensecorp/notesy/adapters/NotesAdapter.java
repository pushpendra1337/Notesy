package net.intensecorp.notesy.adapters;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
