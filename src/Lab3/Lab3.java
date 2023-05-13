package Lab3;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Знайти мінімальний елемент масиву, а також кількість цих елементів.
 */
public class Lab3 {

    private static final AtomicInteger min = new AtomicInteger();
    private static final AtomicInteger counter = new AtomicInteger();
    private static final ArrayList<Double> syncTimes = new ArrayList<>();
    private static Integer simpleMin;
    private static Integer simpleCounter;
    private static final Object simpleLock = new Object();
    private static Integer[] array;
    private static final int maxRange = 100;

    private static void noAtomicSingleThreadRun() {
        int minimal = array[0];
        int counter = 1;
        long begin = System.nanoTime();
        for (Integer item : array) {
            if (item < minimal) {
                minimal = item;
                counter = 1;
            } else if (item == minimal)
                counter++;
        }
        long end = System.nanoTime();
        double time = (double) (end - begin) / 1e6;
        System.out.println("Single thread without atomics");
        System.out.println("minimal value of the array: " + minimal + " found " + counter + " time(s)");
        System.out.println("Time taken to process: " + time + " milliseconds");
    }

    private static void run(int threadCount, int size, boolean blocking) {
        double time;
        min.set(array[0]);
        counter.set(1);
        simpleMin = array[0];
        simpleCounter = 1;
        syncTimes.clear();
        if (threadCount <= 1) {
            Searcher s = new Searcher(0, size, blocking);
            long begin = System.nanoTime();
            s.run();
            long end = System.nanoTime();
            time = (double) (end - begin) / 1e6;
        } else {
            long begin = System.nanoTime();
            ArrayList<Thread> threads = new ArrayList<>();
            for (int i = 0; i < threadCount; i++) {
                double step = (double) size / threadCount;
                threads.add(
                        new Thread(
                                new Searcher((int) (i * step),
                                        (int) ((i + 1) * step), blocking)));
            }
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            long end = System.nanoTime();
            time = (double) (end - begin) / 1e6;
        }
        System.out.println("threads count: " + threadCount);
        if (blocking) {
            System.out.println("minimal value of the array: " + simpleMin + " found " + simpleCounter + " time(s)");
            System.out.println("Max time of waiting for unlocking: " + syncTimes.stream().max(Double::compareTo).get() + " milliseconds");
        } else {
            System.out.println("minimal value of the array: " + min.get() + " found " + counter.get() + " time(s)");
        }
        System.out.println("Time taken to process: " + time + " milliseconds");
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Not enough arguments provided! " +
                    "Try to provide threads count, rows count and columns count in order.");
            return;
        }
        int threadCount = Integer.parseInt(args[0]);
        int size = Integer.parseInt(args[1]);
        boolean blocking = Boolean.parseBoolean(args[2]);
        array = new Integer[size];
        for (int i = 0; i < size; i++) {
            array[i] = ThreadLocalRandom.current().nextInt(-maxRange, maxRange + 1);
        }
        if (Arrays.asList(args).contains("-noAtomics")) {
            noAtomicSingleThreadRun();
        }
        else if (!Arrays.asList(args).contains("-test")) {
            run(threadCount, size, blocking);
        } else {
            run(1, size, blocking);
            run(2, size, blocking);
            run(4, size, blocking);
            run(8, size, blocking);
            noAtomicSingleThreadRun();
        }

    }

    private static class Searcher implements Runnable {

        private final int start;
        private final int end;
        private final boolean blocking;
        private long syncTime;

        public Searcher(int start, int end, boolean blocking) {
            this.start = start;
            this.end = end;
            this.blocking = blocking;
            syncTime = 0;
        }

        @Override
        public void run() {
            int temp;
            for (int i = start; i < end; i++) {
                if (blocking) {
                    long begin = System.nanoTime();
                    synchronized (simpleLock) {
                        long finish = System.nanoTime();
                        syncTime += finish - begin;
                        if (array[i] < simpleMin) {
                            simpleMin = array[i];
                            simpleCounter = 1;
                        } else if (Objects.equals(array[i], simpleMin)) {
                            simpleCounter++;
                        }
                    }
                } else {
                    temp = min.get();
                    if (array[i] < temp) {
                        while (!min.compareAndSet(temp, array[i])) {
                            temp = min.get();
                            if (array[i] >= temp)
                                break;
                        }
                        if (array[i] < temp)
                            counter.set(1);
                    } else if (array[i] == temp) {
                        counter.incrementAndGet();
                    }
                }
            }
            if (blocking) {
                synchronized (syncTimes) {
                    syncTimes.add(syncTime / 1e6);
                }
            }
        }
    }
}
