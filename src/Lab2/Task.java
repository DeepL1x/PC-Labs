package Lab2;

public class Task {
    private final Long time;
    private final Long id;
    private final Long managerId;
    private static Long counter = 0L;
    private static final Object countLock = new Object();

    public void run() throws InterruptedException{
        Thread.sleep(time);
    }

    public Task(Long managerId) {
        this.time = (long)(5 + (Math.random()*5)*10e3) ;
        this.managerId = managerId;
        synchronized (countLock) {
            this.id = counter;
            counter++;
        }
    }

    public Long getManagerId() {
        return managerId;
    }

    public Long getId() {
        return id;
    }
}
