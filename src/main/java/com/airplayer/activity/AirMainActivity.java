package com.airplayer.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;

import com.airplayer.fragment.EqualizerFragment;
import com.airplayer.fragment.MyLibraryFragment;
import com.airplayer.fragment.NavigationDrawerFragment;
import com.airplayer.fragment.PlayNowFragment;
import com.airplayer.R;
import com.airplayer.fragment.PlayMusicFragment;
import com.airplayer.model.AirModel;
import com.airplayer.model.PictureGettable;
import com.airplayer.service.PlayMusicService;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;


public class AirMainActivity extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    public static final String TAG = "AirMainActivity";

    public static final String EXTERNAL_PICTURE_FOLDER = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES).getPath() + "/AirPlayer/";

    // ===== user interface =====
    private DrawerLayout mDrawerLayout;
    private Toolbar mToolbar;
    private AppBarLayout mAppBarLayout;
    private NavigationDrawerFragment mNavigationDrawFragment;
    private SlidingUpPanelLayout mSlidingUpPanelLayout;

    // ===== service =====
    private PlayMusicService.PlayerControlBinder playerControlBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            playerControlBinder = (PlayMusicService.PlayerControlBinder) service;
            // set up bottom sliding fragment when service is connected
            mPlayMusicFragment = new PlayMusicFragment();
            mFragmentManager.beginTransaction()
                    .replace(R.id.sliding_fragment_container,
                            mPlayMusicFragment).commit();

            // set sliding up panel invisible when activity create
            if (!playerControlBinder.isPlaying()) {
                mSlidingUpPanelLayout.setTouchEnabled(false);
                mSlidingUpPanelLayout.setPanelHeight(0);
            } else {
                mSlidingUpPanelLayout.setTouchEnabled(true);
                mSlidingUpPanelLayout.setPanelHeight(mSize);
            }
            Log.d(TAG, "service has connected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "service has disconnected");
        }
    };

    // ===== receiver =====
    private PlayerStateReceiver mPlayerStateReceiver;

    // ===== helper classes =====
    private FragmentManager mFragmentManager;
    private PlayMusicFragment mPlayMusicFragment;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mSlidingUpPanelLayout.setPanelHeight((int) msg.obj);
            }
        }
    };

    // ===== other resource =====
    private int mSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        AirModel.initModels(this);
        setContentView(R.layout.activity_main);

        // ===== get actionbarSize in attr
        int defaultInt = getResources().getDimensionPixelOffset(R.dimen.sliding_up_fragment_bottom_bar_height);
        int[] attrsArray = { R.attr.actionBarSize };
        TypedArray typedArray = obtainStyledAttributes(attrsArray);
        mSize = typedArray.getDimensionPixelOffset(0, defaultInt);

        // ===== bind service =====
        Intent playMusicServiceIntent = new Intent(this, PlayMusicService.class);
        playMusicServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startService(playMusicServiceIntent);
        bindService(playMusicServiceIntent, connection, BIND_AUTO_CREATE);

        // ===== get sliding up panel and set panel slide listener =====
        mSlidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.sliding_up_panel_layout);
        mSlidingUpPanelLayout.setPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View view, float v) { }

            @Override
            public void onPanelCollapsed(View view) {
                Toolbar toolbar = mPlayMusicFragment.getSlidingUpPanelTopBar();
                toolbar.getMenu().clear();
                if (playerControlBinder.isPause()) {
                    toolbar.inflateMenu(R.menu.menu_sliding_panel_down_play_menu);
                } else {
                    toolbar.inflateMenu(R.menu.menu_sliding_panel_down_pause_menu);
                }
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            }

            @Override
            public void onPanelExpanded(View view) {
                Toolbar toolbar = mPlayMusicFragment.getSlidingUpPanelTopBar();
                toolbar.getMenu().clear();
                toolbar.inflateMenu(R.menu.menu_sliding_panel_up_menu);
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            }

            @Override
            public void onPanelAnchored(View view) { }

            @Override
            public void onPanelHidden(View view) { }
        });

        // ===== get fragment manager =====
        mFragmentManager = getSupportFragmentManager();

        // ===== setup tool bar =====
        mToolbar = (Toolbar) findViewById(R.id.global_toolbar);
        mAppBarLayout = (AppBarLayout) findViewById(R.id.main_appbar_layout);

        // ===== setup navigation drawer fragment =====
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationDrawFragment = (NavigationDrawerFragment) mFragmentManager
                .findFragmentById(R.id.navigation_drawer);

        mNavigationDrawFragment.setUp(R.id.navigation_drawer, mDrawerLayout);

        mPlayerStateReceiver = new PlayerStateReceiver();
        IntentFilter filter = new IntentFilter(PlayMusicService.PLAY_STATE_CHANGE);
        registerReceiver(mPlayerStateReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == PictureGettable.REQUEST_CODE_FETCH_PICTURE) {
                mFragmentManager.findFragmentById(R.id.fragment_container)
                        .onActivityResult(requestCode, resultCode, data);
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onDestroy() {
        unbindService(connection);                  /* unbind service */
        unregisterReceiver(mPlayerStateReceiver);   /* unregister receiver */
        super.onDestroy();
    }


    @Override
    public void onBackPressed() {
        if (mNavigationDrawFragment.getDrawerLayout().isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawFragment.getDrawerLayout().closeDrawer(GravityCompat.START);
        } else if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            if (mPlayMusicFragment.isPlayListShow()) {
                super.onBackPressed();
                mPlayMusicFragment.setIsPlayListShow(false);
            } else {
                mSlidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            }
        } else if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
            if (mPlayMusicFragment.isPlayListShow()) {
                super.onBackPressed();
                super.onBackPressed();
                mPlayMusicFragment.setIsPlayListShow(false);
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    /**
     * implements the NavigationDrawerCallbacks
     * @param position of NavigationDrawer item selected
     */
    @Override
    public void onNavigationDrawItemSelected(int position) {
        String[] titles = getResources().getStringArray(R.array.toolbar_title_array);
        mToolbar.setTitle(titles[position]);
        mFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, switchFragment(position)).commit();
    }

    /**
     * getter of main tool bar
     * @return main Toolbar
     */
    public Toolbar getToolbar() {
        return mToolbar;
    }

    /**
     * getter of main app bar layout, use for add a TabLayout in { @link com.airplayer.fragment.MyLibraryFragment}
     * @return main AppBarLayout
     */
    public AppBarLayout getAppBarLayout() {
        return mAppBarLayout;
    }

    /**
     * getter of binder of service
     * @return service which control the music player and music play action
     */
    public PlayMusicService.PlayerControlBinder getPlayerControlBinder() {
        return playerControlBinder;
    }

    /**
     * inside receiver to receiver player state broadcast
     * to control the sliding up panel visible
     */
    public class PlayerStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int playState = intent.getIntExtra(PlayMusicService.PLAY_STATE_KEY, -1);
            if (playState == PlayMusicService.PLAY_STATE_PLAY) {
                if (!mSlidingUpPanelLayout.isTouchEnabled()) {
                    mSlidingUpPanelLayout.setTouchEnabled(true);
                    showPanel();
                }
                if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    Toolbar toolbar = mPlayMusicFragment.getSlidingUpPanelTopBar();
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.menu_sliding_panel_down_pause_menu);
                }
            } else if (playState == PlayMusicService.PLAY_STATE_PAUSE) {
                if (mSlidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    Toolbar toolbar = mPlayMusicFragment.getSlidingUpPanelTopBar();
                    toolbar.getMenu().clear();
                    toolbar.inflateMenu(R.menu.menu_sliding_panel_down_play_menu);
                }
            }
        }
    }

    /**
     * a convenient method to switch fragment with the position of selected item
     * @param position of selected item
     * @return a new Fragment whose name was selected in NavigationDrawer
     */
    private Fragment switchFragment(int position) {

        // pop all fragments in back stack
        mFragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // set play list is not showing when it popped
        if (mPlayMusicFragment != null) {
            mPlayMusicFragment.setIsPlayListShow(false);
        }

        switch (position) {
            case 0:
                return new PlayNowFragment();
            case 1:
                return new MyLibraryFragment();
            case 2:
                return new EqualizerFragment();
            default:
                return null;
        }
    }

    /**
     * package method to animate show panel when start to play music
     */
    private void showPanel() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int height = 0; height < mSize; height += 2) {
                    Message msg = new Message();
                    msg.what = 0;
                    msg.obj = height;
                    handler.sendMessage(msg);
                    try {
                        Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
