package de.skilloverflow.screenrecorderforandroid;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public class RunningService extends Service {
    private static final int NOTIFICATION_ID = 1337;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Context context = getApplicationContext();

        startForeground(NOTIFICATION_ID, createNotification(context));

        return super.onStartCommand(intent, flags, startId);
    }

    private Notification createNotification(Context context) {
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, new Intent(context, MainActivity.class),
                PendingIntent.FLAG_UPDATE_CURRENT);

        // .setSmallIcon(R.drawable.av_play_notification)
        Notification.Builder mBuilder =
                new Notification.Builder(this)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText("Recording Running")
                        .setOngoing(true)
                        .setContentIntent(pendingIntent);

        return mBuilder.build();
    }
}
