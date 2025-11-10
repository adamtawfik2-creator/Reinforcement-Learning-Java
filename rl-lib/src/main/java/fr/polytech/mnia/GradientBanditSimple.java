package fr.polytech.mnia;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class GradientBanditSimple extends Agent {

    private Map<String, Double> H = new HashMap<>();       // préférences
    private Map<String, Double> probs = new HashMap<>();   // π(a)
    private List<String> actions = Arrays.asList("A", "B", "C");
    private Random random = new Random();

    private double alpha;
    private int iterations;
    private double Rbar = 0.0; // moyenne des récompenses

    public GradientBanditSimple(Evironnement env, double alpha, int iterations) {
        super(env, 0.0, 0.0); // gamma/theta inutiles ici
        this.alpha = alpha;
        this.iterations = iterations;

        for (String a : actions) {
            H.put(a, 0.0);
        }
    }

    @Override
    public void learn() {
        for (int t = 1; t <= iterations; t++) {
            computeSoftmax();

            String label = selectAction();
            double reward = getReward(label);

            // Mise à jour de la moyenne des rewards
            Rbar = Rbar + (1.0 / t) * (reward - Rbar);

            // Mise à jour des préférences H(a)
            for (String a : actions) {
                double h = H.get(a);
                double pi = probs.get(a);
                if (a.equals(label)) {
                    H.put(a, h + alpha * (reward - Rbar) * (1 - pi));
                } else {
                    H.put(a, h - alpha * (reward - Rbar) * pi);
                }
            }

            System.out.printf("Épisode %d → Action: %s, Reward: %.1f, Probs: %s\n",
                              t, label, reward, formatProbs());
        }

        System.out.println("Bandit gradient terminé.");
    }

    @Override
    public Transition chooseAction(State s, List<Transition> transitions) {
        // Choisir une action dans la liste transitions, selon probs calculées
        computeSoftmax();
        double p = random.nextDouble();
        double cum = 0.0;

        for (Transition t : transitions) {
            String label = getLabel(t);
            cum += probs.get(label);
            if (p < cum) return t;
        }

        return transitions.get(transitions.size() - 1);
    }

   @Override
    public double getQValue(State s, Transition a) {
        // Ici H(a) = préférence → utilisé à la place de Q-value
        return H.getOrDefault(getLabel(a), 0.0);
    }

    // Helpers

    private void computeSoftmax() {
        double sumExp = actions.stream().mapToDouble(a -> Math.exp(H.get(a))).sum();
        for (String a : actions) {
            probs.put(a, Math.exp(H.get(a)) / sumExp);
        }
    }

    private String selectAction() {
        double p = random.nextDouble();
        double cum = 0.0;
        for (String a : actions) {
            cum += probs.get(a);
            if (p < cum) return a;
        }
        return actions.get(actions.size() - 1);
    }

    private String getLabel(Transition t) {
        return t.getParams().toString().replace("[", "").replace("]", "").trim();
    }

    private double getReward(String label) {
        return (label.equals("A") || label.equals("B")) ? 1.0 : 0.0;
    }

    private String formatProbs() {
        return String.format("A=%.2f, B=%.2f, C=%.2f",
                probs.get("A"), probs.get("B"), probs.get("C"));
    }
}
