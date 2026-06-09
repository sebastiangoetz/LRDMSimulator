package Comparator;

import java.util.ArrayList;

public class EvaluatorRepository {

    private final ArrayList<POMDPEvaluator> evaluators = new ArrayList<>();

    public synchronized void add(POMDPEvaluator evaluator) {
        evaluators.add(evaluator);
    }

    public synchronized ArrayList<POMDPEvaluator> getAll() {
        return new ArrayList<>(evaluators);
    }
    public synchronized int size() {
        return evaluators.size();
    }
}
