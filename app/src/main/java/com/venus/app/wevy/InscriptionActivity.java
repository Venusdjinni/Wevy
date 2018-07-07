package com.venus.app.wevy;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import com.venus.app.Utils.Terminating;

public class InscriptionActivity extends AppCompatActivity implements Terminating {
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inscription);

        viewPager = (ViewPager) findViewById(R.id.inscription_viewpager);
        viewPager.setAdapter(new InscriptionPagerAdapter(getSupportFragmentManager()));
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() == 1) viewPager.setCurrentItem(0);
        else super.onBackPressed();
    }

    void goToNext() {
        viewPager.setCurrentItem(1);
    }

    void goToPrevious() {
        viewPager.setCurrentItem(0);
    }

    @Override
    public void terminer() {
        finish();
    }

    private class InscriptionPagerAdapter extends FragmentStatePagerAdapter {

        public InscriptionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return InscriptionFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
