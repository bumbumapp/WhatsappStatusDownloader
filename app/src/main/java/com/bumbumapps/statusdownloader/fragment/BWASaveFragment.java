package com.bumbumapps.statusdownloader.fragment;


import android.os.AsyncTask;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumbumapps.statusdownloader.adaptor.WASavedAdaptor;
import com.bumbumapps.statusdownloader.model.ModelStatus;
import com.bumbumapps.statusdownloader.R;
import com.bumbumapps.statusdownloader.utils.Config;
import com.bumbumapps.statusdownloader.utils.StatusListLoader;

import java.io.File;
import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BWASaveFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    ArrayList<ModelStatus> data;
    RecyclerView rv;
    TextView textView;
    SwipeRefreshLayout mSwipeRefreshLayout;


    public BWASaveFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_save_pictures, container, false);
        rv = view.findViewById(R.id.rv_status);
        mSwipeRefreshLayout = view.findViewById(R.id.contentView);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        textView = view.findViewById(R.id.textView);

        rv.setHasFixedSize(false); // wrap_content RecyclerView must re-measure


        loadData();

        return view;
    }

    public void loadData() {
        // Scoped storage: SAF folder grant on Android 11+, MediaStore for saved files (see StatusStorage).
        StatusListLoader.load(this, Config.WhatsAppBusinessSaveStatus, new String[]{".jpg", ".gif", ".mp4"}, 2, true, textView, rv,
                list -> new WASavedAdaptor(getActivity(), list));
        refreshItems();
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    public void refreshItems() {
        onItemsLoadComplete();
    }

    public void onItemsLoadComplete() {
        mSwipeRefreshLayout.setRefreshing(false);
    }


}
