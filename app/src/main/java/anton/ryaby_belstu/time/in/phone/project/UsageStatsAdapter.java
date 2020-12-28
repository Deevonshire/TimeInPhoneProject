package anton.ryaby_belstu.time.in.phone.project;

import android.app.usage.UsageStats;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class UsageStatsAdapter extends RecyclerView.Adapter<UsageStatsAdapter.ViewHolder> {

    Context context;
    public static UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();
    public static AppNameComparator mAppLabelComparator;
    private ArrayMap<String, String> mAppLabelMap;
    private ArrayList<UsageStats> mPackageStats;

    public UsageStatsAdapter(Context context, ArrayList<UsageStats> mPackageStats, ArrayMap<String, String> mAppLabelMap) {

        this.context = context;
        this.mPackageStats = mPackageStats;
        this.mAppLabelMap = mAppLabelMap;

        mAppLabelComparator = new UsageStatsAdapter.AppNameComparator(mAppLabelMap);
        sortList(0);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView appIcon;
        TextView pkgName;
        TextView usageTime;

        public ViewHolder(View v) {
            super(v);
            appIcon = v.findViewById(R.id.appIcon);
            pkgName = v.findViewById(R.id.package_name);
            usageTime = v.findViewById(R.id.usage_time);
        }

    }

    @NonNull
    @Override
    public UsageStatsAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.usage_stat_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull UsageStatsAdapter.ViewHolder holder, int position) {
        UsageStats pkgStats = mPackageStats.get(position);
        try {
            Drawable icon = null;
            icon = context.getPackageManager()
                    .getApplicationIcon(pkgStats.getPackageName());
            holder.appIcon.setImageDrawable(icon);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        holder.pkgName.setText(mAppLabelMap.get(pkgStats.getPackageName()));
        holder.usageTime.setText(
                timeToString(pkgStats.getTotalTimeInForeground() / 1000));
    }


    @Override
    public int getItemCount() {
        return mPackageStats.size();
    }

    public static String timeToString(long secs) {
        long hour = secs / 3600,
                min = secs / 60 % 60,
                sec = secs / 1 % 60;
        return String.format("%dh %dm %ds", hour, min, sec);
    }

    void sortList(int position) {
        if (position == 0) {
            mPackageStats.sort(mUsageTimeComparator);
        } else if (position == 1) {
            mPackageStats.sort(mAppLabelComparator);
        }
        notifyDataSetChanged();
    }


    public static class AppNameComparator implements Comparator<UsageStats> {
        private final Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            mAppLabelList = appList;
        }

        @Override
        public final int compare(UsageStats a, UsageStats b) {
            String alabel = mAppLabelList.get(a.getPackageName());
            String blabel = mAppLabelList.get(b.getPackageName());
            return alabel.compareTo(blabel);
        }
    }
    public static class UsageTimeComparator implements Comparator<UsageStats> {
        @Override
        public final int compare(UsageStats a, UsageStats b) {
            return (int) (b.getTotalTimeInForeground() - a.getTotalTimeInForeground());
        }
    }
}
