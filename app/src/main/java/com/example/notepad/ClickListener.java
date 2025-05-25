package com.example.notepad;

import androidx.cardview.widget.CardView;

import com.example.notepad.Models.Notes;

public interface ClickListener {
    void onClick(Notes notes);
    void onLongClick(Notes notes, CardView cardView);

}
