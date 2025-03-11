package com.stefancojita.asteroides;

import android.os.Bundle;
import android.preference.PreferenceManager;

import androidx.appcompat.app.AppCompatActivity;

public class GameActivity extends AppCompatActivity {

    private AsteroidsView gameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        gameView = (AsteroidsView)findViewById(R.id.AsteroidsView);

        gameView.setParent(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Comprovam que gameView no sigui null, ho feim per tots els cicles de vida.
        if (gameView != null) {
            // Reanudam el fil principal del joc, assegurant que continuï l'execució.
            gameView.getThread().unpause();
            // Comprovem si la configuració de sensors està activada i configurada al mode adequat.
            if (PreferenceManager.getDefaultSharedPreferences(this).getString("sensors", "1").equals("2")) {
                // Activem els sensors (per exemple, acceleròmetre) si la configuració ho especifica.
                gameView.activateSensors();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameView != null) {
            // Pausem el fil principal del joc per evitar que continuï executant-se en segon pla.
            gameView.getThread().pause();
            // Desactivem els sensors.
            gameView.deactivateSensors();
        }
    }

    @Override
    protected void onDestroy() {
        if (gameView != null) {
            gameView.getThread().halt(); // Aturem completament el fil del joc, alliberant recursos.
        }
        super.onDestroy();
    }

}
