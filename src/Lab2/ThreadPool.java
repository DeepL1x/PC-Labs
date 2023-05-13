package Lab2;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Пул потоків обслуговується 6-ма робочими потоками й має одну чергу
 * виконання. Черга виконання має обмежений розмір в 20 задач. Задачі
 * додаються одразу в кінець черги виконання, або відкидаються, якщо вони
 * не поміщаються в чергу виконання. Задача береться на виконання з буферу
 * одразу за наявності вільного робочого потоку. Задача займає випадковий
 * час від 5 до 10 секунд.
 */
public class ThreadPool {
    private boolean terminated, initialized, shutdown;
    private final MyQueue<Task> tasks;
    private final ReadWriteLock lock;
    private final ArrayList<Thread> workers;
    private final Object taskWaiter;

    ThreadPool(Integer maxQueueSize, Integer threadsCount) {
        tasks = new MyQueue<>(maxQueueSize);
        lock = new ReentrantReadWriteLock();
        taskWaiter = new Object();
        workers = new ArrayList<>();
        terminated = false;
        initialized = false;
        shutdown = false;
        initialize(threadsCount);
    }

    public boolean working() {
        boolean res;
        lock.readLock().lock();

        res = working_unsafe();

        lock.readLock().unlock();
        return res;
    }

    public boolean working_unsafe() {
        return initialized && !terminated && !shutdown;
    }

    public void initialize(Integer threadsCount) {
        lock.writeLock().lock();

        if (!initialized && !terminated) {
            for (int i = 0; i < threadsCount; i++) {
                workers.add(new Thread(new Worker()));
            }
            for (Thread w : workers) {
                w.start();
            }
            initialized = true;
        }
        System.out.println("Thread pool is successfully initialized");
        lock.writeLock().unlock();
    }

    public void addTask(Task task) {
        if (working()) {
            if (!tasks.add(task)) {
                System.out.println("Task {id=" + task.getId() + ":" + task.getManagerId() + "} was ignored due to queue overflow!");
            } else {
                System.out.println("Task {id=" + task.getId() + ":" + task.getManagerId() + "} was added to queue!");
                synchronized (taskWaiter) {
                    taskWaiter.notify();
                }
            }
        } else {
            System.out.println("Cannot add task {id=" + task.getId() + ":" + task.getManagerId() + " due to thread pool not working");
        }
    }

    public void shutdown() {

        System.out.println("THREAD POOL SHUTDOWN WAS CALLED!");

        lock.writeLock().lock();
        if (working_unsafe()) {
            shutdown = true;
        } else {
            workers.clear();
            initialized = false;
            shutdown = false;
            return;
        }
        synchronized (taskWaiter) {
            taskWaiter.notifyAll();
        }
        for (Thread w : workers) {
            w.interrupt();
        }
        workers.clear();
        initialized = false;
        shutdown = false;
        System.out.println("max time queue was full: " + tasks.getMaxTime() / 1000000);
        System.out.println("min time queue was full: " + tasks.getMinTime() / 1000000);
        System.out.println("ignored tasks due to queue overflow: " + tasks.getIgnored());
        lock.writeLock().unlock();
    }

    public void terminate() {
        lock.writeLock().lock();

        System.out.println("THREAD POOL TERMINATION WAS CALLED!");

        if (working_unsafe()) {
            terminated = true;
        } else {
            workers.clear();
            initialized = false;
            return;
        }

        synchronized (taskWaiter) {
            taskWaiter.notifyAll();
        }

        try {
            for (Thread w : workers) {
                w.join();
            }
        } catch (InterruptedException ignored) {
        }
        workers.clear();
        terminated = false;
        initialized = false;
        System.out.println("max time queue was full: " + tasks.getMaxTime() / 1000000);
        System.out.println("min time queue was full: " + tasks.getMinTime() / 1000000);
        System.out.println("ignored tasks due to queue overflow: " + tasks.getIgnored());
        lock.writeLock().unlock();
    }

    private class Worker extends Thread {
        private static Long counter = 0L;
        private final Long id;
        private static final Object countLock = new Object();

        Worker() {
            synchronized (countLock) {
                id = counter;
                counter++;
            }
        }

        @Override
        public void run() {
            boolean isTaskTaken;
            Task task = null;
            while (true) {
                isTaskTaken = false;
                try {
                    synchronized (taskWaiter) {
                        while (tasks.isEmpty() && !terminated && !shutdown) {
                            taskWaiter.wait();
                        }
                        if (!terminated && !shutdown) {
                            task = tasks.pop();
                            isTaskTaken = true;
                        }
                    }
                } catch (InterruptedException ignored) {
                    System.err.println("Thread Interrupted");
                    return;
                }
                if (shutdown) {
                    System.out.println("Worker {id=" + id + "} is shutting down due to thread pool shutdown");
                    return;
                }
                if (terminated && !isTaskTaken) {
                    System.out.println("Worker {id=" + id + "} is shutting down due to thread pool interruption");
                    return;
                }
                assert task != null;

                try {
                    System.out.println("Worker {id=" + id + "} took task {id=" + task.getId() + ":" + task.getManagerId() + "}");
                    task.run();
                    System.out.println("Worker {id=" + id + "} has finished task {id=" + task.getId() + ":" + task.getManagerId() + "}");
                } catch (InterruptedException e) {
                    System.err.println("Thread Interrupted");
                    return;
                }
            }
        }
    }
}
