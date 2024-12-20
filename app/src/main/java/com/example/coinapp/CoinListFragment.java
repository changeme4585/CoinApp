package com.example.coinapp;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class CoinListFragment extends Fragment {

    private RecyclerView recyclerView;
    private EnglishNameAdapter adapter;
    private List<String> englishNames = new ArrayList<>();
    private List<String> markets = new ArrayList<>();

    public CoinListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // fragment_coin_list.xml을 inflate
        return inflater.inflate(R.layout.fragment_coin_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        recyclerView = view.findViewById(R.id.recyclerView);

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
                    for (JsonElement a : response.body().getAsJsonArray()) {
                        englishNames.add(a.getAsJsonObject().get("english_name").getAsString());
                        markets.add(a.getAsJsonObject().get("market").getAsString());  // 마켓 코드 추가
                    }
                    setupRecyclerView();
                }
            }

            @Override
            public void onFailure(Call<JsonArray> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new EnglishNameAdapter(getContext(), englishNames, markets);
        recyclerView.setAdapter(adapter);
    }
}
