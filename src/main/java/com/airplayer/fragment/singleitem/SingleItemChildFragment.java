package com.airplayer.fragment.singleitem;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.airplayer.R;
import com.airplayer.activity.AirMainActivity;
import com.airplayer.activity.fetchpicture.FetchAlbumArtActivity;
import com.airplayer.activity.fetchpicture.FetchArtistPictureActivity;
import com.airplayer.activity.fetchpicture.FetchPictureActivity;
import com.airplayer.fragment.dialog.MenuDialogFragment;
import com.airplayer.listener.AirScrollListener;
import com.airplayer.fragment.singleitem.itf.SettableRecyclerView;
import com.airplayer.listener.SimpleAirScrollListener;
import com.airplayer.model.Album;
import com.airplayer.model.PictureGettable;

import java.io.File;

/**
 * Created by ZiyiTsang on 15/6/14.
 */
public abstract class SingleItemChildFragment extends Fragment implements SettableRecyclerView {

    private static final String TAG = SingleItemChildFragment.class.getSimpleName();

    private RecyclerView mRecyclerView;

    private FragmentManager mFragmentManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((AirMainActivity) getActivity()).getToolbar().setVisibility(View.INVISIBLE);
        mFragmentManager = getActivity().getSupportFragmentManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_suppressible_toolbar, container, false);

        final Toolbar toolbar = (Toolbar) rootView.findViewById(R.id.suppressible_toolbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recycler_view);
        this.setupRecyclerView(mRecyclerView);
        AirScrollListener listener = new SimpleAirScrollListener(getResources().getInteger(R.integer.padding_action_bar), toolbar) {

            @Override
            public void onHide() {
                super.onHide();
                toolbar.setBackgroundColor(getResources().getColor(R.color.air_dark_primary_color));
            }

            @Override
            public void onShow() {
                super.onShow();
                toolbar.setBackgroundColor(getResources().getColor(R.color.air_dark_primary_color));
            }

            @Override
            public void onViewScrolled(int viewScrolledDistance) {
                super.onViewScrolled(viewScrolledDistance);
                if (viewScrolledDistance >= viewHeight) {
                    toolbar.setBackgroundColor(getResources().getColor(R.color.air_dark_primary_color));
                }
            }

            @Override
            public void onScrollToTop() {
                toolbar.setBackgroundColor(getResources().getColor(android.R.color.transparent));
            }
        };
        listener.setHaveParallax(true);
        mRecyclerView.setOnScrollListener(listener);


        return rootView;
    }

    /* package */ abstract class OnPictureClickListener implements View.OnClickListener {

        private PictureGettable item;
        private Class<?> aClass;

        public OnPictureClickListener(PictureGettable item, Class<?> aClass) {
            this.item = item;
            this.aClass = aClass;
        }

        @Override
        public void onClick(View v) {
            MenuDialogFragment dialog = new MenuDialogFragment() {
                @Override
                public void onFetchButtonClick(View v) {
                    Intent intent = new Intent(getActivity(), aClass);
                    intent.putExtra(FetchPictureActivity.EXTRA_QUERY_KEYWORD, item.getQueryKeyword());
                    intent.putExtra(FetchPictureActivity.EXTRA_SAVE_NAME, item.getSaveName());
                    getActivity().startActivityForResult(intent, PictureGettable.REQUEST_CODE_FETCH_PICTURE);
                }

                @Override
                public void onDeleteButtonClick(View v) {
                    File file = new File(item.getPicturePath());
                    if (file.exists()) {
                        file.delete();
                        onPictureDelete();
                        Toast.makeText(getActivity(), R.string.toast_picture_delete, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), R.string.toast_picture_not_found, Toast.LENGTH_SHORT).show();
                    }
                }
            };
            dialog.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
            dialog.show(mFragmentManager, null);
        }

        public abstract void onPictureDelete();
    }
}
