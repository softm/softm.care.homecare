package net.softm.data.kata.kt;

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class Debouncerkt(private val delayInMillis: Long) {

    private var debounceJob: Job? = null

    fun debounce(task: () -> Unit) {
        // 이전 작업 취소
        debounceJob?.cancel()

        // 새로운 작업 실행
        debounceJob = CoroutineScope(Dispatchers.Main).launch {
            delay(delayInMillis)
            task()
        }
    }
}
