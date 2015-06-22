package com.airplayer.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.airplayer.R;
import com.airplayer.activity.AirMainActivity;
import com.airplayer.model.Song;
import com.airplayer.service.PlayMusicService;
import com.airplayer.util.ImageUtils;
import com.airplayer.util.QueryUtils;

/**
 * Created by ZiyiTsang on 15/6/10.
 */
public class PlayMusicFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "PlayMusicFragment";

    public static final int HANDLE_SEEK_BAR_PROGRESS = 0;

    private PlayMusicService.PlayerControlBinder mBinder;

    private Song mSong;

    // head action bar section
    private Toolbar mTopToolBar;

    public Toolbar getSlidingUpPanelTopBar() {
        return mTopToolBar;
    }

    private ImageView mTitleImageView;
    private TextView mTitleTextView;
    private TextView mArtistTextView;

    // center contain section
    private TextView mPlayingTimeTextView;
    private TextView mDurationTextView;
    private ImageView mCenterAlbumArt;

    // bottom control bar section
    private SeekBar mSeekBar;
    private ImageView mSwitchPlayMode;
    private ImageView mPreviousButton;
    private ImageView mPlayAndPauseButton;
    private ImageView mNextButton;
    private ImageView mShuffleList;


    private boolean shuffle = false;

    private boolean isPlayListShow = false;

    public boolean isPlayListShow() {
        return isPlayListShow;
    }

    public void setIsPlayListShow(boolean isPlayListShow) {
        this.isPlayListShow = isPlayListShow;
    }


    private int mLoopMode = 0;

    private GetSongReceiver receiver;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case HANDLE_SEEK_BAR_PROGRESS:
                    mSeekBar.setProgress(Math.round((float) msg.obj));
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinder = ((AirMainActivity) getActivity()).getPlayerControlBinder();
        receiver = new GetSongReceiver();
        IntentFilter filter = new IntentFilter(PlayMusicService.PLAY_STATE_CHANGE);
        getActivity().registerReceiver(receiver, filter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (mBinder.isPlaying()) {
                        if (!mBinder.isPause()) {
                            Message msg = new Message();
                            msg.what = HANDLE_SEEK_BAR_PROGRESS;
                            msg.obj = mBinder.getProgress() * 1000;
                            handler.sendMessage(msg);
                        }
                        sleepThread();
                    }
                }
            }

            private void sleepThread() {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Fail to sleep thread " + e);
                }
            }
        }).start();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_sliding_up_panel, container, false);

        // head tool bar section
        mTopToolBar = (Toolbar) rootView.findViewById(R.id.sliding_layout_top_tool_bar);
        mTopToolBar.inflateMenu(R.menu.menu_sliding_panel_down_menu);
        mTopToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch(item.getItemId()) {
                    case R.id.action_sliding_panel_top_list:
                        PlayListFragment fragment = new PlayListFragment();
                        FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                        if (isPlayListShow) {
                            getActivity().onBackPressed();
                        } else {
                            ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                            ft.addToBackStack(null);
                            ft.replace(R.id.sliding_panel_inside_fragment_container, fragment).commit();
                            isPlayListShow = !isPlayListShow;
                        }
                        break;
                    case R.id.action_sliding_panel_top_play:
                        playButtonAction();
                }
                return false;
            }
        });
        mTitleImageView = (ImageView) rootView.findViewById(R.id.sliding_layout_title_image);
        mTitleTextView = (TextView) rootView.findViewById(R.id.sliding_layout_title_song_title);
        mArtistTextView = (TextView) rootView.findViewById(R.id.sliding_layout_title_artist_name);

        // center contain section
        mCenterAlbumArt = (ImageView) rootView.findViewById(R.id.sliding_panel_center_album_art);
        mCenterAlbumArt.setImageResource(R.drawable.ic_default_art);

        // foot player control section
        mSeekBar = (SeekBar) rootView.findViewById(R.id.sliding_layout_bottom_seek_bar);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    int maxProgress = seekBar.getMax();
                    double percentOfBar = ((double) progress) / ((double) maxProgress);
                    Log.d(TAG, progress + " / " + maxProgress + " = " + percentOfBar);
                    mBinder.setProgress(percentOfBar);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mSwitchPlayMode = (ImageView) rootView.findViewById(R.id.sliding_layout_switch_mode);
        mSwitchPlayMode.setOnClickListener(this);

        mPreviousButton = (ImageView) rootView.findViewById(R.id.sliding_layout_previous_button);
        mPreviousButton.setOnClickListener(this);

        mPlayAndPauseButton = (ImageView) rootView.findViewById(R.id.sliding_layout_play_and_pause_button);
        mPlayAndPauseButton.setOnClickListener(this);

        mNextButton = (ImageView) rootView.findViewById(R.id.sliding_layout_next_button);
        mNextButton.setOnClickListener(this);

        mShuffleList = (ImageView) rootView.findViewById(R.id.sliding_layout_shuffle_play);
        mShuffleList.setOnClickListener(this);

        if (mBinder.isPlaying()) {
            updateUI();
        }

        return rootView;
    }

    @Override
    public void onDestroy() {
        getActivity().unregisterReceiver(receiver);
        super.onDestroy();
    }

    public class GetSongReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sliding_layout_switch_mode:
                switchLoopMode();
                mBinder.setPlayMode(mLoopMode);
                if (mLoopMode == PlayMusicService.SINGLE_SONG_LOOP_MODE) {
                    mSwitchPlayMode.setImageResource(R.drawable.btn_repeat_one);
                } else if (mLoopMode == PlayMusicService.LOOP_LIST_MODE) {
                    mSwitchPlayMode.setImageResource(R.drawable.btn_repeat_list);
                } else {
                    mSwitchPlayMode.setImageResource(R.drawable.btn_repeat_off);
                }
                break;
            case R.id.sliding_layout_previous_button:
                mBinder.previousMusic();
                break;
            case R.id.sliding_layout_play_and_pause_button:
                playButtonAction();
                break;
            case R.id.sliding_layout_next_button:
                mBinder.nextMusic();
                break;
            case R.id.sliding_layout_shuffle_play:
                shuffle = !shuffle;
                mBinder.setShuffle(shuffle);
                mShuffleList.setImageResource(shuffle ?
                        R.drawable.btn_shuffle_on : R.drawable.btn_shuffle_off);
                break;
        }
    }

    private void updateUI() {
        mSong = mBinder.getSongPlaying();
        Bitmap nowPlaySongArt = ImageUtils.getListItemThumbnail(
                getActivity(), QueryUtils.getAlbumArtPath(getActivity(), mSong.getAlbum()));

        // head tool bar section
        mTitleImageView.setImageBitmap(nowPlaySongArt);
        mTitleTextView.setText(mSong.getTitle());
        mArtistTextView.setText(mSong.getArtist());

        // background and center image section
        mCenterAlbumArt.setImageBitmap(nowPlaySongArt);

        // foot player control section
        if (mBinder.getPlayMode() == PlayMusicService.SINGLE_SONG_LOOP_MODE) {
            mSwitchPlayMode.setImageResource(R.drawable.btn_repeat_one);
        } else if (mBinder.getPlayMode() == PlayMusicService.LOOP_LIST_MODE) {
            mSwitchPlayMode.setImageResource(R.drawable.btn_repeat_list);
        } else {
            mSwitchPlayMode.setImageResource(R.drawable.btn_repeat_off);
        }
        mPlayAndPauseButton.setImageResource(mBinder.isPause() ?
                R.drawable.btn_play : R.drawable.btn_pause);
        mShuffleList.setImageResource((mBinder.isShuffle() ?
                R.drawable.btn_shuffle_on : R.drawable.btn_shuffle_off));
    }

    private void playButtonAction() {
        if (mBinder.isPause()) {
            mBinder.resumeMusic();
            mPlayAndPauseButton.setImageResource(R.drawable.btn_pause);
        } else {
            mBinder.pauseMusic();
            mPlayAndPauseButton.setImageResource(R.drawable.btn_play);
        }
    }

    private void switchLoopMode() {
        mLoopMode++;
        if (mLoopMode > 2) {
            mLoopMode = 0;
        }
    }
}
