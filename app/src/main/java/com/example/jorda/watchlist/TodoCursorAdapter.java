package com.example.jorda.watchlist;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TodoCursorAdapter extends CursorAdapter {
    public TodoCursorAdapter(Context context, Cursor cursor, int flags) {
        super(context, cursor, 0);
    }
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        return null;

    }
    public void bindView(View view, Context context, Cursor cursor) {


    }
}
