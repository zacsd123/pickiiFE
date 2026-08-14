package com.example.pickii.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.savedMeetingScheduleDataStore by preferencesDataStore(name = "pickii_saved_meeting_schedule_store")

private val SAVED_SCHEDULE_IDS_KEY = stringSetPreferencesKey("saved_meeting_schedule_ids")

/**
 * 회의 확정(7-13) 후 "내 캘린더에 저장"을 눌러 이미 저장한 팀 일정의 scheduleId를 기기에 보존한다.
 * 되읽기 API가 없어(`ChatRoomUiState.savedMeetingScheduleIds` 참고) ViewModel 상태만으로는 채팅방을
 * 나갔다 다시 들어오면(ViewModel 재생성) 저장 여부가 초기화되던 문제를 이걸로 해결한다.
 */
@Singleton
class SavedMeetingScheduleStore
    @Inject
    constructor(
        @ApplicationContext private val context: Context
    ) {
        suspend fun getSavedIds(): Set<Long> =
            context.savedMeetingScheduleDataStore.data
                .first()[SAVED_SCHEDULE_IDS_KEY]
                .orEmpty()
                .mapNotNull { it.toLongOrNull() }
                .toSet()

        suspend fun markSaved(scheduleId: Long) {
            context.savedMeetingScheduleDataStore.edit { prefs ->
                val current = prefs[SAVED_SCHEDULE_IDS_KEY].orEmpty()
                prefs[SAVED_SCHEDULE_IDS_KEY] = current + scheduleId.toString()
            }
        }
    }
