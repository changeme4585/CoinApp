package com.example.coinapp;

import android.os.Build;

import androidx.annotation.RequiresApi;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.utils.URLEncodedUtils;
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TradeCoin {
    public  void  sell_coin(String units,String order_currency) throws IOException{

        //OkHttpClient client = new OkHttpClient();
        UserInfo userInfo = new UserInfo();
        String key = userInfo.api_key;
        String sec = userInfo.sec_key;
        Api_Client apiClient = new Api_Client(key,sec);

        HashMap<String, String> rgParams = new HashMap<String, String>();
        rgParams.put("units", units); //소수점 4자리 맞추기
        rgParams.put("order_currency", order_currency); //매도 하려는 코인 이름
        rgParams.put("payment_currency", "KRW"); //매도하려는 통화
        apiClient.callApi("/trade/market_sell", rgParams);

    }

}
