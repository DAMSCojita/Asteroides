package com.stefancojita.asteroides;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

public class ServeiMusica extends Service {

    // Declaració de variables necessàries.
    public static MediaPlayer reproductor; // Feim la variable del reproductor estática per a poder accedir-hi des de MainActivity.
    private static final String PRIMARY_CHANNEL_ID = "primary_notification_channel";
    private NotificationManager mNotifyManager;
    private static final int NOTIFICATION_ID = 0;

    @Override
    public void onCreate() {
        // Mostrem un missatge Toast quan es crea el servei.
        Toast.makeText(this,"Servei creat", Toast.LENGTH_SHORT).show();
        // Inicialitzem el reproductor de música amb l'arxiu d'àudio.
        reproductor = MediaPlayer.create(this, R.raw.audio);
        // Creem el canal de notificació.
        createNotificationChannel();
        // Enviem la notificació.
        sendNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int idArranque) {
        // Mostrem un missatge Toast quan el servei s'inicia.
        Toast.makeText(this,"Servei arrencat "+ idArranque, Toast.LENGTH_SHORT).show();
        // Comencem a reproduir la música.
        reproductor.start();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // Mostrem un missatge Toast quan el servei es deté.
        Toast.makeText(this,"Servei detingut", Toast.LENGTH_SHORT).show();
        // Aturem la reproducció de música.
        reproductor.stop();
    }

    @Override
    public IBinder onBind(Intent intencio) {
        return null;
    }

    public void createNotificationChannel(){
        mNotifyManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Creem un NotificationChannel per a les notificacions de música.
            NotificationChannel notificationChannel = new NotificationChannel(PRIMARY_CHANNEL_ID, "Music Notification", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.enableVibration(true);
            notificationChannel.setDescription("Creant el Servei Música.");
            mNotifyManager.createNotificationChannel(notificationChannel);
        }
    }

    private NotificationCompat.Builder getNotificationBuilder(){
        // Creem una intenció per a la notificació que obrirà GameActivity.
        Intent notificationIntent = new Intent(this, GameActivity.class);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_ID,
                notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE // Afegeim FLAG_IMMUTABLE per evitar errors.
        );

        // Construïm la notificació amb el títol, text, icona i intenció.
        NotificationCompat.Builder notifyBuilder = new NotificationCompat.Builder(this, PRIMARY_CHANNEL_ID)
                .setContentTitle("Servei Música per Asteroides.")
                .setContentText("Creant el Servei Música.")
                .setSmallIcon(R.drawable.ic_music)
                .setContentIntent(notificationPendingIntent)
                .setAutoCancel(true);
        return notifyBuilder;
    }

    public void sendNotification() {
        // Obtenim el NotificationCompat.Builder i enviem la notificació.
        NotificationCompat.Builder notifyBuilder = getNotificationBuilder();
        mNotifyManager.notify(NOTIFICATION_ID, notifyBuilder.build());
    }
}
