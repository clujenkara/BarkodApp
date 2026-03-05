package com.example.barkodappjava;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UrunListeActivity extends AppCompatActivity {

    private LinearLayout kategoriLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urun_liste);
        APIHelper.initialize(this);
        kategoriLayout = findViewById(R.id.kategoriLayout);
        urunleriGetir();
    }
    @Override
    protected void onResume() {
        super.onResume();
        urunleriGetir();
    }
    private void urunleriGetir() {
        APIHelper.getUrunler(new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray urunler = response.getJSONArray("icerik");

                    Map<String, List<JSONObject>> kategoriMap = new HashMap<>();

                    // Kategorilere göre grupla (ÇAKIŞMAYAN ŞEKİLDE)
                    for (int i = 0; i < urunler.length(); i++) {
                        JSONObject urun = urunler.getJSONObject(i);
                        String kategori = urun.optString("kategori", "Bilinmeyen");

                        if (!kategoriMap.containsKey(kategori)) {
                            kategoriMap.put(kategori, new ArrayList<>());
                        }
                        kategoriMap.get(kategori).add(urun);
                    }

                    kategoriLayout.removeAllViews();

                    // Kategorileri ve ürünleri ekrana bas
                    for (String kategori : kategoriMap.keySet()) {
                        kategoriLayout.addView(kategoriBaslikOlustur(kategori));

                        for (JSONObject urun : kategoriMap.get(kategori)) {
                            kategoriLayout.addView(urunKartOlustur(urun));
                        }
                    }

                } catch (Exception e) {
                    Toast.makeText(UrunListeActivity.this, "Listeleme hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccessString(String response) { }

            @Override
            public void onError(Exception error) {
                Toast.makeText(UrunListeActivity.this, "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private TextView kategoriBaslikOlustur(String kategori) {
        TextView baslik = new TextView(this);
        baslik.setText("Kategori: " + kategori);
        baslik.setTextSize(22);
        baslik.setGravity(Gravity.CENTER);
        baslik.setPadding(0, 30, 0, 20);
        return baslik;
    }

    private CardView urunKartOlustur(JSONObject urun) throws Exception {
        CardView card = new CardView(this);
        card.setRadius(20);
        card.setCardElevation(10);
        card.setUseCompatPadding(true);
        card.setContentPadding(20, 20, 20, 20);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(20, 20, 20, 20);
        card.setLayoutParams(params);

        LinearLayout cardLayout = new LinearLayout(this);
        cardLayout.setOrientation(LinearLayout.VERTICAL);

        final String barkod = urun.getString("barkod");
        cardLayout.addView(bilgiYaz("Barkod", barkod));
        cardLayout.addView(bilgiYaz("Ürün Adı", urun.getString("urun_adi")));
        cardLayout.addView(bilgiYaz("Fiyat", urun.getString("fiyat")));
        cardLayout.addView(bilgiYaz("Stok", urun.getString("stok")));
        cardLayout.addView(bilgiYaz("Kategori", urun.getString("kategori")));

        // Sil Butonu
        Button silBtn = new Button(this);
        silBtn.setText("SİL");
        silBtn.setOnClickListener(v -> urunSil(barkod));
        cardLayout.addView(silBtn);

        // Güncelle Butonu
        Button guncelleBtn = new Button(this);
        guncelleBtn.setText("GÜNCELLE");
        guncelleBtn.setOnClickListener(v -> {
            Intent intent = new Intent(UrunListeActivity.this, GuncelleActivity.class);
            intent.putExtra("barkod", barkod);
            startActivity(intent);
        });
        cardLayout.addView(guncelleBtn);

        card.addView(cardLayout);
        return card;
    }

    private TextView bilgiYaz(String etiket, String veri) {
        TextView tv = new TextView(this);
        tv.setText(etiket + ": " + veri);
        tv.setTextSize(16);
        return tv;
    }

    private void urunSil(String barkod) {
        APIHelper.silUrun(barkod, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                Toast.makeText(UrunListeActivity.this, "Ürün silindi.", Toast.LENGTH_SHORT).show();
                urunleriGetir();
            }

            @Override
            public void onSuccessString(String response) {
                Toast.makeText(UrunListeActivity.this, "Ürün silindi.", Toast.LENGTH_SHORT).show();
                urunleriGetir();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(UrunListeActivity.this, "Silme hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }


        });
    }
}
