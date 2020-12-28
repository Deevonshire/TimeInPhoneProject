package anton.ryaby_belstu.time.in.phone.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageStats;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.chart.common.listener.Event;
import com.anychart.chart.common.listener.ListenersInterface;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;

import java.util.ArrayList;
import java.util.List;

public class PieChartActivity extends AppCompatActivity {

    private static int constant = 3600000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pie_chart);

        AnyChartView pieChart = findViewById(R.id.pieChart);
        pieChart.setProgressBar(findViewById(R.id.progress_bar_pieChart));

        Pie pie = AnyChart.pie();

        pie.setOnClickListener(new ListenersInterface.OnClickListener(new String[]{"x","value"}) {
            @Override
            public void onClick(Event event) {
                Toast.makeText(PieChartActivity.this, event.getData().get("x") + ": " + event.getData().get("value") + "h", Toast.LENGTH_SHORT).show();
            }
        });

        List<DataEntry> data = new ArrayList<>();
        for (int i = 0; i < MainActivity.usageStats.size(); i++)
        {
            data.add(new ValueDataEntry(MainActivity.mAppLabelMap.get(MainActivity.usageStats.get(i).getPackageName()),MainActivity.usageStats.get(i).getTotalTimeInForeground() / constant));
        }

        pie.data(data);

        pie.title("Apps usage pie chart");

        pie.labels().position("outside");

        pie.legend().title().enabled(true);
        pie.legend().title()
                .text("app list")
                .padding(0d, 0d, 10d, 0d);

        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        pieChart.setChart(pie);

    }
}