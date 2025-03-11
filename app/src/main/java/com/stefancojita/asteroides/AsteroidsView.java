package com.stefancojita.asteroides;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.graphics.drawable.shapes.RectShape;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class AsteroidsView extends View implements SensorEventListener {
    // //// THREAD I TEMPS //////
    // Thread encarregat de processar el joc
    private GameThread thread = new GameThread();
    // Cada quan volem processar canvis (ms)
    private static int ANIM_INTERVAL = 50;
    // Quan es va realitzar el darrer procés
    private long prevUpdate = 0;

    // //// ASTEROIDS //////
    private List<AsteroidsGraphic> asteroids;
    private int numAsteroids = 5;
    private int numFragments = 3; // Fragments en que es divideix

    /////// SPACESHIP //////
    private AsteroidsGraphic ship;
    private int angleShip;
    private float accelShip;
    private static final double SHIP_MAX_SPEED = 50;
    private static final int STEPSIZE_ROT_SHIP = 5;
    private static final float STEPSIZE_ACCEL_SHIP = 0.5f;
    Drawable drawableMissile;

    ////// MISIL //////
    private List<AsteroidsGraphic> missiles = new ArrayList<AsteroidsGraphic>();
    private static int MISSILE_SPEED = 12;
    private List<Double> missileLifetimes = new ArrayList<Double>();
    private boolean fire = false;

    float mX;
    float mY;

    /////// SENSORS //////
    SensorManager mSensorManager;
    Sensor accelerometerSensor;

    private SharedPreferences pref; // Declaram es SharedPreferences per poder accedir a elles.

    /////// SOROLL I MÚSICA //////
    SoundPool soundPool;
    private MediaPlayer musicaDeFons; // Declaram una variable per representar la música de fons.
    private int musicaActual = 0; // Declaram una variable per a la música actual.

    int idFire, idExplosion;

    private Drawable drawableAsteroid[]= new Drawable[3]; // Reemplaçem el antic drawableAsteroid per un nou array.

    private int score = 0; // Declaram la nova variable per la puntuació.

    /////// PULSACIONS EXTRA PER TELETRANSPORTACIÓ //////
    private int idTeletransportacio; // Identificador del soroll de teletransportació.
    private long ultimaPulsacio = 0; // Temps de la última pulsació.
    private static final long DOBLE_PULSACIO = 300; // Declaram una variable per representar el unbral i considerar una doble pulsació.

    public AsteroidsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Definim variables al constructor.
        Drawable drawableShip;
        pref = PreferenceManager.getDefaultSharedPreferences(getContext()); // Molt important definir el pref en el constructor.
        soundPool = new SoundPool( 10, AudioManager.STREAM_MUSIC , 0);
        idFire = soundPool.load(context, R.raw.dispar, 0);
        idExplosion = soundPool.load(context, R.raw.explosio, 0);
        numFragments = Integer.parseInt(pref.getString("fragments", "3")); // Definim el num de fragments per poder dividr el asteroide, accedim a ell amb la variable pref.
        idTeletransportacio = soundPool.load(context, R.raw.teletransportacio, 0); // Carregam el so de teletransportació.
        // Gestió de gràfics a Prefèrencies.
        if (pref.getString("grafics", "1").equals("0")) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            Path pathAsteroid = new Path();// Definim el nou path per l'asteroide.
            // Definim la forma d'un asteroide traçant un polígon tancat mitjançant una sèrie de línies connectades a un 'Path', començant a (0.3, 0.0) i tornant al mateix punt.
            pathAsteroid.moveTo((float) 0.3, (float) 0.0);
            pathAsteroid.lineTo((float) 0.6, (float) 0.0);
            pathAsteroid.lineTo((float) 0.6, (float) 0.3);
            pathAsteroid.lineTo((float) 0.8, (float) 0.2);
            pathAsteroid.lineTo((float) 1.0, (float) 0.4);
            pathAsteroid.lineTo((float) 0.8, (float) 0.6);
            pathAsteroid.lineTo((float) 0.9, (float) 0.9);
            pathAsteroid.lineTo((float) 0.8, (float) 1.0);
            pathAsteroid.lineTo((float) 0.4, (float) 1.0);
            pathAsteroid.lineTo((float) 0.0, (float) 0.6);
            pathAsteroid.lineTo((float) 0.0, (float) 0.2);
            pathAsteroid.lineTo((float) 0.3, (float) 0.0);
            // Cas que volguem treballar amb gràfics vectorials després de canviar el drawableAsteroid.
            // Al ser un array ara recorrem per a cada posició.
            for (int i = 0;  i < 3; i++) {
                // Definim el nou drawableAsteroid amb el nou path.
                ShapeDrawable dAsteroid = new ShapeDrawable(new PathShape(pathAsteroid, 1, 1));
                dAsteroid.getPaint().setColor(Color.WHITE); // Definim el color de l'asteroide.
                dAsteroid.getPaint().setStyle(Paint.Style.STROKE); // Definim el tipus de línia.
                dAsteroid.setIntrinsicWidth(50 - i * 14); // Definim l'ample de l'asteroide.
                dAsteroid.setIntrinsicHeight(50 - i * 14); // Definim l'alt de l'asteroide.
                drawableAsteroid[i] = dAsteroid; // Afegim el nou drawableAsteroid a l'array.
            }
            setBackgroundColor(Color.BLACK);
            Path pathNau = new Path(); // Definim el nou path per la nau.
            ShapeDrawable dNau = new ShapeDrawable(new PathShape(pathNau, 1, 1)); // Definim el nou drawableNau.
            // Definim la forma de la nau traçant un polígon tancat mitjançant una sèrie de línies connectades a un `Path`, començant a (0.0, 1.0) i tornant al mateix punt.
            pathNau.moveTo((float) 0.0, (float) 1.0);
            pathNau.lineTo((float) 1.0, (float) 0.5);
            pathNau.lineTo((float) 0.0, (float) 0.0);
            pathNau.lineTo((float) 0.0, (float) 1.0);
            dNau.getPaint().setColor(Color.WHITE);
            dNau.getPaint().setStyle(Paint.Style.STROKE); // Definim el tipus de línia.
            dNau.setIntrinsicWidth(50); // Definim l'ample de la nau.
            dNau.setIntrinsicHeight(50); // Definim l'alt de la nau.
            drawableShip = dNau; // Afegim el nou drawableNau.
            ShapeDrawable dMissile = new ShapeDrawable(new RectShape()); // Definim el nou drawableMissile.
            dMissile.getPaint().setColor(Color.WHITE);
            dMissile.getPaint().setStyle(Paint.Style.STROKE);
            dMissile.setIntrinsicWidth(15); // Definim l'ample del missil.
            dMissile.setIntrinsicHeight(3); // Definim l'alt del missil.
            drawableMissile = dMissile;
        } else {
            // Com hem canviat el drawableAsteroid i l'hem fet un array inicialitzam cada posició per defecte.
            drawableAsteroid[0] = context.getResources().getDrawable(R.drawable.asteroide1);
            drawableAsteroid[1] = context.getResources().getDrawable(R.drawable.asteroide2);
            drawableAsteroid[2] = context.getResources().getDrawable(R.drawable.asteroide3);
            // Inicialitzem la nau i el missil.
            drawableShip = context.getResources().getDrawable(R.drawable.nave);
            drawableMissile = context.getResources().getDrawable(R.drawable.misil1); // Aquí declaram es DrawableMissile
        }
        asteroids = new ArrayList<AsteroidsGraphic>();
        for (int i = 0; i < numAsteroids; i++) {
            AsteroidsGraphic asteroid = new AsteroidsGraphic(this, drawableAsteroid[0]);
            asteroid.setIncY(Math.random() * 4 - 2); // Establim la velocitat de l'asteroide eix Y.
            asteroid.setIncX(Math.random() * 4 - 2); // Establim la velocitat de l'asteroide eix X.
            asteroid.setRotAngle((int) (Math.random() * 360)); // Establim l'angle de rotació de l'asteroide.
            asteroid.setRotSpeed((int) (Math.random() * 8 - 4)); // Establim la velocitat de rotació de l'asteroide.
            asteroids.add(asteroid);
        }
        ship = new AsteroidsGraphic(this, drawableShip);
        // Estructura de control 'if'.
        // Verifiquem les preferències per als sensors.
        // Comprovem si l'usuari ha seleccionat utilitzar sensors (valor "2").
        if (pref.getString("sensors", "1").equals("2")) {
            // Si els sensors estan habilitats, s'activen.
            activateSensors();
        } else {
            // Si no, es desactiven per defecte.
            deactivateSensors();
        }
        actualitzarMusica();
    }

    // Cream un mètode per teletransportar sa nau.
    public void teletransportarNau(float x, float y) {
        // Estableixem les noves coordenades a la nau.
        ship.setCenX((int) x);
        ship.setCenY((int) y);

        if (soundPool != null) {
            // Reproduim el so de teletransportació.
            // Recordem que:
            // - idTeletransportacio: identificador del so de teletransportacio.
            // - Volum esquerre i dret (1,1): reproduir amb el volum màxim.
            // - Prioritat (1): nivell d'importància per al sistema de so.
            // - Loop (0): no repetir el so.
            // - Rate (4): velocitat de reproducció normal.
            soundPool.play(idTeletransportacio, 1, 1, 1, 0, 4);
        }
    }

    protected void activateSensors() {
        if (pref.getString("sensors", "1").equals("2")) { // Comprovem preferències.
            if (mSensorManager == null) {
                mSensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
            }
            // Comprovem el sensor d'acceleròmetre.
            if (accelerometerSensor == null) {
                List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
                if (!sensorList.isEmpty()) {
                    accelerometerSensor = sensorList.get(0); // Seleccionem el primer sensor disponible.
                }
            }
            // Registrem el listener per al sensor d'acceleròmetre.
            if (accelerometerSensor != null) {
                mSensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            }
        }
    }

    protected void deactivateSensors() {
        if (mSensorManager != null) {
            // Cancel·lem el registre del listener per evitar ús de recursos
            mSensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onSizeChanged(int width, int height, int prevWidth, int prevHeight) {
        super.onSizeChanged(width, height, prevWidth, prevHeight);
        // Un cop coneixem el nostre ample i alt.
        ship.setCenX(width / 2);
        ship.setCenY(height / 2);
        // Establim la posició de la nau al mig de la pantalla.
        for (AsteroidsGraphic asteroid: asteroids) {
            // Establim la posició dels asteroides.
            do {
                asteroid.setCenX((int) (Math.random() * width)); // Establim la posició X de l'asteroide (ample).
                asteroid.setCenY((int) (Math.random() * height)); // Establim la posició Y de l'asteroide (alt).
            } while (asteroid.distance(ship) < (width+height)/5);
        }
        prevUpdate = System.currentTimeMillis();
        thread.start();
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        ship.drawGraphic(canvas);

        // Dibuixam asteroides fent un bucle i recorrent el seu array.
        for (AsteroidsGraphic asteroid : asteroids) {
            asteroid.drawGraphic(canvas);
        }

        // Dibuixam missils fent un bucle i recorrent el seu array.
        for (AsteroidsGraphic missile : missiles) {
            missile.drawGraphic(canvas);
        }
    }

    protected synchronized void updateView() {
        long now = System.currentTimeMillis(); // Obtenim el temps actual.
        // Si la diferència entre el temps actual i l'última actualització es inferior a l'interval d'animació, no actualitzam.
        if (prevUpdate + ANIM_INTERVAL > now) {
            return;
        }

        double delay = (now - prevUpdate) / ANIM_INTERVAL; // Calculem el retard ('delay') normalitzat de l'animació dividint la diferència de temps entre l'actualització actual ('now') i l'anterior ('prevUpdate') per l'interval d'animació ('ANIM_INTERVAL').
        prevUpdate = now; // Actualitzem 'prevUpdate' amb el valor actual de 'now' per registrar el moment de la darrera actualització.

        // Actualitzem l'angle de rotació de la nau.
        ship.setRotAngle((int) (ship.getRotAngle() + ship.getRotSpeed() * delay));
        // Calculem la nova velocitat en l'eix X.
        double nIncX = ship.getIncX() + accelShip * Math.cos(Math.toRadians(ship.getRotAngle())) * delay;
        // Calculem la nova velocitat en l'eix Y.
        double nIncY = ship.getIncY() + accelShip * Math.sin(Math.toRadians(ship.getRotAngle())) * delay;
        // Comprovem si la velocitat resultant és menor o igual a la velocitat màxima de la nau.
        if (Math.hypot(nIncX, nIncY) <= SHIP_MAX_SPEED) {
            ship.setIncX(nIncX); // Actualitzem la velocitat en l'eix X.
            ship.setIncY(nIncY); // Actualitzem la velocitat en l'eix Y.
        }

        ship.updatePos(delay); // Actualitzem la posició de la nau.

        // Actualitzam la posició dels asteroides fent un bucle i recorrent el seu array.
        for (AsteroidsGraphic asteroid : asteroids) {
            asteroid.updatePos(delay);
        }

        // Actualitzam la posició dels missils fent un bucle i recorrent el seu array.
        for (int i = 0; i < missiles.size(); i++) {
            AsteroidsGraphic missile = missiles.get(i); // Obtenim el missil.
            missile.updatePos(delay); // Actualitzam la posició del missil.
            missileLifetimes.set(i, missileLifetimes.get(i) - delay); // Actualitzam el temps de vida del missil.

            // Eliminem el missil si termina la seva vida útil o golpetja a un asteroide.
            if (missileLifetimes.get(i) < 0) {
                missiles.remove(i);
                missileLifetimes.remove(i);
            } else {
                // Comprovam col·lisions amb els asteroides.
                for (int j = 0; j < asteroids.size(); j++) {
                    // Si hi ha col·lisió amb l'asteroide, destruïm l'asteroide i el missil.
                    if (missile.checkCollision(asteroids.get(j))) {
                        destroyAsteroid(j);
                        missiles.remove(i);
                        missileLifetimes.remove(i);
                        break;
                    }
                }
            }
        }

        // Comprovam col·lisions amb els asteroides.
        for (AsteroidsGraphic asteroid : asteroids) {
            // Si hi ha col·lisió amb l'asteroide, finalitzam el joc.
            if (asteroid.checkCollision(ship)) {
                terminate();
            }
        }
    }

    // Mètode per a gestionar les pulsacions de teclat quan l'usuari presiona una tecla.
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        boolean processed = true; // Variable per controlar si la pulsació ha estat processada.
        // Processam la pulsació.
        switch (keyCode) {
            // Cas que la tecla sigui la tecla de dalt.
            case KeyEvent.KEYCODE_DPAD_UP:
                accelShip = + STEPSIZE_ACCEL_SHIP; // Incrementam la velocitat de la nau.
                break;
            // Cas que la tecla sigui la tecla de l'esquerra.
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ship.setRotSpeed(-STEPSIZE_ROT_SHIP); // Decrementam la velocitat de rotació de la nau.
                break;
            // Cas que la tecla sigui la tecla de la dreta.
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ship.setRotSpeed(STEPSIZE_ROT_SHIP); // Incrementam la velocitat de rotació de la nau.
                break;
            // Cas que la tecla sigui la tecla central o la tecla d'entrada.
            case KeyEvent.KEYCODE_DPAD_CENTER:
            // Cas que l'usuari hagi premut la tecla central o la tecla d'entrada, cridam al mètode per disparar un missil.
            case KeyEvent.KEYCODE_ENTER:
                fireMissile(); // Disparam un missil.
                break;
            default:
                // Si estem aquí, no hi ha pulsació que ens interessi
                processed = false;
                break;
        }
        return processed;
    }

    // Mètode per a gestionar les pulsacions de teclat quan l'usuari solta una tecla.
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        super.onKeyUp(keyCode, event);
        // Processam la pulsació
        boolean processada = true;
        // Comprovam quina tecla ha estat soltada.
        switch (keyCode) {
            // Cas que la tecla sigui la tecla de dalt.
            case KeyEvent.KEYCODE_DPAD_UP:
                accelShip = 0; // Establim la velocitat de la nau a 0.
                break;
            // Cas que la tecla sigui la tecla de l'esquerra.
            case KeyEvent.KEYCODE_DPAD_LEFT:
                ship.setRotSpeed(0); // Establim la velocitat de rotació de la nau a 0.
                break;
            // Cas que la tecla sigui la tecla de la dreta.
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                ship.setRotSpeed(0); // Establim la velocitat de rotació de la nau a 0.
                break;
            // Cas que la tecla sigui la tecla central o la tecla d'entrada.
            case KeyEvent.KEYCODE_DPAD_CENTER:
            // Cas que l'usuari hagi alliberat la tecla central o la tecla d'entrada, no fer res.
            case KeyEvent.KEYCODE_ENTER:
                fireMissile(); // Disparam un missil.
                break;
            default:
                // Si estem aquí, no hi ha pulsació que ens interessi
                processada = false;
                break;
        }
        return processada;
    }

    // Mètode per a gestionar les pulsacions de pantalla quan l'usuari toca la pantalla.
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        float x = event.getX(); // Obtenim la posició X de la pulsació.
        float y = event.getY(); // Obtenim la posició Y de la pulsació.
        switch (event.getAction()) {
            // Cas que es detecti una pulsació.
            case MotionEvent.ACTION_DOWN:
                long tempsActual = System.currentTimeMillis(); // Obtenim el temps actual.
                // Comprovam si la diferència entre la darrera pulsació i l'actual es inferior a la doble pulsació.
                if (tempsActual - ultimaPulsacio < DOBLE_PULSACIO) {
                    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext()); // Obtenim les preferències.
                    // Comprovam si l'usuari ha activat la teletransportació.
                    if (pref.getBoolean("teletransportacio", false)) {
                        teletransportarNau(x, y); // Cas que l'usuari hagi activat la teletransportació trucam al mètode.
                    }
                }
                ultimaPulsacio = tempsActual; // Actualitzam el temps de la darrera pulsació.
                fire = true; // Indicam que es pot disparar.
                break;
            // Cas que es detecti un moviment.
            case MotionEvent.ACTION_MOVE:
                float dx = Math.abs(x - mX); // Calculem la diferència en l'eix X.
                float dy = Math.abs(y - mY); // Calculem la diferència en l'eix Y.
                // Comprovam si la diferència en l'eix Y es inferior a 6 i la diferència en l'eix X es superior a 6.
                if (dy < 6 && dx > 6) {
                    ship.setRotSpeed(Math.round((x - mX) / 2)); // Establim la velocitat de rotació de la nau.
                    fire = false; // Indicam que no es pot disparar.
                // Comprovam si la diferència en l'eix X es inferior a 6 i la diferència en l'eix Y es superior a 6.
                } else if (dx < 6 && dy > 6) {
                    accelShip = Math.round((mY - y) / 50); // Establim la velocitat de la nau.
                    fire = false; // Indicam que no es pot disparar.
                }
                break;
            // Cas que es detecti una alliberació.
            case MotionEvent.ACTION_UP:
                ship.setRotSpeed(0); // Establim la velocitat de rotació de la nau a 0.

                // Si podem disparar.
                if (fire) {
                    fireMissile(); // Disparam un missil.
                }
                break;
        }
        mX = x; // Actualitzam la posició X de la pulsació.
        mY = y; // Actualitzam la posició Y de la pulsació.
        return true;
    }

    class GameThread extends Thread {
        private boolean paused, running; // Variables per controlar l'estat del joc.

        // Mètode per a pausar el joc.
        public synchronized void pause() {
            paused = true;
        }

        // Mètode per a reprendre el joc.
        public synchronized void unpause() {
            paused = false;
            notify();
        }

        // Mètode per a aturar el joc.
        public void halt() {
            running = false;
            if (paused) unpause();
        }

        // Mètode per a executar el joc.
        @Override
        public void run() {
            running = true; // Indicam que el joc està en marxa.
            // Mentre el joc estigui en marxa.
            while (running) {
                updateView(); // Actualitzam la vista.
                // Sincronizam el joc.
                synchronized (this) {
                    // Mentre el joc estigui pausat.
                    while (paused)
                        // Esperam fins que es reprengui el joc.
                        try {
                            wait();
                        } catch (Exception e) {
                        }
                }
            }
        }
    }

    private float initValue;
    private boolean initValueValid = false;

    // Mètode per a gestionar els canvis en els sensors.
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float value = sensorEvent.values[1]; // Obtenim el valor del sensor.
        // Si el valor inicial no és vàlid, el definim com a valor inicial.
        if (!initValueValid){
            initValue = value; // Establim el valor inicial.
            initValueValid = true; // Indicam que el valor inicial es vàlid.
        }
        ship.setRotSpeed((int) (value - initValue) / 3); // Establim la velocitat de rotació de la nau.
        accelShip = sensorEvent.values[2] / 10; // Establim la velocitat de la nau.
    }

    // Mètode per a gestionar els canvis en la precisió del sensor. No ho necessitem encara.
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    // Mètode per gestionar la destrucció dels asteroides.
    private void destroyAsteroid(int i) {
        // Comprovam si l'asteroide es pot dividir.
        if (asteroids.get(i).getDrawable() != drawableAsteroid[2]){
            int size; // Declaram la variable per la mida de l'asteroide.
            // Comprovam la mida de l'asteroide.
            if (asteroids.get(i).getDrawable() == drawableAsteroid[1]){
                size = 2; // Establim la mida de l'asteroide.
            } else {
                size = 1; // Establim la mida de l'asteroide.
            }

            // Creem els fragments de l'asteroide.
            for(int n = 0; n < numFragments;n++){
                // Creem un nou asteroide.
                AsteroidsGraphic asteroid = new AsteroidsGraphic(this,drawableAsteroid[size]); // Creem un nou asteroide.
                asteroid.setCenX(asteroids.get(i).getCenX()); // Establim la posició X de l'asteroide.
                asteroid.setCenY(asteroids.get(i).getCenY()); // Establim la posició Y de l'asteroide.
                asteroid.setIncX(Math.random() * 4 - 2); // Establim la velocitat de l'asteroide eix X.
                asteroid.setIncY(Math.random() * 4 - 2); // Establim la velocitat de l'asteroide eix Y.
                asteroid.setRotAngle((int)(Math.random() * 360)); // Establim l'angle de rotació de l'asteroide.
                asteroid.setRotSpeed((int)(Math.random() * 8 - 4)); // Establim la velocitat de rotació de l'asteroide.
                asteroids.add(asteroid); // Afegim l'asteroide a la llista d'asteroides.
            }
        }

        // Eliminem l'asteroide de la llista d'asteroides actius.
        asteroids.remove(i);
        actualitzarMusica(); // Actualitzam la música de fons.

        score += 1000; // Cada cop que es destrueixi un asteroide se incrementa la variable per la puntuació.

        // Reproduim el so d'explosió (quan es destrueix):
        // - idExplosion: identificador del so d'explosió.
        // - Volum esquerre i dret (1,1): reproduir amb el volum màxim.
        // - Prioritat (2): nivell d'importància per al sistema de so.
        // - Loop (0): no repetir el so.
        // - Rate (1): velocitat de reproducció normal.
        soundPool.play(idExplosion, 1, 1, 2, 0, 1);

        // Comprovam si no queden asteroides.
        if (asteroids.isEmpty()) {
            terminate();
        }
    }

    // Mètode per disparar missils.
    private void fireMissile() {
        // So que es reprodueix quan disparem, les explicacions de cada paràmetre ja les tenim a l'anterior mètode.
        soundPool.play(idFire, 1, 1, 1, 0, 1);
        AsteroidsGraphic missils = new AsteroidsGraphic(this, drawableMissile);

        // Configuram missils.
        missils.setCenX(ship.getCenX());
        missils.setCenY(ship.getCenY());
        missils.setRotAngle(ship.getRotAngle());
        missils.setIncX(Math.cos(Math.toRadians(missils.getRotAngle())) * MISSILE_SPEED);
        missils.setIncY(Math.sin(Math.toRadians(missils.getRotAngle())) * MISSILE_SPEED);

        // Afegim missils.
        missiles.add(missils);

        missileLifetimes.add((double) Math.min(this.getWidth() / Math.abs(missils.getIncX()), this.getHeight() / Math.abs(missils.getIncY())) - 2);
    }

    public GameThread getThread() {
        return thread;
    }

    private Activity parent; // Afegim la variable per l'activitat pare.

    // Mètode per a accedir a la puntuació.
    public void setParent(Activity parent) {
        this.parent = parent;
    }

    private void terminate() {
        Bundle bundle = new Bundle(); // Creem un nou Bundle per a la puntuació.
        bundle.putInt("score", score); // Afegim la puntuació al Bundle.
        Intent intent = new Intent();
        intent.putExtras(bundle); // Afegim el Bundle a l'Intent.
        parent.setResult(Activity.RESULT_OK, intent); // Establim el resultat de l'activitat pare.
        parent.finish();
    }

    // Cream un mètode que s'encargara d'actualitzar la música de fons quan pertoqui.
    private void actualitzarMusica() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getContext()); // Obtenim les preferències.
        boolean hiHaMusica = pref.getBoolean("musica", true); // Obtenim el valor de la preferència de la música.

        // Si no hi ha música, aturam la música de fons i sortim del mètode.
        if (!hiHaMusica) {
            if (musicaDeFons != null) {
                musicaDeFons.stop();
                musicaDeFons.release();
                musicaDeFons = null;
            }
            musicaActual = 0;
            return;
        }

        int musica = 0; // Declaram la variable per a la música.

        // Comprovam el nombre d'asteroides per a seleccionar la música.
        if (asteroids.size() > 3) {
            musica = R.raw.dummy; // Definim la música per a més de 3 asteroides.
        } else if (asteroids.size() == 3) {
            musica = R.raw.fallen_down; // Definim la música per a 3 asteroides.
        } else if (asteroids.size() == 2) {
            musica = R.raw.papyrus; // Definim la música per a 2 asteroides.
        } else if (asteroids.size() == 1) {
            musica = R.raw.second_warning; // Definim la música per a 1 asteroide.
        }

        // Verifiquem si la variable música es diferent de 0 (el que indica que s'ha de reproduïr la música)
        // i si la nova música es diferent a la que está sonant (musicaActual).
        // Si les dues condicions es cumpleixen, significa que hem de canviar la música.
        if (musica != 0 && musica != musicaActual) {
            // Cas que la música de fons no sigui nul·la.
            if (musicaDeFons != null) {
                musicaDeFons.stop(); // Aturem la música.
                musicaDeFons.release(); // Alliberam recursos.
            }
            musicaDeFons = MediaPlayer.create(getContext(), musica); // Creem un nou reproductor amb la nova música.
            musicaDeFons.setLooping(true); // Indicam que la música es repeteixi.
            musicaDeFons.start(); // Començam a reproduir la música.
            musicaActual = musica; // Actualitzam la música actual.
        // Si la música es 0, significa que volem aturar la música de fons. També ens asseguram que la música de fons no sigui nul·la.
        } else if (musica == 0 && musicaDeFons != null) {
            musicaDeFons.stop(); // Aturem la música.
            musicaDeFons.release(); // Alliberem recursos.
            musicaDeFons = null; // Indicam que la música de fons es nul·la. Ho feim per indicar que no hi ha música en reproducció.
            musicaActual = 0; // Actualitzam la música actual.
        }
    }


    // Mètode que es crida quan la vista es desenganxa de la finestra.
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // Comprovem si la música de fons està inicialitzada.
        if (musicaDeFons != null) {
            musicaDeFons.stop(); // Aturem la reproducció de la música.
            musicaDeFons.release(); // Alliberem els recursos de la música.
            musicaDeFons = null; // Indiquem que la música ja no està disponible.
        }
    }

}
