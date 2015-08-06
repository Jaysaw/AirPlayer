package com.airplayer.fragment;


import android.animation.Animator;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.airplayer.R;
import com.airplayer.activity.AirMainActivity;
import com.airplayer.listener.AirMulScrollListener;
import com.airplayer.fragment.child.AlbumGridFragment;
import com.airplayer.fragment.child.ArtistGridFragment;
import com.airplayer.fragment.child.SongListFragment;
import com.airplayer.google.SlidingTabLayout;

/**
 * Created by ZiyiTsang on 15/6/2.
 */
public class MyLibraryFragment extends Fragment {

    private ViewPager viewPager;
    private Toolbar globalBar;
    private Toolbar paddingBar;
    private SlidingTabLayout tabLayout;

    private AirMulScrollListener listener;
    public AirMulScrollListener getListener() {
        return listener;
    }

    private boolean shouldSend = true;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int viewHeight = getResources().getInteger(R.integer.padding_action_bar) + getResources().getInteger(R.integer.padding_tabs);
        listener = new AirMulScrollListener(viewHeight) {
            @Override
            public void onViewScrolled(int viewScrolledDistance) {
                noAnimateTranslate(-viewScrolledDistance);
                shouldSend = toolbarHide;
            }

            @Override
            public void onHide() {
                animateTranslate(-viewHeight);
            }

            @Override
            public void onShow() {
                animateTranslate(0);
            }
        };
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_my_library, container, false);
        globalBar = ((AirMainActivity) getActivity()).getToolbar();
        globalBar.setVisibility(View.VISIBLE);

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

        paddingBar = (Toolbar) rootView.findViewById(R.id.padding_toolbar);

        viewPager = (ViewPager) rootView.findViewById(R.id.my_library_pager);
        viewPager.setAdapter(new LibraryPagerAdapter(fm));
        tabLayout = (SlidingTabLayout) rootView.findViewById(R.id.tabs);
        tabLayout.setDistributeEvenly(true);
        tabLayout.setViewPager(viewPager);

        tabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (listener.isToolbarHide() && shouldSend) {
                    animateTranslate(0);
                    listener.reset();
                    shouldSend = false;
                }
            }

            @Override
            public void onPageSelected(int position) {
                listener.setPage(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) { }
        });

        noAnimateTranslate(0);
        return rootView;
    }

    private class LibraryPagerAdapter extends FragmentPagerAdapter {
        String[] tabItemArray = getResources().getStringArray(R.array.tab_item_array);

        public LibraryPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new ArtistGridFragment();
                case 1:
                    return new AlbumGridFragment();
                case 2:
                    return new SongListFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabItemArray[position];
        }
    }

    private void noAnimateTranslate(int y) {
        globalBar.setTranslationY(y);
        paddingBar.setTranslationY(y);
        tabLayout.setTranslationY(y);
    }

    private void animateTranslate(final int y) {
        globalBar.animate().translationY(y).setInterpolator(new AccelerateInterpolator(3));
        paddingBar.animate().translationY(y).setInterpolator(new AccelerateInterpolator(3));
        tabLayout.animate().setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) { }

            @Override
            public void onAnimationEnd(Animator animation) {
                shouldSend = listener.isToolbarHide();
            }

            @Override
            public void onAnimationCancel(Animator animation) { }

            @Override
            public void onAnimationRepeat(Animator animation) { }
        });
        tabLayout.animate().translationY(y).setInterpolator(new AccelerateInterpolator(3));
    }
}

