package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class AlarmReceiver: BroadcastReceiver() {
    //Activity가 아니기 때문에 this를 사용할 수 없으므로 Context 이용

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "1000"
        const val NOTIFICATION_ID = 100
    }

    override fun onReceive(context: Context, intent: Intent) {
        createNotificationChannel(context)
        notifyNotification(context)
    }

    private fun createNotificationChannel(context: Context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "기상 알람",
                    NotificationManager.IMPORTANCE_HIGH
            ) //채널 생성

            NotificationManagerCompat.from(context).createNotificationChannel(notificationChannel)
            //알람 매니저 호출
        }
    }

    private fun notifyNotification(context: Context) {
        with(NotificationManagerCompat.from(context)) { //알람 매니저 호출
            val build = NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("알람")
                    .setContentText("일어날 시간입니다.")
                    .setSmallIcon(R.drawable.ic_launcher_foreground)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
            //알람 내용 지정

            notify(NOTIFICATION_ID, build.build())
            //알람 실행
        }

    }
}