package com.example.jorda.watchlist;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.database.*;
/*
The main activity class. It serves no purpose other than a hub.
 */
public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainpage);

        //creates database
        SQLiteDatabase sqlTVListings=openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);
        sqlTVListings.execSQL("CREATE TABLE IF NOT EXISTS Shows(id VARCHAR,name VARCHAR,seasonno VARCHAR,noepisodes VARCHAR,curepisode VARCHAR,airtime VARCHAR, airdate VARCHAR, airfreq VARCHAR);");

        requestPermissions();
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
        //checks if has correct permissions
        final Context context = this;
        PackageManager pm = context.getPackageManager();
        int hasPerm = pm.checkPermission(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                context.getPackageName());
        if (hasPerm != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }else{
            Intent i = new Intent(view.getContext(),UpcomingPage.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);
        }
    }

    public void gotoSettings(View view){


            Intent i = new Intent(view.getContext(),SettingsPage.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivity(i);


    }


    public void requestPermissions(){ //requests permissions to read and write
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            final String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
            ActivityCompat.requestPermissions(this, permissions, 0);
        }


    }


}
