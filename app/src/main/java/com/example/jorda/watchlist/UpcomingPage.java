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

        listView = (ListView) findViewById(R.id.listView);
        db2 = openOrCreateDatabase("newTVDB", Context.MODE_PRIVATE, null);







        display();







    }

    public void display(){


        Cursor c = db2.rawQuery("SELECT * FROM Shows", null);

        //buffers the SQL listing and invokes the showMessage void to display the listing details
        while (c.moveToNext()) {

            StringBuffer buffer = new StringBuffer();

            intEpisodesRemaining = Integer.parseInt(c.getString(3)) - Integer.parseInt(c.getString(4));
            int intCurrentEpisode = 1;
            Calendar cal = Calendar.getInstance();
            Date currentDate = cal.getTime();

            System.out.println(currentDate);
            String mm = "";

            boolean m = false;
            for(int f=0; !m ;f++){

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yy hh:mm");
                try{
                    mm = c.getString(6).substring((f*18)+4,(f*18)+18);
                }catch(Exception e){
                    mm = "01/01/16 01:01";
                    m=true;
                }
                System.out.println(mm);
                try {
                    date = sdf.parse(mm);
                    System.out.println("OTHER VARIABLE   " + date);
                } catch (Exception e) {

                }

                if(currentDate.before(date)){
                    intCurrentEpisode = f + 1;
                    m=true;
                }
            }



            long lngCurrentTime = System.currentTimeMillis();
            long longTimeUntil = date.getTime();
            long lngTimeUntilAir = longTimeUntil - lngCurrentTime;


            String time = "";
            long seconds = lngTimeUntilAir / 1000;
            long minutes = seconds / 60;
            long hours = minutes / 60;
            long days = hours / 24;
            if(days==0){
                time =  hours % 24 + " hours, " + minutes % 60 + " minutes";

            }else if(days==1){
                time = days + " day, " + hours % 24 + " hours, " + minutes % 60 + " minutes" ;

            }else if(days>1){
                time = days + " days, " + hours % 24 + " hours, " + minutes % 60 + " minutes" ;

            }

            buffer.append("ID: " + c.getString(0) + "\n");
            buffer.append("Name: " + c.getString(1) + "\n");
            buffer.append("Episode: " + c.getString(2));
            buffer.append("x" + intCurrentEpisode + "\n");
            if(mm.equals("01/01/16 01:01")){
                buffer.append("SEASON COMPLETE!" + "\n\n");

            }else{
                buffer.append("Time until episode airs: " + (time) + "\n\n");

            }


            String possibleSearch = c.getString(1).replaceAll("\\s+","+");
            System.out.println("AYY POSSIBLE SEARCH : " + possibleSearch);



            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

            StrictMode.setThreadPolicy(policy);

            File storagePath = Environment.getExternalStorageDirectory();

            try {
                JSONObject json = new JSONObject(readUrl("http://www.omdbapi.com/?t=" + possibleSearch));

                String imID = (String) json.get("Poster");
                fileName = (String) json.get("imdbID");

                System.out.println(imID);

                File ef=new File("/storage/emulated/0/Watchlist_images/" + fileName + ".jpg");

                if(ef.exists() && ! ef.isDirectory()){

                }else{
                    file_download(imID);

                    try {
                        Thread.sleep(1000);
                    } catch(InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }


                }




                try {
                    File f=new File("/storage/emulated/0/Watchlist_images/" + fileName + ".jpg");
                    System.out.println("/storage/emulated/0/Watchlist_images/" + fileName + ".jpg");
                    Bitmap b = BitmapFactory.decodeStream(new FileInputStream(f));


                    View view = LayoutInflater.from(getApplication()).inflate(R.layout.mylist, null, true);


                    Drawable d = new BitmapDrawable(getResources(), b);

                    //ImageView img=(ImageView) view.findViewById(R.id.icon);
                    //img.setImageBitmap(b);
                    bl.add(f);




                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }




            } catch (Exception e) {
                e.printStackTrace();
                bl.add("N/A");
            }


            al.add(buffer);

            db2.execSQL("UPDATE Shows SET curepisode='"
                        +intCurrentEpisode+
                        "' WHERE id='"+c.getString(0)+"'");




        }


        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                al);





       // listView.setAdapter(new ArrayAdapter<String>(
          //      this, R.layout.mylist,
            //    R.id.Itemname,al));






        //listView.setAdapter(arrayAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView parent, View v, int position, long id){

                System.out.println(listView.getItemAtPosition(position));

                String s = listView.getItemAtPosition(position).toString();

                s = s.substring(s.indexOf("Name: ") + 6);
                s = s.substring(0, s.indexOf("Episode"));




                System.out.println(s);



                String possibleSearch = s.replaceAll("\\s+","+");
                System.out.println(possibleSearch);

                try {
                    JSONObject json = new JSONObject(readUrl("http://www.omdbapi.com/?t=" + possibleSearch));

                    String imID = (String) json.get("imdbID");
                    fileName = (String) json.get("imdbID");
                    System.out.println(imID);

                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.imdb.com/title/" + imID + "/"));
                    startActivity(browserIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }




            }
        });

        CustomListAdapter adapter=new CustomListAdapter(this, al, bl);
        listView=(ListView)findViewById(R.id.listView);
        listView.setAdapter(adapter);


        mHandler.postDelayed(new Runnable() {
            public void run() {


                listView.setAdapter(null);
                al.clear();
                display();



            }
        }, 60000);

    }

    public void file_download(String uRl) {
        File direct = new File("/Watchlist_images");

        if (!direct.exists()) {
            direct.mkdirs();
        }

        DownloadManager mgr = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);

        Uri downloadUri = Uri.parse(uRl);
        DownloadManager.Request request = new DownloadManager.Request(
                downloadUri);

        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI
                        | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(false).setTitle("Demo")
                .setDescription("Something useful. No, really.")
                .setDestinationInExternalPublicDir("/Watchlist_images", fileName + ".jpg");

        mgr.enqueue(request);

    }


    private static String readUrl(String urlString) throws Exception {
        BufferedReader reader = null;
        try {
            URL url = new URL(urlString);
            reader = new BufferedReader(new InputStreamReader(url.openStream()));
            StringBuffer buffer = new StringBuffer();
            int read;
            char[] chars = new char[1024];
            while ((read = reader.read(chars)) != -1)
                buffer.append(chars, 0, read);

            return buffer.toString();
        } finally {
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
