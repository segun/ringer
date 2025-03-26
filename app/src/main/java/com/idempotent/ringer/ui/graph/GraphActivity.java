package com.idempotent.ringer.ui.graph;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.idempotent.ringer.R;
import com.idempotent.ringer.service.ApiService;
import com.idempotent.ringer.service.RetrofitClient;
import com.idempotent.ringer.ui.data.ChargingStatusResponse;
import com.idempotent.ringer.ui.data.GetChargingStatusRequest;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class GraphActivity extends AppCompatActivity {

    private LineChart lineChart;
    private Button btnStartDate, btnEndDate, btnFetch;
    private Calendar startDate, endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

        lineChart = findViewById(R.id.lineChart);
        btnStartDate = findViewById(R.id.btnStartDate);
        btnEndDate = findViewById(R.id.btnEndDate);
        btnFetch = findViewById(R.id.btnFetch);

        // Default values: 1 month ago to today
        startDate = Calendar.getInstance();
        startDate.add(Calendar.MONTH, -1); // Set to 1 month ago

        endDate = Calendar.getInstance(); // Set to today

        // Display initial date values
        btnStartDate.setText(formatDate(startDate.getTime()));
        btnEndDate.setText(formatDate(endDate.getTime()));

        // Date picker for start date
        btnStartDate.setOnClickListener(v -> showDatePicker(startDate, btnStartDate));

        // Date picker for end date
        btnEndDate.setOnClickListener(v -> showDatePicker(endDate, btnEndDate));

        // Fetch button click
        btnFetch.setOnClickListener(v -> fetchGraphData());

        // Initial data fetch
        fetchGraphData();
    }

    private void showDatePicker(Calendar date, Button button) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (DatePicker view, int year, int month, int dayOfMonth) -> {
                    date.set(year, month, dayOfMonth);
                    button.setText(formatDate(date.getTime()));
                },
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(date);
    }

    private void fetchGraphData() {
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String userLocation = sharedPreferences.getString("userLocation", "Unknown");
        String userId = sharedPreferences.getString("userId", null);

        long startTimestamp = startDate.getTimeInMillis();
        long endTimestamp = endDate.getTimeInMillis();

        // Create request object
        GetChargingStatusRequest request = new GetChargingStatusRequest();
        request.setStartDate(startTimestamp);
        request.setEndDate(endTimestamp);
        request.setUserId(userId);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        ApiService apiService = retrofit.create(ApiService.class);

        Call<List<ChargingStatusResponse>> call = apiService.getChargingStatus(request);
        call.enqueue(new Callback<List<ChargingStatusResponse>>() {
            @Override
            public void onResponse(Call<List<ChargingStatusResponse>> call, Response<List<ChargingStatusResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    plotGraph(response.body());
                } else {
                    Toast.makeText(GraphActivity.this, "Failed to get data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ChargingStatusResponse>> call, Throwable t) {
                Log.e("GraphActivity", "API Error: " + t.getMessage());
                Toast.makeText(GraphActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void plotGraph(List<ChargingStatusResponse> data) {
        ArrayList<Entry> entries = new ArrayList<>();

        for (int i = 0; i < data.size(); i++) {
            ChargingStatusResponse status = data.get(i);
            float time = (float) (status.getStatusTime() / 1000); // Convert ms to seconds
            float pluggedIn = status.isPluggedIn() ? 1f : 0f;

            // Add the first point
            entries.add(new Entry(time, pluggedIn));

            // Add a duplicate time point if it's not the last entry (to create step effect)
            if (i < data.size() - 1) {
                ChargingStatusResponse nextStatus = data.get(i + 1);
                float nextTime = (float) (nextStatus.getStatusTime() / 1000);
                entries.add(new Entry(nextTime, pluggedIn)); // Maintain same value until next change
            }
        }

        LineDataSet dataSet = new LineDataSet(entries, "Charging Status");
        dataSet.setLineWidth(2f);
        dataSet.setDrawCircles(false); // Avoid circles to keep the step chart clean
        dataSet.setDrawValues(false);
        dataSet.setDrawFilled(true); // Optional: Fill the area below the line
        dataSet.setMode(LineDataSet.Mode.STEPPED); // Ensure a step-like effect
        dataSet.setColor(Color.BLUE);
        dataSet.setFillColor(Color.CYAN);
        dataSet.setFillAlpha(100);
        dataSet.setDrawFilled(true);
        Drawable fillDrawable = ContextCompat.getDrawable(this, R.drawable.chart_gradient);
        dataSet.setFillDrawable(fillDrawable);

        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);

        // Customize X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularityEnabled(true);
        xAxis.setGranularity(3600f); // Show labels every 1 hour
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return formatTimestamp(value);
            }
        });

        // Customize Y-axis
        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.setAxisMinimum(-0.2f);
        leftAxis.setAxisMaximum(1.2f);
        leftAxis.setLabelCount(2, true);
        leftAxis.setGranularity(1f);
        leftAxis.setTextSize(12f);
        leftAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                if (Math.round(value) == 1) {
                    return "🔌 Plugged In";
                } else {
                    return "⚡ Unplugged";
                }
            }
        });


        lineChart.getAxisRight().setEnabled(false); // Disable right axis
        lineChart.getLegend().setEnabled(false); // Hide legend if not needed
        lineChart.invalidate(); // Refresh chart
        lineChart.setTouchEnabled(true);
        lineChart.setPinchZoom(true);
        lineChart.setDragEnabled(true);
        lineChart.setHighlightPerTapEnabled(true);
        lineChart.fitScreen();
        lineChart.getViewPortHandler().getMatrixTouch().reset();
        lineChart.invalidate();
        lineChart.moveViewToX(entries.get(entries.size() - 1).getX());
    }

    private String formatTimestamp(float timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault());
        return sdf.format(new Date((long) timestamp * 1000));
    }
}
