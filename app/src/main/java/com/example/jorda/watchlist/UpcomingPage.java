package com.example.jorda.watchlist;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class UpcomingPage extends AppCompatActivity {

    ListView listView;
    SQLiteDatabase db2;
    ArrayList al = new ArrayList();
    ArrayList bl = new ArrayList();

    Date date;
    String fileName = "";
    private Handler mHandler = new Handler();
    int intEpisodesRemaining;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upcoming_page);

        //creates the listview to be populated
        listView = (ListView) findViewById(R.id.listView);

        //opens the database

        db2 = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);

        //executes the main loop
        display();







    }

    public void display(){

        Cursor c = db2.rawQuery("SELECT * FROM Shows", null);

        //buffers the SQL listing and invokes the showMessage void to display the listing details
        while (c.moveToNext()) {

            StringBuffer buffer = new StringBuffer();

            //TODO: determine if the intEpisodesRemaining variable is useless
            intEpisodesRemaining = Integer.parseInt(c.getString(3)) - Integer.parseInt(c.getString(4));
            int intCurrentEpisode = 1;

            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();
            System.out.println(currentDate);

            String mm = "";
            boolean m = false;
            for(int f=0; !m ;f++){ //this for finds the next possible air date and time based off the substring.

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm"); //used later to convert to a readable format
                try{
                    mm = c.getString(6).substring((f*18)+4,(f*18)+18); //f will increase when the previous episode has aired
                }catch(Exception e){
                    /*this catch is vital is recognising complete seasons. if the error for the index size being to small occurs,
                    we can assume that the season is complete as there is no remaining episodes. the variable is set to a
                    default date which is tested later to determine if the season is complete. m becomes true to break the for loop*/
                    mm = "01/01/16 01:01";
                    m=true;
                }

                System.out.println(mm);

                try { //used to convert the substring found above into a readable format for android
                    date = sdf.parse(mm);
                    System.out.println("OTHER VARIABLE   " + date);
                } catch (Exception e) {
                    //catches any errors. highly unlikely in this try
                }

                if(currentDate.before(date)){ //used to determine if the current date is before the next episode, if yes then it has found the correct episode and the for can break
                   //TODO: ensure the variable below can handle a listing edit correctly
                    intCurrentEpisode = f + 1; //sets the current number of episodes
                    m=true;
                }
            }


            //finds the time until the next episode airs
            long lngCurrentTime = System.currentTimeMillis();
            long longTimeUntil = date.getTime();
            long lngTimeUntilAir = longTimeUntil - lngCurrentTime;


            //simple maths to create seconds, minutes, hours, and days from the lngTimeUntilAir variable
            String time = "";
            long seconds = lngTimeUntilAir / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;

            //used for handling the display of time, based on the remaining days
            if(days==0){
                time =  hours % 24 + " hours, " + minutes % 60 + " minutes";
            }else if(days==1){
                time = days + " day, " + hours % 24 + " hours, " + minutes % 60 + " minutes" ;
            }else if(days>1){
                time = days + " days, " + hours % 24 + " hours, " + minutes % 60 + " minutes" ;
            }
            //adds most of the information to the buffer
            buffer.append("ID: " + c.getString(0) + "\n");
            buffer.append("Name: " + c.getString(1) + "\n");
            //these 2 lines allow for a more tradition "season" x "episode" look
            buffer.append("Episode: " + c.getString(2));
            buffer.append("x" + intCurrentEpisode + "\n");

            //handles a complete season
            if(mm.equals("01/01/16 01:01")){
                buffer.append("SEASON COMPLETE!" + "\n\n");
            }else{ //if season is not complete
                buffer.append("Time until episode airs: " + (time) + "\n\n");
            }


            //creates a possible search using the name of the listing ; replaces all spaces with a + to be used in the query
            String possibleSearch = c.getString(1).replaceAll("\\s+","+");
            System.out.println("POSSIBLE SEARCH : " + possibleSearch);


            //enables HTTP to be executed in main thread, suitable for this project
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try { //used to grab the poster

                //creates a JSON object that is reading the URL provided with the variable determined above
                JSONObject json = new JSONObject(readUrl("http://www.omdbapi.com/?t=" + possibleSearch));

                //grabs both the Poster URL as well as the imdbID which is used as the fileName (not required, but it works)
                //TODO: change below variables name to something more appropriate
                String imID = (String) json.get("Poster");
                fileName = (String) json.get("imdbID");

                //this print shows whether a url was found or not
                System.out.println(imID);

                //creates a new file with the imdb ID as a name.
                File ef=new File("/storage/emulated/0/Watchlist_images/" + fileName + ".jpg");

                if(ef.exists() && ! ef.isDirectory()){ //used to determine if the image already exists. it would keep downloading the same image hundreds of times if this wasnt here :/
                    //breaks if
                }else{

                    //uses the file_download to download using the imID string as a url
                    file_download(imID);

                    /*This try is required in some form as the file_download will be occur at the same time this thread
                    * attempts to continue. This can result on faster phones the listview populating TO quickly and returning
                    * null as the posters have not yet been downloaded. 1 second per poster should always be enough considering
                    * that the file is tiny, but may need to be adjusted / different method used for slower connections*/
                    try {
                        //TODO: find better method
                        Thread.sleep(1000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }

                try {
                    //finds the previously downloaded file
                    File f=new File("/storage/emulated/0/Watchlist_images/" + fileName + ".jpg");
                    System.out.println("/storage/emulated/0/Watchlist_images/" + fileName + ".jpg");

                    //inflates the custom-made listview layout to this view
                    View view = LayoutInflater.from(getApplication()).inflate(R.layout.mylist, null, true);

                    //adds the file to the secondary String array. decoded in the custom list adapter, not here
                    bl.add(f);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                bl.add("N/A"); //this catch occurs when a poster was not downloaded for the listing. "N/A" will allow the CustomListAdapter to set it as the default poster.
            }

            //adds the information created way above to the primary array
            al.add(buffer);

            //updates the database based on the new data determined in the episode check way above.
            db2.execSQL("UPDATE Shows SET curepisode='"
                        +intCurrentEpisode+
                        "' WHERE id='"+c.getString(0)+"'");

        }

        //used to handle clicks on the listViews items
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){

                System.out.println(listView.getItemAtPosition(position));
                String s = listView.getItemAtPosition(position).toString();

                //used to get the name of the listing.
                //TODO: why is the below code done the hard way?
                s = s.substring(s.indexOf("Name: ") + 6);
                s = s.substring(0, s.indexOf("Episode"));

                System.out.println(s);

                //generates the possible search
                String possibleSearch = s.replaceAll("\\s+","+");
                System.out.println(possibleSearch);

                //this try is for determining the imdb url and opening it in app.
                try {
                    //creates a jsonobject that reads the possible search with API
                    JSONObject json = new JSONObject(readUrl("http://www.omdbapi.com/?t=" + possibleSearch));

                    String imID = (String) json.get("imdbID");
                    fileName = (String) json.get("imdbID");
                    System.out.println(imID);

                    //immediately starts up a web browser that goes directly to the TV shows imdb page
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + imID + "/"));
                    startActivity(browserIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //adds the primary and secondary arrays to CustomListAdapter to be decoded
        CustomListAdapter adapter=new CustomListAdapter(this, al, bl);

        //finally sets the listView to the above adapter
        listView=(ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);


        //used to automatically refresh the display every minute.
        //TODO: determine if the below is really necessary
        mHandler.postDelayed(new Runnable() {
            public void run() {
                listView.setAdapter(null);
                al.clear();
                display();
            }
        }, 60000);
    }


    //the file downloader.
    public void file_download(String uRl) {
        //used for below to check if the directory exists
        File direct = new File("/Watchlist_images");

        //creates the directory if it doesnt exist yet
        if (!direct.exists()) {
            direct.mkdirs();
        }

        //creates the download manage
        DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        //parses the string from the secondary array to a downloadable URL
        Uri downloadUri = Uri.parse(uRl);

        //requests the image to download
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        //allows the image to be downloaded over a wifi or mobile network but not on roaming. Sets to download to correct directory using correct name
        //TODO: change below strings
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir("/Watchlist_images", fileName + ".jpg");

        //downloads the image
        mgr.enqueue(request);
    }

    //the URL reader. used with the JSON parser exclusively.
    private static String readUrl(String urlString) throws Exception {

        BufferedReader reader = null;

        //this try converts the url string created for the API to a readable format and adds it to a readable buffer that can be used with JSON commands to find metadata
        try {

            URL url = new URL(urlString);
            //reads the URL into the BufferedReader created above
            reader = new BufferedReader(new InputStreamReader(url.openStream()));

            StringBuffer buffer = new StringBuffer();
            int read;
            //sets amount of chars that will be read at once in the while.
            char[] chars = new char[1024];

            //while more is to be read from the BufferedReader
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            //adds the characters read to the buffer
            return buffer.toString();
        } finally {
            //after the file has successfully read completely, exits the void.
            if (reader != null)
                reader.close();
        }
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
