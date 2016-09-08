package com.example.jorda.watchlist;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
/*
The purpose of this class is to allow TV Listings created in the AddPage class to be modified or deleted.
 */
public class ModifyPage extends AppCompatActivity {


    SQLiteDatabase sqlTVListings;
    EditText edtNameField;
    EditText edtSeasonField;
    EditText edtNumberEpisodesField;
    EditText edtCurrentEpisodeField;
    EditText edtAirTimeField;
    EditText edtDateField;
    private RadioGroup radioGroupMain;
    private RadioButton radSelected;
    Button btnSaveListing;
    ListView lisMain;

    static int id;

    int intYear;
    int intMonth;
    int intDay;

    public static int intNotificationID;
    int intNotificationIDprefix = 0;
    Cursor curDisplayListings;

    String strIDlistings;
    String strNameListings;
    int intSelectedID;

    ArrayList arrListings = new ArrayList<>();

    TextView texAirFreq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.modifypage);

        edtNameField =(EditText)findViewById(R.id.editText);
        edtSeasonField =(EditText)findViewById(R.id.editText2);
        edtNumberEpisodesField =(EditText)findViewById(R.id.editText3);
        edtCurrentEpisodeField =(EditText)findViewById(R.id.editText4);
        edtAirTimeField =(EditText)findViewById(R.id.editText5);
        edtDateField =(EditText)findViewById(R.id.editText6);
        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup);
        btnSaveListing = (Button) findViewById(R.id.button3);
        texAirFreq = (TextView) findViewById(R.id.editText5);



        sqlTVListings = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);
        sqlTVListings.execSQL("CREATE TABLE IF NOT EXISTS Shows(id VARCHAR,name VARCHAR,seasonno VARCHAR,noepisodes VARCHAR,curepisode VARCHAR,airtime VARCHAR, airdate VARCHAR, airfreq VARCHAR);");

        lisMain = (ListView) findViewById(R.id.listView2);

        displayList();

        ArrayAdapter<String> arrayAdapter=new ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1, arrListings){

            //used to quickly change the colour to black
            @Override
            public View getView(int position, View convertView,
                                ViewGroup parent) {
                View view =super.getView(position, convertView, parent);

                TextView textView=(TextView) view.findViewById(android.R.id.text1);

                textView.setTextColor(Color.BLACK);

                return view;
            }
        };

        lisMain.setAdapter(arrayAdapter);

    }

    public void displayList(){

        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int intOrderFashion = pref.getInt("your key1", 0); //comes from radio button in settings activity
        System.out.println(intOrderFashion);
        if(intOrderFashion==2){ //sorts by name
            curDisplayListings = sqlTVListings.rawQuery("SELECT * FROM Shows ORDER BY name COLLATE NOCASE;", null);
        } else{ //sorts by ID
            curDisplayListings = sqlTVListings.rawQuery("SELECT * FROM Shows", null);
        }


        //if no listings are found
        if(curDisplayListings.getCount()==0)
        {
            showMessage("Error", "No Listings Found");
            return;
        }



        while(curDisplayListings.moveToNext())
        {

            StringBuffer bufAllListings = new StringBuffer();
            //adds the listing to a string buffer
            bufAllListings.append("ID: " + curDisplayListings.getString(0)+"             ");
            bufAllListings.append("Name: "+ curDisplayListings.getString(1)+"\n");


            //adds the buffer to the master array used in the CustomListAdapter
            arrListings.add(bufAllListings.toString());

        }


        //onItemClick
        lisMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){

                //finds the item clicked
                String strBefore = lisMain.getItemAtPosition(position).toString();
                String strAfter = lisMain.getItemAtPosition(position).toString();

                //used to find the ID in a list view
                strBefore = strBefore.substring(strBefore.indexOf("ID: ") + 4);
                strBefore = strBefore.substring(0, strBefore.indexOf("             "));

                //used to find the name in a list view
                strAfter = strAfter.substring(strBefore.indexOf("Name: ") + 6);
                strAfter = strAfter.substring(strBefore.indexOf("             ") + 20);

                //sets into variable
                strIDlistings = strBefore;
                strNameListings = strAfter;

                //popups option for selected show
                showPopup("Select Option" ,"Selected Show: " + strNameListings, strIDlistings);

            }
        });


    }

    public void showPopup(String strTitle, String strMessage, String strListingID) {
        //used to pop up a dialog allowing the user to modify, delete, or cancel out of a listing
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        intSelectedID = Integer.parseInt(strListingID);
        builder.setTitle(strTitle);

        builder.setMessage(strMessage);

        builder.setPositiveButton("Modify",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        displayID(intSelectedID); //if modify is selected, doisplayID with releveant ID
                    }
                });

        builder.setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        //if delete is selected, delete Listing with relevant ID
                        deleteID(intSelectedID);


                        //toast the process
                        Toast toast = Toast.makeText(getApplicationContext(), "Successfully deleted " + strNameListings + " from the database", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
        builder.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); //exits

                    }
                });
        builder.show();
    }

    public void showMessage(String title,String message) { //message builder
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    public void displayID(int id){

        //finds the SQL entry where the ID equals the selected ID
        Cursor curSelectedID= sqlTVListings.rawQuery("SELECT * FROM Shows WHERE id='"+id+"'", null);
        if(curSelectedID.getCount()==0)
        {
            showMessage("Error", "Please enter a valid ID");
            return;
        }
        while(curSelectedID.moveToNext())
        { //replaces the empty fields with the data in the selected listing
            edtNameField.setText(curSelectedID.getString(1));
            edtSeasonField.setText(curSelectedID.getString(2));
            edtNumberEpisodesField.setText(curSelectedID.getString(3));
            edtCurrentEpisodeField.setText(curSelectedID.getString(4));
            edtAirTimeField.setText(curSelectedID.getString(5));
            edtDateField.setText(curSelectedID.getString(6).substring(4,10) + "20" + curSelectedID.getString(6).substring(10,12));
            //ticks the correct radio button
            if(curSelectedID.getString(7).equals("Daily")){
                radioGroupMain.check(R.id.option1);
            }
            if(curSelectedID.getString(7).equals("Weekly")){
                radioGroupMain.check(R.id.option1b);
            }


        }

        //sets the modify fields as visible
        edtNameField.setVisibility(View.VISIBLE);
        edtSeasonField.setVisibility(View.VISIBLE);
        edtNumberEpisodesField.setVisibility(View.VISIBLE);
        edtCurrentEpisodeField.setVisibility(View.VISIBLE);
        edtAirTimeField.setVisibility(View.VISIBLE);
        edtDateField.setVisibility(View.VISIBLE);
        radioGroupMain.setVisibility(View.VISIBLE);
        btnSaveListing.setVisibility(View.VISIBLE);
        texAirFreq.setVisibility(View.VISIBLE);

        //sets the listview as invisible
        lisMain.setVisibility(View.INVISIBLE);

    }

    public void deleteID(int id){
        //deletes from database where the ID equals the given ID
        sqlTVListings.execSQL("DELETE FROM Shows WHERE id='"+id+"'");
        final Context context = this;

        Intent i = new Intent(context,ModifyPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);

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

    public void gotoSettings(View view){
        Intent i = new Intent(view.getContext(),SettingsPage.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
    }

    public void addListenerOnButton() { //used to find selected radio button

        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup);

        // get selected radio button from radioGroup
        int intSelectedID = radioGroupMain.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radSelected = (RadioButton) findViewById(intSelectedID);

    }

    public void timePick(View view){
        //inits current time (UF)
        Calendar calCurrentTime = Calendar.getInstance();
        int intHour = calCurrentTime.get(Calendar.HOUR_OF_DAY);
        int intMinute = calCurrentTime.get(Calendar.MINUTE);

        TimePickerDialog tpdTimePicker;
        tpdTimePicker = new TimePickerDialog(ModifyPage.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                edtAirTimeField.setText( selectedHour + ":" + selectedMinute);
            }
        }, intHour, intMinute, true);
        tpdTimePicker.setTitle("Select airtime");
        tpdTimePicker.show();
    }

    public void datePick(View view){

        //finds the current datMain to make more user friendly
        Calendar calCurrentDate=Calendar.getInstance();
        intYear =calCurrentDate.get(Calendar.YEAR);
        intMonth =calCurrentDate.get(Calendar.MONTH);
        intDay =calCurrentDate.get(Calendar.DAY_OF_MONTH);

        //inits dialog popup
        DatePickerDialog dpdDatePicker=new DatePickerDialog(ModifyPage.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                edtDateField.setText(selectedday + "/" + (selectedmonth+1) + "/" + selectedyear); //sets edittext (userfriendly)
            }
        }, intYear, intMonth, intDay);
        dpdDatePicker.setTitle("Select airdate");
        dpdDatePicker.show();  }

    private void scheduleNotification(Notification notMain, long lngDelay) {

        Intent intentNotification = new Intent(this, Receiver.class); //creates the intent for the receiver class
        intentNotification.putExtra(Receiver.NOTIFICATION, notMain); //puts the actual notification

        intNotificationID = intNotificationIDprefix;

        //creates the pending intent to delay the notification, stores the unique id , the notification Intent
        PendingIntent intentPending = PendingIntent.getBroadcast(this, intNotificationID, intentNotification, PendingIntent.FLAG_UPDATE_CURRENT);

        //the android alarm works of the systemclock elapsed time, the amount of millis since boot. the delay variable (generated when the save button is clicked) is added to allow the alarm to determine the system clock time to pend the alarm to
        long lngFutureInMillis = SystemClock.elapsedRealtime() + lngDelay;
        System.out.println("SYSTEM TIME WHEN ALARM FINISHES : " + lngFutureInMillis);

        //sets the alarm to the correct time, and tells it to use the pendingIntent when awoken, which invokes the reciever class...
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, lngFutureInMillis, intentPending);

    }

    private Notification getNotification(String strContent) { //builds the actual notification
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Watchlist");
        builder.setContentText(strContent + " is now airing");
        builder.setSmallIcon(R.drawable.logo);
        return builder.build();
    }

    public void finish(View view) throws Exception{

        //sets ID to existing ID, instead of creating new
        id = intSelectedID;
        System.out.println("VARIABLE: id - " + id);
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
        if (edtNameField.getText().toString().equals("") || edtSeasonField.getText().toString().equals("") || edtNumberEpisodesField.getText().toString().equals("") || edtCurrentEpisodeField.getText().toString().equals("") || edtAirTimeField.getText().toString().equals("") || edtDateField.getText().toString().equals("")) {
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

            //used to parse the time and the datMain, allowing them to be converted into milliseconds to be used with the alarmmanager class
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");
            String strDateFromFields = edtDateField.getText().toString() + " " + edtAirTimeField.getText().toString();
            System.out.println(strDateFromFields);
            Date datMain = sdf.parse(strDateFromFields);

            //inits the current time
            long lngCurrentTime = System.currentTimeMillis();

            String strFinalDate = "";
            //finds number of remaining episodes
            int intTotalEpisodes = Integer.parseInt(edtNumberEpisodesField.getText().toString()) - Integer.parseInt(edtCurrentEpisodeField.getText().toString());
            long longTimeUntil = datMain.getTime();

            //finds the time until the first airdate in milliseconds
            long lngTimeUntilAir = longTimeUntil - lngCurrentTime;

            //gets the current date to test below if
            Calendar cal = Calendar.getInstance();
            Date datCurrent = cal.getTime();
            String strNotificationIDprefix;


            if (datCurrent.after(datMain)) {//test to ensure only future dates can be selected
                showMessage("Error", "Please select a future datMain for scheduling TV Listings");
            } else { //if it isnt a future date...

                //uses the radio button to determine the air freq
                if (radSelected.getText().equals("Daily")) {

                    int f;
                    for (f = 0; f < (intTotalEpisodes + 1); f++) { //invokes scheduleNotification the correct amount of episodes, multiplying the current episode by 24 hours (86400000 in millis) to schedule the correct number of notifications through the alarmmanager
                        strNotificationIDprefix = "" + id + "00"; //REPLACES the unique ID for each air date, with new information
                        intNotificationIDprefix = Integer.parseInt(strNotificationIDprefix);
                        intNotificationIDprefix = intNotificationIDprefix + f;
                        scheduleNotification(getNotification(edtNameField.getText().toString()), (lngTimeUntilAir + (86400000 * f)));
                        System.out.println("TIME UNTIL THE NOTIFICATION FOR DAY " + f + " : " + (lngTimeUntilAir + (86400000 * f)));
                    }

                    StringBuffer bufDates = new StringBuffer();


                    //gets parsed date
                    Date datParsed = sdf.parse(strDateFromFields);
                    Date datTemp = datParsed;
                    bufDates.append(" ");

                    //used for finding the future air dates for the listings BY DAY
                    for (int fg = 0; fg < (intTotalEpisodes + 1); fg++) {

                        //formats date
                        String strFormattedDate = new SimpleDateFormat("dd/MM/yy HH:mm").format(datTemp);

                        //adds the future dates into buffer
                        bufDates.append(fg + ": " + strFormattedDate + " ");

                        int intNumberDays = 7;
                        Calendar calFutureDate = Calendar.getInstance();
                        calFutureDate.setTime(datTemp);
                        calFutureDate.add(Calendar.DAY_OF_YEAR, intNumberDays); //adds 1 day to variable

                        Date datNext = calFutureDate.getTime();

                        datTemp = datNext; //puts the date into temp variable
                    }

                    strFinalDate = bufDates.toString(); //puts the final dates into string
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
                        lngFinalAirTime = lngTimeUntilAir; //has to use longs due to massive numbers for a week in millis
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


                //finally updates the database with the new information
                Cursor curCurrentListing = sqlTVListings.rawQuery("SELECT * FROM Shows WHERE id='" + intSelectedID + "'", null);
                if (curCurrentListing.moveToFirst()) {

                    sqlTVListings.execSQL("UPDATE Shows SET name='"
                            + edtNameField.getText()
                            + "',seasonno='"
                            + edtSeasonField.getText()
                            + "',noepisodes='"
                            + edtNumberEpisodesField.getText()
                            + "',curepisode='"
                            + edtCurrentEpisodeField.getText()
                            + "',airtime='"
                            + edtAirTimeField.getText()
                            + "',airdate='"
                            + strFinalDate
                            + "',airfreq='"
                            + radSelected.getText()
                            +
                            "' WHERE id='" + intSelectedID + "'");
                }

                StringBuffer bufCurrentListing = new StringBuffer();
                Cursor d = sqlTVListings.rawQuery("SELECT * FROM Shows WHERE name='" + edtNameField.getText() + "'", null);

                //prints the updated notification
                while (d.moveToNext()) {

                    bufCurrentListing.append("ID: " + d.getString(0) + "\n");
                    bufCurrentListing.append("Name: " + d.getString(1) + "\n");
                    bufCurrentListing.append("Season Number: " + d.getString(2) + "\n");
                    bufCurrentListing.append("Number of Episodes: " + d.getString(3) + "\n");
                    bufCurrentListing.append("Current Episode: " + d.getString(4) + "\n");
                    bufCurrentListing.append("Air Time: " + d.getString(5) + "\n");
                    bufCurrentListing.append("Next Episode Airs: " + (d.getString(6).substring(4, 10) + "20" + d.getString(6).substring(10, 12) + "\n"));
                    bufCurrentListing.append("Air Freq: " + d.getString(7) + "\n\n");

                    showMessage("Listing Details", bufCurrentListing.toString());

                    edtNameField.setText("");
                    edtSeasonField.setText("");
                    edtNumberEpisodesField.setText("");
                    edtCurrentEpisodeField.setText("");
                    edtAirTimeField.setText("");
                    edtDateField.setText("");
                }


            }
        }
    }

}



