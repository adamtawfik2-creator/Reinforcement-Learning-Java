package fr.polytech.mnia;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public class SimpleEnv extends Evironnement {

    public SimpleEnv(Runner runner) {
        super(runner);
    }

    @Override
    public double reward(State s, Transition a, State sPrime) {
        
        String label = a.getName(); // nom de la transition

        if (label.equals("a") || label.equals("b")) {
            return 1.0;
        } else if (label.equals("c")) {
            return 0.0;
        }

        // Pour toute autre action inconnue (optionnel)
        return 0.0;
    }
    

    @Override
    public double getTerminalReward(State s) {
        return reward(null, null, s);
    }
}
