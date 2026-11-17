package net.softm.data.kata.kt;

import android.content.Context
import android.util.Log
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.animation.AnimationSet
import android.view.animation.ScaleAnimation
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.softm.data.kata.ACSFrameLayout
import net.softm.data.kata.job.WorkExecutor
import net.softm.data.kata.job.WorkQueue
import net.softm.data.kata.util.AcsUtil
import net.softm.data.kata.util.Utils

class WorkerExeckt {
    companion object {
        @JvmStatic
        fun start() {
            CoroutineScope(Dispatchers.Main).launch {
                WorkExecutor.start()
            }
        }
        @JvmStatic
        fun nextTask() {
            Utils.sleepMicoSeconds(500)
            CoroutineScope(Dispatchers.IO).launch {
                withContext(Dispatchers.Main) {
//                    WorkQueue.nextTask();
//                    WorkQueue.execute()
                }
                WorkQueue.execute()
            }
        }

        @JvmStatic
        var job: Job? = null
        @JvmStatic
        fun startTimer(textView: TextView,limitSec:Long,animation:Boolean,callback: (String) -> Unit) {
            cancelTimer()
            var scaleAnimation2: Animation? = null
            var scaleAnimation: Animation? = null
            var alphaAnimation: Animation? = null
            if ( animation ) {
                // 크기 줄이기 애니메이션
                scaleAnimation2 = ScaleAnimation(
                    1f, 0.5f,  // X축 크기 변화
                    1f, 0.5f,  // Y축 크기 변화
                    Animation.RELATIVE_TO_SELF, 0.5f,  // X축 중심
                    Animation.RELATIVE_TO_SELF, 0.5f // Y축 중심
                )
                scaleAnimation2.duration = 1000
                scaleAnimation2.fillAfter = true
                scaleAnimation = ScaleAnimation(
                    1.0f, 0.0f, 1.0f,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                scaleAnimation.duration = 1000
                scaleAnimation.fillAfter = true
                // 알파값 변화 애니메이션
                alphaAnimation = AlphaAnimation(1f, 0f) // 시작: 1f, 종료: 0f
                alphaAnimation.duration = 1000
                alphaAnimation.fillAfter = true
            }
            val animationSet = AnimationSet(true)
            textView.text = (limitSec).toString()
            if ( animation ) {
                animationSet.addAnimation(scaleAnimation)
                animationSet.addAnimation(alphaAnimation)
                textView.startAnimation(animationSet)
            }
//            textView.text = "$limitSec"
            // 메인 스레드에서 코루틴 시작
                job = CoroutineScope(Dispatchers.Main).launch {
                    try {
                        var seconds = 0
                        var count = 0
                        while (seconds < limitSec) { // 10초 동안 실행
                            if (!isActive) return@launch
                            count = (limitSec-seconds).toInt();
                            seconds++
                            textView.startAnimation(animationSet)
                            textView.text = "$count"
                            delay(1000L) // 1초 대기
                        }

    //                GlobalScope.launch(Dispatchers.Main) {
                        // TextView를 갱신 (메인 스레드에서 실행)

    //                }
                    } catch (e: Exception) {
                        // 예외 처리 (필요한 경우)
                        e.printStackTrace()
                    } finally {
    //                withContext(Dispatchers.Main) {
                        withContext(NonCancellable) {
                            textView.text = "-"
                            if ( job != null ) callback("Task Completed!")
                        }
//                        job?.cancel()
                    }
            }
        }

        @JvmStatic
        fun cancelTimer() {
            if ( job != null ) {
                Log.i("WorkerExeckt","WorkerExeckt.cancelTimer");
                job?.cancel()
                job = null
            }
        }

        @JvmStatic
        fun goHomeScreen(context:Context,workQueueNext:Boolean) {
            CoroutineScope(Dispatchers.IO).launch {
                val acsUtil = AcsUtil(context)
//                DeviceUtil.acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_HOME)
                acsUtil.sendEvent(AcsUtil.KEY_GLOBAL_ACTION_HOME) {
                    if (workQueueNext) {
                        Log.d("ACS", "goHomeScreen.nextTask")
                        WorkQueue.nextTask()
                    }
                }
//                withContext(Dispatchers.Main) {
//                    if (workQueueNext) {
//                        Log.d("ACS", "goHomeScreen.nextTask")
//                        WorkQueue.nextTask()
//                    }
//                }
            }

        }

        @JvmStatic
        var waitJob: Job? = null
        @JvmStatic
        fun waitStartTimer(textView: TextView,limitSec:Long,animation:Boolean,callback: (String) -> Unit) {
            waitCancelTimer()
            var scaleAnimation2: Animation? = null
            var scaleAnimation: Animation? = null
            var alphaAnimation: Animation? = null
            if ( animation ) {
                // 크기 줄이기 애니메이션
                scaleAnimation2 = ScaleAnimation(
                    1f, 0.5f,  // X축 크기 변화
                    1f, 0.5f,  // Y축 크기 변화
                    Animation.RELATIVE_TO_SELF, 0.5f,  // X축 중심
                    Animation.RELATIVE_TO_SELF, 0.5f // Y축 중심
                )
                scaleAnimation2.duration = 1000
                scaleAnimation2.fillAfter = true
                scaleAnimation = ScaleAnimation(
                    1.0f, 0.0f, 1.0f,
                    0.0f, Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f
                )
                scaleAnimation.duration = 1000
                scaleAnimation.fillAfter = true
                // 알파값 변화 애니메이션
                alphaAnimation = AlphaAnimation(1f, 0f) // 시작: 1f, 종료: 0f
                alphaAnimation.duration = 1000
                alphaAnimation.fillAfter = true
            }
            val animationSet = AnimationSet(true)
            textView.text = (limitSec).toString()
            if ( animation ) {
                animationSet.addAnimation(scaleAnimation)
                animationSet.addAnimation(alphaAnimation)
                textView.startAnimation(animationSet)
            }
//            textView.text = "$limitSec"
            // 메인 스레드에서 코루틴 시작
            waitJob = CoroutineScope(Dispatchers.Main).launch {
                try {
                    var seconds = 0
                    var count = 0
                    while (seconds < limitSec) { // 10초 동안 실행
                        if (!isActive) return@launch
                        count = (limitSec-seconds).toInt();
                        seconds++
                        textView.startAnimation(animationSet)
                        textView.text = "$count"
                        delay(1000L) // 1초 대기
                    }

                    //                GlobalScope.launch(Dispatchers.Main) {
                    // TextView를 갱신 (메인 스레드에서 실행)

                    //                }
                } catch (e: Exception) {
                    // 예외 처리 (필요한 경우)
                    e.printStackTrace()
                } finally {
                    //                withContext(Dispatchers.Main) {
                    withContext(NonCancellable) {
                        textView.text = "-"
                        if ( waitJob!=null) callback("Task Completed!")
                    }
//                        job?.cancel()
                }
            }
        }

        @JvmStatic
        fun waitCancelTimer() {
            if ( waitJob != null ) waitJob?.cancel();
            waitJob = null;
        }


        @JvmStatic
        fun testDrawLayout(layout: ACSFrameLayout,limitSec:Long,callback: (String) -> Unit) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    var seconds = 0
                    var count = 0
                    while (seconds < limitSec) { // 10초 동안 실행
                        if (!isActive) return@launch
                        count = (limitSec-seconds).toInt();
                        seconds++
                        delay(1000L) // 1초 대기
                    }
                } catch (e: Exception) {
                    // 예외 처리 (필요한 경우)
                    e.printStackTrace()
                } finally {
                    withContext(NonCancellable) {
                        callback("Task Completed!")
                    }
                }
            }
        }

        @JvmStatic
        var delayJob: Job? = null
        @JvmStatic
        fun delayCallback(callback: (String) -> Unit,delayTime:Long) {
            if ( delayJob != null ) delayJob?.cancel()
            // 메인 스레드에서 코루틴 시작
            delayJob = CoroutineScope(Dispatchers.Main).launch {
                try{
                    delay(delayTime)
                } catch (e: Exception) {
                    // 예외 처리 (필요한 경우)
                    e.printStackTrace()
                } finally {
                    if ( callback != null ) {
                        withContext(NonCancellable) {
                            callback("Task Completed!")
                        }
                    }
                }
            }
        }

        @JvmStatic
        fun delayCallback(callback: (String) -> Unit) {
            delayCallback(callback,0)
        }

        @JvmStatic
        var mainReceiverJob: Job? = null
        @JvmStatic
        fun runReceiver(callback: (String) -> Unit,delayTime:Long) {
            if ( mainReceiverJob != null ) mainReceiverJob?.cancel()
            // 메인 스레드에서 코루틴 시작
            mainReceiverJob = CoroutineScope(Dispatchers.Main).launch {
                try{
                    delay(delayTime)
                } catch (e: Exception) {
                    // 예외 처리 (필요한 경우)
                    e.printStackTrace()
                } finally {
                    if ( callback != null ) {
                        withContext(NonCancellable) {
                            callback("runReceiver")
                        }
                    }
                }
            }
        }

        @JvmStatic
        var runToCallbackJob: Job? = null
        @JvmStatic
        fun runToCallback(runCallback: (String) -> Unit,callback: (String) -> Unit, gubun:String) {
            cancelRunToCallbackJob()
            // 메인 스레드에서 코루틴 시작
            waitJob = CoroutineScope(Dispatchers.Main).launch {
                try {
                    runCallback(gubun);
                } catch (e: Exception) {
                    // 예외 처리 (필요한 경우)
                    e.printStackTrace()
                } finally {
                    withContext(NonCancellable) {
                        callback(gubun)
                    }
                }
            }
        }

        @JvmStatic
        fun cancelRunToCallbackJob() {
            if ( runToCallbackJob != null ) runToCallbackJob?.cancel()
        }
    }



}

