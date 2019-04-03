package com.bradperkins.confinder.fragments;

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import com.bradperkins.confinder.R;
import com.bradperkins.confinder.objects.Con;
import com.bradperkins.confinder.utils.CustomAdapter;
import com.bradperkins.confinder.utils.DataCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FavoriteFragment extends ListFragment {

    private FaveListClickListener mListener;

    private ArrayList<Con> favList;
    public CustomAdapter adapter;

    public FavoriteFragment() {
    }

    public static FavoriteFragment newInstance() {
        return new FavoriteFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_sort_date:
                Toast.makeText(getActivity(), "Date", Toast.LENGTH_SHORT).show();
                boolean sortDist = false;
                boolean sortDate = true;
                Comparator comp1 = new Comparator<Con>() {
                    public int compare(Con o1, Con o2) {
                        return (int) (o1.getId() - o2.getId());
                    }
                };
                if (favList != null) {
                    Collections.sort(favList, comp1);
                    adapter.notifyDataSetChanged();
                }

                break;
            case R.id.action_sort_distance:
                Toast.makeText(getActivity(), "distance", Toast.LENGTH_SHORT).show();
                sortDist = true;
                sortDate = false;
                Comparator comp2 = new Comparator<Con>() {
                    public int compare(Con o1, Con o2) {
                        return (int) (o1.getDistance() - o2.getDistance());
                    }
                };

                if (favList != null) {
                    Collections.sort(favList, comp2);
                    adapter.notifyDataSetChanged();
                }
                break;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_favorite, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        favList = DataCache.loadFavConData(getActivity());
        adapter = new CustomAdapter(getActivity(), favList);
        setListAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        favList = DataCache.loadFavConData(getContext());
        adapter = new CustomAdapter(getActivity(), favList);
        setListAdapter(adapter);
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mListener.favPos(position, favList);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FaveListClickListener){
            mListener = (FaveListClickListener) context;
        }
    }

    public interface FaveListClickListener{
        void favPos(int pos, ArrayList<Con> favList);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


}
