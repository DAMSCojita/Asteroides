package com.stefancojita.asteroides;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.widget.Toast;

public class PreferencesActivity extends PreferenceActivity {

    // Declaram un MediaPlayer per posar música de fons.
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        // Afegim un listener a la preferència fragments per controlar que el valor sigui correcte.
        final EditTextPreference fragments = (EditTextPreference) findPreference("fragments");

        // Mostrem un missatge informatiu amb el valor actual de la preferència.
        fragments.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int valor; // Valor de la preferència.
                // Comprovem que el valor sigui un número.
                try {
                    valor = Integer.parseInt((String) newValue); // Convertim el valor a enter.
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "Ha de ser un número", Toast.LENGTH_SHORT).show();
                    return false;
                }
                // Comprovem que el valor dels fragments sigui correcte.
                if (valor >= 0 && valor <= 9) {
                    fragments.setSummary("En quants trossos es divideix un asteroide (" + valor + ")");
                    return true;
                } else {
                    Toast.makeText(getApplicationContext(), "Màxim de fragments 9", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        });

        // Afegim un listener a la preferència música per controlar si s'ha d'iniciar o aturar la música.
        CheckBoxPreference musicPreference = (CheckBoxPreference) findPreference("musica");
        // Iniciem la música si la preferència està activada.
        if (musicPreference.isChecked()) {
            iniciaMusica();
        }

        // Iniciem o aturem la música segons el valor de la preferència.
        musicPreference.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean isMusicEnabled = (Boolean) newValue; // Valor de la preferència.
                // Iniciem o aturem la música segons el valor de la preferència.
                if (isMusicEnabled) {
                    iniciaMusica();
                } else {
                    paraMusica();
                }
                return true;
            }
        });
    }

    // Cream un mètode per iniciar la música de fons.
    private void iniciaMusica() {
        // Iniciem la música si no està ja en marxa.
        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(this, R.raw.spring_yard); // Creem un nou MediaPlayer amb la música.
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