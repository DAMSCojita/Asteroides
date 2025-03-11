package com.stefancojita.asteroides;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceptorBateria extends BroadcastReceiver {

    // Implementam y sobreescribim el métode onReceive().
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, MainActivitySobre.class); // Cream un Intent i li passem el context i la classe MainActivitySobre.
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // Afegim la flag FLAG_ACTIVITY_NEW_TASK.
        context.startActivity(i); // Iniciam l'activitat.
    }

}
