package com.example.pickii.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.pickii.MainActivity
import com.example.pickii.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.koin.android.ext.android.inject

private const val CHANNEL_ID = "general_notifications"
private const val DATA_KEY_TYPE = "type"
private const val DATA_KEY_REFERENCE_TYPE = "referenceType"
private const val DATA_KEY_REFERENCE_ID = "referenceId"

/**
 * 시스템 푸시(FCM, 9-8/9-9) 수신을 처리한다.
 *
 * 서버는 알림 제목/본문을 [RemoteMessage.getNotification]에, 딥링크 정보(type/referenceType/referenceId,
 * 9-1 응답과 동일한 필드)를 [RemoteMessage.getData]에 실어 보낸다(PUSH_NOTIFICATION.md 4-4).
 */
class FcmService : FirebaseMessagingService() {
    private val fcmTokenRegistrar: FcmTokenRegistrar by inject()
    private val activeChatRoomTracker: ActiveChatRoomTracker by inject()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    getString(R.string.general_notification_channel_name),
                    NotificationManager.IMPORTANCE_HIGH
                )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        fcmTokenRegistrar.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: return
        val body = message.notification?.body.orEmpty()
        if (!hasNotificationPermission()) return
        if (isForActiveChatRoom(message)) return

        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                message.data[DATA_KEY_TYPE]?.let { putExtra(MainActivity.EXTRA_NOTIFICATION_TYPE, it) }
                message.data[DATA_KEY_REFERENCE_TYPE]?.let {
                    putExtra(MainActivity.EXTRA_NOTIFICATION_REFERENCE_TYPE, it)
                }
                message.data[DATA_KEY_REFERENCE_ID]?.let { putExtra(MainActivity.EXTRA_NOTIFICATION_REFERENCE_ID, it) }
            }
        val requestCode = message.data[DATA_KEY_REFERENCE_ID]?.hashCode() ?: System.currentTimeMillis().toInt()
        val pendingIntent =
            PendingIntent.getActivity(
                this,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat
                .Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .build()

        NotificationManagerCompat.from(this).notify(requestCode, notification)
    }

    /** 지금 열려 있는 채팅방으로 온 메시지 알림인지 확인한다(그 방을 보고 있는 동안엔 알림을 띄우지 않는다). */
    private fun isForActiveChatRoom(message: RemoteMessage): Boolean {
        val referenceType = message.data[DATA_KEY_REFERENCE_TYPE]
        if (referenceType != "CHATROOM" && referenceType != "CHAT") return false
        val referenceRoomId = message.data[DATA_KEY_REFERENCE_ID]?.toLongOrNull() ?: return false
        return referenceRoomId == activeChatRoomTracker.activeRoomId
    }

    private fun hasNotificationPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return true
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }
}
