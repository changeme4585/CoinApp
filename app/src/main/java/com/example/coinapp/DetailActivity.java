package com.example.coinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DetailActivity extends AppCompatActivity {
    private  TradeDB tradeDB;
    Button buyBtn;
    Button sellBtn ;
    EditText buyText;
    private CandleStickChart candleStickChart;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);  // activity_detail.xml로 설정

        tradeDB = new TradeDB(this);
        SQLiteDatabase db = tradeDB.getReadableDatabase();
        buyBtn = (Button) findViewById(R.id.buyBtn);
        sellBtn = (Button) findViewById(R.id.sellBtn);
        buyText = findViewById(R.id.buyText);  // 매수 할 현금량

//        candleStickChart = findViewById(R.id.candleStickChart);

        String coinCode = getIntent().getStringExtra("market");
        String coinName = coinCode.substring(4,coinCode.length());
        fetchCandleData(coinName);
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


        buyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TradeCoin tradeCoin = new TradeCoin();
                try {
                    String buyAmount = buyText.getText().toString(); // 매수 할 현금량
                    //String coinPrice =tradeCoin.getCoinPrice(coinName);
                    Map<String, Object> returnValues = tradeCoin.buy_coin(coinName,Integer.valueOf(buyAmount));
                    String state = String.valueOf(returnValues.get("state"));
                    if (!state.equals("ok")) {
                        Toast.makeText(DetailActivity.this, state, Toast.LENGTH_SHORT).show();

                    }else {
                        TradeDB dbHelper = new TradeDB(DetailActivity.this);
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        String amount = String.valueOf(returnValues.get("amount"));
                        String coinPrice =  String.valueOf(returnValues.get("coinPrice"));
                        String insertSql = "INSERT INTO " + TradeDB.TABLE_NAME + " (" +
                                TradeDB.state + ", " +
                                TradeDB.coinName + ", " +
                                TradeDB.coinPrice + ", " +
                                TradeDB.amount + ") VALUES (?, ?, ?, ?);";
                        db.execSQL(insertSql, new Object[]{"buy", coinName, coinPrice,amount});
                    }

                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (JSONException | ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        sellBtn.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {

            TradeCoin tradeCoin = new TradeCoin();
            try {
                Cursor cursor = db.rawQuery("SELECT state, amount, coinName, coinPrice FROM "
                        + tradeDB.TABLE_NAME
                        + " WHERE coinName = '" + coinName + "'", null);

                Double units = 0.0;  //해당 코인 보유 수량
                while (cursor.moveToNext()) {
                    String state = cursor.getString(cursor.getColumnIndexOrThrow(tradeDB.state));
                    String amount = cursor.getString(cursor.getColumnIndexOrThrow(tradeDB.amount));
                    String  coinName = cursor.getString(cursor.getColumnIndexOrThrow(tradeDB.coinName));
                    String  coinPrice  =  cursor.getString(cursor.getColumnIndexOrThrow(tradeDB.coinPrice));
                    if (state.equals("buy")){
                        units+=Double.valueOf(amount);
                    }else{
                        units-=Double.valueOf(amount);
                    }
                   // System.out.println("coinName: " + coinName + ", state: " + state + ", amount: " + amount + ", coinPrice: " + coinPrice );
                }

                cursor.close();
                tradeCoin.sell_coin(units,coinName);

                TradeDB dbHelper = new TradeDB(DetailActivity.this);
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String insertSql = "INSERT INTO " + TradeDB.TABLE_NAME + " (" +
                        TradeDB.state + ", " +
                        TradeDB.coinName + ", " +
                        TradeDB.coinPrice + ", " +
                        TradeDB.amount + ") VALUES (?, ?, ?, ?);";
                db.execSQL(insertSql, new Object[]{"sell", coinName, tradeCoin.getCoinPrice(coinName),units});

            } catch (IOException | ExecutionException | InterruptedException e) {
                System.out.println("매도 에러 "+e);
                throw new RuntimeException(e);
            } catch (JSONException e) {
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
                    List<Double> coinClose = new ArrayList<>();
                    ArrayList<CandleEntry> entries = new ArrayList<>();
                    JsonArray data = response.body().getAsJsonArray("data");
                    for (JsonElement element : data) {
                        JsonArray candlestick = element.getAsJsonArray();
                        long time = candlestick.get(0).getAsLong();
                        float open = candlestick.get(1).getAsFloat();
                        float high = candlestick.get(2).getAsFloat();
                        float low = candlestick.get(3).getAsFloat();
                        float close = candlestick.get(4).getAsFloat();
                        coinClose.add(Double.parseDouble(String.valueOf(close)));
                        entries.add(new CandleEntry(time, high, low, open, close));
                    }
                    TechnicalIndicators technicalIndicators = new TechnicalIndicators(coinClose);
                    System.out.println("RSI값: "+technicalIndicators.calculateRSI(14));
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