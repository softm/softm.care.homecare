package net.softm.data.kata.util;

import java.util.LinkedList;
import java.util.Queue;

class ThreadPool {
    Queue<Runnable> tasks = new LinkedList<>();

    public ThreadPool() {
        Thread worker1 = new Thread(() -> {
            while (true) {
                Runnable newTaskToBeExecuted = pollNextTask();
                if (newTaskToBeExecuted != null) newTaskToBeExecuted.run();
            }
        }, "Worker 1");
        worker1.start();

        Thread worker2 = new Thread(() -> {
            while (true) {
                Runnable newTaskToBeExecuted = pollNextTask();
                if (newTaskToBeExecuted != null) newTaskToBeExecuted.run();
            }
        }, "Worker 2");
        worker2.start();

    }

    public void execute(Runnable newTask) {
        tasks.add(newTask);
    }

    private synchronized Runnable pollNextTask() {
        return tasks.poll();
    }
}