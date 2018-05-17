package yomusic.developer.it.yomusic.Adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * Created by anupam on 21-01-2018.
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {

    ArrayList<String> tabTitles = new ArrayList<>();
    ArrayList <Fragment> fragments = new ArrayList<>();

    public void addFragments(Fragment fragments, String tabTitles){
        this.fragments.add(fragments);
        this.tabTitles.add(tabTitles);
    }

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }
}
