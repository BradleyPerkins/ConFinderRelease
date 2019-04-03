package com.bradperkins.confinder.utils;

// Date 9/19/18
// Bradley Perkins
// AID - 1809
// CustomAdapter.Java

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Con;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class CustomAdapter extends BaseAdapter {

    private static final int ID_CONSTANT = 0x01010101;
    ImageView iv;

    private final Context context;
    private final ArrayList<Con> conList;

    public CustomAdapter(Context context, ArrayList<Con> conList) {
        this.context = context;
        this.conList = conList;
    }

    @Override
    public int getCount() {
        if (conList == null){
            return 0;
        }
        return conList.size();
    }

    @Override
    public Con getItem(int i) {
        if (conList != null && i < conList.size() && i >= 0){
            return conList.get(i);
        }
        return null;
    }

    @Override
    public long getItemId(int i) {
        return ID_CONSTANT + i;
    }

    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        if (view == null){
            view = LayoutInflater.from(context).inflate(R.layout.list_row, viewGroup, false);
        }

        Con con = getItem(i);

        if (con != null){
            TextView nameTV = view.findViewById(R.id.name_tv);
            nameTV.setText(con.getTitle());

            String date = DataHelper.dateFormatter(con.getId());

            TextView dateTV = view.findViewById(R.id.date_tv);
            dateTV.setText(date);

            String placeholder = "https://firebasestorage.googleapis.com/v0/b/comic-shop-finder.appspot.com/o/temp1.png?alt=media&token=323d2764-5606-4ca0-95b9-ad469e115f3e";
            iv = view.findViewById(R.id.con_iv);

            TextView distTV = view.findViewById(R.id.distance_tv);
            DecimalFormat df = new DecimalFormat("#.#");
            String dist = String.valueOf(df.format(con.getDistance()));

            distTV.setText(dist + " - Miles Away");

            String imageLocation = con.getImage();
            if (imageLocation == null || imageLocation == "" || imageLocation.isEmpty()){
                Picasso.with(context).load(placeholder).fit().into(iv);
            }else {
//                Picasso.with(context).load(imageLocation).into(iv);
                Glide.with(context).load(imageLocation).into(iv);
            }
        }

        return view;
    }

}
