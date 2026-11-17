package net.softm.data.kata.kt;

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.softm.data.kata.WApplication
import net.softm.data.kata.util.Fc

class Callkt(private val gubun: String) {
    private val TAG = "Callkt"
    private var callbackJob: Job? = null
    private var cancel: Boolean = false
    constructor(delayInMillis: Long, gubun: String, cancel: Boolean = false) : this(gubun) {
        this.cancel = cancel
    }

    fun run(callback: (String) -> Unit) {
        if ( cancel && callbackJob != null ) callbackJob?.cancel()
        // 메인 스레드에서 코루틴 시작
        callbackJob = CoroutineScope(Dispatchers.Main).launch {
            if ( callback != null ) {
                withContext(NonCancellable) {
                    Fc.i(WApplication.mInstance.applicationContext, TAG, "run.call")
                    callback("call[" + gubun + "]")
                }
            }
        }
    }

    fun runIO(callback: (String) -> Unit) {
        if ( cancel && callbackJob != null ) callbackJob?.cancel()
        // 메인 스레드에서 코루틴 시작
        callbackJob = CoroutineScope(Dispatchers.IO).launch {
            if ( callback != null ) {
                withContext(NonCancellable) {
                    Fc.i(WApplication.mInstance, TAG, "runIO.call",gubun)
                    callback("call[" + gubun + "]")
                }
            }
        }
    }
}
