package fr.polytech.mnia;

import java.util.List;
import java.util.Random;
import java.util.Scanner;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class TicTacToeRunner extends Runner{
    /*
     * Le constructeur lance ProB sur la machine tictac.mch
     * ensuite initialise les variables 
     */
    public TicTacToeRunner(){
        super("/TicTacToe/tictac.mch") ;
        this.initialise();
    } 

    /*
     * La méthode exec donne un exemple d'exécution aléatoire de transitions
     * A chaque étape on affiche l'état de la grille avec la méthode
     * prettyPrintTicTacToe. Cette dernière lit l'état de la machine B
     * et l'affiche de manière plus jolie et compréhensible
     */
    public void execSequence() throws Exception {
        String win1, win0 ;
        win1 = state.eval("win(1)").toString() ;
        win0 = state.eval("win(0)").toString() ;

        while (
                win1.equals("FALSE") 
                & win0.equals("FALSE")
                & state.getOutTransitions().size() != 0
        ) {
			state = state.anyOperation(null).explore();
            win1 = state.eval("win(1)").toString() ;
            win0 = state.eval("win(0)").toString() ;
            this.prettyPrintTicTacToe() ;

            System.out.println("\nwin(1) " + win1);
            System.out.println("win(0) " + win0 + "\n");
		}
    }

    private void prettyPrintTicTacToe(){
        String input = state.eval("square").toString() ;

        // Grille 3x3 (remplie avec ' ' pour les cases vides)
        String[][] board = {{" ", " ", " "}, {" ", " ", " "}, {" ", " ", " "}};

        // Extraction des valeurs, Supprime tout sauf chiffres et séparateurs
        input = input.replaceAll("[^0-9↦,]", ""); 
        String[] entries = input.split(",");

        for (String entry : entries) {
            String[] parts = entry.split("↦");
            int row = Integer.parseInt(parts[0]) - 1; // Convertir en index (0-2)
            int col = Integer.parseInt(parts[1]) - 1;
            String value = parts[2];  // Garder la valeur en texte (0 ou 1)
            board[row][col] = value;  // Remplir la grille
        }

        // Affichage de la grille
        for (int i = 0; i < 3; i++) {
            System.out.println(" " + board[i][0] + " | " + board[i][1] + " | " + board[i][2]);
            if (i < 2) System.out.println("---+---+---");
        }
    }
    public void runAgent(String algo) throws Exception {
    Scanner scanner = new Scanner(System.in);
    Evironnement env = new TicTacToe();
    Agent agent = null;

    switch (algo) {
        case "q":
            System.out.print("Epsilon : ");
            double epsilon = scanner.nextDouble();
            System.out.print("Alpha : ");
            double alpha = scanner.nextDouble();
            System.out.print("Gamma : ");
            double gamma = scanner.nextDouble();
            System.out.print("Episodes : ");
            int episodes = scanner.nextInt();
            agent = new QLearning(env, alpha, gamma, epsilon, episodes);
            break;

        case "value":
            System.out.print("Gamma : ");
            double gammaV = scanner.nextDouble();
            System.out.print("Seuil epsilon : ");
            double epsilonV = scanner.nextDouble();
            agent = new ValueIteration(env, gammaV, epsilonV);
            break;

        case "policy":
            System.out.print("Gamma : ");
            double gammaP = scanner.nextDouble();
            System.out.print("Seuil epsilon : ");
            double epsilonP = scanner.nextDouble();
            agent = new PolicyIteration(env, gammaP, epsilonP);
            break;

        default:
            System.out.println("⛔ Algorithme non reconnu.");
            return;
    }

    // Apprentissage
    agent.learn();

    // Affichage des Q-valeurs depuis l’état initial
    printResults(env, agent);

    // Combat Agent vs Agent (adversaire stratégique)
    simulateAgentVsAgent(env, agent);

    //Partie humaine contre agent
    playHumanVsAgent(agent);
}
private void simulateAgentVsAgent(Evironnement env, Agent agent) {
    System.out.println(" Simulation d'une partie contre un adversaire stratégique...");
    State current = env.getState();
    int turn = 0;
    Random rand = new Random();

    while (!current.getOutTransitions().isEmpty()) {
        List<Transition> actions = current.getOutTransitions();
        Transition chosen;

        if (turn % 2 == 0) {
            chosen = agent.chooseAction(current, actions);
            System.out.println("Joueur 0 joue : " + chosen.getParams());
        } else {
            Transition worstForAgent = null;
            double minQ = Double.POSITIVE_INFINITY;
            for (Transition t : actions) {
                State sNext = t.getDestination();
                double maxQ = agent.getQValue(sNext, agent.chooseAction(sNext, sNext.getOutTransitions()));
                if (maxQ < minQ) {
                    minQ = maxQ;
                    worstForAgent = t;
                }
            }
            chosen = (worstForAgent != null) ? worstForAgent : actions.get(rand.nextInt(actions.size()));
            System.out.println("Joueur 1 joue : " + chosen.getParams());
        }

        current = chosen.getDestination().explore();
        System.out.println(current);

        if (current.eval("win(0)").toString().equals("TRUE")) {
            System.out.println("Joueur 0 (agent) a gagné !");
            break;
        } else if (current.eval("win(1)").toString().equals("TRUE")) {
            System.out.println("Joueur 1 a gagné !");
            break;
        } else if (current.getOutTransitions().isEmpty()) {
            System.out.println("Match nul !");
            break;
        }

        turn++;
    }
}

public void playHumanVsAgent(Agent agent) {
    Scanner scanner = new Scanner(System.in);
    State current = new TicTacToe().getState();
    int turn = 0;

    System.out.println("Partie humaine contre l'agent !");
    System.out.println("Vous êtes le joueur 1. L’agent RL est joueur 0.\n");

    while (!current.getOutTransitions().isEmpty()) {
        System.out.println(current); // Afficher le plateau

        List<Transition> actions = current.getOutTransitions();
        Transition chosen = null;

        if (turn % 2 == 1) {
            // Agent joue
            chosen = agent.chooseAction(current, actions);
            System.out.println("Agent joue : " + chosen.getParams());
        } else {
            // Humain joue
            System.out.println("Vos actions possibles :");
            for (int i = 0; i < actions.size(); i++) {
                System.out.printf("%d → %s\n", i + 1, actions.get(i).getParams());
            }

            System.out.print("Entrez le numéro de votre action : ");
            int index = scanner.nextInt() - 1;

            while (index < 0 || index >= actions.size()) {
                System.out.print("Choix invalide. Réessayez : ");
                index = scanner.nextInt() - 1;
            }

            chosen = actions.get(index);
        }

        // Appliquer l'action
        current = chosen.getDestination().explore();
        turn++;

        // Vérifier victoire
        if (current.eval("win(0)").toString().equals("TRUE")) {
            System.out.println(current);
            System.out.println("L'agent (joueur 0) a gagné !");
            return;
        } else if (current.eval("win(1)").toString().equals("TRUE")) {
            System.out.println(current);
            System.out.println(" Bravo ! Vous avez gagné !");
            return;
        } else if (current.getOutTransitions().isEmpty()) {
            System.out.println(current);
            System.out.println(" Match nul !");
            return;
        }
    }

    System.out.println("Partie terminée.");
}


private void printResults(Evironnement env, Agent agent) {
    State s0 = env.getState();
    System.out.println("Résultats depuis l'état initial :");

    for (Transition a : s0.getOutTransitions()) {
        double q = agent.getQValue(s0, a);
        System.out.printf("Action: %s → Q = %.2f\n", a.getParams(), q);
    }

    Transition best = agent.chooseAction(s0, s0.getOutTransitions());
    System.out.println("Meilleure action : " + best.getParams());
}


}
