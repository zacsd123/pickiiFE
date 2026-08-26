package com.example.pickii.di

import android.content.Context
import com.example.pickii.data.repository.ChatApiRepository
import com.example.pickii.domain.repository.ChatRepository
import org.koin.dsl.module

/**
 * 13개 리포지토리 바인딩은 shared의 `sharedRepositoryModule`로 옮겨갔다. `ChatRepository`만 여기
 * 남았다 — `ChatApiRepository`가 사진 업로드에 `android.content.Context`/`Uri`를 직접 써서
 * iOS 쪽 `expect/actual` 재구현이 끝나야(Phase 5) 옮길 수 있다.
 */
val repositoryModule =
    module {
        single<ChatRepository> {
            ChatApiRepository(
                context = get<Context>(),
                chatApiService = get(),
                sessionRepository = get(),
                projectRepository = get()
            )
        }
    }
