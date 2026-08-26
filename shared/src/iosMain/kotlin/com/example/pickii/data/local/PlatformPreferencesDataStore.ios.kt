package com.example.pickii.data.local

import platform.Foundation.NSHomeDirectory

internal actual fun preferencesDataStoreFilePath(fileName: String): String =
    NSHomeDirectory() + "/Documents/$fileName.preferences_pb"
