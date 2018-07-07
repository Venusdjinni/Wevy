package com.venus.app.wevy;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.venus.app.Base.ListeCours;
import com.venus.app.IO.Asyncable;

import java.util.Calendar;

public class EdtActivity extends AppCompatActivity implements Asyncable {
    public static final String EDT_PAGER_MODE_H = "horizontal";
    public static final String EDT_PAGER_MODE_V = "vertical";
    public static final String FOA_COURS = "foa_cours";
    public static final String LFD_COURS = "lfd_cours";
    public static final String FOA_AINFOS = "foa_ainfos";
    public static final String LFD_AINFOS = "lfd_ainfos";
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ListeCours coursSemaine;
    private static String mode = EDT_PAGER_MODE_V;
    private ProgressDialog loading;
    int transition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edt);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        viewPager = (ViewPager) findViewById(R.id.edt_viewpager);
        tabLayout = (TabLayout) findViewById(R.id.edt_tablayout);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setVisibility((mode.equals(EDT_PAGER_MODE_V) ? View.GONE : View.VISIBLE));

        loading = Utilities.newLoadingDialog(this);
        getSupportActionBar().setTitle(texteSemaine());

        // recherche des donnees
        loading.show();
        new LoadFromDbAsc(this, LFD_COURS).execute(LoadFromDbAsc.PARAM_COURS);
        // (new FetchOnlineAsc(this, MainActivity.PREF_URL_VALUE + "getEdt.php", FOA_COURS)).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate((mode.equals(EDT_PAGER_MODE_V) ? R.menu.activity_edt_vertical : R.menu.activity_edt_horizontal), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.ab_edt_switch) {
            // on change le mode d'affichage
            mode = mode.equals(EDT_PAGER_MODE_V) ? EDT_PAGER_MODE_H : EDT_PAGER_MODE_V;
            // on recree le viewpager
            viewPager.setAdapter(new EdtPagerAdapter(getSupportFragmentManager(), mode));
            // Si on est en mode horizontal
            if (mode.equals(EDT_PAGER_MODE_H)) {
                viewPager.setCurrentItem(1);
                viewPager.addOnPageChangeListener(vp_listener);
            }
            tabLayout.setVisibility((mode.equals(EDT_PAGER_MODE_V) ? View.GONE : View.VISIBLE));
            invalidateOptionsMenu();
            //
            return true;
        } else if (id == android.R.id.home) finish();

        return super.onOptionsItemSelected(item);
    }

    void onPrevButtonPressed() {
        transition--;
        updateTitle();
    }

    void onNextButtonPressed() {
        transition++;
        updateTitle();
    }

    private void updateTitle() {
        getSupportActionBar().setTitle(texteSemaine());
    }

    private String texteSemaine() {
        int jour = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        Calendar lundi = Calendar.getInstance(), vendredi = Calendar.getInstance();
        lundi.add(Calendar.DAY_OF_MONTH, transition * 7 - (jour - Calendar.MONDAY));
        vendredi.add(Calendar.DAY_OF_MONTH, transition * 7 + Calendar.FRIDAY - jour);
        return "Du " + lundi.get(Calendar.DAY_OF_MONTH) + "/" + (lundi.get(Calendar.MONTH) + 1) +
                " au " + vendredi.get(Calendar.DAY_OF_MONTH) + "/" + (vendredi.get(Calendar.MONTH) + 1);
    }

    private ViewPager.OnPageChangeListener vp_listener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 2) {
                transition++;
                updateTitle();
            } else if (position == 0) {
                transition--;
                updateTitle();
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (state == ViewPager.SCROLL_STATE_IDLE)
                viewPager.setCurrentItem(1, false);
        }
    };

    void updateGUI() {
        viewPager.setAdapter(new EdtPagerAdapter(getSupportFragmentManager(), mode));
        // Si on est en mode horizontal
        if (mode.equals(EDT_PAGER_MODE_H)) {
            viewPager.setCurrentItem(1);
            viewPager.addOnPageChangeListener(vp_listener);
        }
    }

    @Override
    public void fetchOnlineResult(Object result, String code) {
        loading.dismiss();
        switch (code) {
            case FOA_COURS:
                new LoadFromDbAsc(this, LFD_COURS).execute(LoadFromDbAsc.PARAM_COURS);
                break;
            /*case FOA_AINFOS:
                new LoadFromDbAsc(this, LFD_COURS).execute(LoadFromDbAsc.PARAM_ABSINFO);
                break;*/
            case LFD_COURS:
                coursSemaine = ListeCours.parseArray((Object[]) result);
                updateGUI();
                //new LoadFromDbAsc(this, LFD_AINFOS).execute(LoadFromDbAsc.PARAM_ABSINFO);
                break;
            /*case LFD_AINFOS:
                infos = new AbstractInformation[((Object[]) result).length];
                for (int i = 0; i < infos.length; i++) infos[i] = (AbstractInformation) ((Object[]) result)[i];
                updateGUI();
                break;*/
        }
    }

    private class EdtPagerAdapter extends FragmentStatePagerAdapter {
        private String mode;

        public EdtPagerAdapter(FragmentManager fm, String mode) {
            super(fm);
            this.mode = mode;
        }

        @Override
        public Fragment getItem(int position) {
            return EdtFragment.newInstance(coursSemaine, mode, position);
        }

        @Override
        public int getCount() {
            return mode.equals(EDT_PAGER_MODE_V) ? 1 : 3;
        }
    }
}
