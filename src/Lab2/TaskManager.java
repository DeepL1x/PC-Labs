package Lab2;

public class TaskManager implements Runnable {

    private final Long id;
    private static Long counter = 0L;
    private static final Object countLock = new Object();
    private final ThreadPool pool;
    private final Integer tasksAmount;
    private final Integer maxSecs = 5;
    private boolean stop = false;

    public TaskManager(ThreadPool pool, Integer tasksAmount) {
        this.pool = pool;
        this.tasksAmount = tasksAmount;
        synchronized (countLock) {
            this.id = counter;
            counter++;
        }
    }

    public void pushTask() throws InterruptedException {
        long time;
        time = (long) (Math.random() * 1000 * maxSecs);
        Thread.sleep(time);
        pool.addTask(new Task(id));
    }

    public void stop(){
        stop = true;
    }

    @Override
    public void run() {
        try {
            for (int i = 0; i < (tasksAmount / 3) * 2; i++) {
                if (stop)
                    return;
                pushTask();
            }
            Thread.sleep(25000);
            for (int i = (tasksAmount / 3) * 2; i < tasksAmount; i++){
                if (stop)
                    return;
                pushTask();
            }
        } catch (InterruptedException ignored) {}
    }
}
