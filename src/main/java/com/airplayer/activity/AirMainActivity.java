package com.airplayer.activity;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.airplayer.fragment.MyLibraryFragment;
import com.airplayer.fragment.NavigationDrawerFragment;
import com.airplayer.fragment.NowPlayingFragment;
import com.airplayer.R;
import com.airplayer.fragment.PlayMusicFragment;
import com.airplayer.model.Song;
import com.airplayer.service.PlayMusicService;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class AirMainActivity extends AppCompatActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = "AirMainActivity";

    // shared preference
    public static final String PREF_IS_FIRST_OPEN = "pref_is_first_open";
    public static final String PREF_DATA_BASE_VERSION = "pref_data_base_version";
    private SharedPreferences sp;

    // user interface
    private NavigationDrawerFragment mNavigationDrawFragment;
    private Toolbar mToolbar;
    private FrameLayout mSlidingFragmentContainer;

    private FragmentManager mFragmentManager;

    // data base
//    private AirPlayerDB db;
    private int dbVersion;
    private ProgressDialog mProgressDialog;

    private boolean isFirstOpen;

    // service
    private PlayMusicService.PlayerControlBinder playerControlBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerControlBinder = (PlayMusicService.PlayerControlBinder) service;
            Log.d(TAG, "service has connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "service has disconnected");
        }
    };

    // receiver
    private SlidingPanelControlReceiver mSlidingPanelControlReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind service
        Intent playMusicServiceIntent = new Intent(this, PlayMusicService.class);
        playMusicServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(playMusicServiceIntent);
        bindService(playMusicServiceIntent, connection, BIND_AUTO_CREATE);

        // get fragment manager
        mFragmentManager = getSupportFragmentManager();

        // set up tool bar
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.inflateMenu(R.menu.menu_main);
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }
        });

        // set up navigation drawer fragment
        mNavigationDrawFragment = (NavigationDrawerFragment) mFragmentManager
                .findFragmentById(R.id.navigation_drawer);

        mNavigationDrawFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));

        // set up sliding up panel
        mSlidingFragmentContainer = (FrameLayout) findViewById(R.id.sliding_fragment_container);
//        mSlidingFragmentContainer.setVisibility(View.INVISIBLE);

        // get data from share preference
        sp = PreferenceManager.getDefaultSharedPreferences(this);
        isFirstOpen = sp.getBoolean(PREF_IS_FIRST_OPEN, true);
        dbVersion = sp.getInt(PREF_DATA_BASE_VERSION, 1);

//        db = AirPlayerDB.newInstance(this, dbVersion);

        if (isFirstOpen) {
            isFirstOpen = false;
            sp.edit().putBoolean(PREF_IS_FIRST_OPEN, isFirstOpen).apply();
            sp.edit().putInt(PREF_DATA_BASE_VERSION, dbVersion).apply();
        }

        mSlidingPanelControlReceiver = new SlidingPanelControlReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(PlayMusicService.START_TO_PLAY_MUSIC);
        registerReceiver(mSlidingPanelControlReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mSlidingPanelControlReceiver);
        unbindService(connection);
        super.onDestroy();
    }

    /**
     * implements the NavigationDrawerCallbacks
     * @param position of NavigationDrawer item selected
     */
    @Override
    public void onNavigationDrawItemSelected(int position) {
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, switchFragment(position)).commit();
    }

    public Toolbar getToolbar() {
        return mToolbar;
    }

    public PlayMusicService.PlayerControlBinder getPlayerControlBinder() {
        return playerControlBinder;
    }

    public class SlidingPanelControlReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Song songPlaying = playerControlBinder.getSongPlaying();
//            mSlidingFragmentContainer.setVisibility(View.VISIBLE);
            // set up bottom sliding fragment
            mFragmentManager.beginTransaction()
                    .replace(R.id.sliding_fragment_container,
                            PlayMusicFragment.newInstance(songPlaying)).commit();
            Toast.makeText(context, songPlaying.getTitle(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * a convenient method to switch fragment with the position of selected item
     * @param position position of selected item
     * @return a new Fragment whose name was selected in NavigationDrawer
     */
    private Fragment switchFragment(int position) {
        switch (position) {
            case 0:
                mToolbar.setTitle("Now Playing");
                return new NowPlayingFragment();
            case 1:
                mToolbar.setTitle("My Library");
                return new MyLibraryFragment();
            default:
                return null;
        }
    }


}
