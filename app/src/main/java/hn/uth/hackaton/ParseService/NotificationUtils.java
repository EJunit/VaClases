package hn.uth.hackaton.ParseService;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.view.View;

import java.util.List;
import java.util.Objects;

import hn.uth.hackaton.R;

public class NotificationUtils {

    private Context mContext;

    public NotificationUtils(Context mContext) {
        this.mContext = mContext;
    }


    public void showNotificationMessage(String title, String message, Intent intent) {
        // Verifica si la notificacion viene vacia
        if (TextUtils.isEmpty(message))
            return;

        if (isAppIsInBackground(mContext)) {
            // notification icon
            int icon = R.drawable.ic_launcher;

            int smallIcon = R.drawable.ic_action_person;

            int mNotificationId = AppConfig.NOTIFICATION_ID;

            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            mContext,
                            0,
                            intent,
                            PendingIntent.FLAG_CANCEL_CURRENT
                    );

            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext);

            Notification notification = mBuilder.setSmallIcon(smallIcon, View.INVISIBLE)
                    .setTicker(title).setWhen(0)
                    .setAutoCancel(true)
                    .setContentTitle(title)
                    .setStyle(inboxStyle)
                    .setContentIntent(resultPendingIntent)
                    .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                    .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), icon))
                    .setContentText(message)
                    .build();

            NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(mNotificationId, notification);
        } else {
            intent.putExtra("title", title);
            intent.putExtra("message", message);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mContext.startActivity(intent);
        }
    }
    //verifica si la app esta en background
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        } else {
            List<ActivityManager.AppTask> taskInfo = am.getAppTasks();
            ActivityManager.RecentTaskInfo componentInfo = taskInfo.get(0).getTaskInfo();

            if (Objects.equals(componentInfo.getClass().toString(), context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
}
