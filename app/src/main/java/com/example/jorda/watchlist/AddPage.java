package com.example.jorda.watchlist;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.*;
import android.database.sqlite.SQLiteDatabase;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class AddPage extends AppCompatActivity {

    EditText edtNameField;
    EditText edtSeasonField;
    EditText edtNumberEpisodesField;
    EditText edtCurrentEpisodeField;
    EditText edtAirTimeField;
    EditText edtAirDateField;
    SQLiteDatabase sqlTVListings;

    int intYear;
    int intMonth;
    int intDay;

    static int id;
    private RadioGroup radioGroupMain;
    private RadioButton radSelected;
    public static int intNotificationID;
    int intNotificationIDprefix = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.addpage);


        edtNameField = (EditText) findViewById(R.id.editText);
        edtSeasonField = (EditText) findViewById(R.id.editText2);
        edtNumberEpisodesField = (EditText) findViewById(R.id.editText3);
        edtCurrentEpisodeField = (EditText) findViewById(R.id.editText4);
        edtAirTimeField = (EditText) findViewById(R.id.editText5);
        edtAirDateField = (EditText) findViewById(R.id.editText6);

        sqlTVListings = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);
        sqlTVListings.execSQL("CREATE TABLE IF NOT EXISTS Shows(id VARCHAR,name VARCHAR,seasonno VARCHAR,noepisodes VARCHAR,curepisode VARCHAR,airtime VARCHAR, airdate VARCHAR, airfreq VARCHAR);");


    }

    //used for finding the selected radiobutton
    public void addListenerOnButton() {

        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup);

        // get selected radio button from radioGroup
        int intSelectedID = radioGroupMain.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radSelected = (RadioButton) findViewById(intSelectedID);

    }

    //can be invoked for a simple Alert Dialog easily with a title and a message
    public void showMessage(String strTitle, String strMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(strTitle);
        builder.setMessage(strMessage);
        builder.show();
    }

    //simple intents to go to other classes on a button click (next 4 classes)
    public void gotoAdd(View view) {
        Intent i = new Intent(view.getContext(), AddPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void gotoModify(View view) {
        Intent i = new Intent(view.getContext(), ModifyPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void gotoUpcoming(View view) {
        Intent i = new Intent(view.getContext(), UpcomingPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void gotoSettings(View view) {
        Intent i = new Intent(view.getContext(), SettingsPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    //used to pop up a time picker dialog
    public void timePick(View view) {


        //inits current time (UF)
        Calendar calCurrentTime = Calendar.getInstance();
        int intHour = calCurrentTime.get(Calendar.HOUR_OF_DAY);
        int intMinute = calCurrentTime.get(Calendar.MINUTE);

        TimePickerDialog tpdTimePicker;
        tpdTimePicker = new TimePickerDialog(AddPage.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                edtAirTimeField.setText(selectedHour + ":" + selectedMinute);

            }
        }, intHour, intMinute, true);
        tpdTimePicker.setTitle("Select airtime");
        tpdTimePicker.show();


    }

    //used to pop up a datMain picker dialog
    public void datePick(View view) {

        //finds the current datMain to make more user friendly
        Calendar calCurrentDate = Calendar.getInstance();
        intYear = calCurrentDate.get(Calendar.YEAR);
        intMonth = calCurrentDate.get(Calendar.MONTH);
        intDay = calCurrentDate.get(Calendar.DAY_OF_MONTH);

        //inits dialog popup
        DatePickerDialog dpdDatePicker = new DatePickerDialog(AddPage.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                edtAirDateField.setText(selectedday + "/" + (selectedmonth + 1) + "/" + selectedyear); //sets edittext (userfriendly)
            }
        }, intYear, intMonth, intDay);
        dpdDatePicker.setTitle("Select airdate");
        dpdDatePicker.show();
    }

    //the scheduling of the notification
    private void scheduleNotification(Notification notMain, long lngDelay) {


        //since all alarms must be unique and CANNOT overlap, a new set of intents and alarms must be created each time to allow
        //the notifications to not overwrite eachother.

            Intent intentNotification = new Intent(this, Receiver.class); //creates the intent for the receiver class
            intentNotification.putExtra(Receiver.NOTIFICATION, notMain); //puts the actual notification

            intNotificationID = intNotificationIDprefix;

            //creates the pending intent to delay the notification, stores the unique id , the notification Intent, and itself
            PendingIntent intentPending = PendingIntent.getBroadcast(this, intNotificationID, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);

            //the android alarm works of the systemclock elapsed time, the amount of millis since boot. the delay variable (generated when the save button is clicked) is added to allow the alarm to determine the system clock time to pend the alarm to
            long lngFutureInMillis = SystemClock.elapsedRealtime() + lngDelay;
            System.out.println("SYSTEM TIME WHEN ALARM FINISHES : " + lngFutureInMillis);

            //sets the alarm to the correct time, and tells it to use the pendingIntent when awoken, which invokes the reciever class...
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, lngFutureInMillis, intentPending);



    }

    //the actual notification builder. creates the notification and "hides" it, until it can be displayed corresponding the alarmManager
    private Notification getNotification(String strContent) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Watchlist");
        builder.setContentText(strContent + " is now airing");
        builder.setSmallIcon(R.drawable.logo);
        return builder.build();
    }

    public void finish(View view) throws Exception {

        //finds the selected airfreq radio button
        addListenerOnButton();

        //below try is to determine if the integer fields are actually integers before continuing. Catches strings, longs, floats
        try{
            Integer.parseInt(edtNumberEpisodesField.getText().toString());
            Integer.parseInt(edtCurrentEpisodeField.getText().toString());
            Integer.parseInt(edtSeasonField.getText().toString());
        }catch (NumberFormatException e){
            showMessage("Error", "Please ensure correct data has been filled out for each field");
            return;
        }

        //data validation
        if (edtNameField.getText().toString().equals("") || edtSeasonField.getText().toString().equals("") || edtNumberEpisodesField.getText().toString().equals("") || edtCurrentEpisodeField.getText().toString().equals("") || edtAirTimeField.getText().toString().equals("") || edtAirDateField.getText().toString().equals("")) {
            showMessage("Error", "Please ensure all fields are correctly filled out.");
        }else if( Integer.parseInt(edtNumberEpisodesField.getText().toString()) < Integer.parseInt(edtCurrentEpisodeField.getText().toString())){
            showMessage("Error", "The current episode in the season cannot be larger than the total number of episodes in the season.");
        }else if( Integer.parseInt(edtSeasonField.getText().toString()) > 1000){
            showMessage("Error", "Season number must be between 1-1000");
        }else if( Integer.parseInt(edtSeasonField.getText().toString()) < 0 ){
            showMessage("Error", "Season Number must be a number between 1-1000");
        }else if( Integer.parseInt(edtNumberEpisodesField.getText().toString()) < 0 || Integer.parseInt(edtNumberEpisodesField.getText().toString()) > 100 ){
            showMessage("Error", "Total Episodes must be a number between 1-100");
        }else if( Integer.parseInt(edtCurrentEpisodeField.getText().toString()) < 0 || Integer.parseInt(edtCurrentEpisodeField.getText().toString()) > 100 ){
            showMessage("Error", "Current episode must be a number between 1-100");
        }else { //end data validation

            //gets the current count variable stored within the application, which keeps the running id to keep each listing unique for the sqlite database
            SharedPreferences shpMain = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
            int intCount = shpMain.getInt("your key", 0); //0 is default value.
            intCount++;
            //assigns the count to id, which is used throughout the class
            id = intCount;
            //puts the count variable back into the storage
            SharedPreferences.Editor edit = shpMain.edit();
            edit.putInt("your key", intCount);
            edit.commit();

            //used to parse the time and the datMain, allowing them to be converted into milliseconds to be used with the alarmmanager class
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");
            String strDateFromFields = edtAirDateField.getText().toString() + " " + edtAirTimeField.getText().toString();
            System.out.println(strDateFromFields);
            Date datMain = sdf.parse(strDateFromFields);

            //inits the current time
            long lngCurrentTime = System.currentTimeMillis();

            String strFinalDate = "";
            //finds the number of remaining episodes
            int intTotalEpisodes = Integer.parseInt(edtNumberEpisodesField.getText().toString()) - Integer.parseInt(edtCurrentEpisodeField.getText().toString());

            //finds the time until the first airdate in milliseconds
            long lngTimeUntil = datMain.getTime();
            long lngTimeUntilAir = lngTimeUntil - lngCurrentTime;

            //gets the current datMain to test below if
            Calendar cal = Calendar.getInstance();
            Date datCurrent = cal.getTime();
            String strNotificationIDprefix;


            if (datCurrent.after(datMain)) { //test to ensure only future dates can be selected
                showMessage("Error", "Please select a future datMain for scheduling TV Listings");
            } else { //if it isnt a future datMain...


                //uses the radio button to determine the air freq
                if (radSelected.getText().equals("Daily")) {

                    int f;
                    for (f = 0; f < (intTotalEpisodes + 1); f++) { //invokes scheduleNotification the correct amount of episodes, multiplying the current episode by 24 hours (86400000 in millis) to schedule the correct number of notifications through the alarmmanager
                        strNotificationIDprefix = "" + id + "00";
                        intNotificationIDprefix = Integer.parseInt(strNotificationIDprefix);
                        intNotificationIDprefix = intNotificationIDprefix + f;
                        scheduleNotification(getNotification(edtNameField.getText().toString()), (lngTimeUntilAir + (86400000 * f)));
                        System.out.println("TIME UNTIL THE NOTIFICATION FOR DAY " + f + " : " + (lngTimeUntilAir + (86400000 * f)));
                    }
                    StringBuffer bufDates = new StringBuffer();


                    Date datParsed = sdf.parse(strDateFromFields);
                    Date datTemp = datParsed;
                    bufDates.append(" ");

                    //used for finding the future air dates for the listings BY DAY
                    for (int fg = 0; fg < (intTotalEpisodes + 1); fg++) {

                        String strFormattedDate = new SimpleDateFormat("dd/MM/yy HH:mm").format(datTemp);

                        bufDates.append(fg + ": " + strFormattedDate + " ");

                        int intNumberDays = 1;
                        Calendar calFutureDate = Calendar.getInstance();
                        calFutureDate.setTime(datTemp);
                        calFutureDate.add(Calendar.DAY_OF_YEAR, intNumberDays);

                        Date datNext = calFutureDate.getTime();

                        datTemp = datNext;
                    }

                    strFinalDate = bufDates.toString();
                    System.out.println(strFinalDate);
                }

                long lngFinalAirTime;
                long lngBigMillis;

                if (radSelected.getText().equals("Weekly")) { //does same for above but for a week instead of day
                    int f;
                    for (f = 0; f < (intTotalEpisodes + 1); f++) {
                        strNotificationIDprefix = "" + id + "00";
                        intNotificationIDprefix = Integer.parseInt(strNotificationIDprefix);
                        intNotificationIDprefix = intNotificationIDprefix + f;
                        lngFinalAirTime = lngTimeUntilAir;
                        lngBigMillis = Long.valueOf(86400000 * f);
                        System.out.println(lngBigMillis);
                        lngFinalAirTime = lngFinalAirTime + lngBigMillis;
                        scheduleNotification(getNotification(edtNameField.getText().toString()), lngFinalAirTime);
                        System.out.println("TIME UNTIL THE NOTIFICATION FOR WEEK " + f + " : " + (lngFinalAirTime));
                        System.out.println(f);
                    }

                    StringBuffer bufDates = new StringBuffer();


                    Date datParsed = sdf.parse(strDateFromFields);
                    Date datTemp = datParsed;
                    bufDates.append(" ");


                    //used for finding the future air dates for the listings BY WEEK
                    for (int fg = 0; fg < (intTotalEpisodes + 1); fg++) {

                        String strFormattedDate = new SimpleDateFormat("dd/MM/yy HH:mm").format(datTemp);

                        bufDates.append(fg + ": " + strFormattedDate + " ");

                        int intNumberDays = 7;
                        Calendar calFutureDate = Calendar.getInstance();
                        calFutureDate.setTime(datTemp);
                        calFutureDate.add(Calendar.DAY_OF_YEAR, intNumberDays);

                        Date datNext = calFutureDate.getTime();

                        datTemp = datNext;
                    }

                    strFinalDate = bufDates.toString();
                    System.out.println(strFinalDate);
                }


                //inserts the new listing into the master database
                sqlTVListings.execSQL("INSERT INTO Shows VALUES('"
                        + id
                        + "','" +
                        edtNameField.getText()
                        + "','" +
                        edtSeasonField.getText()
                        + "','" +
                        edtNumberEpisodesField.getText()
                        + "','" +
                        edtCurrentEpisodeField.getText()
                        + "','" +
                        edtAirTimeField.getText()
                        + "','" +
                        strFinalDate
                        + "','" +
                        radSelected.getText()
                        + "');");


                //used for displayed the listing details after they have been injected into the SQL database

                //inits the cursor to find the location of the correct listing where the name matches the listing
                //just created
                Cursor curAllListings = sqlTVListings.rawQuery("SELECT * FROM Shows WHERE name='" + edtNameField.getText() + "'", null);

                //used to handle any null exceptions
                if (curAllListings.getCount() == 0) {
                    showMessage("Error", "No records found");
                    return;
                }
                //buffers the SQL listing and invokes the showMessage void to display the listing details
                StringBuffer bufCurrentListing = new StringBuffer();
                while (curAllListings.moveToNext()) {

                    bufCurrentListing.append("ID: " + curAllListings.getString(0) + "\n");
                    bufCurrentListing.append("Name: " + curAllListings.getString(1) + "\n");
                    bufCurrentListing.append("Season Number: " + curAllListings.getString(2) + "\n");
                    bufCurrentListing.append("Number of Episodes: " + curAllListings.getString(3) + "\n");
                    bufCurrentListing.append("Current Episode: " + curAllListings.getString(4) + "\n");
                    bufCurrentListing.append("Air Time: " + curAllListings.getString(5) + "\n");
                    bufCurrentListing.append("Next Episode Airs: " + (curAllListings.getString(6).substring(4, 10) + "20" + curAllListings.getString(6).substring(10, 12) + "\n"));
                    bufCurrentListing.append("Air Freq: " + curAllListings.getString(7) + "\n\n");
                }
                showMessage("Listing Details", bufCurrentListing.toString());

            }

        }
    }
}