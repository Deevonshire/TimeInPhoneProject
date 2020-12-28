package anton.ryaby_belstu.time.in.phone.project;

import androidx.appcompat.app.AppCompatActivity;

import android.app.usage.UsageStats;
import android.os.Bundle;
import android.util.ArrayMap;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BarChartActivity extends AppCompatActivity {

    private static int constant = 3600000;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bar_chart);

        AnyChartView anyChartView = findViewById(R.id.barChart);
        anyChartView.setProgressBar(findViewById(R.id.progress_bar_barChart));

        Cartesian cartesian = AnyChart.column();

        List<DataEntry> data = new ArrayList<>();
        for (int i = 0; i < MainActivity.usageStats.size(); i++)
        {
            data.add(new ValueDataEntry(MainActivity.mAppLabelMap.get(MainActivity.usageStats.get(i).getPackageName()),MainActivity.usageStats.get(i).getTotalTimeInForeground() / constant));
        }

        Column column = cartesian.column(data);

        column.tooltip()
                .titleFormat("{%X}")
                .position(Position.CENTER_BOTTOM)
                .anchor(Anchor.CENTER_BOTTOM)
                .offsetX(0d)
                .offsetY(5d)
                .format("{%Value}{groupsSeparator: }");

        cartesian.animation(true);
        cartesian.title("Top 10 apps by usage time");

        cartesian.yScale().minimum(0d);

        cartesian.yAxis(0).labels().format("{%Value}{groupsSeparator: }");

        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);

        cartesian.xAxis(0).title("Apps");
        cartesian.yAxis(0).title("Hour");

        anyChartView.setChart(cartesian);

    }
}