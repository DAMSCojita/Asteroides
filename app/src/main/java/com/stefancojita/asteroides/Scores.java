package com.stefancojita.asteroides;

import android.app.ListActivity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class Scores extends ListActivity {

    // Declaram un MediaPlayer per posar música de fons.
    private MediaPlayer mediaPlayer;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scores);
        setListAdapter(new MyAdapter(this, MainActivity.scoreStorage.getScoreList(10)));

        // Verifiquem si la preferència de música está activa.
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean musicaActiva = pref.getBoolean("musica", true);

        // Iniciem la música si la preferència está activa.
        if (musicaActiva) {
            iniciaMusica();
        }
    }

    @Override
    protected void onListItemClick(ListView listView, View view, int position, long id) {
        super.onListItemClick(listView, view, position, id);
        Object o = getListAdapter().getItem(position);
        Toast.makeText(this, "Selecció: " + Integer.toString(position) + " - " + o.toString(), Toast.LENGTH_LONG).show();
    }

    // Cream un mètode per iniciar la música de fons.
    private void iniciaMusica() {
        // Iniciem la música si no està ja en marxa.
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.once_upon_a_time); // Creem un nou MediaPlayer amb la música.
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
