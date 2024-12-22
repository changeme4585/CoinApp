package com.example.coinapp;

import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {
    private  TradeDB tradeDB;
    private Button buttonCoinList;
    private Button buttonOwnedCoins;
    private Button buttonTradeHistory;
    private Button buttonGPT;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // 위에서 수정한 레이아웃

        tradeDB = new TradeDB(this);
        SQLiteDatabase db = tradeDB.getReadableDatabase();
        tradeDB.onCreate(db);


        buttonCoinList = findViewById(R.id.buttonCoinList);
        buttonOwnedCoins = findViewById(R.id.buttonOwnedCoins);
        buttonTradeHistory = findViewById(R.id.buttonTradeHistory);
        buttonGPT = findViewById(R.id.buttonGPT);

        // 앱 시작 시 기본 페이지: CoinListFragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CoinListFragment())
                    .commit();
        }

        buttonCoinList.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new CoinListFragment())
                    .commit();
        });

        buttonOwnedCoins.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new OwnedCoinsFragment())
                    .commit();
        });

        buttonTradeHistory.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new SearchCoinFragment())
                    .commit();
        });

        buttonGPT.setOnClickListener(v -> {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, new GptFragment())
                    .commit();
        });
    }
}
