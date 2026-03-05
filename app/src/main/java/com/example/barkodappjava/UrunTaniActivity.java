package com.example.barkodappjava;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;
import androidx.activity.result.ActivityResultLauncher;

public class UrunTaniActivity extends AppCompatActivity {

    private TextView sonucTextView;
    private Button barkodTaraBtn, kaydetBtn;
    private String sonBarkod = "";
    private String urunAdi = "Bilinmeyen";
    private String kategori = "Bilinmeyen";

    private final ActivityResultLauncher<ScanOptions> barcodeLauncher =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    sonBarkod = result.getContents();
                    urunSorgula(sonBarkod);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_urun_tani);
        APIHelper.initialize(this);

        sonucTextView = findViewById(R.id.sonucTextView);
        barkodTaraBtn = findViewById(R.id.barkodTaraBtn);
        kaydetBtn = findViewById(R.id.kaydetBtn);

        kaydetBtn.setEnabled(false); // İlk başta pasif

        barkodTaraBtn.setOnClickListener(v -> scanCode());

        kaydetBtn.setOnClickListener(v -> urunuKaydet());
    }

    private void scanCode() {
        ScanOptions options = new ScanOptions();
        options.setPrompt("Barkodu göster");
        options.setBeepEnabled(true);
        options.setOrientationLocked(true);
        barcodeLauncher.launch(options);
    }

    private void urunSorgula(String barkod) {
        APIHelper.getProductFromOpenFoodFacts(barkod, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.getInt("status") == 1) {
                        JSONObject product = response.getJSONObject("product");
                        urunAdi = product.optString("product_name", "Ürün adı bulunamadı");
                        kategori = product.optString("categories", "Kategori yok");

                        String detay = "Barkod: " + barkod + "\nÜrün: " + urunAdi + "\nKategori: " + kategori;
                        sonucTextView.setText(detay);
                        kaydetBtn.setEnabled(true);
                    } else {
                        sonucTextView.setText("İnternette ürün bulunamadı.");
                        kaydetBtn.setEnabled(false);
                    }
                } catch (Exception e) {
                    sonucTextView.setText("Hata: " + e.getMessage());
                }
            }

            @Override
            public void onSuccessString(String response) {}

            @Override
            public void onError(Exception error) {
                sonucTextView.setText("Hata: " + error.getMessage());
            }
        });
    }

    private void urunuKaydet() {
        Map<String, String> params = new HashMap<>();
        params.put("barkod", sonBarkod);
        params.put("urun_adi", urunAdi);
        params.put("fiyat", "0");
        params.put("stok", "1");
        params.put("kategori", kategori);

        APIHelper.ekleUrun(params, new APIHelper.ResponseListener() {
            @Override
            public void onSuccess(JSONObject response) {}

            @Override
            public void onSuccessString(String response) {
                Toast.makeText(UrunTaniActivity.this, "Ürün başarıyla kaydedildi!", Toast.LENGTH_SHORT).show();
                kaydetBtn.setEnabled(false);
            }

            @Override
            public void onError(Exception error) {
                Toast.makeText(UrunTaniActivity.this, "Hata: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
