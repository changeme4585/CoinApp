package com.example.coinapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EnglishNameAdapter adapter;
    private List<String> englishNames = new ArrayList<>();  // 빈 리스트로 초
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        System.out.println("여기:");
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.bithumb.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        BithumbApiService apiService = retrofit.create(BithumbApiService.class);
        Call<JsonArray> call = apiService.getMarketData(false);

        call.enqueue(new Callback<JsonArray>() {
            @Override
            public void onResponse(Call<JsonArray> call, Response<JsonArray> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<String> markets = new ArrayList<>();
                    //List<String> englishNames = new ArrayList<>();
                    for(JsonElement a:response.body().getAsJsonArray()){
                        englishNames.add( a.getAsJsonObject().get("english_name").getAsString());
                        markets.add(a.getAsJsonObject().get("market").getAsString());  // 마켓 코드 추가
                    }
//                    // 여기서 RecyclerView에 데이터를 넘겨주면 돼
                    setupRecyclerView(englishNames, markets);  // 마켓 코드 전달
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                // 에러 처리
                t.printStackTrace();  // 에러 로그 출력
                System.out.println("API 호출 실패: " + t.getMessage());
            }
        });

    }
    private void setupRecyclerView(List<String> englishNames, List<String> markets) {
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new EnglishNameAdapter(this, englishNames, markets);  // Context 추가
        recyclerView.setAdapter(adapter);
    }

}