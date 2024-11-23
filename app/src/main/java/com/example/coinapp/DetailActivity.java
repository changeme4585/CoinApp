package com.example.coinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;


import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {
     Button button ;
    private CandleStickChart candleStickChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);  // activity_detail.xml로 설정
//        candleStickChart = findViewById(R.id.candleStickChart);
        String coinName = getIntent().getStringExtra("market");
        fetchCandleData(coinName.substring(4,coinName.length()));
        candleStickChart = findViewById(R.id.candleStickChart);
        candleStickChart.setHighlightPerDragEnabled(true);

        candleStickChart.setDrawBorders(true);

        candleStickChart.setBorderColor(Color.BLACK);

        YAxis yAxis = candleStickChart.getAxisLeft();
        YAxis rightAxis = candleStickChart.getAxisRight();
        yAxis.setDrawGridLines(false);
        rightAxis.setDrawGridLines(false);
        candleStickChart.requestDisallowInterceptTouchEvent(true);

        XAxis xAxis = candleStickChart.getXAxis();

        xAxis.setDrawGridLines(false);// disable x axis grid lines
        xAxis.setDrawLabels(false);
        rightAxis.setTextColor(Color.WHITE);
        yAxis.setDrawLabels(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);
        xAxis.setAvoidFirstLastClipping(true);

        Legend l = candleStickChart.getLegend();
        l.setEnabled(false);

        // String market = ;
        button = (Button) findViewById(R.id.sellBtn);
//        if (button == null) {
//            Log.e("ButtonError", "sellBtn 버튼을 찾을 수 없습니다.");
//        } else {
//            button.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Log.d("ButtonClick", "sds");
//                }
//            });
//        }
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

            TradeCoin tradeCoin = new TradeCoin();
            try {
                tradeCoin.sell_coin("50.0000","TFUEL");
            } catch (IOException e) {
                System.out.println("매도 에러 "+e);
                throw new RuntimeException(e);
            }
                Toast.makeText(DetailActivity.this, "sellBtn 클릭됨", Toast.LENGTH_SHORT).show();
                Log.d("ButtonClick", "sds");
            }
        });
    }
    private void fetchCandleData(String coinName) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.bithumb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BithumbApiService apiService = retrofit.create(BithumbApiService.class);
        Call<JsonObject> call = apiService.getCandleData(coinName+"_KRW");

        call.enqueue(new Callback<JsonObject>() {
            @Override
            public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ArrayList<CandleEntry> entries = new ArrayList<>();
                    JsonArray data = response.body().getAsJsonArray("data");
                    for (JsonElement element : data) {
                        JsonArray candlestick = element.getAsJsonArray();
                        long time = candlestick.get(0).getAsLong();
                        float open = candlestick.get(1).getAsFloat();
                        float high = candlestick.get(2).getAsFloat();
                        float low = candlestick.get(3).getAsFloat();
                        float close = candlestick.get(4).getAsFloat();
                        entries.add(new CandleEntry(time, high, low, open, close));
                    }
                    setCandleData(entries);
                }
            }

            @Override
            public void onFailure(Call<JsonObject> call, Throwable t) {
                Log.e("CandleChart", "API 호출 실패: " + t.getMessage());
            }
        });
    }

    private void setCandleData(ArrayList<CandleEntry> entries) {

        CandleDataSet dataSet = new CandleDataSet(entries, "Candle Data");
//        ArrayList<Integer> colors = new ArrayList<>();
//
//        for (CandleEntry entry : entries) {
//            if (entry.getClose() > entry.getOpen()) {
//                colors.add(Color.RED); // 종가 > 시가
//            } else {
//                colors.add(Color.BLUE); // 시가 > 종가
//            }
//        }


//        dataSet.setColors(colors); // Color for the body
//        dataSet.setShadowColor(Color.LTGRAY); // Color for the shadow
//
//        dataSet.setValueTextSize(10f); // Text size for value labels
//        dataSet.setDrawValues(false); // Set to true if you want to show the values
//
//        CandleData candleData = new CandleData(dataSet);
//        candleStickChart.setData(candleData);
//        candleStickChart.invalidate(); // refresh
//
//        // Additional configurations
//        candleStickChart.getDescription().setEnabled(false); // Disable description
//        candleStickChart.setDrawGridBackground(false); // Hide grid background
//        candleStickChart.setPinchZoom(true); // Enable pinch zoom
//        candleStickChart.setDragEnabled(true); // Enable dragging
//        candleStickChart.setScaleEnabled(true); // Enable scaling
        dataSet.setColor(Color.rgb(80, 80, 80));
        dataSet.setShadowColor(Color.LTGRAY);
        dataSet.setShadowWidth(0.8f);
        dataSet.setDecreasingColor(Color.BLUE);
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(Color.RED);
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        dataSet.setNeutralColor(Color.LTGRAY);
        dataSet.setDrawValues(false);



// create a data object with the datasets
        CandleData data = new CandleData(dataSet);


// set data
        candleStickChart.setData(data);
        candleStickChart.invalidate();
    }

}