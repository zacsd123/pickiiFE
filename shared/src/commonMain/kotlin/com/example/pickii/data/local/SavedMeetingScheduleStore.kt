package com.example.pickii.data.local

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.first

private val SAVED_SCHEDULE_IDS_KEY = stringSetPreferencesKey("saved_meeting_schedule_ids")

/**
 * 회의 확정(7-13) 후 "내 캘린더에 저장"을 눌러 이미 저장한 팀 일정의 scheduleId를 기기에 보존한다.
 * 되읽기 API가 없어(`ChatRoomUiState.savedMeetingScheduleIds` 참고) ViewModel 상태만으로는 채팅방을
 * 나갔다 다시 들어오면(ViewModel 재생성) 저장 여부가 초기화되던 문제를 이걸로 해결한다.
 *
 * Koin 싱글턴(`di/InfraModule.kt`).
 */
class SavedMeetingScheduleStore {
    private val dataStore = preferencesDataStore("pickii_saved_meeting_schedule_store")

    suspend fun getSavedIds(): Set<Long> =
        dataStore.data
            .first()[SAVED_SCHEDULE_IDS_KEY]
            .orEmpty()
            .mapNotNull { it.toLongOrNull() }
            .toSet()

    suspend fun markSaved(scheduleId: Long) {
        dataStore.edit { prefs ->
            val current = prefs[SAVED_SCHEDULE_IDS_KEY].orEmpty()
            prefs[SAVED_SCHEDULE_IDS_KEY] = current + scheduleId.toString()
        }
    }
}
