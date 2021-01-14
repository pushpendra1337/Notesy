package net.intensecorp.notesy.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import net.intensecorp.notesy.R;
import net.intensecorp.notesy.database.NotesDatabase;
import net.intensecorp.notesy.entities.Note;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private TextInputEditText mNoteTitleInputField, mNoteSubtitleInputField, mNoteContentInputField;
    private TextView mTimestampField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);
        ImageView backButton = findViewById(R.id.imageView_back);
        ImageView saveNoteButton = findViewById(R.id.imageView_save_note);
        mNoteTitleInputField = findViewById(R.id.textInputEditText_note_title);
        mNoteSubtitleInputField = findViewById(R.id.textInputEditText_note_subtitle);
        mNoteContentInputField = findViewById(R.id.textInputEditText_note_content);
        mTimestampField = findViewById(R.id.textView_timestamp);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNote();
            }
        });

        mTimestampField.setText(new SimpleDateFormat("EEEE, dd MMMM yyyy hh:mm a", Locale.getDefault()).format(new Date()));
    }

    private void saveNote() {
        if (mNoteTitleInputField.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Title can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (mNoteTitleInputField.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note Subtitle can't be empty", Toast.LENGTH_SHORT).show();
            return;
        } else if (mNoteTitleInputField.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Note can't be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        final Note note = new Note();
        note.setTitle(mNoteTitleInputField.getText().toString());
        note.setSubtitle(mNoteSubtitleInputField.getText().toString());
        note.setNoteContent(mNoteContentInputField.getText().toString());
        note.setTimestamp(mTimestampField.getText().toString());

        class saveNoteTask extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... voids) {
                NotesDatabase.getDatabase(getApplicationContext()).noteDao().insertNote(note);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        }
        new saveNoteTask().execute();
    }
}