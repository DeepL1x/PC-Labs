package Lab2;

import java.util.ArrayList;

public class Lab2 {
    public static void main(String[] args) throws InterruptedException {
        ThreadPool pool = new ThreadPool(20, 6);
        ArrayList<Thread> managers = new ArrayList<>();
        managers.add(new Thread(new TaskManager(pool, 6)));
        for (int i = 0; i < 9; i++) {
            managers.add(new Thread(new TaskManager(pool, 9)));
        }
        for (Thread m : managers) {
            m.start();
        }
        managers.get(0).join();
        pool.terminate();
//        pool.shutdown();
        for (Thread m : managers){
            m.interrupt();
        }
    }
}
