package fr.polytech.mnia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class ValueIteration extends Agent {

    private Map<State, Map<Transition, Double>> qValues = new HashMap<>();
    private Map<State, Double> vValues = new HashMap<>();

    public ValueIteration(Evironnement env, double gamma, double theta) {
        super(env, gamma, theta);
        State initial = env.getState();
        vValues.put(initial, 0.0);
        qValues.put(initial, new HashMap<>());
        exploreAllStates(initial, new HashSet<>());
    }

    private void exploreAllStates(State state, Set<String> visited) {
    String id = state.getId(); // identifiant unique de l’état (hashable)

    if (visited.contains(id)) return;
    visited.add(id);

    System.out.println("Exploré : état #" + visited.size());

    vValues.putIfAbsent(state, 0.0);
    qValues.putIfAbsent(state, new HashMap<>());

    for (Transition a : state.getOutTransitions()) {
        State next = a.getDestination();
        qValues.get(state).put(a, 0.0);
        exploreAllStates(next, visited);
    }
    }

/*

    @Override
    public void learn() {
        double delta;
        do {
            exploreAllStates(env.getState(), new HashSet<>());
            delta = 0.0;
            System.out.println(delta);
            for (State s : vValues.keySet()) {
                if (s.getOutTransitions().isEmpty()) continue;

                double oldV = vValues.get(s);
                double maxQ = Double.NEGATIVE_INFINITY;

                for (Transition a : s.getOutTransitions()) {
                    State sPrime = a.getDestination();
                    double reward = env.reward(s, a, sPrime);
                    double q = reward + gamma * vValues.getOrDefault(sPrime, 0.0);

                    qValues.get(s).put(a, q);
                    maxQ = Math.max(maxQ, q);
                }

                vValues.put(s, maxQ);
                delta = Math.max(delta, Math.abs(oldV - maxQ));
            }
        } while (delta > theta);
    }

*/

@Override
public void learn() {
    double delta;
    int iteration = 0;
    int maxIterations = 1000;

    do {
        delta = 0.0;
        iteration++;
        System.out.println("Iteration " + iteration);

        for (State s : vValues.keySet()) {
            List<Transition> actions = s.getOutTransitions();
            if (actions.isEmpty()) {
                // État terminal, on passe
                continue;
            }

            double oldV = vValues.get(s);
            double maxQ = Double.NEGATIVE_INFINITY;

            for (Transition a : actions) {
                State sPrime = a.getDestination();
                double reward = env.reward(s, a, sPrime);
                double q = reward + gamma * vValues.getOrDefault(sPrime, 0.0);

                qValues.get(s).put(a, q);
                maxQ = Math.max(maxQ, q);
            }

            vValues.put(s, maxQ);
            delta = Math.max(delta, Math.abs(oldV - maxQ));
        }

        System.out.printf("    delta = %.6f%n", delta);

        if (iteration >= maxIterations) {
            System.out.println("Trop d'itérations. Arrêt forcé.");
            break;
        }

    } while (delta > theta);

    System.out.println("Apprentissage terminé.");
}

    @Override
    public Transition chooseAction(State s, List<Transition> actions) {
        Map<Transition, Double> qForState = qValues.getOrDefault(s, new HashMap<>());

        Transition best = null;
        double maxQ = Double.NEGATIVE_INFINITY;
        for (Transition a : actions) {
            double q = qForState.getOrDefault(a, 0.0);
            if (q > maxQ) {
                maxQ = q;
                best = a;
            }
        }
        return best;
    }

    public double getValue(State s) {
        return vValues.getOrDefault(s, 0.0);
    }
    @Override
    public double getQValue(State s, Transition a) {
        return qValues.getOrDefault(s, new HashMap<>()).getOrDefault(a, 0.0);
    }
}
