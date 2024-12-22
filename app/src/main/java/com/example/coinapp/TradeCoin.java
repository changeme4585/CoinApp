package com.example.coinapp;

import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.URLEncodedUtils;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TradeCoin {

    public String getCoinPrice(String coinName) throws IOException, JSONException, ExecutionException, InterruptedException {


        Callable<String> callable = () -> {
            String URL = "https://api.bithumb.com/public/transaction_history/"+coinName+"_KRW";

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(URL)
                    .get()
                    .build();
            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();
            JSONObject json = new JSONObject(body.string());
            JSONArray dataArray= json.getJSONArray("data");

            JSONObject json1 = new JSONObject(dataArray.get(dataArray.length()-1).toString());
            String coinPrice = json1.get("price").toString();
            // ... (네트워크 요청 코드)
            return coinPrice;
        };

        FutureTask<String> futureTask = new FutureTask<>(callable);
        new Thread(futureTask).start();
        return futureTask.get(); // 결과를 반환할 때까지 대기



    }

    public  Map<String, Object> buy_coin(String coinName, int cashAmount) throws JSONException, IOException, ExecutionException, InterruptedException {
        UserInfo userInfo = new UserInfo();
        String key = userInfo.api_key;
        String sec = userInfo.sec_key;
        Api_Client api = new Api_Client(key,sec);
        String state = "";
        Map<String, Object> returnValues = new HashMap<>();
        String coinPrice = getCoinPrice(coinName);
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("currency", coinName);
        String result1 = api.callApi("/info/balance", rgParams);

        JSONObject json = new JSONObject(result1);
        String data = json.getString("data"); //string()쓰기

        JSONObject json1 = new JSONObject(data);
        String total_krw = json1.getString("total_krw").toString();  //보유 자산 구하기

        if( Double.parseDouble(total_krw)<cashAmount){
            state=  "현금이 부족합니다.";
        }

        Double unit = Double.parseDouble(total_krw)/Double.parseDouble(coinPrice)*0.69;
        if(Double.parseDouble(coinPrice)*unit<5000) {
            state= "최소 주문금액은 5000원 입니다." ;
        }
//        if(Double.parseDouble(coinPrice)*unit>Double.parseDouble(total_krw)*0.69&Double.parseDouble(total_krw)<500){
//            return ;
//        }
        DecimalFormat decimalFormat = new DecimalFormat("#.####");

        // 잘라내기
        String formattedNumber = decimalFormat.format(unit);

        HashMap<String, String> rgParams1 = new HashMap<String, String>();
        rgParams1.put("units", formattedNumber); //소수점 4자리 맞추기=코인수량
        rgParams1.put("order_currency", coinName); //매수 하려는 코인 이름
        rgParams1.put("payment_currency", "KRW"); // 매수하려는 통화
        api.callApi("/trade/market_buy", rgParams1);
        state = "ok";
        returnValues.put("state", state);  //상태
        returnValues.put("amount",formattedNumber); //매수 수량
        returnValues.put("coinPrice",coinPrice); // 매수 체결 가격
       return returnValues;
    }
    public  void  sell_coin(Double units,String order_currency) throws IOException, ExecutionException, InterruptedException {
        ///units 매도수량 , order_currency 매도 할 코인이름
        //OkHttpClient client = new OkHttpClient();
        UserInfo userInfo = new UserInfo();
        String key = userInfo.api_key;
        String sec = userInfo.sec_key;
        Api_Client apiClient = new Api_Client(key,sec);

        DecimalFormat decimalFormat = new DecimalFormat("#.####");
        String formattedNumber = decimalFormat.format(units);
        System.out.println("order_currency: "+order_currency);
        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("units", formattedNumber); //소수점 4자리 맞추기
        rgParams.put("order_currency", order_currency); //매도 하려는 코인 이름
        rgParams.put("payment_currency", "KRW"); //매도하려는 통화
        apiClient.callApi("/trade/market_sell", rgParams);

    }

}
