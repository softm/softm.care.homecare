//package net.softm.data.kata.job;
//
//
//import com.google.firebase.crashlytics.FirebaseCrashlytics;
//
//import net.softm.data.kata.util.Utils;
//
//import java.util.concurrent.BlockingQueue;
//import java.util.concurrent.LinkedBlockingQueue;
//
//public class WorkQueue {
//    public static BlockingQueue<Runnable> queue = null;
//
//    static FirebaseCrashlytics crashlytics = FirebaseCrashlytics.getInstance();
//
//    public WorkQueue() {
//    }
//
//    public static void init() {
//        crashlytics.log("WorkQueue.init");
//        queue = new LinkedBlockingQueue<>();
//    }
//
//    public static void addTask(Runnable task) {
//
//        // Add a task to the work queue
//        queue.offer(task);
//    }
//
//    public static void execute() {
//        Runnable task = null;
//        try {
//            task = queue.take();
//            Thread thread = new Thread(task);
//            thread.start();
////            try {
////                Thread.sleep(1000);
////            } catch(InterruptedException e) {
////                e.printStackTrace();
////            }
////            thread.interrupt(); // InterruptedException 발생
//
////            task.run();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
////            throw new RuntimeException(e);
//        }
//    }
//
//    public static void nextTask() {
//        if ( queue != null && !queue.isEmpty() ) {
//
////            if ( MapUtil.isTest() ) {
////                WorkerExeckt.nextTask();
////            } else {
//                Utils.sleepMicoSeconds(500);
//                execute();
////            }
//        }
//    }
//}