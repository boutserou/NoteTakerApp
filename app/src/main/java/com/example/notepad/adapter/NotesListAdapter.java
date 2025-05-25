package com.example.notepad.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.notepad.ClickListener;
import com.example.notepad.Models.Notes;
import com.example.notepad.R;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesListAdapter extends RecyclerView.Adapter<NoteViewHolder>{

    Context context;
    List<Notes> noteList;
    ClickListener listener;

    public NotesListAdapter(Context context, List<Notes> noteList, ClickListener listener) {
        this.context = context;
        this.noteList = noteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(context).inflate(R.layout.notelist, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {

        holder.View_title.setText(noteList.get(position).getTitle());
        holder.View_title.setSelected(true);

        holder.View_notes.setText(noteList.get(position).getWrite());


        holder.note_container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onClick(noteList.get(holder.getAdapterPosition()));
            }
        });

        holder.note_container.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                listener.onLongClick(noteList.get(holder.getAdapterPosition()), holder.note_container);
                return true;
            }
        });

    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

}

class NoteViewHolder extends RecyclerView.ViewHolder {

    CardView note_container;
    TextView View_title;
    TextView View_notes;

    public NoteViewHolder(@NonNull View itemView) {
        super(itemView);
        note_container = itemView.findViewById(R.id.note_container);
        View_title = itemView.findViewById(R.id.View_title);
        View_notes = itemView.findViewById(R.id.View_notes);

    }
}