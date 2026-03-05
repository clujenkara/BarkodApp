package com.example.barkodappjava;

import android.content.Context;
import java.util.HashMap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.Map;

public class APIHelper {

    private static final String BASE_URL = "http://192.168.58.70:8080/urunlerapi/urunler_api.php";  // kendi IP adresin

    private static RequestQueue requestQueue;

    public static void initialize(Context context) {
        if (requestQueue == null)
            requestQueue = Volley.newRequestQueue(context);
    }


    // Ürünleri listele (GET)
    public static void getUrunler(ResponseListener listener) {
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, BASE_URL, null,
                listener::onSuccess,
                error -> listener.onError(new Exception(error.toString())));
        requestQueue.add(request);
    }
    public static void getProductFromOpenFoodFacts(String barkod, ResponseListener listener) {
        String url = "https://world.openfoodfacts.org/api/v0/product/" + barkod + ".json";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                listener::onSuccess,
                error -> listener.onError(new Exception(error.toString()))
        );
        requestQueue.add(request);
    }

    // Ürün ekle (POST)
    public static void ekleUrun(Map<String, String> params, ResponseListener listener) {
        StringRequest request = new StringRequest(Request.Method.POST, BASE_URL,
                listener::onSuccessString,
                error -> listener.onError(new Exception(error.toString()))) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        requestQueue.add(request);
    }
    public static void guncelleUrun(Map<String, String> params, ResponseListener listener) {
        StringRequest request = new StringRequest(Request.Method.PUT, BASE_URL,
                listener::onSuccessString,
                error -> listener.onError(new Exception(error.toString()))) {
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };
        requestQueue.add(request);
    }
    public static void silUrun(String barkod, ResponseListener listener) {
        String url = BASE_URL + "?barkod=" + barkod;

        StringRequest request = new StringRequest(Request.Method.DELETE, url,
                listener::onSuccessString,
                error -> listener.onError(new Exception(error.toString()))
        );

        requestQueue.add(request);
    }


    // Barkod ile ürün sorgulama (GET) — ÜRÜN TANIMA İÇİN
    public static void getUrunByBarkod(String barkod, ResponseListener listener) {
        String url = BASE_URL + "?barkod=" + barkod;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        listener.onSuccess(response);
                    } catch (Exception e) {
                        listener.onError(e);
                    }
                },
                error -> listener.onError(new Exception(error.toString()))
        );

        requestQueue.add(request);
    }

    // Ortak response interface
    public interface ResponseListener {
        void onSuccess(JSONObject response);
        void onSuccessString(String response);
        void onError(Exception error);
    }
}
