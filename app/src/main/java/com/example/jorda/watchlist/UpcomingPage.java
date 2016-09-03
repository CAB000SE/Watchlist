package com.example.jorda.watchlist;


import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class UpcomingPage extends AppCompatActivity {

    ListView lstMain;
    SQLiteDatabase sqlTVListings;
    ArrayList arrListString = new ArrayList();
    ArrayList arrListBitmap = new ArrayList();

    Date datMain;
    String strFileName = "";
    private Handler hanRefresh = new Handler();
    int intEpisodesRemaining;
    Cursor curAllListings;

    @Override
    protected void onCreate(Bundle savedInstanceState)  {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.upcomingpage);

        //creates the listview to be populated
        lstMain = (ListView) findViewById(R.id.listView);

        //opens the database

        sqlTVListings = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);

        //executes the main loop
        display();







    }

    public void display(){

        SharedPreferences pref = this.getSharedPreferences("Share", Context.MODE_PRIVATE);
        int intOrderFashion = pref.getInt("your key1", 0); //1 is default value.
        System.out.println(intOrderFashion);
        if(intOrderFashion==2){
            curAllListings = sqlTVListings.rawQuery("SELECT * FROM Shows ORDER BY name COLLATE NOCASE;", null);
        } else{
            curAllListings = sqlTVListings.rawQuery("SELECT * FROM Shows", null);
        }



        //buffers the SQL listing and invokes the showMessage void to display the listing details
        while (curAllListings.moveToNext()) {

            StringBuffer bufListings = new StringBuffer();

            intEpisodesRemaining = Integer.parseInt(curAllListings.getString(3)) - Integer.parseInt(curAllListings.getString(4));
            int intCurrentEpisode = 1;

            Calendar cal = Calendar.getInstance();
            Date datCurrent = cal.getTime();
            System.out.println(datCurrent);

            String strDateOfListing = "";
            boolean m = false;
            for(int f=0; !m ;f++){ //this for finds the next possible air datMain and time based off the substring.

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm"); //used later to convert to a readable format
                try{
                    strDateOfListing = curAllListings.getString(6).substring((f*18)+4,(f*18)+18); //f will increase when the previous episode has aired
                }catch(Exception e){
                    /*this catch is vital is recognising complete seasons. if the error for the index size being to small occurs,
                    we can assume that the season is complete as there is no remaining episodes. the variable is set to a
                    default datMain which is tested later to determine if the season is complete. m becomes true to break the for loop*/
                    strDateOfListing = "01/01/16 01:01";
                    m=true;
                }

                System.out.println(strDateOfListing);

                try { //used to convert the substring found above into a readable format for android
                    datMain = sdf.parse(strDateOfListing);
                    System.out.println("OTHER VARIABLE   " + datMain);
                } catch (Exception e) {
                    //catches any errors. highly unlikely in this try
                }

                if(datCurrent.before(datMain)){ //used to determine if the current datMain is before the next episode, if yes then it has found the correct episode and the for can break
                    intCurrentEpisode = f + 1; //sets the current number of episodes
                    m=true;
                }
            }


            //finds the time until the next episode airs
            long lngCurrentTime = System.currentTimeMillis();
            long lngTimeUntil = datMain.getTime();
            long lngTimeUntilAir = lngTimeUntil - lngCurrentTime;


            //simple maths to create seconds, minutes, hours, and days from the lngTimeUntilAir variable
            String strTime = "";
            long lngSeconds = lngTimeUntilAir / 1000;
            long lngMinutes = lngSeconds / 60;
            long lngHours = lngMinutes / 60;
            long lngDays = lngHours / 24;

            //used for handling the display of time, based on the remaining days
            if(lngDays==0){
                strTime =  lngHours % 24 + " hours, " + lngMinutes % 60 + " minutes";
            }else if(lngDays==1){
                strTime = lngDays + " day, " + lngHours % 24 + " hours, " + lngMinutes % 60 + " minutes" ;
            }else if(lngDays>1){
                strTime = lngDays + " days, " + lngHours % 24 + " hours, " + lngMinutes % 60 + " minutes" ;
            }
            //adds most of the information to the buffer
            bufListings.append("ID: " + curAllListings.getString(0) + "\n");
            bufListings.append("Name: " + curAllListings.getString(1) + "\n");
            //these 2 lines allow for a more tradition "season" x "episode" look
            bufListings.append("Episode: " + curAllListings.getString(2));
            bufListings.append("x" + curAllListings.getString(4) + "\n");

            //handles a complete season
            if(strDateOfListing.equals("01/01/16 01:01")){
                bufListings.append("SEASON COMPLETE!" + "\n\n");
            }else{ //if season is not complete
                bufListings.append("Time until episode airs: " + (strTime) + "\n\n");
            }


            //creates a possible search using the name of the listing ; replaces all spaces with a + to be used in the query
            String strPossibleSearch = curAllListings.getString(1).replaceAll("\\s+","+");
            System.out.println("POSSIBLE SEARCH : " + strPossibleSearch);


            //enables HTTP to be executed in main thread, suitable for this project
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

            try { //used to grab the poster

                //creates a JSON object that is reading the URL provided with the variable determined above
                JSONObject jsoAPISearch = new JSONObject(readUrl("http://www.omdbapi.com/?t=" + strPossibleSearch));

                //grabs both the Poster URL as well as the imdbID which is used as the strFileName (not required, but it works)
                String strPosterURL = (String) jsoAPISearch.get("Poster");
                strFileName = (String) jsoAPISearch.get("imdbID");

                //this print shows whether a url was found or not
                System.out.println(strPosterURL);

                //creates a new file with the imdb ID as a name.
                File filImage=new File("/storage/emulated/0/Watchlist_images/" + strFileName + ".jpg");

                if(filImage.exists() && ! filImage.isDirectory()){ //used to determine if the image already exists. it would keep downloading the same image hundreds of times if this wasnt here :/
                    //breaks if
                }else{

                    //uses the file_download to download using the imID string as a url
                    file_download(strPosterURL);

                    /*This try is required in some form as the file_download will be occur at the same time this thread
                    * attempts to continue. This can result on faster phones the listview populating TO quickly and returning
                    * null as the posters have not yet been downloaded. 1 second per poster should always be enough considering
                    * that the file is tiny, but may need to be adjusted / different method used for slower connections*/
                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }

                try {
                    //finds the previously downloaded file
                    File filPrevious=new File("/storage/emulated/0/Watchlist_images/" + strFileName + ".jpg");
                    System.out.println("/storage/emulated/0/Watchlist_images/" + strFileName + ".jpg");

                    //inflates the custom-made listview layout to this view
                    View view = LayoutInflater.from(getApplication()).inflate(R.layout.mylist, null, true);

                    //adds the file to the secondary String array. decoded in the custom list adapter, not here
                    arrListBitmap.add(filPrevious);

                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                arrListBitmap.add("N/A"); //this catch occurs when a poster was not downloaded for the listing. "N/A" will allow the CustomListAdapter to set it as the default poster.
            }

            //adds the information created way above to the primary array
            arrListString.add(bufListings);

            //updates the database based on the new data determined in the episode check way above.
            sqlTVListings.execSQL("UPDATE Shows SET curepisode='"
                        +intCurrentEpisode+
                        "' WHERE id='"+ curAllListings.getString(0)+"'");

        }

        //used to handle clicks on the listViews items
        lstMain.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){

                System.out.println(lstMain.getItemAtPosition(position));
                String strListingName = lstMain.getItemAtPosition(position).toString();

                //used to get the name of the listing.
                strListingName = strListingName.substring(strListingName.indexOf("Name: ") + 6);
                strListingName = strListingName.substring(0, strListingName.indexOf("Episode"));

                System.out.println(strListingName);

                //generates the possible search
                String strPossibleSearch = strListingName.replaceAll("\\s+","+");
                System.out.println(strPossibleSearch);

                //this try is for determining the imdb url and opening it in app.
                try {
                    //creates a jsonobject that reads the possible search with API
                    JSONObject jsoAPISearch = new JSONObject(readUrl("http://www.omdbapi.com/?t=" + strPossibleSearch));

                    String strImdbID = (String) jsoAPISearch.get("imdbID");
                    strFileName = (String) jsoAPISearch.get("imdbID");
                    System.out.println(strImdbID);

                    //immediately starts up a web browser that goes directly to the TV shows imdb page
                    Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + strImdbID + "/"));
                    startActivity(intentBrowser);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //adds the primary and secondary arrays to CustomListAdapter to be decoded
        CustomListAdapter adapter=new CustomListAdapter(this, arrListString, arrListBitmap);

        //finally sets the lstMain to the above adapter
        lstMain =(ListView)findViewById(R.id.listView);
        lstMain.setAdapter(adapter);


        //used to automatically refresh the display every minute.
        hanRefresh.postDelayed(new Runnable() {
            public void run() {
                lstMain.setAdapter(null);
                arrListString.clear();
                display();
            }
        }, 60000);
    }


    //the file downloader.
    public void file_download(String uRl) {
        //used for below to check if the directory exists
        File filDirectory = new File("/Watchlist_images");

        //creates the directory if it doesnt exist yet
        if (!filDirectory.exists()) {
            filDirectory.mkdirs();
        }

        //creates the download manage
        DownloadManager mgrMain = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        //parses the string from the secondary array to a downloadable URL
        Uri uriDownloadURL = Uri.parse(uRl);

        //requests the image to download
        DownloadManager.Request request = new DownloadManager.Request(
                uriDownloadURL);

        //allows the image to be downloaded over a wifi or mobile network but not on roaming. Sets to download to correct directory using correct name
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Watchlistdownload")
                .setDescription("Used for downloading posters for Watchlist")
                .setDestinationInExternalPublicDir("/Watchlist_images", strFileName + ".jpg");

        //downloads the image
        mgrMain.enqueue(request);
    }

    //the URL reader. used with the JSON parser exclusively.
    private static String readUrl(String urlString) throws Exception {

        BufferedReader burMain = null;

        //this try converts the url string created for the API to a readable format and adds it to a readable buffer that can be used with JSON commands to find metadata
        try {

            URL urlAPIListing = new URL(urlString);
            //reads the URL into the BufferedReader created above
            burMain = new BufferedReader(new InputStreamReader(urlAPIListing.openStream()));

            StringBuffer bufJSONPage = new StringBuffer();
            int intRead;
            //sets amount of chars that will be read at once in the while.
            char[] chars = new char[1024];

            //while more is to be read from the BufferedReader
            while ((intRead = burMain.read(chars)) != -1)
                bufJSONPage.append(chars, 0, intRead);

            //adds the characters read to the buffer
            return bufJSONPage.toString();
        } finally {
            //after the file has successfully read completely, exits the void.
            if (burMain != null)
                burMain.close();
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
