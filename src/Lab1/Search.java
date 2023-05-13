package Lab1;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class Search implements Runnable {

    private static final ReentrantLock resMutex = new ReentrantLock();
    private static final ArrayList<Double[]> resArr = new ArrayList<>();

    private static Double min;
    private static Double max;

    private ArrayList<ArrayList<Double>> matrix;
    private final boolean column;
    private final int start;
    private final int end;

    public void setMatrix(ArrayList<ArrayList<Double>> matrix) {
        this.matrix = matrix;
    }

    public ArrayList<ArrayList<Double>> getMatrix() {
        return matrix;
    }

    public Search(ArrayList<ArrayList<Double>> matrix, boolean column, int start, int end) {
        this.matrix = matrix;
        this.column = column;
        this.start = start;
        this.end = end;
    }

    public static void clear(){
        resArr.clear();
    }

    private static void setMinMax() {
        if (resArr.size() > 0) {
            min = resArr.get(0)[0];
            max = resArr.get(0)[1];
            for (Double[] i : resArr) {
                if (i[0] <= min)
                    min = i[0];
                else if (i[1] >= max)
                    max = i[1];
            }
        }
    }

    public static Double getMin() {
        return min;
    }

    public static Double getMax() {
        return max;
    }

    public static void showMinMax() {
        setMinMax();
        System.out.format("min: %04.02f\nmax: %04.02f\n", min, max);
    }

    private void checkPosInMatrix(int start, int end, boolean column) throws Exception {
        if (start >= end)
            throw new Exception("start pos must be < end pos");
        if (start < 0) {
            throw new Exception("start and end positions must be >0 " +
                    "start: " + start +
                    " end: " + end);
        }
        if (column) {
            if (start > matrix.get(0).size() - 1 || end > matrix.get(0).size()) {
                throw new Exception("start or end position does not match rows length. " +
                        "start: " + start +
                        " end: " + end);
            }
        } else {
            if (start > matrix.size() - 1 || end > matrix.size())
                throw new Exception("start or end position does not match columns length. " +
                        "start: " + start +
                        " end: " + end);
        }
    }

    /**Функція для роботи з єдиною матрицею по індексам */
    private void findMinMax() {
        try {
            checkPosInMatrix(start, end, column);

            Double min, max;
            if (column) {
                min = max = matrix.get(0).get(start);
                for (int j = start; j < end; j++) {
                    for (int i = 0; i < matrix.size(); i++) {
                        if (matrix.get(i).get(j) > max) {
                            max = matrix.get(i).get(j);
                        } else if (matrix.get(i).get(j) < min) {
                            min = matrix.get(i).get(j);
                        }
                    }
                }
            } else {
                min = max = matrix.get(start).get(0);
                for (int i = start; i < end; i++) {
                    for (int j = 0; j < matrix.get(start).size(); j++) {
                        if (matrix.get(i).get(j) > max) {
                            max = matrix.get(i).get(j);
                        } else if (matrix.get(i).get(j) < min) {
                            min = matrix.get(i).get(j);
                        }
                    }
                }
            }
            resMutex.lock();
            resArr.add(new Double[]{min, max});
            resMutex.unlock();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    /**Функція для роботи із скопійованими даними*/
    private void findMinMaxA() {
        try {
            Double min, max;
            min = max = matrix.get(0).get(0);
            for (ArrayList<Double> rows : matrix) {
                for (Double value : rows) {
                    if (value > max) {
                        max = value;
                    } else if (value < min) {
                        min = value;
                    }
                }
            }
            resMutex.lock();
            resArr.add(new Double[]{min, max});
            resMutex.unlock();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void run() {
        findMinMax();
//        findMinMaxA();
    }
}
