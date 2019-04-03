package com.bradperkins.confinder.fragments;


import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Comments;
import com.bradperkins.confinder.utils.CommentsAdapter;

import java.util.ArrayList;


public class CommentsFragment extends ListFragment {

    private ArrayList<Comments> commentList;
    private static final String ARG_COMMENTS = "ARG_COMMENTS";
    private static final String ARG_STR = "ARG_STR";
    private static final String ARG_TITLE = "ARG_TITLE";

    private CommentListener mListener;

    public static CommentsAdapter adapter;

    public CommentsFragment() {}

    public static CommentsFragment newInstance(ArrayList<Comments> commentsList, String str, String conTitle) {
        Bundle args = new Bundle();
        args.putString(ARG_STR, str);
        args.putSerializable(ARG_COMMENTS, commentsList);
        args.putString(ARG_TITLE, conTitle);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        commentList = (ArrayList<Comments>) getArguments().getSerializable(ARG_COMMENTS);
        String argString = getArguments().getString(ARG_STR);
        String conTitle = getArguments().getString(ARG_TITLE);

        adapter = new CommentsAdapter(getActivity(), commentList, argString);

        getListView().setDivider(null);
        getListView().setDividerHeight(0);

        setListAdapter(adapter);
        final EditText commentET = getView().findViewById(R.id.comment_et);
        ImageView addBtn = getView().findViewById(R.id.add_btn);
        TextView headerTV = getView().findViewById(R.id.comments_header_tv);

        if (argString != "comments"){
            commentET.setVisibility(getView().INVISIBLE);
            addBtn.setVisibility(getView().INVISIBLE);
        }

        //Set Header
        if (argString == "comments"){
            headerTV.setText("Comments for\n" + conTitle);
        } else if (argString == "likes"){
            headerTV.setText("Users that Like\n" + conTitle);
        } else if (argString == "attending"){
            headerTV.setText("Users Attending\n" + conTitle);
        } else if (argString == "reports"){
            headerTV.setText("Reports");
        }else {
            headerTV.setText("");
        }


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.addComment(commentET.getText().toString().trim());
            }
        });

    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mListener.conPosition(position, commentList);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof CommentListener){
            mListener = (CommentListener) context;
        }
    }

    public interface CommentListener {
        void addComment(String comment);
        void conPosition(int pos, ArrayList<Comments> conList);

    }

}

