package anton.ryaby_belstu.time.in.phone.project;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.jaredrummler.materialspinner.MaterialSpinner;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import anton.ryaby_belstu.time.in.phone.project.IntervalEnum.Interval;

import static android.app.AppOpsManager.MODE_ALLOWED;
import static android.app.AppOpsManager.OPSTR_GET_USAGE_STATS;
import static anton.ryaby_belstu.time.in.phone.project.UsageStatsAdapter.mAppLabelComparator;
import static anton.ryaby_belstu.time.in.phone.project.UsageStatsAdapter.mUsageTimeComparator;

public class MainActivity extends AppCompatActivity {

    DrawerLayout drawerLayout;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    Interval usageInterval;

    TextView totalUsage;

    public static UsageStatsManager mUsageStatsManager;
    private static LayoutInflater mInflater;
    private UsageStatsAdapter mAdapter;
    public static PackageManager mPm;
    private static int SPINNER_SELECTED_ITEM = 0;
    private ArrayList<UsageStats> mPackageStats;
    public static ArrayMap<String, String> mAppLabelMap;
    public static ArrayList<UsageStats> usageStats;

    RecyclerView rv_list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpToolbar();
        initNavView();

        mUsageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        checkPermission();
        mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPm = getPackageManager();
        mPackageStats = new ArrayList<>();
        mAppLabelMap = new ArrayMap<>();
        usageInterval = Interval.WEEKLY;

        initListAppUsage(usageInterval.mInterval);
        initSpinner();
        initAdapter();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (getGrantStatus()) {
            initListAppUsage(usageInterval.mInterval);
            initSpinner();
            initAdapter();
        }
    }

    private void checkPermission() {
        if (getGrantStatus()) {
        } else {
            new AlertDialog.Builder(this)
                    .setIcon(R.drawable.ic_baseline_warning_24)
                    .setTitle("Get permission")
                    .setMessage("The app uses services that need permissions")
                    .setPositiveButton("Allow", (dialog, which) -> {
                        startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                    })
                    .setNegativeButton("Next time", (dialog, which) -> {
                    }).show();
        }
    }

    private boolean getGrantStatus() {
        AppOpsManager appOps = (AppOpsManager) getApplicationContext()
                .getSystemService(Context.APP_OPS_SERVICE);

        int mode = appOps.checkOpNoThrow(OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getApplicationContext().getPackageName());

        if (mode == AppOpsManager.MODE_DEFAULT) {
            return (getApplicationContext().checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED);
        } else {
            return (mode == MODE_ALLOWED);
        }
    }

    public void setUpToolbar() {
        drawerLayout = findViewById(R.id.drawerLayout);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        actionBarDrawerToggle.syncState();

    }

    private void initListAppUsage(int intervalType) {

        mAppLabelMap.clear();
        mPackageStats.clear();

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);

        final List<UsageStats> stats =
                mUsageStatsManager.queryUsageStats(intervalType,
                        cal.getTimeInMillis(), System.currentTimeMillis());
        if (stats == null) {
            return;
        }

        ArrayMap<String, UsageStats> map = new ArrayMap<>();
        final int statCount = stats.size();
        for (int i = 0; i < statCount; i++) {
            final UsageStats pkgStats = stats.get(i);

            try {
                ApplicationInfo appInfo = mPm.getApplicationInfo(pkgStats.getPackageName(), 0);
                String label = appInfo.loadLabel(mPm).toString();
                mAppLabelMap.put(pkgStats.getPackageName(), label);

                UsageStats existingStats =
                        map.get(pkgStats.getPackageName());

                if (existingStats == null) map.put(pkgStats.getPackageName(), pkgStats);
                else existingStats.add(pkgStats);


            } catch (PackageManager.NameNotFoundException e) {
            }
        }
        mPackageStats.addAll(map.values());

        long usageTotal = 0;
        for (UsageStats ut : mPackageStats) {
            usageTotal += ut.getTotalTimeInForeground() / 1000;
        }
        totalUsage = findViewById(R.id.tv_totalUsage);
        totalUsage.setText(UsageStatsAdapter.timeToString(usageTotal));
    }

    private void initNavView() {
        navigationView = findViewById(R.id.navigation_menu);
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.nav_barChart:
                    listUsageAppsForBarChart();
                    startActivity(new Intent(getApplicationContext(), BarChartActivity.class));
                    break;
                case R.id.nav_pieChart:
                    listUsageAppsForPieChart();
                    startActivity(new Intent(getApplicationContext(), PieChartActivity.class));
                    break;
            }
            return false;
        });
    }

    private void initAdapter() {
        rv_list = findViewById(R.id.pkg_list);
        rv_list.setHasFixedSize(true);
        rv_list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mAdapter = new UsageStatsAdapter(getApplicationContext(), mPackageStats, mAppLabelMap);
        rv_list.setAdapter(mAdapter);
    }

    private void initSpinner() {
        MaterialSpinner spinner = findViewById(R.id.spinner);
        MaterialSpinner spinnerIntervals = findViewById(R.id.spinnerIntervals);
        spinner.setItems("Usage time", "App name");
        spinner.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> {
            SPINNER_SELECTED_ITEM = position;
            mAdapter.sortList(SPINNER_SELECTED_ITEM);
            Snackbar.make(view, "Selected: " + item, Snackbar.LENGTH_LONG).show();
        });

        String[] strings = getResources().getStringArray(R.array.list_intervals);
        spinnerIntervals.setItems(strings);
        spinnerIntervals.setOnItemSelectedListener((MaterialSpinner.OnItemSelectedListener<String>) (view, position, id, item) -> {
            usageInterval = Interval.getValue(strings[position]);
            initListAppUsage(usageInterval.mInterval);
            mAdapter.sortList(SPINNER_SELECTED_ITEM);
            mAdapter.notifyDataSetChanged();
        });
    }

    private void listUsageAppsForPieChart() {
        mAppLabelComparator = new UsageStatsAdapter.AppNameComparator(mAppLabelMap);
        mPackageStats.sort(mUsageTimeComparator);
        usageStats = new ArrayList<>();
        int index = 0;
        for (UsageStats pkg : mPackageStats) {
            if (index <= 5) {
                usageStats.add(pkg);
                index++;
            } else break;
        }
    }

    private void listUsageAppsForBarChart() {
        mAppLabelComparator = new UsageStatsAdapter.AppNameComparator(mAppLabelMap);
        mPackageStats.sort(mUsageTimeComparator);
        usageStats = new ArrayList<>();
        int index = 0;
        for (UsageStats pkg : mPackageStats) {
            if (index <= 10) {
                usageStats.add(pkg);
                index++;
            } else break;
        }
    }
}