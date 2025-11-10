package fr.polytech.mnia;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

/**
 * Implémentation de l'algorithme Policy Iteration (avec amélioration).
 * Objectif : apprendre une politique optimale π : état → action.
 */
public class PolicyIteration extends Agent {

    // Table des politiques : pour chaque état, on garde la meilleure action choisie
    private Map<State, Transition> policy = new HashMap<>();

    // Valeur de chaque état sous la politique courante
    private Map<State, Double> vValues = new HashMap<>();

    // Table de Q-valeurs (utile pour analyse ou affichage)
    private Map<State, Map<Transition, Double>> qValues = new HashMap<>();

    public PolicyIteration(Evironnement env, double gamma, double theta) {
        super(env, gamma, theta);

        // Initialisation depuis l'état initial
        State initial = env.getState();
        vValues.put(initial, 0.0);
        exploreAllStates(initial, new HashSet<>());
    }

    /**
     * Explore récursivement tous les états atteignables et initialise :
     * - v(s) = 0
     * - q(s,a) = 0
     * - π(s) = une action arbitraire (initialisation)
     */
    private void exploreAllStates(State state, Set<String> visited) {
        String id = state.getId();
        if (visited.contains(id)) return;
        visited.add(id);

        vValues.putIfAbsent(state, 0.0);
        qValues.putIfAbsent(state, new HashMap<>());

        List<Transition> actions = state.getOutTransitions();

        if (!actions.isEmpty()) {
            // Initialiser la politique par une action arbitraire
            policy.putIfAbsent(state, actions.get(0));

            for (Transition a : actions) {
                qValues.get(state).put(a, 0.0);
                exploreAllStates(a.getDestination(), visited); // récursivité
            }
        }
    }

    /**
     * Algorithme principal : alternance entre évaluation de la politique
     * et amélioration jusqu’à stabilité.
     */
    @Override
    public void learn() {
        boolean policyStable;
        int iteration = 0;

        do {
            iteration++;
            System.out.println(" Iteration " + iteration + " - Évaluation de la politique...");

            // Étape 1 : évaluation de la politique courante π
            boolean converged;
            do {
                converged = true;

                for (State s : policy.keySet()) {
                    Transition a = policy.get(s); // action choisie par la politique
                    State sPrime = a.getDestination(); // état atteint

                    double reward = env.reward(s, a, sPrime);
                    double newV = reward + gamma * vValues.getOrDefault(sPrime, 0.0);

                    // Comparaison pour convergence
                    double delta = Math.abs(vValues.get(s) - newV);
                    if (delta > theta) converged = false;

                    vValues.put(s, newV);
                }

            } while (!converged); // répéter jusqu'à stabilité des valeurs

            // Étape 2 : amélioration de la politique
            System.out.println("Amélioration de la politique...");
            policyStable = true;

            for (State s : policy.keySet()) {
                List<Transition> actions = s.getOutTransitions();
                if (actions.isEmpty()) continue;

                Transition oldAction = policy.get(s); // action actuelle
                Transition bestAction = null;
                double bestQ = Double.NEGATIVE_INFINITY;

                for (Transition a : actions) {
                    State sPrime = a.getDestination();
                    double reward = env.reward(s, a, sPrime);
                    double q = reward + gamma * vValues.getOrDefault(sPrime, 0.0);

                    // stocker pour analyse
                    qValues.get(s).put(a, q);

                    // sélection de la meilleure action
                    if (q > bestQ) {
                        bestQ = q;
                        bestAction = a;
                    }
                }

                policy.put(s, bestAction); // mise à jour

                if (!bestAction.equals(oldAction)) {
                    policyStable = false; // la politique a changé
                }
            }

        } while (!policyStable); // répéter jusqu'à ce que π soit stable

        System.out.println("✅ Politique optimale trouvée !");
    }

    /**
     * Retourne l’action choisie par la politique optimale dans un état donné.
     */
    @Override
    public Transition chooseAction(State s, List<Transition> actions) {
        return policy.getOrDefault(s, actions.get(0));
    }

    /**
     * Récupère la Q-value pour (s,a) (utile pour analyse).
     */
    @Override
    public double getQValue(State s, Transition a) {
        return qValues.getOrDefault(s, new HashMap<>()).getOrDefault(a, 0.0);
    }

    /**
     * Récupère la valeur v(s) de l’état.
     */
    public double getValue(State s) {
        return vValues.getOrDefault(s, 0.0);
    }
}
