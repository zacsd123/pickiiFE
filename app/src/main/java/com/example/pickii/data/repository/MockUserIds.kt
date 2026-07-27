package com.example.pickii.data.repository

/** 목업 Repository들이 공유하는 사용자 id 상수. 로그인 사용자와 목업 모집 글의 작성자를 같은 값으로 맞춰 데모에서 "내가 작성자인 경우" 흐름을 확인할 수 있게 한다. */
internal object MockUserIds {
    const val CURRENT_USER_ID = "user-me"
}
