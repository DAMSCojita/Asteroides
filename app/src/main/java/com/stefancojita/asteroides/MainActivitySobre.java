package com.stefancojita.asteroides;

import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivitySobre extends AppCompatActivity {

    // Declaram un MediaPlayer per posar música de fons.
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_sobre);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Verifiquem si la preferència de música está activa.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean musicaActiva = pref.getBoolean("musica", true);

        // Iniciem la música si la preferència está activa.
        if (musicaActiva) {
            iniciaMusica();
        }
    }

    // Cream un mètode per iniciar la música de fons.
    private void iniciaMusica() {
        // Iniciem la música si no està ja en marxa.
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.spider_dance); // Creem un nou MediaPlayer amb la música.
            mediaPlayer.setLooping(true); // Indiquem que la música s'ha de repetir.
            mediaPlayer.start(); // Iniciem la música.
        }
    }

    // Cream un mètode per aturar la música de fons.
    private void paraMusica() {
        // Aturem la música si està en marxa.
        if (mediaPlayer != null) {
            mediaPlayer.stop(); // Aturem la música.
            mediaPlayer.release(); // Alliberem els recursos del MediaPlayer.
            mediaPlayer = null; // Indiquem que el MediaPlayer ja no està en marxa.
        }
    }

    // Aturem la música quan es tanca l'activitat.
    @Override
    protected void onDestroy() {
        super.onDestroy();
        paraMusica(); // Aturem la música.
    }
}