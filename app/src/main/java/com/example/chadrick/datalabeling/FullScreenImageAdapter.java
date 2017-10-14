package com.example.chadrick.datalabeling;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by chadrick on 17. 10. 12.
 */

public class FullScreenImageAdapter extends PagerAdapter {
    private Context context;

    private ArrayList<File> imagefiles;
    private CustomViewPager customViewPager;

    public FullScreenImageAdapter(Context context, ArrayList<File> imagefiles, CustomViewPager customViewPager){
        this.context = context;
        this.imagefiles = imagefiles;
        this.customViewPager = customViewPager;

    }

    @Override
    public int getCount(){
        return this.imagefiles.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((RelativeLayout) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
//        ImageView imgDisplay;
        TouchImageView touchimageview;


        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.layout_fullscreen_image, container,
                false);

        //imgDisplay = (ImageView) viewLayout.findViewById(R.id.imgDisplay);
        touchimageview = (TouchImageView) viewLayout.findViewById(R.id.touchimageview);
        touchimageview.setCustomViewPager(customViewPager);


        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(imagefiles.get(position).toString(), options);
//        imgDisplay.setImageBitmap(bitmap);
        touchimageview.setImageBitmap(bitmap);


        ((ViewPager) container).addView(viewLayout);


        return viewLayout;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);
    }

}
