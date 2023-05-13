package Lab1;

import java.util.ArrayList;
import java.util.Arrays;


public class Lab1 {

    private static void printMatrix(ArrayList<ArrayList<Double>> matrix, int m, int n) {
        System.out.println("Your matrix is: ");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                System.out.format("%04.02f ", matrix.get(i).get(j));
            }
            System.out.println("\n");
        }
    }

    private static void run(ArrayList<ArrayList<Double>> matrix, int threadCount, int m, int n) {
        double time;
        if (threadCount <= 1) {
            Search s = new Search(matrix, false, 0, m);
            long begin = System.nanoTime();
            s.run();
            long end = System.nanoTime();
            time = (double) (end - begin) / 1e6;
        } else {
            long begin = System.nanoTime();
            ArrayList<Thread> threads = new ArrayList<>();
            if (m > n) {
                double step = (double) m / threadCount;
                for (int i = 0; i < threadCount; i++) {
                    threads.add(
                            new Thread(
                                    new Search(
                                            matrix,
                                            false,
                                            (int) (i * step),
                                            (int) ((i + 1) * step))));
                }
            } else {
                double step = (double) n / threadCount;
                for (int i = 0; i < threadCount; i++) {
                    threads.add(
                            new Thread(
                                    new Search(
                                            matrix,
                                            true,
                                            (int) (i * step),
                                            (int) ((i + 1) * step))));
                }
            }
            for (Thread t : threads) {
                t.start();
            }
            for (Thread t : threads) {
                try {
                    t.join();
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
            long end = System.nanoTime();
            time = (double) (end - begin) / 1e6;
        }
        System.out.println("threads count: " + threadCount);
        Search.showMinMax();
        System.out.println("Time taken to process: " + time + " milliseconds");
        Search.clear();
    }

    public static void main(String[] args) {
        ArrayList<ArrayList<Double>> matrix = new ArrayList<>();

        if (args.length < 3) {
            System.out.println("Not enough arguments provided! " +
                    "Try to provide threads count, rows count and columns count in order.");
        }
        int threadCount = Integer.parseInt(args[0]);
        int m = Integer.parseInt(args[1]);
        int n = Integer.parseInt(args[2]);
        for (int i = 0; i < m; i++) {
            matrix.add(new ArrayList<>());
            for (int j = 0; j < n; j++) {
                matrix.get(i).add((Math.random() * 100));
            }
        }

        if (!Arrays.asList(args).contains("-test")) {
            run(matrix, threadCount, m, n);
        } else {
            run(matrix, 1, m, n);
            run(matrix, 2, m, n);
            run(matrix, 4, m, n);
            run(matrix, 8, m, n);
            run(matrix, 16, m, n);
            run(matrix, 32, m, n);
            run(matrix, 64, m, n);
            run(matrix, 128, m, n);
        }
        if (Arrays.asList(args).contains("-show"))
            printMatrix(matrix, m, n);
    }
}
