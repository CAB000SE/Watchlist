package com.example.jorda.watchlist;

import android.app.Activity;
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
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class AddPage extends AppCompatActivity {

    EditText editName;
    EditText editSeason;
    EditText editNumberEpisodes;
    EditText editCurrentEpisode;
    EditText editAirTime;
    EditText editAirDate;
    SQLiteDatabase db2;

    int mYear;
    int mMonth;
    int mDay;

    static int id;
    private RadioGroup radioGroupMain;
    private RadioButton radioSelected;
    public static int notificationID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_page);


        editName = (EditText) findViewById(R.id.editText);
        editSeason = (EditText) findViewById(R.id.editText2);
        editNumberEpisodes = (EditText) findViewById(R.id.editText3);
        editCurrentEpisode = (EditText) findViewById(R.id.editText4);
        editAirTime = (EditText) findViewById(R.id.editText5);
        editAirDate = (EditText) findViewById(R.id.editText6);

        db2 = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);
        db2.execSQL("CREATE TABLE IF NOT EXISTS Shows(id VARCHAR,name VARCHAR,seasonno VARCHAR,noepisodes VARCHAR,curepisode VARCHAR,airtime VARCHAR, airdate VARCHAR, airfreq VARCHAR);");


    }

    //used for finding the selected radiobutton
    public void addListenerOnButton() {

        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup);

        // get selected radio button from radioGroup
        int selectedId = radioGroupMain.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radioSelected = (RadioButton) findViewById(selectedId);

    }

    //can be invoked for a simple Alert Dialog easily with a title and a message
    public void showMessage(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
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
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(AddPage.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                editAirTime.setText(selectedHour + ":" + selectedMinute);

            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select airtime");
        mTimePicker.show();


    }

    //used to pop up a date picker dialog
    public void datePick(View view) {

        //finds the current date to make more user friendly
        Calendar mcurrentDate = Calendar.getInstance();
        mYear = mcurrentDate.get(Calendar.YEAR);
        mMonth = mcurrentDate.get(Calendar.MONTH);
        mDay = mcurrentDate.get(Calendar.DAY_OF_MONTH);

        //inits dialog popup
        DatePickerDialog mDatePicker = new DatePickerDialog(AddPage.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                editAirDate.setText(selectedday + "/" + (selectedmonth + 1) + "/" + selectedyear); //sets edittext (userfriendly)
            }
        }, mYear, mMonth, mDay);
        mDatePicker.setTitle("Select airdate");
        mDatePicker.show();
    }

    //the scheduling of the notification
    private void scheduleNotification(Notification notification, long delay) {


        //since all alarms must be unique and CANNOT overlap, a new set of intents and alarms must be created each time to allow
        //the notifications to not overwrite eachother.

            Intent notificationIntent = new Intent(this, Receiver.class); //creates the intent for the receiver class
            notificationIntent.putExtra(Receiver.NOTIFICATION, notification); //puts the actual notification

            Random generator = new Random(); //generates a random number
            notificationID = generator.nextInt(100000000);
            //TODO: keep track of above variable to allow notification to be overwritten in ModifyPage

            //creates the pending intent to delay the notification, stores the unique id (a - random int), the notification Intent, and itself
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            //the android alarm works of the systemclock elapsed time, the amount of millis since boot. the delay variable (generated when the save button is clicked) is added to allow the alarm to determine the system clock time to pend the alarm to
            long futureInMillis = SystemClock.elapsedRealtime() + delay;
            System.out.println("SYSTEM TIME WHEN ALARM FINISHES : " + futureInMillis);

            //sets the alarm to the correct time, and tells it to use the pendingIntent when awoken, which invokes the reciever class...
            AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);



    }

    //the actual notification builder. creates the notification and "hides" it, until it can be displayed corresponding the alarmManager
    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.logo);
        return builder.build();
    }















    public void finish(View view) throws Exception {

        //finds the selected airfreq radio button
        addListenerOnButton();

        //gets the current count variable stored within the application, which keeps the running id to keep each listing unique for the sqlite database
        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int count = pref.getInt("your key", 0); //0 is default value.
        count++;
        //assigns the count to id, which is used throughout the class
        id = count;
        //puts the count variable back into the storage
        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("your key", count);
        edit.commit();

        //used to parse the time and the date, allowing them to be converted into milliseconds to be used with the alarmmanager class
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");
        String mm = editAirDate.getText().toString() + " " + editAirTime.getText().toString();
        System.out.println(mm);
        Date date = sdf.parse(mm);

        //inits the current time
        long lngCurrentTime = System.currentTimeMillis();

        String meme = "";
        //finds the number of remaining episodes
        int intTotalEpisodes = Integer.parseInt(editNumberEpisodes.getText().toString()) - Integer.parseInt(editCurrentEpisode.getText().toString());
        System.out.println("INTTOTALEPISODES: " + intTotalEpisodes);

        //finds the time until the first airdate in milliseconds
        long longTimeUntil = date.getTime();
        long lngTimeUntilAir = longTimeUntil - lngCurrentTime;

        //gets the current date to test below if
        Calendar cal = Calendar.getInstance();
        Date currentDate = cal.getTime();

        if(currentDate.after(date)){ //test to ensure only future dates can be selected
            showMessage("Error", "Please select a future date for scheduling TV Listings");
        }else{ //if it isnt a future date...



        //uses the radio button to determine the air freq
        if (radioSelected.getText().equals("Daily")) {

            int f;
            for (f = 0; f < intTotalEpisodes; f++) { //invokes scheduleNotification the correct amount of episodes, multiplying the current episode by 24 hours (86400000 in millis) to schedule the correct number of notifications through the alarmmanager
                scheduleNotification(getNotification(editName.getText().toString()), (lngTimeUntilAir + (86400000 * f)));
                System.out.println("TIME UNTIL THE NOTIFICATION FOR DAY " + f + " : " + (lngTimeUntilAir + (86400000 * f)));
            }
            StringBuffer buffer2 = new StringBuffer();


            Date date2 = sdf.parse(mm);
            Date temp = date2;
            buffer2.append(" ");

            //used for finding the future air dates for the listings BY DAY
            for(int fg=0;fg < (intTotalEpisodes + 1);fg++) {

                String newstring = new SimpleDateFormat("dd/MM/yy HH:mm").format(temp);

                buffer2.append(fg + ": " + newstring + " ");

                int noOfDays = 1;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(temp);
                calendar.add(Calendar.DAY_OF_YEAR, noOfDays);

                Date nextDate = calendar.getTime();

                temp = nextDate;
            }

            meme = buffer2.toString();
            System.out.println(meme);
        }
        if (radioSelected.getText().equals("Weekly")) { //does same for above but for a week instead of day
            int f;
            for (f = 0; f < intTotalEpisodes; f++) {
                scheduleNotification(getNotification(editName.getText().toString()), (lngTimeUntilAir + (604800000 * f)));
                System.out.println("TIME UNTIL THE NOTIFICATION FOR WEEK ONE" + (lngTimeUntilAir + (604800000 * f)));
            }

            StringBuffer buffer2 = new StringBuffer();


            Date date2 = sdf.parse(mm);
            Date temp = date2;

            //used for finding the future air dates for the listings BY WEEK
            for(int fg=0;fg < (intTotalEpisodes + 1);fg++) {

                String newstring = new SimpleDateFormat("dd/MM/yy HH:mm").format(temp);

                buffer2.append(fg + ": " + newstring + " ");

                int noOfDays = 7;
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(temp);
                calendar.add(Calendar.DAY_OF_YEAR, noOfDays);

                Date nextDate = calendar.getTime();

                temp = nextDate;
            }

            meme = buffer2.toString();
            System.out.println(meme);
        }




        //inserts the new listing into the master database
        db2.execSQL("INSERT INTO Shows VALUES('"
                + id
                + "','" +
                editName.getText()
                + "','" +
                editSeason.getText()
                + "','" +
                editNumberEpisodes.getText()
                + "','" +
                editCurrentEpisode.getText()
                + "','" +
                editAirTime.getText()
                + "','" +
                meme
                + "','" +
                radioSelected.getText()
                + "');");


        //used for displayed the listing details after they have been injected into the SQL database

        //inits the cursor to find the location of the correct listing where the name matches the listing
        //just created
        Cursor c = db2.rawQuery("SELECT * FROM Shows WHERE name='" + editName.getText() + "'", null);

        //used to handle any null exceptions
        if (c.getCount() == 0) {
            showMessage("Error", "No records found");
            return;
        }
        //buffers the SQL listing and invokes the showMessage void to display the listing details
        StringBuffer buffer = new StringBuffer();
        while (c.moveToNext()) {

            buffer.append("ID: " + c.getString(0) + "\n");
            buffer.append("Name: " + c.getString(1) + "\n");
            buffer.append("Season Number: " + c.getString(2) + "\n");
            buffer.append("Number of Episodes: " + c.getString(3) + "\n");
            buffer.append("Current Episode: " + c.getString(4) + "\n");
            buffer.append("Air Time: " + c.getString(5) + "\n");
            buffer.append("Next Episode Airs: " +(c.getString(6).substring(4,10) + "20" + c.getString(6).substring(10,12) + "\n"));
            buffer.append("Air Freq: " + c.getString(7) + "\n\n");
        }
        showMessage("Listing Details", buffer.toString());

        }

    }
}