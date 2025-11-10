package fr.polytech.mnia;
import java.util.Scanner;

public class App {
    public static void main(String[] args) throws Exception {
         Scanner scanner = new Scanner(System.in);

        // Choix de l'environnement
        System.out.println("Choisissez l’environnement : [simple / tictactoe]");
        String env = scanner.nextLine().trim().toLowerCase();

        // Choix de l'algorithme
        System.out.println("Choisissez l’algorithme : [epsilon / gradient / ucb / q / value / policy]");
        String algo = scanner.nextLine().trim().toLowerCase();

        // Lancer le bon environnement
        switch (env) {
            case "simple":
                new SimpleRunner().runAgent(algo);
                break;
            case "tictactoe":
                new TicTacToeRunner().runAgent(algo);
                break;
            default:
                System.out.println("Environnement inconnu.");
        }

        scanner.close();
        System.exit(0);
    }
    }
