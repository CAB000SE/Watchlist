package com.example.jorda.watchlist;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class SettingsPage extends AppCompatActivity {

    SQLiteDatabase db2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_page);

        db2 = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);


    }

    public void deleteAll(View view){
        db2.execSQL("DELETE FROM Shows");
    }

    public void orderAll(){
        Cursor c = db2.rawQuery("SELECT * FROM Shows ORDER BY name", null);
        StringBuffer buffer = new StringBuffer();
        while (c.moveToNext()) {


            db2.execSQL("INSERT INTO Shows VALUES('"
                    + c.getString(0)
                    + "','" +
                    c.getString(1)
                    + "','" +
                    c.getString(2)
                    + "','" +
                    c.getString(3)
                    + "','" +
                    c.getString(4)
                    + "','" +
                    c.getString(5)
                    + "','" +
                    c.getString(6)
                    + "','" +
                    c.getString(7)
                    + "');");



            buffer.append("ID: " + c.getString(0)+"\n");
            buffer.append("Name: "+c.getString(1)+"\n");
            buffer.append("Season Number: "+c.getString(2)+"\n");
            buffer.append("Number of Episodes: "+c.getString(3)+"\n");
            buffer.append("Current Episodes: "+c.getString(4)+"\n");
            buffer.append("Air Time: "+c.getString(5)+"\n");
            buffer.append("Air Date: "+c.getString(6)+"\n");
            buffer.append("Air Freq: "+c.getString(7)+"\n\n");
        }  showMessage("TV Listings", buffer.toString());
    }

    public void showMessage(String title,String message) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();



    }

    public void gotoAdd(View view){
        Intent i = new Intent(view.getContext(),AddPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void gotoModify(View view){
        Intent i = new Intent(view.getContext(),ModifyPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void gotoUpcoming(View view){
        Intent i = new Intent(view.getContext(),UpcomingPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void gotoSettings(View view){
        Intent i = new Intent(view.getContext(),SettingsPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }
}
