package com.mcksfg.noteadd;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class SingleNoteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_note);

        String image_url = getIntent().getStringExtra("image_url");

        if(image_url != null) {
            PhotoView singleNoteImage = findViewById(R.id.singleNoteImage);
            Glide.with(this).load(image_url).into(singleNoteImage);
        }
    }
}
