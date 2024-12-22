package com.example.coinapp;
//
//import androidx.annotation.NonNull;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.List;
//
//import okhttp3.Request;
//import okhttp3.RequestBody;
//import retrofit2.Callback;
//
//public class ChatGPT {
//    List<Message> messageList;
//    public void callAPI(String question){
//        //okhttp
//        messageList.add(new Message("...", Message.SENT_BY_BOT));
//
//        //추가된 내용
//        JSONArray arr = new JSONArray();
//        JSONObject baseAi = new JSONObject();
//        JSONObject userMsg = new JSONObject();
//        try {
//            //AI 속성설정
//            baseAi.put("role", "user");
//            baseAi.put("content", "You are a helpful and kind AI Assistant.");
//            //유저 메세지
//            userMsg.put("role", "user");
//            userMsg.put("content", question);
//            //array로 담아서 한번에 보낸다
//            arr.put(baseAi);
//            arr.put(userMsg);
//        } catch ( JSONException e) {
//            throw new RuntimeException(e);
//        }
//
//        JSONObject object = new JSONObject();
//        try {
//            //모델명 변경
//            object.put("model", "gpt-3.5-turbo-0125");
//            object.put("messages", arr);
////            아래 put 내용은 삭제하면 된다
////            object.put("model", "text-davinci-003");
////            object.put("prompt", question);
////            object.put("max_tokens", 4000);
////            object.put("temperature", 0);
//
//        } catch (JSONException e){
//            e.printStackTrace();
//        }
//        RequestBody body = RequestBody.create(object.toString(), JSON);
//        Request request = new Request.Builder()
//                .url("https://api.openai.com/v1/chat/completions")  //url 경로 수정됨
//                .header("Authorization", "Bearer "+MY_SECRET_KEY)
//                .post(body)
//                .build();
//
//        client.newCall(request).enqueue(new Callback() {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e) {
//                addResponse("Failed to load response due to "+e.getMessage());
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
//                if(response.isSuccessful()){
//                    JSONObject jsonObject = null;
//                    try {
//                        jsonObject = new JSONObject(response.body().string());
//                        JSONArray jsonArray = jsonObject.getJSONArray("choices");
//                        //아래 result 받아오는 경로가 좀 수정되었다.
//                        String result = jsonArray.getJSONObject(0).getJSONObject("message").getString("content");
//                        addResponse(result.trim());
//                    }catch (JSONException e){
//                        e.printStackTrace();
//                    }
//                } else {
//                    addResponse("Failed to load response due to "+response.body().string());
//                }
//            }
//        });
//    }
//
//}


import android.util.Log;


import com.example.coinapp.dto.ChatRequest;
import com.example.coinapp.dto.ChatResponse;
import com.example.coinapp.dto.Message;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatGPT {


    private static final String TAG = "ChatGPT";
    private static final String CHAT_MODEL = "gpt-3.5-turbo"; // Replace with your chat model
    private static final String CHAT_API_URL = "https://api.openai.com/v1/chat/completions"; // Replace with your chat API URL
    static UserInfo userInfo = new UserInfo();
    private static final String OPENAI_API_KEY = userInfo.gpt_key; // Replace with your API key
    private final OkHttpClient client;
    private final Gson gson;
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    public ChatGPT() {
        this.client = new OkHttpClient();
        this.gson = new Gson();
    }

    public String chat(String prompt) throws ExecutionException, InterruptedException {

        Callable<String> callable = () -> {
            System.out.println("질문: "+prompt);
            ChatRequest request = new ChatRequest(CHAT_MODEL, prompt);
            request.setMessages(new ArrayList<>());
            request.getMessages().add(new Message("system", prompt));

            String requestBody = gson.toJson(request);
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            RequestBody body = RequestBody.create(requestBody, JSON);

            Request httpRequest = new Request.Builder()
                    .url(CHAT_API_URL)
                    .addHeader("Authorization", "Bearer " + OPENAI_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .post(body)
                    .build();
        try (Response response = client.newCall(httpRequest).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().string();
                ChatResponse chatResponse = gson.fromJson(responseBody, ChatResponse.class);
                return chatResponse.getChoices().get(0).getMessage().getContent();
            } else {
                Log.e(TAG, "Chat API Error: " + response.message());
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException: " + e.getMessage(), e);
        }
        return "An error occurred";
        };
        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        return futureTask.get(); // 결과를 반환할 때까지 대기
    }

    public String question(String prompt) throws ExecutionException, InterruptedException {
        String chatAnswer = chat(prompt);
        Log.d(TAG, "Chat response: " + chatAnswer);
        return chatAnswer;
    }
}

