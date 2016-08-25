package com.example.jorda.watchlist;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ModifyPage extends AppCompatActivity {


    SQLiteDatabase db;
    EditText editName;
    EditText editSeason;
    EditText editNumberEpisodes;
    EditText editCurrentEpisode;
    EditText editAirTime;
    EditText editAirDate;
    private RadioGroup radioGroupMain;
    private RadioButton radioSelected;
    Button saveListing;
    Button deleteListing;
    ListView lv;


    static int id;

    int mYear;
    int mMonth;
    int mDay;

    public static int notificationID;
    int intNotificationIDprefix = 0;




    String strIDlistings;
    String strNameListings;
    int intSelectedID;

    //List<String> arrListings = new ArrayList<String>();

    ArrayList arrListings = new ArrayList<>();

    TextView textAirFreq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_page);

        editName=(EditText)findViewById(R.id.editText);
        editSeason=(EditText)findViewById(R.id.editText2);
        editNumberEpisodes=(EditText)findViewById(R.id.editText3);
        editCurrentEpisode=(EditText)findViewById(R.id.editText4);
        editAirTime=(EditText)findViewById(R.id.editText5);
        editAirDate=(EditText)findViewById(R.id.editText6);
        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup);
        saveListing = (Button) findViewById(R.id.button3);
        textAirFreq = (TextView) findViewById(R.id.editText5);



        db = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Shows(id VARCHAR,name VARCHAR,seasonno VARCHAR,noepisodes VARCHAR,curepisode VARCHAR,airtime VARCHAR, airdate VARCHAR, airfreq VARCHAR);");

        lv = (ListView) findViewById(R.id.listView2);

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

        lv.setAdapter(arrayAdapter);

    }

    public void displayList(){

        Cursor c=db.rawQuery("SELECT * FROM Shows", null);
        if(c.getCount()==0)
        {
            showMessage("Error", "No Listings Found");
            return;
        }



        while(c.moveToNext())
        {
            StringBuffer buffer = new StringBuffer();

            buffer.append("ID: " + c.getString(0)+"             ");
            buffer.append("Name: "+c.getString(1)+"\n");


            arrListings.add(buffer.toString());

        }


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){

                String s = lv.getItemAtPosition(position).toString();
                String a = lv.getItemAtPosition(position).toString();

                s = s.substring(s.indexOf("ID: ") + 4);
                s = s.substring(0, s.indexOf("             "));

                a = a.substring(s.indexOf("Name: ") + 6);
                a = a.substring(s.indexOf("             ") + 20);


                strIDlistings = s;
                strNameListings = a;


                System.out.println(s);


                showPopup("Select Option" ,"Selected Show: " + strNameListings, strIDlistings);


            }
        });


    }

    public void showPopup(String title, String message, String listingID) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        intSelectedID = Integer.parseInt(listingID);
        builder.setTitle(title);

        builder.setMessage(message);

        builder.setPositiveButton("Modify",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        displayID(intSelectedID);
                    }
                });

        builder.setNegativeButton("Delete",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        deleteID(intSelectedID);

                        Toast toast = Toast.makeText(getApplicationContext(), "Successfully deleted " + strNameListings + " from the database", Toast.LENGTH_LONG);
                        toast.show();
                    }
                });
        builder.setNeutralButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();

                    }
                });
        builder.show();
    }

    public void showMessage(String title,String message) {
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();



    }

    public void displayID(int id){

        Cursor c=db.rawQuery("SELECT * FROM Shows WHERE id='"+id+"'", null);
        if(c.getCount()==0)
        {
            showMessage("Error", "Please enter a valid ID");
            return;
        }
        while(c.moveToNext())
        {
            editName.setText(c.getString(1));
            editSeason.setText(c.getString(2));
            editNumberEpisodes.setText(c.getString(3));
            editCurrentEpisode.setText(c.getString(4));
            editAirTime.setText(c.getString(5));
            editAirDate.setText(c.getString(6).substring(4,10) + "20" + c.getString(6).substring(10,12));
            if(c.getString(7).equals("Daily")){
                radioGroupMain.check(R.id.option1);
            }
            if(c.getString(7).equals("Weekly")){
                radioGroupMain.check(R.id.option1a);
            }
            if(c.getString(7).equals("Monthly")){
                radioGroupMain.check(R.id.option1b);
            }


        }

        editName.setVisibility(View.VISIBLE);
        editSeason.setVisibility(View.VISIBLE);
        editNumberEpisodes.setVisibility(View.VISIBLE);
        editCurrentEpisode.setVisibility(View.VISIBLE);
        editAirTime.setVisibility(View.VISIBLE);
        editAirDate.setVisibility(View.VISIBLE);
        radioGroupMain.setVisibility(View.VISIBLE);
        saveListing.setVisibility(View.VISIBLE);
        textAirFreq.setVisibility(View.VISIBLE);

        lv.setVisibility(View.INVISIBLE);

    }

    public void deleteID(int id){

        db.execSQL("DELETE FROM Shows WHERE id='"+id+"'");




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

    public void addListenerOnButton() {

        radioGroupMain = (RadioGroup) findViewById(R.id.radioGroup);

        // get selected radio button from radioGroup
        int selectedId = radioGroupMain.getCheckedRadioButtonId();

        // find the radiobutton by returned id
        radioSelected = (RadioButton) findViewById(selectedId);

    }

    public void timePick(View view){


        //inits current time (UF)
        Calendar mcurrentTime = Calendar.getInstance();
        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(ModifyPage.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                editAirTime.setText( selectedHour + ":" + selectedMinute);
            }
        }, hour, minute, true);
        mTimePicker.setTitle("Select airtime");
        mTimePicker.show();

    }

    public void datePick(View view){

        //finds the current date to make more user friendly
        Calendar mcurrentDate=Calendar.getInstance();
        mYear=mcurrentDate.get(Calendar.YEAR);
        mMonth=mcurrentDate.get(Calendar.MONTH);
        mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

        //inits dialog popup
        DatePickerDialog mDatePicker=new DatePickerDialog(ModifyPage.this, new DatePickerDialog.OnDateSetListener() {
            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                editAirDate.setText(selectedday + "/" + (selectedmonth+1) + "/" + selectedyear); //sets edittext (userfriendly)
            }
        },mYear, mMonth, mDay);
        mDatePicker.setTitle("Select airdate");
        mDatePicker.show();  }

    private void scheduleNotification(Notification notification, long delay) {

        Intent notificationIntent = new Intent(this, Receiver.class); //creates the intent for the receiver class
        notificationIntent.putExtra(Receiver.NOTIFICATION, notification); //puts the actual notification

        notificationID = intNotificationIDprefix;
        System.out.println(notificationID);

        //creates the pending intent to delay the notification, stores the unique id (a - random int), the notification Intent, and itself
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, notificationID, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //the android alarm works of the systemclock elapsed time, the amount of millis since boot. the delay variable (generated when the save button is clicked) is added to allow the alarm to determine the system clock time to pend the alarm to
        long futureInMillis = SystemClock.elapsedRealtime() + delay;
        System.out.println("SYSTEM TIME WHEN ALARM FINISHES : " + futureInMillis);

        //sets the alarm to the correct time, and tells it to use the pendingIntent when awoken, which invokes the reciever class...
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, futureInMillis, pendingIntent);

    }

    private Notification getNotification(String content) {
        Notification.Builder builder = new Notification.Builder(this);
        builder.setContentTitle("Scheduled Notification");
        builder.setContentText(content);
        builder.setSmallIcon(R.drawable.logo);
        return builder.build();
    }

    public void finish(View view) throws Exception{

        id = intSelectedID;
        System.out.println("VARIABLE: id - " + id);
        addListenerOnButton();

        if (editName.getText().toString().equals("") || editSeason.getText().toString().equals("") || editNumberEpisodes.getText().toString().equals("") || editCurrentEpisode.getText().toString().equals("") || editAirTime.getText().toString().equals("") || editAirDate.getText().toString().equals("")) {
            showMessage("Error", "Please ensure all fields are correctly filled out.");
        } else {

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");
            String mm = editAirDate.getText().toString() + " " + editAirTime.getText().toString();
            System.out.println(mm);
            Date date = sdf.parse(mm);
            long lngCurrentTime = System.currentTimeMillis();
            System.out.println("CURRENT TIME " + lngCurrentTime);

            String meme = "";
            int intTotalEpisodes = Integer.parseInt(editNumberEpisodes.getText().toString()) - Integer.parseInt(editCurrentEpisode.getText().toString());
            System.out.println("INTTOTALEPISODES" + intTotalEpisodes);
            System.out.println("Date - Time in milliseconds : " + date.getTime());
            long longTimeUntil = date.getTime();

            long lngTimeUntilAir = longTimeUntil - lngCurrentTime;

            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            String strNotificationIDprefix;


            if (currentDate.after(date)) {
                showMessage("Error", "Please select a future date for scheduling TV Listings");
            } else {

                System.out.println("TIME UNTIL " + lngTimeUntilAir);

                if (radioSelected.getText().equals("Daily")) {

                    int f;
                    for (f = 0; f < (intTotalEpisodes + 1); f++) {
                        strNotificationIDprefix = "" + id + "00";
                        intNotificationIDprefix = Integer.parseInt(strNotificationIDprefix);
                        intNotificationIDprefix = intNotificationIDprefix + f;
                        scheduleNotification(getNotification(editName.getText().toString()), (lngTimeUntilAir + (86400000 * f)));
                        System.out.println("TIME UNTIL THE NOTIFICATION FOR DAY " + f + " : " + (lngTimeUntilAir + (86400000 * f)));
                    }

                    StringBuffer buffer2 = new StringBuffer();


                    Date date2 = sdf.parse(mm);
                    Date temp = date2;

                    buffer2.append(" ");

                    for (int fg = 0; fg < (intTotalEpisodes + 1); fg++) {

                        String newstring = new SimpleDateFormat("dd/MM/yy HH:mm").format(temp);

                        buffer2.append(fg + ": " + newstring + " ");

                        int noOfDays = 7; //i.e two weeks
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(temp);
                        calendar.add(Calendar.DAY_OF_YEAR, noOfDays);

                        Date nextDate = calendar.getTime();

                        temp = nextDate;
                    }

                    meme = buffer2.toString();
                    System.out.println(meme);
                }

                long lngFinalAirTime;
                long lngBigMillis;

                if (radioSelected.getText().equals("Weekly")) {
                    int f;
                    for (f = 0; f < (intTotalEpisodes + 1); f++) {
                        strNotificationIDprefix = "" + id + "00";
                        intNotificationIDprefix = Integer.parseInt(strNotificationIDprefix);
                        intNotificationIDprefix = intNotificationIDprefix + f;
                        lngFinalAirTime = lngTimeUntilAir;
                        lngBigMillis = Long.valueOf(86400000 * f);
                        System.out.println(lngBigMillis);
                        lngFinalAirTime = lngFinalAirTime + lngBigMillis;
                        scheduleNotification(getNotification(editName.getText().toString()), lngFinalAirTime);
                        System.out.println("TIME UNTIL THE NOTIFICATION FOR WEEK " + f + " : " + (lngFinalAirTime));
                        System.out.println(f);
                    }

                    StringBuffer buffer2 = new StringBuffer();


                    Date date2 = sdf.parse(mm);
                    Date temp = date2;
                    buffer2.append(" ");


                    for (int fg = 0; fg < (intTotalEpisodes + 1); fg++) {

                        String newstring = new SimpleDateFormat("dd/MM/yy HH:mm").format(temp);

                        buffer2.append(fg + ": " + newstring + " ");

                        int noOfDays = 7; //i.e two weeks
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(temp);
                        calendar.add(Calendar.DAY_OF_YEAR, noOfDays);

                        Date nextDate = calendar.getTime();

                        temp = nextDate;
                    }

                    meme = buffer2.toString();
                    System.out.println(meme);

                }


                Cursor c = db.rawQuery("SELECT * FROM Shows WHERE id='" + intSelectedID + "'", null);
                if (c.moveToFirst()) {

                    db.execSQL("UPDATE Shows SET name='"
                            + editName.getText()
                            + "',seasonno='"
                            + editSeason.getText()
                            + "',noepisodes='"
                            + editNumberEpisodes.getText()
                            + "',curepisode='"
                            + editCurrentEpisode.getText()
                            + "',airtime='"
                            + editAirTime.getText()
                            + "',airdate='"
                            + meme
                            + "',airfreq='"
                            + radioSelected.getText()
                            +
                            "' WHERE id='" + intSelectedID + "'");
                }

                StringBuffer buffer = new StringBuffer();
                Cursor d = db.rawQuery("SELECT * FROM Shows WHERE name='" + editName.getText() + "'", null);

                while (d.moveToNext()) {

                    buffer.append("ID: " + d.getString(0) + "\n");
                    buffer.append("Name: " + d.getString(1) + "\n");
                    buffer.append("Season Number: " + d.getString(2) + "\n");
                    buffer.append("Number of Episodes: " + d.getString(3) + "\n");
                    buffer.append("Current Episode: " + d.getString(4) + "\n");
                    buffer.append("Air Time: " + d.getString(5) + "\n");
                    buffer.append("Next Episode Airs: " + (d.getString(6).substring(4, 10) + "20" + d.getString(6).substring(10, 12) + "\n"));
                    buffer.append("Air Freq: " + d.getString(7) + "\n\n");

                    showMessage("Listing Details", buffer.toString());

                }

            }
        }
    }

}



