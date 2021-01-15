package net.intensecorp.notesy.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import net.intensecorp.notesy.R;
import net.intensecorp.notesy.adapters.NotesAdapter;
import net.intensecorp.notesy.database.NotesDatabase;
import net.intensecorp.notesy.entities.Note;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private static final int REQUEST_CODE_ADD_NOTE = 1;
    private RecyclerView mNotesRecyclerView;
    private List<Note> mNoteList;
    private NotesAdapter mNotesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ImageView addNoteButton = findViewById(R.id.imageView_add_note);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(getApplicationContext(), CreateNoteActivity.class), REQUEST_CODE_ADD_NOTE);
            }
        });

        mNotesRecyclerView = findViewById(R.id.recyclerView_notes);
        mNotesRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));

        mNoteList = new ArrayList<>();
        mNotesAdapter = new NotesAdapter(mNoteList);
        mNotesRecyclerView.setAdapter(mNotesAdapter);

        getNotes();
    }

    private void getNotes() {
        @SuppressLint("StaticFieldLeak")
        class GetNotesTask extends AsyncTask<Void, Void, List<Note>> {
            @Override
            protected List<Note> doInBackground(Void... voids) {
                return NotesDatabase.getDatabase(getApplicationContext()).noteDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List<Note> notes) {
                super.onPostExecute(notes);
                if (mNoteList.size() == 0) {
                    mNoteList.addAll(notes);
                    mNotesAdapter.notifyDataSetChanged();
                } else {
                    mNoteList.add(0, notes.get(0));
                    mNotesAdapter.notifyItemInserted(0);
                }
                mNotesRecyclerView.smoothScrollToPosition(0);
            }
        }
        new GetNotesTask().execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ADD_NOTE && resultCode == RESULT_OK) {
            getNotes();
        }
    }
}