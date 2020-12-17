package com.deekshant.browser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

public class HistoryAndBookmarks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_and_bookmarks);
    }

    public void closeActivity(View view){
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}