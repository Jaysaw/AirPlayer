package com.airplayer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airplayer.R;
import com.airplayer.model.Album;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by ZiyiTsang on 15/6/11.
 */


public class AlbumAdapter extends AirAdapter<Album> {

    public AlbumAdapter(Context context, List<Album> list) {
        super(context, list);
    }

    @Override
    public AirHeadViewHolder onCreateHeadViewHolder(ViewGroup parent) {
        return new AlbumHeaderViewHolder(getLayoutInflater()
                .inflate(R.layout.recycler_header_text, parent, false));
    }

    @Override
    public AirItemViewHolder onCreateItemViewHolder(ViewGroup parent) {
        return new AlbumItemViewHolder(getLayoutInflater()
                .inflate(R.layout.recycler_item_album, parent, false));
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int i) {

        if (holder instanceof AlbumItemViewHolder) {
            AlbumItemViewHolder albumViewHolder = (AlbumItemViewHolder) holder;
            albumViewHolder.textView.setText(getList().get(i - 1).getTitle());
            albumViewHolder.artistText.setText(getList().get(i - 1).getAlbumArtist());

            Picasso.with(getContext()).load(getList().get(i - 1).getAlbumArtUri())
                    .into(albumViewHolder.imageView);
        }

        if (holder instanceof AlbumHeaderViewHolder) {

        }
    }

    public class AlbumItemViewHolder extends AirItemViewHolder {
        ImageView imageView;
        TextView textView;
        TextView artistText;

        public AlbumItemViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView) itemView.findViewById(R.id.album_art);
            textView = (TextView) itemView.findViewById(R.id.album_title);
            artistText = (TextView) itemView.findViewById(R.id.album_artist_name);
        }
    }

    public class AlbumHeaderViewHolder extends AirHeadViewHolder {

        public AlbumHeaderViewHolder(View itemView) {
            super(itemView);
        }
    }

}