package Lab4_5;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class FutureSearch implements Callable<Integer[]> {

    private Integer min;
    private Integer max;

    private final ArrayList<ArrayList<Integer>> matrix;
    private final boolean column;
    private final int start;
    private final int end;

    public FutureSearch(ArrayList<ArrayList<Integer>> matrix, boolean column, int start, int end) {
        this.matrix = matrix;
        this.column = column;
        this.start = start;
        this.end = end;
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
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public Integer[] call() {
        findMinMax();
        return new Integer[]{min, max};
    }
}
