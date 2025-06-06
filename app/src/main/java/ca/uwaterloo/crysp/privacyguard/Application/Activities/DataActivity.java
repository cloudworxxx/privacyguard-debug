package ca.uwaterloo.crysp.privacyguard.Application.Activities;

/**
 * Created by lucas on 27/03/17.
 */

import android.content.Context;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;

import ca.uwaterloo.crysp.privacyguard.Application.Database.DataLeak;
import ca.uwaterloo.crysp.privacyguard.Application.Database.Traffic;
import ca.uwaterloo.crysp.privacyguard.Application.Fragments.LeakQueryFragment;
import ca.uwaterloo.crysp.privacyguard.Application.Fragments.LeakReportFragment;
import ca.uwaterloo.crysp.privacyguard.Application.Fragments.LeakSummaryFragment;
import ca.uwaterloo.crysp.privacyguard.Application.Fragments.TrafficFragment;
import ca.uwaterloo.crysp.privacyguard.Application.Interfaces.AppDataInterface;
import ca.uwaterloo.crysp.privacyguard.Plugin.LeakReport;
import ca.uwaterloo.crysp.privacyguard.R;

import java.util.List;

public abstract class DataActivity extends AppCompatActivity implements AppDataInterface {

    private static final int TAB_COUNT = 4;

    private LeakReportFragment leakReportFragment;
    private LeakSummaryFragment leakSummaryFragment;
    private LeakQueryFragment leakQueryFragment;

    protected List<DataLeak> locationLeaks;
    protected List<DataLeak> contactLeaks;
    protected List<DataLeak> deviceLeaks;
    protected List<DataLeak> keywordLeaks;

    private TrafficFragment trafficFragment;
    protected List<Traffic> trafficsInE;
    protected List<Traffic> trafficsOutE;
    protected List<Traffic> trafficsInNe;
    protected List<Traffic> trafficsOutNe;

    protected TabLayout tabLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_app_data);

        leakReportFragment = new LeakReportFragment();
        leakSummaryFragment = new LeakSummaryFragment();
        leakQueryFragment = new LeakQueryFragment();

        trafficFragment = new TrafficFragment();

        ViewPager viewPager = findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomFragmentPagerAdapter(getSupportFragmentManager(), this));
        viewPager.setOffscreenPageLimit(TAB_COUNT - 1);

        tabLayout = findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu); // Убедитесь, что R.menu.main_menu совпадает с вашим файлом
        return true;
    }

    public class CustomFragmentPagerAdapter extends FragmentPagerAdapter {
        private final String[] tabTitles = new String[] { "Report", "Summary", "Query", "Traffic"};

        public CustomFragmentPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return leakReportFragment;
                case 1:
                    return leakSummaryFragment;
                case 2:
                    return leakQueryFragment;
                case 3:
                    return trafficFragment;
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    @Override
    public String getAppName() {
        return null;
    }

    @Override
    public String getAppPackageName() {
        return null;
    }

    @Override
    public List<DataLeak> getLeaks(LeakReport.LeakCategory category) {
        switch (category) {
            case LOCATION:
                return locationLeaks;
            case CONTACT:
                return contactLeaks;
            case DEVICE:
                return deviceLeaks;
            case KEYWORD:
                return keywordLeaks;
        }
        return null;
    }

    @Override
    public List<Traffic> getTraffics(boolean encrypted, boolean outgoing){
        if(encrypted && outgoing){
            return trafficsOutE;
        }
        if(!encrypted && outgoing){
            return trafficsOutNe;
        }
        if(!encrypted && !outgoing){
            return trafficsInNe;
        }
        return trafficsInE;
    }
}

