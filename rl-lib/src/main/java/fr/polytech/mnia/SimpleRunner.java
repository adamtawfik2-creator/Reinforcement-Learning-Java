package fr.polytech.mnia;

import java.util.Random;
import java.util.Scanner;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class SimpleRunner extends Runner {
    private Evironnement environnement;
    private Random random = new Random();
    private double alpha = 0.1;
    private double gamma = 0.9;
    private double epsilon = 0.2;
    private int iterations = 150;
    private double valueA = 0.0;
    private double valueB = 0.0;
    private double valueC = 0.0;

    public SimpleRunner() throws Exception {
        super("/Simple/SimpleRL.mch");
        this.initialise();
        this.environnement = new SimpleEnv(this);
    }

    public void execSequence() throws Exception {
        for (int i = 0; i < iterations; i++) {
            Transition transition = chooseAction();

            if (transition != null) {
                environnement.runAction(transition);

                int reward = getReward(transition);
                updateValues(transition.getParams().toString(), reward);

                System.out.println("Iteration " + (i + 1) + ": Action = " + transition.getParams() + ", Reward = " + reward);
                System.out.println("Values: A = " + valueA + ", B = " + valueB + ", C = " + valueC);
                animator.printState(environnement.getState());
                animator.printActions(environnement.getActions());
            } else {
                System.out.println("Aucune transition trouvée à l'itération : " + i);
            }
        }
    }

    private Transition chooseAction() {
        if (random.nextDouble() < epsilon) {
            // Exploration
            return randomAction();
        } else {
            // Exploitation
            return bestAction();
        }
    }

    private Transition randomAction() {
        var actions = environnement.getActions();
        if (!actions.isEmpty()) {
            return actions.get(random.nextInt(actions.size()));
        }
        return null;
    }

    private Transition bestAction() {
        Transition best = null;
        double bestValue = Double.NEGATIVE_INFINITY;

        for (Transition t : environnement.getActions()) {
            double value = getValue(t.getParams().toString());
            if (value > bestValue) {
                bestValue = value;
                best = t;
            }
        }
        return best;
    }

    private int getReward(Transition transition) {
        String params = transition.getParams().toString();
        if (params.contains("A") | params.contains("B"))  {
            return 1;
        } else {
            return 0;
        }
    }

    private void updateValues(String params, int reward) {
        if (params.contains("A")) {
            valueA = valueA + alpha * (reward + gamma * 0 - valueA);
        } else if (params.contains("B")) {
            valueB = valueB + alpha * (reward + gamma * 0 - valueB);
        } else if (params.contains("C")) {
            valueC = valueC + alpha * (reward + gamma * 0 - valueC);
        }
    }

    private double getValue(String params) {
        if (params.contains("A")) {
            return valueA;
        } else if (params.contains("B")) {
            return valueB;
        } else if (params.contains("C")) {
            return valueC;
        }
        return 0.0;
    }
   public void runAgent(String algo) throws Exception {
    Scanner scanner = new Scanner(System.in);
    Evironnement env = new SimpleEnv(this);
    Agent agent = null;

    switch (algo) {
        case "epsilon":
            System.out.print("Epsilon : ");
            double epsilonn = scanner.nextDouble();
            System.out.print("Alpha : ");
            double alphaa = scanner.nextDouble();
            System.out.print("Gamma : ");
            double gammaa = scanner.nextDouble();
            System.out.print("Episodes : ");
            int episodesEps = scanner.nextInt();
            agent = new EpsilonGreedySimple(env, alphaa, gammaa, epsilonn, episodesEps);
            break;

        case "gradient":
            System.out.print("Alpha : ");
            double alphaGrad = scanner.nextDouble();
            System.out.print("Episodes : ");
            int episodesGrad = scanner.nextInt();
            agent = new GradientBanditSimple(env, alphaGrad, episodesGrad);
            break;

        case "ucb":
            System.out.print("C (exploration constant) : ");
            double c = scanner.nextDouble();
            System.out.print("Episodes : ");
            int episodesUcb = scanner.nextInt();
            agent = new UCBSimple(env, c, episodesUcb);
            break;

        default:
            System.out.println("Algorithme inconnu.");
            scanner.close();
            return;
    }

    scanner.close();
    agent.learn();
    printResults(env, agent);
}

private void printResults(Evironnement env, Agent agent) {
    State s0 = env.getState();
    System.out.println(" Résultats depuis l'état initial :");

    for (Transition a : s0.getOutTransitions()) {
        double q = agent.getQValue(s0, a);
        System.out.printf("Action: %s → Q = %.2f\n", a.getParams(), q);
    }

    Transition best = agent.chooseAction(s0, s0.getOutTransitions());
    System.out.println(" Meilleure action : " + best.getParams());
}

}
