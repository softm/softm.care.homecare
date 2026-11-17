package net.softm.data.kata.kt;

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.softm.data.kata.util.L

class Timerkt(private val duration: Long,
              private val onTick: (Long,gubun:String,timer:Timerkt) -> Unit,
              private val onComplete: (gubun:String) -> Unit,
              private val gubun: String) {
    val TAG = "Timerkt"

    private var job: Job? = null
//    constructor(delayInMillis: Long, gubun: String, cancel: Boolean = false) : this(delayInMillis,gubun) {
//        this.cancel = cancel
//    }
//    constructor(gubun: String,cancel: Boolean = false) : this(0,gubun) {
//        this.cancel = cancel
//    }

    fun run() {
        stop()

        job = CoroutineScope(Dispatchers.Main).launch {
            try {
                for (remainingTime in duration downTo 0) {
                    if (!isActive) return@launch
                    L.i(TAG,"run.onTick[$gubun] : $remainingTime")
                    onTick(remainingTime,gubun,this@Timerkt) // Tick callback 호출
                    delay(1000L) // 1초 대기
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //                withContext(Dispatchers.Main) {
//                    withContext(NonCancellable) {
//                        if ( job != null ) onComplete(gubun) // Completed callback 호출
//                        job?.cancel()
//                    }
                withContext(Dispatchers.Main) {
                    L.i(TAG,"run.onComplete[$gubun]")
                    onComplete(gubun) // Completed callback 호출
                }
//                        job?.cancel()
            }
        }
    }

    fun runIO() {
        stop()

        job = CoroutineScope(Dispatchers.IO).launch {
            try {
                for (remainingTime in duration downTo 0) {
                    if (!isActive) return@launch
                    L.i(TAG,"run.onTick[$gubun] : $remainingTime")
                    onTick(remainingTime,gubun,this@Timerkt) // Tick callback 호출
                    delay(1000L) // 1초 대기
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                //                withContext(Dispatchers.Main) {
//                    withContext(NonCancellable) {
//                        if ( job != null ) onComplete(gubun) // Completed callback 호출
//                        job?.cancel()
//                    }
                withContext(Dispatchers.Main) {
                    L.i(TAG,"run.onComplete[$gubun]")
                    onComplete(gubun) // Completed callback 호출
                }
//                        job?.cancel()
            }
        }
    }


    fun stop() {
        if ( job != null ) {
            Log.i("Timerkt","Timerkt.stop");
            job?.cancel()
            job = null
        }
    }

}
