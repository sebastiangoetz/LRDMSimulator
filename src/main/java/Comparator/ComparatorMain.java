package Comparator;
import program.SolvePOMDP;

import java.util.*;
import java.util.concurrent.*;


public class ComparatorMain {
    public static void main(String[] args) throws Exception {
		System.out.println("SolvePOMDP v0.0.3");
		System.out.println("Author: Erwin Walraven");
		System.out.println("Web: erwinwalraven.nl/solvepomdp");
		System.out.println("Delft University of Technology");


        int runs = 10; //number of compared POMDPs
        int scenarios = 10; //number of scenarios

        ExecutorService executor = Executors.newFixedThreadPool(runs);
        List<Future<POMDPEvaluator>> futures = new ArrayList<>();
        for (int s = 0; s < scenarios; s++) {
            for (int i = 0; i < runs; i++) {
                SolvePOMDP task = new SolvePOMDP( i, true, s,true);
                futures.add(executor.submit(task));
            }
        }
        //Solver ID 100 is only a placeholder. pOMDPRun false activates the basic optimizer
        for (int s = 0; s < scenarios; s++) {
            SolvePOMDP optimal = new SolvePOMDP(100, false, s,true);
            futures.add(executor.submit(optimal));
        }
        //POMDP_101 is the original POMDP
        for (int s = 0; s < scenarios; s++) {
            SolvePOMDP original = new SolvePOMDP(101, true,s,true);
            futures.add(executor.submit(original));
        }
        EvaluatorRepository repository = new EvaluatorRepository();
        ComparatorGUI gui = new ComparatorGUI();
        gui.setVisible(true);

        for (Future<POMDPEvaluator> future : futures) {
            POMDPEvaluator eval = future.get();
            repository.add(eval);
            gui.updateResults(repository.getAll());
        }

        executor.shutdown();
    }
}
