package com.example.spotifyalarm;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.spotifyalarm.model.AlarmModel;

public class AlarmManagerService extends Service {
    private static final String TAG = "AlarmManagerService";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        if(AlarmModel.getInstance().getCurrentState() == AlarmModel.State.OFF){
            Notification notification = createNotification();
            startForeground(42, notification);
            setAlarm();
        }

        return START_STICKY;
    }

    private Notification createNotification(){
        String NOTIFICATION_CHANNEL_ID = "example.permanence";

        String formattedHour = String.format("%02d", AlarmModel.getInstance().getHour());
        String formattedMinute = String.format("%02d", AlarmModel.getInstance().getMinute());

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.app_logo)
                .setOngoing(true)
                .setContentTitle("Alarm is running")
                .setContentText("Time : "+String.format("%s:%s", formattedHour, formattedMinute))
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setCategory(Notification.CATEGORY_ALARM);

        return notificationBuilder.build();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public void setAlarm(){
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms();
        }

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, AlarmModel.getInstance().getCalendar().getTimeInMillis(), pendingIntent);

        AlarmModel.getInstance().setAlarmOn();

        Log.i(TAG, AlarmModel.getInstance().getAlarmModelContent().toString() );
        LogFile logFile = new LogFile(this);
        logFile.writeToFile(TAG, AlarmModel.getInstance().getAlarmModelContent().toString());
    }

    @Override
    public void onDestroy() {
        stopForeground(STOP_FOREGROUND_REMOVE);
        super.onDestroy();
    }
}