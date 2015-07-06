package com.airplayer.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.airplayer.R;
import com.airplayer.activity.AirMainActivity;
import com.airplayer.adapter.AirAdapter;
import com.airplayer.adapter.AirScrollListener;
import com.airplayer.adapter.AlbumAdapter;
import com.airplayer.fragment.singleItem.AlbumFragment;
import com.airplayer.model.Album;
import com.airplayer.util.QueryUtils;

import java.util.List;


/**
 * Created by ZiyiTsang on 15/6/4.
 */
public class PlayNowFragment extends Fragment {

    private List<Album> recentAlbumList;
    private RecyclerView mRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recentAlbumList = QueryUtils.loadRecentAlbum(getActivity());
        Toolbar globalBar = ((AirMainActivity) getActivity()).getToolbar();
        globalBar.setTranslationY(0);
        globalBar.setVisibility(View.VISIBLE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recycler, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        final GridLayoutManager manager = new GridLayoutManager(getActivity(), 2);
        manager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return position == 0 ? manager.getSpanCount() : 1;
            }
        });
        mRecyclerView.setLayoutManager(manager);
        PlayNowAdapter adapter = new PlayNowAdapter(getActivity(), recentAlbumList);
        adapter.setItemClickListener(new AirAdapter.ClickListener() {
            @Override
            public void itemClicked(View view, int position) {
                FragmentTransaction ft = getActivity()
                        .getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.fragment_container, AlbumFragment.newInstance(recentAlbumList.get(position - 1)));
                ft.addToBackStack(null);
                ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                ft.commit();
            }

            @Override
            public void headerClicked(View view) {

            }

            @Override
            public void footerClicked(View view) { }
        });
        mRecyclerView.setAdapter(adapter);

        final Toolbar toolbar = ((AirMainActivity) getActivity()).getToolbar();
        toolbar.setVisibility(View.VISIBLE);
        mRecyclerView.setOnScrollListener(new AirScrollListener(getResources().getInteger(R.integer.padding_action_bar)) {
            @Override
            public void onViewScrolled(int viewScrolledDistance) {
                toolbar.setTranslationY(-viewScrolledDistance);
            }

            @Override
            public void onHide() {
                toolbar.animate().translationY(toolbar.getHeight()).setInterpolator(new AccelerateInterpolator(1));
            }

            @Override
            public void onShow() {
                toolbar.animate().translationY(0).setInterpolator(new AccelerateInterpolator(1));
            }

            @Override
            public void onScrollBackToTop() { }
        });

        return rootView;
    }

    private class PlayNowAdapter extends AlbumAdapter {

        public PlayNowAdapter(Context context, List<Album> list) {
            super(context, list);
        }

        @Override
        public AirHeadViewHolder onCreateHeadViewHolder(ViewGroup parent) {
            return new PlayNowHeadViewHolder(getLayoutInflater().inflate(R.layout.recycler_header_image, parent, false));
        }

        @Override
        public void onBindHeadViewHolder(AirAdapter.AirHeadViewHolder holder) {
            PlayNowHeadViewHolder playNowHeadViewHolder = (PlayNowHeadViewHolder) holder;
            playNowHeadViewHolder.image.setMinimumHeight(getResources().getInteger(R.integer.padding_action_bar));
            playNowHeadViewHolder.title.setText("Recent Added");
            int numOfSongs = 0;
            for (int i = 0; i < getList().size(); i++) {
                    numOfSongs += ((Album) getList().get(i)).getSongsHave();
            }
            playNowHeadViewHolder.subTitle.setText(getList().size() + " albums " + numOfSongs +" songs");
            playNowHeadViewHolder.desc.setText("click to shuffle all recent added");
        }

        private class PlayNowHeadViewHolder extends AirAdapter.AirHeadViewHolder {

            ImageView image;
            TextView title;
            TextView subTitle;
            TextView desc;

            public PlayNowHeadViewHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView.findViewById(R.id.header_image);
                title = (TextView) itemView.findViewById(R.id.header_title);
                subTitle = (TextView) itemView.findViewById(R.id.header_sub_title);
                desc = (TextView) itemView.findViewById(R.id.header_desc);
            }
        }
    }
}
