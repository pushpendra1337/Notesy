package net.intensecorp.notesy.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;

import net.intensecorp.notesy.R;
import net.intensecorp.notesy.database.NotesDatabase;
import net.intensecorp.notesy.entities.Note;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CreateNoteActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_STORAGE_PERMISSION = 1;
    private static final int REQUEST_CODE_SELECT_IMAGE = 2;

    private TextInputEditText mNoteTitleInputField, mNoteSubtitleInputField, mNoteContentInputField;
    private TextView mTimestampField;
    private String mSelectedNoteColor;
    private String mSelectedImagePath;
    private View mSubtitleHighlighter;
    private ImageView mNoteImage;
    private TextView mNoteWebUrl;
    private LinearLayout mNoteWebUrlLayout;
    private AlertDialog mAddUrlDialog;
    private AlertDialog mDeleteNoteDialog;
    private Note mAlreadyAvailableNote;

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
        mSubtitleHighlighter = findViewById(R.id.view_subtitle_highlighter);
        mNoteImage = findViewById(R.id.imageView_note_image);
        mNoteWebUrl = findViewById(R.id.textView_note_web_url);
        mNoteWebUrlLayout = findViewById(R.id.linearLayout_note_web_url);

        mTimestampField.setText(new SimpleDateFormat("EEEE, dd MMMM yy yy hh:mm a", Locale.getDefault()).format(new Date()));

        mSelectedNoteColor = "#333333";
        mSelectedImagePath = "";

        if (getIntent().getBooleanExtra("isViewOrUpdate", false)) {
            mAlreadyAvailableNote = (Note) getIntent().getSerializableExtra("note");
            setViewOrUpdateNote();
        }

        findViewById(R.id.imageView_button_remove_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteImage.setImageBitmap(null);
                mNoteImage.setVisibility(View.GONE);
                findViewById(R.id.imageView_button_remove_image).setVisibility(View.GONE);
                mSelectedImagePath = "";
            }
        });

        findViewById(R.id.imageView_button_remove_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mNoteWebUrl.setText(null);
                mNoteWebUrlLayout.setVisibility(View.GONE);
            }
        });

        initMiscellaneous();
        setSubtitleHighlighterColor();

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
    }

    private void setViewOrUpdateNote() {
        mNoteTitleInputField.setText(mAlreadyAvailableNote.getTitle());
        mNoteSubtitleInputField.setText(mAlreadyAvailableNote.getSubtitle());
        mNoteContentInputField.setText(mAlreadyAvailableNote.getNoteContent());
        mTimestampField.setText(mAlreadyAvailableNote.getTimestamp());

        if (mAlreadyAvailableNote.getImagePath() != null && !mAlreadyAvailableNote.getImagePath().trim().isEmpty()) {
            mNoteImage.setImageBitmap(BitmapFactory.decodeFile(mAlreadyAvailableNote.getImagePath()));
            mNoteImage.setVisibility(View.VISIBLE);
            findViewById(R.id.imageView_button_remove_image).setVisibility(View.VISIBLE);
            mSelectedImagePath = mAlreadyAvailableNote.getImagePath();
        }

        if (mAlreadyAvailableNote.getWebLink() != null && !mAlreadyAvailableNote.getWebLink().trim().isEmpty()) {
            mNoteWebUrl.setText(mAlreadyAvailableNote.getWebLink());
            mNoteWebUrlLayout.setVisibility(View.VISIBLE);
        }
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
        note.setNoteColor(mSelectedNoteColor);
        note.setImagePath(mSelectedImagePath);

        if (mNoteWebUrlLayout.getVisibility() == View.VISIBLE) {
            note.setWebLink(mNoteWebUrl.getText().toString());
        }

        if (mAlreadyAvailableNote != null) {
            note.setId(mAlreadyAvailableNote.getId());
        }

        @SuppressLint("StaticFieldLeak")
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

    public void initMiscellaneous() {
        final LinearLayout layoutMiscellaneous = findViewById(R.id.linearLayout_miscellaneous);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(layoutMiscellaneous);
        layoutMiscellaneous.findViewById(R.id.textView_miscellaneous).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                } else {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }
        });

        final ImageView imageColor1 = layoutMiscellaneous.findViewById(R.id.imageView_color_1);
        final ImageView imageColor2 = layoutMiscellaneous.findViewById(R.id.imageView_color_2);
        final ImageView imageColor3 = layoutMiscellaneous.findViewById(R.id.imageView_color_3);
        final ImageView imageColor4 = layoutMiscellaneous.findViewById(R.id.imageView_color_4);
        final ImageView imageColor5 = layoutMiscellaneous.findViewById(R.id.imageView_color_5);

        layoutMiscellaneous.findViewById(R.id.view_color_1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedNoteColor = "#333333";
                imageColor1.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleHighlighterColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedNoteColor = "#FEBD3D";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleHighlighterColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedNoteColor = "#FF4842";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(0);
                setSubtitleHighlighterColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedNoteColor = "#3A52FC";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(R.drawable.ic_baseline_done_24);
                imageColor5.setImageResource(0);
                setSubtitleHighlighterColor();
            }
        });

        layoutMiscellaneous.findViewById(R.id.view_color_5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedNoteColor = "#000000";
                imageColor1.setImageResource(0);
                imageColor2.setImageResource(0);
                imageColor3.setImageResource(0);
                imageColor4.setImageResource(0);
                imageColor5.setImageResource(R.drawable.ic_baseline_done_24);
                setSubtitleHighlighterColor();
            }
        });

        if (mAlreadyAvailableNote != null && mAlreadyAvailableNote.getNoteColor() != null && !mAlreadyAvailableNote.getNoteColor().trim().isEmpty()) {
            switch (mAlreadyAvailableNote.getNoteColor()) {
                case "#FEBD3D":
                    layoutMiscellaneous.findViewById(R.id.view_color_2).performClick();
                    break;
                case "#FF4842":
                    layoutMiscellaneous.findViewById(R.id.view_color_3).performClick();
                    break;
                case "#3A52FC":
                    layoutMiscellaneous.findViewById(R.id.view_color_4).performClick();
                    break;
                case "#000000":
                    layoutMiscellaneous.findViewById(R.id.view_color_5).performClick();
                    break;
            }
        }

        layoutMiscellaneous.findViewById(R.id.linearLayout_add_image).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(CreateNoteActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_STORAGE_PERMISSION);
                } else {
                    selectImage();
                }
            }
        });

        layoutMiscellaneous.findViewById(R.id.linearLayout_add_url).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                showAddUrlDialog();
            }
        });

        if (mAlreadyAvailableNote != null) {
            layoutMiscellaneous.findViewById(R.id.linearLayout_delete_note).setVisibility(View.VISIBLE);
            layoutMiscellaneous.findViewById(R.id.linearLayout_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                    showDeleteNoteDialog();
                }
            });
        }
    }

    private void setSubtitleHighlighterColor() {
        GradientDrawable gradientDrawable = (GradientDrawable) mSubtitleHighlighter.getBackground();
        gradientDrawable.setColor(Color.parseColor(mSelectedNoteColor));
    }

    @SuppressLint("QueryPermissionsNeeded")
    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_STORAGE_PERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                selectImage();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_IMAGE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri selectedImageUri = data.getData();
                if (selectedImageUri != null) {
                    try {
                        InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        mNoteImage.setImageBitmap(bitmap);
                        mNoteImage.setVisibility(View.VISIBLE);
                        findViewById(R.id.imageView_button_remove_image).setVisibility(View.VISIBLE);
                        mSelectedImagePath = getPathFromUri(selectedImageUri);
                    } catch (Exception exception) {
                        Toast.makeText(this, exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void showDeleteNoteDialog() {
        if (mDeleteNoteDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_dialog_delete_note, (ViewGroup) findViewById(R.id.constraintLayout_delete_note_dialog_container));
            builder.setView(view);
            mDeleteNoteDialog = builder.create();

            if (mDeleteNoteDialog.getWindow() != null) {
                mDeleteNoteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            view.findViewById(R.id.textView_button_delete_note).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    class DeleteNoteTask extends AsyncTask<Void, Void, Void> {
                        @Override
                        protected Void doInBackground(Void... voids) {
                            NotesDatabase.getDatabase(getApplicationContext()).noteDao().deleteNote(mAlreadyAvailableNote);
                            return null;
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            Intent intent = new Intent();
                            intent.putExtra("isNoteDeleted", true);
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                    new DeleteNoteTask().execute();
                }
            });

            view.findViewById(R.id.textView_button_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeleteNoteDialog.dismiss();
                }
            });
        }
        mDeleteNoteDialog.show();
    }

    private String getPathFromUri(Uri contentUri) {
        String filePath;
        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null);
        if (cursor == null) {
            filePath = contentUri.getPath();
        } else {
            cursor.moveToFirst();
            int index = cursor.getColumnIndex("_data");
            filePath = cursor.getString(index);
            cursor.close();
        }
        return filePath;
    }

    private void showAddUrlDialog() {
        if (mAddUrlDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(CreateNoteActivity.this);
            View view = LayoutInflater.from(this).inflate(R.layout.layout_add_url_dialog, (ViewGroup) findViewById(R.id.constraintLayout_add_url_dialog_container));
            builder.setView(view);
            mAddUrlDialog = builder.create();

            if (mAddUrlDialog.getWindow() != null) {
                mAddUrlDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
            }

            final EditText urlField = view.findViewById(R.id.editText_url_field);
            urlField.requestFocus();

            view.findViewById(R.id.textView_button_add).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (urlField.getText().toString().trim().isEmpty()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter URL", Toast.LENGTH_SHORT).show();
                    } else if (!Patterns.WEB_URL.matcher(urlField.getText().toString()).matches()) {
                        Toast.makeText(CreateNoteActivity.this, "Enter valid URL", Toast.LENGTH_SHORT).show();
                    } else {
                        mNoteWebUrl.setText(urlField.getText().toString());
                        mNoteWebUrlLayout.setVisibility(View.VISIBLE);
                        mAddUrlDialog.dismiss();
                    }
                }
            });

            view.findViewById(R.id.textView_button_cancel).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAddUrlDialog.dismiss();
                }
            });
        }
        mAddUrlDialog.show();
    }
}
