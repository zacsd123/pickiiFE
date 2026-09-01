package com.example.pickii.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import okio.Path.Companion.toPath

/**
 * [fileName] 하나당 하나씩 만드는, 앱 전용 저장 공간의 Preferences DataStore. `androidx.datastore`는
 * 1.1.0부터 `PreferenceDataStoreFactory.createWithPath`로 진짜 멀티플랫폼이 됐다 — Android에서 쓰던
 * `Context.preferencesDataStore(name)` 델리게이트(Android 전용)만 플랫폼별 파일 경로 생성으로
 * 바꾸면 된다.
 */
internal fun preferencesDataStore(fileName: String): DataStore<Preferences> =
    PreferenceDataStoreFactory.createWithPath { preferencesDataStoreFilePath(fileName).toPath() }

/** [fileName]에 대응하는 플랫폼별 절대 경로(Android는 앱 전용 파일 디렉터리, iOS는 홈 디렉터리 하위). */
internal expect fun preferencesDataStoreFilePath(fileName: String): String
