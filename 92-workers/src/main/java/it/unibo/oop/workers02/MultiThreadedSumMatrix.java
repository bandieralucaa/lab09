package it.unibo.oop.workers02;

import java.util.List;
import java.util.ArrayList;

/**
 * MultiThreadedSumMatrix class.
 */
public class MultiThreadedSumMatrix implements SumMatrix {
    private int n;

    /**
     * Costructtor.
     * @param n
     */
    public MultiThreadedSumMatrix(final int n) {
        super();
        if (n < 1) {
            throw new IllegalArgumentException("n must be > 0");
        }
        this.n = n;
    }

    private final class Worker extends Thread {
        private final double[][] matrix;
        private final int startpos;
        private final int nelem;
        private double res;

        private Worker(final double[][] matrix, final int startpos, final int nelem) {
            super();
            this.matrix = matrix;
            this.startpos = startpos;
            this.nelem = nelem;
        }
        @Override
        public void run() {
            for (int i = startpos; i < matrix.length && i < startpos + nelem; i++) {
                for (final double d : this.matrix[i]) {
                    this.res += d;
                }
            }
        }

        public double getResult() {
            return this.res;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double sum(final double[][] matrix) {
        final int size = matrix.length / n + matrix .length % n;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(n);
        for (int start = 0; start < matrix.length; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        /*
         * Start them
         */
        for (final Thread w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        double sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }
}
