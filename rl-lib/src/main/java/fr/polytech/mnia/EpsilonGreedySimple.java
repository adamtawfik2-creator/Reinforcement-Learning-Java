package fr.polytech.mnia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

/**
 * Implémentation personnalisée de l'algorithme ε-greedy pour l’environnement "Simple".
 * Il ne prend pas en compte les états, seulement les actions (A, B, C).
 */
public class EpsilonGreedySimple extends Agent {

    private double alpha;
    private double epsilon;
    private int episodes;

    private Map<String, Double> qValues = new HashMap<>();
    private Random random = new Random();

    public EpsilonGreedySimple(Evironnement env, double gamma, double alpha, double epsilon, int episodes) {
        super(env, gamma, 0.0); // theta non utilisé ici
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.episodes = episodes;

        // Initialiser Q(A), Q(B), Q(C)
        qValues.put("A", 0.0);
        qValues.put("B", 0.0);
        qValues.put("C", 0.0);
    }
    private double getReward(Transition transition) {
        String params = transition.getParams().toString();
        System.out.println("params: "+params);
        if (params.contains("B") || params.contains("A"))  {
            return 1.0;
        } else {
            return 0.0;
        }
    }

    @Override
    public void learn() {
        for (int i = 1; i <= episodes; i++) {
            List<Transition> actions = env.getActions();
            Transition chosen;

            // ε-greedy : exploration ou exploitation
            if (random.nextDouble() < epsilon) {
                chosen = actions.get(random.nextInt(actions.size()));
            } else {
                chosen = bestAction(actions);
            }

            String label = getLabel(chosen); // "A", "B", "C"
            double reward = getReward(chosen);

            // Mise à jour : Q ← Q + α [r − Q]
            double oldQ = qValues.getOrDefault(label, 0.0);

            double newQ = oldQ + alpha * (reward - oldQ);
            qValues.put(label, newQ);

            System.out.printf("Épisode %d : Action = %s, Reward = %.1f, Q = %.3f\n",
                              i, label, reward, newQ);
        }

        System.out.println("ε-greedy terminé.");
    }

    private String getLabel(Transition t) {
        // Récupère A, B ou C depuis les paramètres
        return t.getParams().toString().replace("[", "").replace("]", "").trim();
    }

    private Transition bestAction(List<Transition> actions) {
        Transition best = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Transition t : actions) {
            String label = getLabel(t);
            double q = qValues.getOrDefault(label, 0.0);
            if (q > bestValue) {
                bestValue = q;
                best = t;
            }
        }

        return best != null ? best : actions.get(0); // fallback
    }

    @Override
    public Transition chooseAction(State s, List<Transition> actions) {
        return bestAction(actions);
    }

    @Override
    public double getQValue(State s, Transition a) {
    String label = a.getParams().toString().replace("[", "").replace("]", "").trim();
    return qValues.getOrDefault(label, 0.0);
}}
