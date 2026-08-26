package com.example.pickii.data.local

import android.content.Context
import java.io.File

/**
 * DataStore 파일 경로를 만들 때 필요한 Android [Context]. Koin이 시작되기 전에는 이 함수가 호출될
 * 일이 없어서(Koin 싱글턴 생성 시점에만 쓰임) `PickiiApplication.onCreate()`에서 Koin을 시작하기
 * 직전에 채워두면 충분하다.
 */
lateinit var pickiiApplicationContext: Context

internal actual fun preferencesDataStoreFilePath(fileName: String): String =
    File(pickiiApplicationContext.filesDir, "$fileName.preferences_pb").absolutePath
