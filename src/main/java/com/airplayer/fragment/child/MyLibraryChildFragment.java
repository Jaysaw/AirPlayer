package com.airplayer.fragment.child;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airplayer.R;
import com.airplayer.fragment.singleitem.SettableFragmentView;

/**
 * Created by ZiyiTsang on 15/6/9.
 */
public abstract class MyLibraryChildFragment extends Fragment implements SettableFragmentView {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(getRootViewId(), container, false);

        //find a recycler view and set it up
        RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        setupRecyclerView(recyclerView);

        return rootView;
    }

    @Override
    public int getRootViewId() {
        return R.layout.fragment_recycler;
    }
}
