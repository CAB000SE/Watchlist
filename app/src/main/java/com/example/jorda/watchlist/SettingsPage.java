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
/*
allows the user to change the sorting order, as well as erase all listings.
 */
public class SettingsPage extends AppCompatActivity {

    SQLiteDatabase sqlTVListings;
    private RadioGroup radioGroupMain;
    private RadioButton radSelected;




    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //gets the order fashion (if exists)
        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int intOrderFashion = pref.getInt("your key1", 0);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.settingspage);

        sqlTVListings = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);

        addListenerOnButton(); //listens to a change in radio button

        if(intOrderFashion==2){ //if the radio button has already been changed
            radioGroupMain.check(R.id.orderName);
        }else{ //if it hasnt, default at sort by ID
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

        if(radSelected.getText().equals("Order By ID")){ //if the radio button is ID
            orderID();
        }

        if(radSelected.getText().equals("Order By Name")){ //if the radio button is Name
            orderName();
        }


    }

    public void orderName(){

        //orders listings by name
        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int orderFashion;

        orderFashion = 2; //sets the orderFashion as 2. This is used in the modify listing / upcoming class.

        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("your key1", orderFashion);
        edit.commit();


    }

    public void orderID(){
        //orders listings by ID

        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int orderFashion;

        orderFashion = 3; //sets the orderFashion as 3. This is used in the modify listing / upcoming class.

        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("your key1", orderFashion);
        edit.commit();


    }


    public void showMessage(String title,String message) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();



    }

    //intent buttons
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
