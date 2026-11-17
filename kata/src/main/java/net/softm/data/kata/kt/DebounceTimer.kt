package net.softm.data.kata.kt;

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class DebounceTimer(
    private val waitTime: Long // 지연 시간 (milliseconds)
) {
    private var debounceJob: Job? = null  // 현재 진행 중인 Job

    fun debounce(newAction: Runnable) {
        debounceJob?.cancel()  // 이전에 실행된 Job이 있다면 취소
        debounceJob = CoroutineScope(Dispatchers.Default).launch {
            delay(waitTime)  // 지정된 시간만큼 대기
            newAction.run()  // 지연 후 실행할 작업
        }
    }
}
