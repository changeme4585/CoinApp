package com.example.coinapp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EnglishNameAdapter extends RecyclerView.Adapter<EnglishNameAdapter.ViewHolder> {

    private List<String> englishNames;
    private List<String> markets;  // 마켓 코드 리스트 추가
    private Context context;

    public EnglishNameAdapter(Context context, List<String> englishNames, List<String> markets) {
        this.context = context;
        this.englishNames = englishNames;
        this.markets = markets;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(englishNames.get(position));

        // 클릭 리스너 추가
        holder.itemView.setOnClickListener(v -> {
            // 클릭 시 새로운 Activity로 이동하며 마켓 코드 전달
            Intent intent = new Intent(context, DetailActivity.class);
            intent.putExtra("coinName",englishNames.get(position));
            intent.putExtra("market", markets.get(position));  // 마켓 코드 추가
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return englishNames.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
