package com.example.hope.reminder.notification

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.hope.MainActivity
import com.example.hope.R

class TaskAlarmReceiver : BroadcastReceiver() {
    @SuppressLint("ServiceCast", "ScheduleExactAlarm")
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPreferences = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val showAlarmScreen = sharedPreferences.getBoolean("is_notification_enabled", false)

        val taskId = intent.getLongExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: "No Title"
        val content = intent.getStringExtra("content") ?: "No Content"
        val time = intent.getStringExtra("time") ?: "No Time"

        if (showAlarmScreen) {
            Log.d("time", time)
            // Mở màn hình báo thức
            val alarmIntent = Intent(context, AlarmActivity::class.java).apply {
                putExtra("taskId", taskId)
                putExtra("title", title)
                putExtra("content", content)
                putExtra("time", time)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
//            alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//            alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
//            context.startActivity(alarmIntent)

            alarmIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            val pendingIntent = PendingIntent.getActivity(
                context,
                taskId.toInt(), // ID riêng cho PendingIntent
                alarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // Gọi pendingIntent để mở Activity
            pendingIntent.send()

        }
        showNotification(context, taskId, title, content, time)
    }

    @SuppressLint("ServiceCast")
    private fun showNotification(context: Context, taskId: Long, title: String, content: String, time: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "task_notification_channel"

        // Tạo channel nếu chưa có (Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Task Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.app_logo_big_eb82ef2b)  // Sử dụng icon phù hợp
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_launcher_foreground, // Icon tắt báo thức
                "Dismiss",
                getDismissPendingIntent(context, taskId)
            )
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    private fun getDismissPendingIntent(context: Context, taskId: Long): PendingIntent {
        val intent = Intent(context, DismissNotificationReceiver::class.java).apply {
            putExtra("taskId", taskId)
        }
        return PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}
