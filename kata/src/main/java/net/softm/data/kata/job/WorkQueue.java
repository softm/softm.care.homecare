package net.softm.data.kata.job;


import net.softm.data.kata.kt.Callbackkt;
import net.softm.data.kata.kt.Callkt;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicReference;

public class WorkQueue {

    public static final String TAG = "WorkExecutor";

    public static BlockingQueue<Runnable> queue = null;

    public WorkQueue() {
    }

    public static void init() {
        queue = new LinkedBlockingQueue<>();
    }

    public static void addTask(Runnable task) {

        // Add a task to the work queue
        queue.offer(task);
    }

    public static void execute() {
        AtomicReference<Runnable> task = new AtomicReference<>();
        try {
            new Callkt("WorkQueue-execute").run((params)->{
                try {
                    task.set(queue.take());
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
// TODO SOFTM : ANR 2024.11.28 : ANR 조치 - thread.start() 로직제거
//            Thread thread = new Thread(task);
//            thread.start();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                executor.submit(task.get());
                executor.shutdown();
                return null;
            });
        } catch (Exception e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    public static void execute_() {
        Runnable task = null;
        try {
            task = queue.take();
// TODO SOFTM : ANR 2024.11.28 : ANR 조치 - thread.start() 로직제거
//            Thread thread = new Thread(task);
//            thread.start();
            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(task);
            executor.shutdown();
//            try {
//                Thread.sleep(1000);
//            } catch(InterruptedException e) {
//                e.printStackTrace();
//            }
//            thread.interrupt(); // InterruptedException 발생

//            task.run();
        } catch (InterruptedException e) {
            e.printStackTrace();
//            throw new RuntimeException(e);
        }
    }

    public static void nextTask() {
        if ( queue != null && !queue.isEmpty() ) {
//            if ( MapUtil.isTest() ) {
//                WorkerExeckt.nextTask();
//            } else {
//                Utils.sleepMicoSeconds(300); // TODO SOFTM : ANR 2024.11.28 : ANR 조치 500 --> 300
            Callbackkt callbackkt = new Callbackkt(300,"WorkQueue.nextTask;");
            callbackkt.run((param)->{
                execute();
                return null;
            });

//            }
        } else {
            WorkExecutor.isExecuting = false;
        }
    }
}