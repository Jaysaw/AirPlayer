package com.airplayer.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import com.airplayer.R;
import com.airplayer.listener.AirMulScrollListener;
import com.airplayer.fragment.child.AlbumGridFragment;
import com.airplayer.fragment.child.ArtistGridFragment;
import com.airplayer.fragment.child.SongListFragment;

/**
 * Created by ZiyiTsang on 15/6/2.
 */
public class MyLibraryFragment extends Fragment {

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_my_library, container, false);

        FragmentManager fm = getChildFragmentManager();
        fm.beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();

        mViewPager = (ViewPager) rootView.findViewById(R.id.my_library_pager);
        mViewPager.setAdapter(new LibraryPagerAdapter(fm));
        mTabLayout = (TabLayout) rootView.findViewById(R.id.tabs);
        mTabLayout.setupWithViewPager(mViewPager);

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
}

