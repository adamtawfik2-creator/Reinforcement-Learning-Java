package fr.polytech.mnia;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class UCBSimple extends Agent {

    private Map<String, Double> qValues = new HashMap<>(); // Moyenne des récompenses
    private Map<String, Integer> counts = new HashMap<>(); // Nombre de fois que a a été joué
    private List<String> actions = Arrays.asList("A", "B", "C");

    private double c;        // facteur de confiance
    private int iterations;

    public UCBSimple(Evironnement env, double c, int iterations) {
        super(env, 0.0, 0.0); // gamma/theta inutiles ici
        this.c = c;
        this.iterations = iterations;

        for (String a : actions) {
            qValues.put(a, 0.0);
            counts.put(a, 0); // initialisé à 0
        }
    }

    @Override
    public void learn() {
        for (int t = 1; t <= iterations; t++) {
            String chosen = selectAction(t);
            double reward = getReward(chosen);

            int n = counts.get(chosen);
            counts.put(chosen, n + 1);

            double oldQ = qValues.get(chosen);
            double newQ = oldQ + (1.0 / (n + 1)) * (reward - oldQ);
            qValues.put(chosen, newQ);

            System.out.printf("Itération %d → Action: %s, Reward: %.1f, Q = %.3f\n",
                              t, chosen, reward, newQ);
        }

        System.out.println("UCB terminé.");
    }

    private String selectAction(int t) {
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestAction = null;

        for (String a : actions) {
            int n = counts.get(a);
            double q = qValues.get(a);

            double bonus = (n == 0) ? Double.POSITIVE_INFINITY
                                    : c * Math.sqrt(Math.log(t) / n);

            double score = q + bonus;

            if (score > bestScore) {
                bestScore = score;
                bestAction = a;
            }
        }

        return bestAction;
    }

    private double getReward(String label) {
        return (label.equals("A") || label.equals("B")) ? 1.0 : 0.0;
    }

    @Override
    public Transition chooseAction(State s, List<Transition> transitions) {
        String best = selectAction(iterations + 1); // ou t courant si fourni

        for (Transition t : transitions) {
            if (getLabel(t).equals(best)) return t;
        }
        return transitions.get(0); // fallback
    }

    private String getLabel(Transition t) {
        return t.getParams().toString().replace("[", "").replace("]", "").trim();
    }

   @Override
    public double getQValue(State s, Transition a) {
        return qValues.getOrDefault(getLabel(a), 0.0);
    }
}
