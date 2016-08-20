package com.example.jorda.watchlist;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class CustomListAdapter extends ArrayAdapter<String> {

    private final Activity context;
    private final ArrayList itemname;
    private final ArrayList imgid;

    public CustomListAdapter(Activity context, ArrayList itemname, ArrayList imgid) {
        super(context, R.layout.mylist, itemname);
        // TODO Auto-generated constructor stub

        this.context=context;
        this.itemname=itemname;
        this.imgid=imgid;
    }

    public View getView(int position,View view,ViewGroup parent) {

        Bitmap b = null;
        if(imgid.get(position).equals("N/A")){


            b = BitmapFactory.decodeResource(context.getResources(),R.drawable.noposter);



        }else{


        File f = new File(imgid.get(position).toString());
        try{
            b = BitmapFactory.decodeStream(new FileInputStream(f));
        }catch(Exception e){

        }
        }

        LayoutInflater inflater=context.getLayoutInflater();
        View rowView=inflater.inflate(R.layout.mylist, null,true);

        TextView txtTitle = (TextView) rowView.findViewById(R.id.item);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

        txtTitle.setText(itemname.get(position).toString());
        imageView.setImageBitmap(b);
        return rowView;

    }
}