package com.example.jorda.watchlist;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SettingsPage extends AppCompatActivity {

    SQLiteDatabase sqlTVListings;
    private RadioGroup radioGroupMain;
    private RadioButton radSelected;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int intOrderFashion = pref.getInt("your key1", 0); //1 is default value.

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingspage);

        sqlTVListings = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);

        addListenerOnButton();

        if(intOrderFashion==2){
            radioGroupMain.check(R.id.orderName);
        }else{
            radioGroupMain.check(R.id.orderID);
        }

        addListenerOnButton();


    }

    public void deleteAll(View view){
        sqlTVListings.execSQL("DELETE FROM Shows");
    }

    public void reset(View view) {
        addListenerOnButton();
    }
    //used for finding the selected radiobutton
    public void addListenerOnButton() {

        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup1);

        // get selected radio button from radioGroup
        int selectedId = radioGroupMain.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radSelected = (RadioButton) findViewById(selectedId);

        if(radSelected.getText().equals("Order By ID")){
            orderID();
        }

        if(radSelected.getText().equals("Order By Name")){
            orderName();
        }


    }

    public void orderName(){

        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int orderFashion;

        orderFashion = 2;

        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("your key1", orderFashion);
        edit.commit();

        System.out.println("yes im here");

    }

    public void orderID(){

        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int orderFashion;

        orderFashion = 3;

        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("your key1", orderFashion);
        edit.commit();

        System.out.println("yes im again");

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
