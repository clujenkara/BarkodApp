package com.example.barkodappjava;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import androidx.activity.result.ActivityResultLauncher;

public class MainActivity extends AppCompatActivity {

    private Button kaydetBtn, barkodBtn, getirBtn, urunTaniBtn;
    private EditText barkodEditText, urunAdiEditText, fiyatEditText, stokEditText, kategoriEditText;

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String barkod = result.getContents();
                    barkodEditText.setText(barkod);
                    barkodSonrasiVeriGetir(barkod);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        APIHelper.initialize(this);

        barkodEditText = findViewById(R.id.barkodEditText);
        urunAdiEditText = findViewById(R.id.urunAdiEditText);
        fiyatEditText = findViewById(R.id.fiyatEditText);
        stokEditText = findViewById(R.id.stokEditText);
        kategoriEditText = findViewById(R.id.kategoriEditText);
        kaydetBtn = findViewById(R.id.kaydetBtn);
        barkodBtn = findViewById(R.id.scanButton);
        getirBtn = findViewById(R.id.getirBtn);
        urunTaniBtn = findViewById(R.id.urunTaniBtn);

        urunTaniBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UrunTaniActivity.class);
            startActivity(intent);
        });
        kaydetBtn.setOnClickListener(v -> kaydetUrun());
        barkodBtn.setOnClickListener(v -> scanCode());
        getirBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, UrunListeActivity.class);
            startActivity(intent);
        });
    }

    private void kaydetUrun() {
        Map<String, String> params = new HashMap<>();
        params.put("barkod", barkodEditText.getText().toString());
        params.put("urun_adi", urunAdiEditText.getText().toString());
        params.put("fiyat", fiyatEditText.getText().toString());
        params.put("stok", stokEditText.getText().toString());
        params.put("kategori", kategoriEditText.getText().toString());

        APIHelper.ekleUrun(params, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) { }

            @Override
            public void onSuccessString(String response) {
                Toast.makeText(MainActivity.this, "Kayıt Başarılı", Toast.LENGTH_SHORT).show();
                temizleForm();
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(MainActivity.this, "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void temizleForm() {
        barkodEditText.setText("");
        urunAdiEditText.setText("");
        fiyatEditText.setText("");
        stokEditText.setText("");
        kategoriEditText.setText("");
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Barkodu göster");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    // Barkod sonrası hem veritabanı hem internet kontrolü yap
    private void barkodSonrasiVeriGetir(String barkod) {
        APIHelper.getUrunByBarkod(barkod, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    JSONArray arr = response.getJSONArray("icerik");
                    if (arr.length() > 0) {
                        JSONObject urun = arr.getJSONObject(0);
                        urunAdiEditText.setText(urun.getString("urun_adi"));
                        fiyatEditText.setText(urun.getString("fiyat"));
                        stokEditText.setText(urun.getString("stok"));
                        kategoriEditText.setText(urun.getString("kategori"));
                    } else {
                        // Veritabanında yoksa OpenFoodFacts'ten getir
                        getFromOpenFoodFacts(barkod);
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "Veritabanı okuma hatası", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccessString(String response) { }

            @Override
            public void onError(Exception error) {
                getFromOpenFoodFacts(barkod);
            }
        });
    }

    private void getFromOpenFoodFacts(String barkod) {
        APIHelper.getProductFromOpenFoodFacts(barkod, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getInt("status") == 1) {
                        JSONObject product = response.getJSONObject("product");
                        urunAdiEditText.setText(product.optString("product_name", ""));
                        kategoriEditText.setText(product.optString("categories", ""));
                        fiyatEditText.setText("0");
                        stokEditText.setText("1");
                        Toast.makeText(MainActivity.this, "Ürün OpenFoodFacts'ten getirildi", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity.this, "OpenFoodFacts: Ürün bulunamadı.", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Toast.makeText(MainActivity.this, "OpenFoodFacts veri çözümleme hatası", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onSuccessString(String response) { }

            @Override
            public void onError(Exception error) {
                Toast.makeText(MainActivity.this, "OpenFoodFacts bağlantı hatası", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
