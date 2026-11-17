package net.softm.data.kata.kt;

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.softm.data.kata.WApplication
import net.softm.data.kata.util.Fc

class Callbackkt(private val delayInMillis: Long,private val gubun: String) {
    val TAG = "Callbackkt"

    private var callbackJob: Job? = null
    private var cancel: Boolean = false
    constructor(delayInMillis: Long, gubun: String, cancel: Boolean = false) : this(delayInMillis,gubun) {
        this.cancel = cancel
    }
    constructor(gubun: String,cancel: Boolean = false) : this(0,gubun) {
        this.cancel = cancel
    }

//    fun cancelJob() {
//        if ( callbackJob != null ) {
//            callbackJob?.cancel()
//            callbackJob = null
//        }
//    }
    fun stop() {
        if ( callbackJob != null ) {
            Log.i("Callbackkt","Callbackkt.stop");
            callbackJob?.cancel()
            callbackJob = null
        }
    }

    /**
     * cancel 동작은 하지 않음. 구현 오류. 적용 안됨.
     *
     */
    fun run(callback: (String) -> Unit) {
        if ( cancel && callbackJob != null ) {
            callbackJob?.cancel()
            Fc.i(
                WApplication.mInstance,
                TAG,
                "run.call",
                "cancel['" + gubun + "']")
        }
        // 메인 스레드에서 코루틴 시작
        callbackJob = CoroutineScope(Dispatchers.Main).launch {
            try{
                if ( delayInMillis > 0 ) delay(delayInMillis)
            } catch (e: Exception) {
                // 예외 처리 (필요한 경우)
                e.printStackTrace()
            } finally {
                if ( callback != null ) {
                    Fc.i(
                        WApplication.mInstance,
                        TAG,
                        "run.call",
                        "callback['" + cancel + "','" + gubun + "']"
                    )
                    if (!cancel) {
                        withContext(NonCancellable) {
                            callback("callback[" + gubun + "]")
                        }
                    } else {
                        callback("callback[" + gubun + "]")
                    }
                }
            }
        }
    }

    /**
     * cancel 동작은 하지 않음. 구현 오류. 적용 안됨.
     *
     */
    fun run(callback: (String) -> Unit,vararg delayTime:Long) {
        if ( cancel && callbackJob != null ) {
            callbackJob?.cancel()
            Fc.i(
                WApplication.mInstance,
                TAG,
                "run.call",
                "cancel['" + gubun + "']")
        }
        // 메인 스레드에서 코루틴 시작
        callbackJob = CoroutineScope(Dispatchers.Main).launch {
            try{
                if ( delayInMillis > 0 ) delay(if (delayTime != null) delayTime.get(0)!!.toLong() else delayInMillis)
            } catch (e: Exception) {
                // 예외 처리 (필요한 경우)
                e.printStackTrace()
            } finally {
                if ( callback != null ) {
                    Fc.i(
                        WApplication.mInstance,
                        TAG,
                        "run.call",
                        "callback['" + cancel + "','" + gubun + "']"
                    )
                    if (!cancel) {
                        withContext(NonCancellable) {
                            callback("callback[" + gubun + "]")
                        }
                    } else {
                        callback("callback[" + gubun + "]")
                    }
                }
            }
        }
    }



    /**
     * cancel 동작은 하지 않음. 구현 오류. 적용 안됨.
     *
     */
    fun runIO(callback: (String) -> Unit) {
        if ( cancel && callbackJob != null ) {
            callbackJob?.cancel()
            Fc.i(
                WApplication.mInstance,
                TAG,
                "run.call",
                "cancel['" + gubun + "']")
        }
        // 메인 스레드에서 코루틴 시작
        callbackJob = CoroutineScope(Dispatchers.IO).launch {
            try{
                if ( delayInMillis > 0 ) delay(delayInMillis)
            } catch (e: Exception) {
                // 예외 처리 (필요한 경우)
                e.printStackTrace()
            } finally {
                if ( callback != null ) {
                    Fc.i(
                        WApplication.mInstance,
                        TAG,
                        "run.call",
                        "callback['" + cancel + "','" + gubun + "']"
                    )
                    if (!cancel) {
                        withContext(NonCancellable) {
                            callback("callback[" + gubun + "]")
                        }
                    } else {
                        callback("callback[" + gubun + "]")
                    }
                }
            }
        }
    }


//
//    companion object {
//        @JvmStatic
//        var manager: Callbackkt? = null
//
//        // 사용 예시
//        @JvmStatic
//        fun init() {
//            if ( manager == null ) manager = Callbackkt(OverlayViewUtil.TAG, true)
//        }
//
//        // 사용 예시
//        @JvmStatic
//        fun runLast(callback: (String) -> Unit) = runBlocking {
//            manager?.run(callback);
//        }
//
//        // 사용 예시
//        @JvmStatic
//        fun runFirst(callback: (String) -> Unit) = runBlocking {
//            val flow = MutableSharedFlow<String>()
//
//            // 마지막 호출만 실행하도록 debounce 설정
//            flow
//                .debounce(300) // 300ms 이내 호출된 작업은 무시
//                .onEach { value ->
//                    manager?.run(callback);
//                }
//                .launchIn(this)
//
//        }
//
//    }
}
