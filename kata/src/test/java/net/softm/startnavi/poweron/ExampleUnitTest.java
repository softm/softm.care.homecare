package net.softm.data.kata;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */

@Config(manifest = Config.NONE)
@RunWith(RobolectricTestRunner.class)
//@RunWith(JUnit4.class)
public class ExampleUnitTest {
//    @Test
//    public void addition_isCorrect() {
//        assertEquals(4, 2 + 2);
//    }

    @Test
    public void assetXmlpath() throws InterruptedException {
//        Context ctx = InstrumentationRegistry.getTargetContext();
//        Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Context ctx = InstrumentationRegistry.getInstrumentation().getContext();
//        Context context = ApplicationProvider.getApplicationContext();
//        Context context = ApplicationProvider.getApplicationContext();
        //
        InputStream is = null;
        try {

//            /Users/softm/workspace/softm.data.kata/kata/src/test/assets/strings.xml
            is = ctx.getResources().getAssets().open("strings.xml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            String s = Utils.readTextStream(is,"UTF8");
            Log.d("string.xml",s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        AssetManager am = getAssets() ;
    }
//    @Test
    public void wq() throws InterruptedException {
        System.out.println("Hello, World!");
        WorkQueue.init();
        WorkQueue.addTask(() -> {
            System.out.println("Task 1 is being executed.");
            WorkQueue.nextTask();
        });
//
        WorkQueue.addTask(() -> {
            System.out.println("Task 2 is being executed.");
            WorkQueue.nextTask();
        });

        WorkQueue.execTask();

//        workQueue.queue.clear();
//        System.exit(1);

//
//        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 10,
//                0L, TimeUnit.MILLISECONDS,
//                workQueue.queue);


//        ThreadPoolExecutor executor =
//                new ThreadPoolExecutor(10, 20, 5, TimeUnit.SECONDS,
//                        workQueue.queue, new ThreadPoolExecutor.AbortPolicy());
//
//        executor.shutdown();
//        workQueue.execTask();
//        executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.MILLISECONDS);
    }
}

class WorkQueue {
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

    public static void execTask() {
        Runnable task = null;
        try {
            task = queue.take();
            Thread thread = new Thread(task);
            thread.start();
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
        if ( !queue.isEmpty() ) {
            execTask();
        }
    }
}