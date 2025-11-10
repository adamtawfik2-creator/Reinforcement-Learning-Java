package fr.polytech.mnia;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

/**
 * Agent d’apprentissage par Q-learning.
 * L'agent est basé sur une exploration ε-greedy.
 */
public class QLearning extends Agent {

    private Map<State, Map<Transition, Double>> qValues = new HashMap<>();
    private double alpha;      // taux d’apprentissage
    private double epsilon;    // probabilité d'explorer
    private int episodes;      // nombre d'épisodes à jouer
   
    public QLearning(Evironnement env, double gamma, double alpha, double epsilon, int episodes) {
        super(env, gamma, 0.0); // theta inutile ici
        this.alpha = alpha;
        this.epsilon = epsilon;
        this.episodes = episodes;
        
    }
/**Dans notre apprentissage , l agent qui est le joueur 0 va apprendre a jouer contre un joueur 1 qui va aider l agent a gagner  */
   
@Override
public void learn() {
    Random rand = new Random();

    for (int ep = 1; ep <= episodes; ep++) {
        State current = env.getState(); // état initial
        int step = 0;

        while (!current.getOutTransitions().isEmpty()) {
            List<Transition> actions = current.getOutTransitions();

            //  Choix d'action pour le joueur 0 (agent) - epsilon greedy
            Transition action;
            if (rand.nextDouble() < epsilon) {
                action = actions.get(rand.nextInt(actions.size())); // exploration
            } else {
                action = chooseAction(current, actions); // exploitation
            }

            // Joueur 0 joue
            State next = action.getDestination().explore();

            // Joueur 1 (coopératif) joue ensuite pour aider l’agent
            if (!next.getOutTransitions().isEmpty()) {
                List<Transition> oppActions = next.getOutTransitions();
                Transition bestForAgent = null;
                double maxQ = Double.NEGATIVE_INFINITY;

                for (Transition t : oppActions) {
                    State sNext = t.getDestination();
                    double q = getQValue(sNext, chooseAction(sNext, sNext.getOutTransitions()));
                    if (q > maxQ) {
                        maxQ = q;
                        bestForAgent = t;
                    }
                }

                if (bestForAgent != null) {
                    next = bestForAgent.getDestination().explore();
                }
            }

            double reward = env.reward(current, action, next);

            // Initialisation des structures
            qValues.putIfAbsent(current, new HashMap<>());
            qValues.putIfAbsent(next, new HashMap<>());
            qValues.get(current).putIfAbsent(action, 0.0);

            // Mise à jour de la Q-value selon la formule de Q-learning
            double oldQ = qValues.get(current).get(action);
            double maxQNext = qValues.get(next).values().stream()
                                     .max(Double::compare)
                                     .orElse(0.0);
            double newQ = oldQ + alpha * (reward + gamma * maxQNext - oldQ);
            qValues.get(current).put(action, newQ);

            current = next;
            step++;
        }

        System.out.printf("Épisode %d terminé en %d coups.\n", ep, step);
    }

    System.out.println("Q-learning terminé.");
}

    

    @Override
    public Transition chooseAction(State s, List<Transition> actions) {
        // Choisir l’action avec la Q-valeur maximale
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
        return best != null ? best : actions.get(0); // fallback si aucune Q connue
    }
    @Override
    public double getQValue(State s, Transition a) {
        return qValues.getOrDefault(s, new HashMap<>()).getOrDefault(a, 0.0);
    }
}
