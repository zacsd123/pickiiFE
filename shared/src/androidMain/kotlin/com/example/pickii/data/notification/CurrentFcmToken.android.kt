package com.example.pickii.data.notification

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

actual suspend fun currentFcmToken(): String? =
    runCatching { FirebaseMessaging.getInstance().token.await() }.getOrNull()
