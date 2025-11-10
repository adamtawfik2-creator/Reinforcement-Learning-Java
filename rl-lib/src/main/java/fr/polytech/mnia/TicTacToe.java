package fr.polytech.mnia;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class TicTacToe extends Evironnement{

    public TicTacToe() {
        super(new TicTacToeRunner());
    }

   
    public double reward(State s, Transition a, State sPrime) {
         if (sPrime.eval("win(0)").toString().equals("TRUE")) return 1.0;
    if (sPrime.eval("win(1)").toString().equals("TRUE")) return -1.0;
    if (sPrime.getOutTransitions().isEmpty()) return 0.0; // match nul
    return -0.05; // pénalité pour chaque coup
}

   
    public double getTerminalReward(State s) {
        // facultatif ici, utilisé si tu veux une fonction spécifique pour états terminaux
        return reward(null, null, s);
    }
}
