package com.example.barkodappjava;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class GuncelleActivity extends AppCompatActivity {

    private EditText urunAdiEditText, fiyatEditText, stokEditText, kategoriEditText;
    private Button guncelleBtn;
    private String barkod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_guncelle);
        APIHelper.initialize(this);

        barkod = getIntent().getStringExtra("barkod");

        urunAdiEditText = findViewById(R.id.urunAdiEditText);
        fiyatEditText = findViewById(R.id.fiyatEditText);
        stokEditText = findViewById(R.id.stokEditText);
        kategoriEditText = findViewById(R.id.kategoriEditText);
        guncelleBtn = findViewById(R.id.guncelleBtn);

        urunGetir();

        guncelleBtn.setOnClickListener(v -> kaydetGuncelle());
    }

    private void urunGetir() {
        APIHelper.getUrunByBarkod(barkod, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    // Bu kısımda PHP'den gelen veri array içinde olabilir veya doğrudan obje olabilir.
                    JSONArray urunler = response.optJSONArray("icerik");
                    if (urunler != null && urunler.length() > 0) {
                        JSONObject urun = urunler.getJSONObject(0);

                        urunAdiEditText.setText(urun.getString("urun_adi"));
                        fiyatEditText.setText(urun.getString("fiyat"));
                        stokEditText.setText(urun.getString("stok"));
                        kategoriEditText.setText(urun.getString("kategori"));
                    } else {
                        Toast.makeText(GuncelleActivity.this, "Ürün bulunamadı", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                } catch (Exception e) {
                    Toast.makeText(GuncelleActivity.this, "Veri çözümleme hatası: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccessString(String response) {}

            @Override
            public void onError(Exception error) {
                Toast.makeText(GuncelleActivity.this, "Sunucu hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void kaydetGuncelle() {
        Map<String, String> params = new HashMap<>();
        params.put("barkod", barkod);
        params.put("urun_adi", urunAdiEditText.getText().toString().trim());
        params.put("fiyat", fiyatEditText.getText().toString().trim());
        params.put("stok", stokEditText.getText().toString().trim());
        params.put("kategori", kategoriEditText.getText().toString().trim());

        APIHelper.guncelleUrun(params, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {}

            @Override
            public void onSuccessString(String response) {
                Toast.makeText(GuncelleActivity.this, "Ürün başarıyla güncellendi", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(GuncelleActivity.this, "Güncelleme hatası: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}

