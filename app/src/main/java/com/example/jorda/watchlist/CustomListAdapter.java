package com.example.jorda.watchlist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
/*
The purpose of this class is to act as a custom listview for the UpcomingPage class.
 */
public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context; //gets the current activity this class is in (UpcomingPage)
    private final ArrayList arrItemName; //used for the listings INFORMATION
    private final ArrayList arrImageID; //used for storing the URL towards the listings POSTERS

    public CustomListAdapter(Activity context, ArrayList arrItemName, ArrayList arrImageID) { //gets the context and the 2 required arrays from Upcoming
        super(context, R.layout.mylist, arrItemName);

        this.context=context; //gets the variables from the context to be used in getView
        this.arrItemName = arrItemName;
        this.arrImageID = arrImageID;
    }

    public View getView(int position,View view,ViewGroup parent) {

        Bitmap bitPoster = null;
        if(arrImageID.get(position).equals("N/A")){ //test to see if the TV listing had a poster attached in the API

            bitPoster = BitmapFactory.decodeResource(context.getResources(),R.drawable.noposter); //if it doesnt, set as the default drawable

        }else{ //if it does have a valid poster downloaded


        File filImage = new File(arrImageID.get(position).toString()); //converts the string to a valid file
        try{
            bitPoster = BitmapFactory.decodeStream(new FileInputStream(filImage)); //attempts to decode it into a bitmap (this will only fail if permissions are not correct)
        }catch(Exception e){

        }
        }

        //allows this class to be within UpcomingPage
        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        //inits the text and image for each row (imageView will never actually become icon drawable, just placeholder)
        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imgView = (ImageView) rowView.findViewById(R.id.icon);

        //sets the relevant description and poster to the textview and imageview
        txtTitle.setText(arrItemName.get(position).toString());
        imgView.setImageBitmap(bitPoster);

        return rowView;

    }
}