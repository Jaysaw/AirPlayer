package com.airplayer.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airplayer.R;
import com.airplayer.database.AirPlayerDB;
import com.airplayer.model.Music;
import com.airplayer.util.AirAdapter;

import java.util.List;

/**
 * Created by ZiyiTsang on 15/6/5.
 */
public class ArtistActivity extends Activity {

    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    private List<Music> mList;
    private AirPlayerDB db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artist);

        Intent intent = getIntent();
        String artistName = intent.getStringExtra("artist_name");

        TextView artistNameTextView = (TextView) findViewById(R.id.activity_artist_artist_name);
        artistNameTextView.setText(artistName);

        db = AirPlayerDB.newInstance(this, 0);
        mList = db.loadList(
                new String[]{AirPlayerDB.ALBUM, AirPlayerDB.ALBUM_ART},
                AirPlayerDB.ARTIST + " = ?",
                new String[]{artistName},
                1
        );

        mImageView = (ImageView) findViewById(R.id.activity_artist_image);

        mRecyclerView = (RecyclerView) findViewById(R.id.artist_recycler_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        mRecyclerView.setAdapter(new ArtistActivityAdapter(this, mList));
    }

    public class ArtistActivityAdapter extends AirAdapter {

        public ArtistActivityAdapter(Context context, List<Music> list) {
            super(context, list);
        }

        @Override
        public View getView(LayoutInflater layoutInflater, ViewGroup viewGroup) {
            return layoutInflater.inflate(R.layout.recycler_album_item, viewGroup, false);
        }

        @Override
        public String getText(int position) {
            return mList.get(position).getAlbum();
        }

        @Override
        public String getImagePath(int position) {
            return mList.get(position).getAlbumArt();
        }

        @Override
        public int getTextViewId() {
            return R.id.album_title;
        }

        @Override
        public int getImageViewId() {
            return R.id.album_art;
        }

        @Override
        public void onItemClick(int position) {

        }
    }
}
