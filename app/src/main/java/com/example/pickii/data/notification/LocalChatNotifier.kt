package com.example.pickii.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pickii.MainActivity
import com.example.pickii.R
import com.example.pickii.domain.model.NotificationEntry
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "chat_messages"

/** 채팅 메시지 알림(9-1, type=CHAT)을 시스템 알림으로 띄운다. 탭하면 해당 채팅방으로 이동한다. */
@Singleton
class LocalChatNotifier
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) {
        init {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel =
                    NotificationChannel(
                        CHANNEL_ID,
                        context.getString(R.string.chat_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH
                    )
                context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
            }
        }

        /** [entry]의 referenceId(채팅방 id)가 없으면 조용히 무시한다. */
        fun notify(entry: NotificationEntry) {
            val roomId = entry.referenceId?.toLongOrNull() ?: return
            if (!hasNotificationPermission()) return

            val intent =
                Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(MainActivity.EXTRA_CHAT_ROOM_ID, roomId)
                }
            val pendingIntent =
                PendingIntent.getActivity(
                    context,
                    roomId.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

            val notification =
                NotificationCompat
                    .Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle(entry.title)
                    .setContentText(entry.content)
                    .setAutoCancel(true)
                    .setContentIntent(pendingIntent)
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .build()

            NotificationManagerCompat.from(context).notify(roomId.toInt(), notification)
        }

        private fun hasNotificationPermission(): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
            return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
    }
