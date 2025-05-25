package com.example.notepad;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import android.view.LayoutInflater;
import androidx.core.view.WindowInsetsCompat;

import com.example.notepad.Database.DB;
import com.example.notepad.Database.MainData;
import com.example.notepad.Models.Notes;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

public class NoteTakerActivity extends AppCompatActivity {

    EditText EditText_Title, EditText_Notes;
    ImageView ImageView_save, ImageView_back, ImageView_delete;
    Notes notes;
    Toolbar toolbar_notes;
    DB db;
    MainData dao;
    Notes old_notes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_note_taker);

        db = DB.getInstance(this);
        dao = db.mainData();

        toolbar_notes = findViewById(R.id.toolbar_notes);
        ViewCompat.setOnApplyWindowInsetsListener(toolbar_notes, (v, insets) -> {
            int topInset = insets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
            v.setPadding(0, topInset, 0, 0);
            return WindowInsetsCompat.CONSUMED;
        });

        ImageView_save = findViewById(R.id.ImageView_save);
        ImageView_back = findViewById(R.id.ImageView_back);
        ImageView_delete = findViewById(R.id.ImageView_delete);
        EditText_Title = findViewById(R.id.EditText_Title);
        EditText_Notes = findViewById(R.id.EditText_Notes);

        old_notes = (Notes) getIntent().getSerializableExtra("old_note");

        if (old_notes != null){
            // Get the note from the database
            new getNoteAsyncTask(dao).execute(old_notes.getId());
            ImageView_delete.setVisibility(View.VISIBLE);
        } else {
            ImageView_delete.setVisibility(View.GONE);
        }


        ImageView_save.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                v.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(80)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .setInterpolator(new OvershootInterpolator())
                                    .withEndAction(() -> {

                                        String title = EditText_Title.getText().toString();
                                        String description = EditText_Notes.getText().toString();

                                        if (description.isEmpty()) {
                                            EditText_Notes.setError("Please enter a note");
                                            return;
                                        }

                                        if (old_notes == null) {
                                            notes = new Notes();
                                            notes.setTitle(title);
                                            notes.setWrite(description);
                                            Intent intent = new Intent();
                                            intent.putExtra("notes", notes);
                                            setResult(RESULT_OK, intent);

                                            showSavedToast();

                                            finish();

                                        } else {
                                            old_notes.setTitle(title);
                                            old_notes.setWrite(description);
                                            new UpdateAsyncTask(dao).execute(old_notes);

                                            Intent intent = new Intent();
                                            intent.putExtra("notes", old_notes);
                                            setResult(RESULT_OK, intent);

                                            showSavedToast();

                                            finish();
                                        }

                                    })
                                    .start();
                        })
                        .start();
            }
        });

        ImageView_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.animate()
                        .scaleX(0.8f)
                        .scaleY(0.8f)
                        .setDuration(80)
                        .withEndAction(() -> {
                            v.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(80)
                                    .setInterpolator(new OvershootInterpolator())
                                    .withEndAction(() -> {
                                        finish();
                                    })
                                    .start();
                        })
                        .start();
            }
        });

        ImageView_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (old_notes != null) {
                    View dialogView = getLayoutInflater().inflate(R.layout.delete_dialog_layout, null);

                    android.widget.Button confirmButton = dialogView.findViewById(R.id.confirm_button);
                    android.widget.Button cancelButton = dialogView.findViewById(R.id.cancel_button);

                    AlertDialog dialog = new AlertDialog.Builder(NoteTakerActivity.this)
                            .setView(dialogView)
                            .create();


                    confirmButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            new DeleteAsyncTask(dao).execute(old_notes);
                            Intent intent = new Intent();
                            setResult(RESULT_OK, intent);
                            dialog.dismiss();

                            showDeletedToast();

                            finish();
                        }
                    });

                    cancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });
                    dialog.show();

                } else {
                    Toast.makeText(NoteTakerActivity.this, "Cannot delete a new note", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fillNote(Notes note) {
        EditText_Title.setText(note.getTitle());
        EditText_Notes.setText(note.getWrite());
    }

    private class getNoteAsyncTask extends AsyncTask<Integer, Void, Notes> {
        private MainData dao;

        public getNoteAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected Notes doInBackground(Integer... integers) {
            return dao.getNoteById(integers[0]);
        }

        @Override
        protected void onPostExecute(Notes notes) {
            // Fill the note
            fillNote(notes);
        }
    }

    //Update the database
    private static class UpdateAsyncTask extends AsyncTask<Notes, Void, Void> {
        private MainData dao;

        UpdateAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Notes... notes) {
            dao.update(notes[0]);
            return null;
        }
    }

    // Delete task for NoteTakerActivity
    private static class DeleteAsyncTask extends AsyncTask<Notes, Void, Void> {
        private MainData dao;

        DeleteAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Notes... notes) {
            dao.delete(notes[0]);
            return null;
        }
    }


    private void showSavedToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.saved, findViewById(android.R.id.content), false);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        Animation fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_out);
        layout.startAnimation(fade);
    }

    private void showDeletedToast() {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.deleted, findViewById(android.R.id.content), false);

        Toast toast = new Toast(getApplicationContext());
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();

        Animation fade = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in_out);
        layout.startAnimation(fade);
    }

}