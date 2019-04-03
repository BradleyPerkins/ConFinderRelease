package com.bradperkins.confinder.utils;

// Date 12/6/18
// 
// Bradley Perkins

// AID - 1809

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Comments;

import java.util.ArrayList;

// PerkinsBradley_CE
public class CommentsAdapter  extends BaseAdapter {

    private static final int ID_CONSTANT = 0x01010101;

    private final Context context;
    private final ArrayList<Comments> commentList;
    private String styleString;

    public CommentsAdapter(Context context, ArrayList<Comments> commentList, String argStr) {
        this.context = context;
        this.commentList = commentList;
        this.styleString = argStr;
    }

    @Override
    public int getCount() {
        if (commentList == null){
            return 0;
        }

        return commentList.size();
    }

    @Override
    public Comments getItem(int i) {
        if (commentList != null && i < commentList.size() && i >= 0){
            return commentList.get(i);
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
            view = LayoutInflater.from(context).inflate(R.layout.comment_row, viewGroup, false);
        }

        Comments comment = getItem(i);

        if (comment != null){

            if (styleString == "likes" || styleString == "attending" ){
                TextView commentTV = view.findViewById(R.id.comment_tv);
                commentTV.setVisibility(View.GONE);
                TextView usernameTV = view.findViewById(R.id.com_username_tv);
                usernameTV.setText(commentList.get(i).getUsername());
                TextView timeTV = view.findViewById(R.id.com_time_tv);
                timeTV.setVisibility(View.GONE);
            } else {
                TextView commentTV = view.findViewById(R.id.comment_tv);
                commentTV.setText(commentList.get(i).getMessage());
                TextView usernameTV = view.findViewById(R.id.com_username_tv);
                usernameTV.setText(commentList.get(i).getUsername());
                TextView timeTV = view.findViewById(R.id.com_time_tv);
                timeTV.setText("");
            }
        }

        return view;
    }







}
