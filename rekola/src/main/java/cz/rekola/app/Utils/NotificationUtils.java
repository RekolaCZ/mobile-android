package cz.rekola.app.utils;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import cz.rekola.app.R;
import cz.rekola.app.activity.LoginActivity;

/**
 * Create notification when bike was not returned
 * Created by Tomas Krabac[tomas.krabac@ackee.cz] on {20.10.2015}
 **/
public class NotificationUtils {
    public static final String TAG = NotificationUtils.class.getName();

    private static final int NOTIFICATION_ID = 164654654;

    public static void createNotification(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        // This intent is fired when notification is clicked
        Intent intent = new Intent(context, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
        builder.setContentIntent(pendingIntent);

        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(context.getString(R.string.notification_message));
        builder.setAutoCancel(true);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        builder.setSound(alarmSound);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
