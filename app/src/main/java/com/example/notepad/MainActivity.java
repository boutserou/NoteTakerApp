package com.example.notepad;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.notepad.Database.DB;
import com.example.notepad.Database.MainData;
import com.example.notepad.Models.Notes;
import com.example.notepad.adapter.NotesListAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> noteList = new ArrayList<>();
    DB db;
    MainData dao;
    FloatingActionButton float_add;

    public static final int INSERT_NOTE_REQUEST = 101;
    public static final int UPDATE_NOTE_REQUEST = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.recyclerView);
        float_add = findViewById(R.id.float_add);

        db = DB.getInstance(this);
        dao = db.mainData();

        // Get the data on background
        new GetAllNotesAsyncTask(dao).execute();

        float_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteTakerActivity.class);
                startActivityForResult(intent, INSERT_NOTE_REQUEST);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == INSERT_NOTE_REQUEST && resultCode == RESULT_OK) {
            Notes notes = (Notes) Objects.requireNonNull(data).getSerializableExtra("notes");
            new InsertAsyncTask(dao).execute(notes);
        }

        if (requestCode == UPDATE_NOTE_REQUEST && resultCode == RESULT_OK) {
            Notes notes = (Notes) Objects.requireNonNull(data).getSerializableExtra("notes");
            new UpdateAsyncTask(dao).execute(notes);
        }
    }

    private void updateRecycler(List<Notes> noteList) {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, noteList, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    // Update the list
    private void updateNoteList(List<Notes> notes) {
        noteList.clear();
        noteList.addAll(notes);
        if (notesListAdapter == null)
            updateRecycler(noteList);
        else
            notesListAdapter.notifyDataSetChanged();
    }

    // Delete note from the database
    private void deleteNote(Notes notes) {
        new DeleteAsyncTask(dao).execute(notes);
    }

    // Async tasks
    @SuppressLint("StaticFieldLeak")
    private class InsertAsyncTask extends AsyncTask<Notes, Void, Void> {
        private MainData dao;

        InsertAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Notes... notes) {
            dao.insert(notes[0]);
            return null;
        }

        // Update the UI after inserting
        @Override
        protected void onPostExecute(Void unused) {
            new GetAllNotesAsyncTask(dao).execute();
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class GetAllNotesAsyncTask extends AsyncTask<Void, Void, List<Notes>> {
        private MainData dao;

        GetAllNotesAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected List<Notes> doInBackground(Void... voids) {
            return dao.getAllNotes();
        }

        // Update the UI after retrieving the list
        @Override
        protected void onPostExecute(List<Notes> notes) {
            updateNoteList(notes);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class DeleteAsyncTask extends AsyncTask<Notes, Void, Void> {
        private MainData dao;

        DeleteAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Notes... notes) {
            dao.delete(notes[0]);
            return null;
        }

        // Update the list after deleting
        @Override
        protected void onPostExecute(Void unused) {
            new GetAllNotesAsyncTask(dao).execute();
        }
    }

    //Update the database
    @SuppressLint("StaticFieldLeak")
    private class UpdateAsyncTask extends AsyncTask<Notes, Void, Void> {
        private MainData dao;

        UpdateAsyncTask(MainData dao) {
            this.dao = dao;
        }

        @Override
        protected Void doInBackground(Notes... notes) {
            dao.update(notes[0]);
            return null;
        }

        // Update the list after updating
        @Override
        protected void onPostExecute(Void unused) {
            new GetAllNotesAsyncTask(dao).execute();
        }
    }

    final ClickListener notesClickListener = new ClickListener() {
        @Override
        public void onClick(Notes notes) {
            Log.d("ClickListener", "Note Clicked: " + notes.getTitle());
            // Open NoteTakerActivity and pass the note data
            Intent intent = new Intent(MainActivity.this, NoteTakerActivity.class);
            intent.putExtra("old_note", notes);
            startActivityForResult(intent, UPDATE_NOTE_REQUEST);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView) {

            Log.d("ClickListener", "Note Long Clicked: " + notes.getTitle());

            View dialogView = getLayoutInflater().inflate(R.layout.delete_dialog_layout, null);

            Button confirmButton = dialogView.findViewById(R.id.confirm_button);
            Button cancelButton = dialogView.findViewById(R.id.cancel_button);


            AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                    .setView(dialogView)
                    .create();

            confirmButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    deleteNote(notes);
                    showDeletedToast();
                    dialog.dismiss();
                }
            });

            cancelButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }
    };

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