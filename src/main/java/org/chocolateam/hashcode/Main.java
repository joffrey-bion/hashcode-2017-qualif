package org.chocolateam.hashcode;

import java.util.concurrent.TimeUnit;

import com.lwouis.hashcode.ProblemSolver;

public class Main {

    private static final int TIMEOUT = 30;

    private static final TimeUnit TIMEOUT_UNIT = TimeUnit.MINUTES;

    private static final int N_THREADS = 4;

    public static void main(String[] args) {
        ProblemSolver solver = new Solver();
        for (String inputFilename : args) {
            String outputFilename = inputFilename.replaceAll("in", "out");
            String threadName = Thread.currentThread().getName();
            System.out.println(threadName + ": started solving '" + inputFilename + "'");
            solver.solve(inputFilename, outputFilename);
            System.out.println(threadName + ": finished solving '" + inputFilename + "' in '" + outputFilename + "'");
        }
//        ParallelFilesExecutorService.processInputFiles(args, TIMEOUT, TIMEOUT_UNIT, N_THREADS, solver);
    }
}
