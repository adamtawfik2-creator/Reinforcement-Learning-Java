package fr.polytech.mnia;

import java.util.List;

import de.prob.statespace.State;
import de.prob.statespace.Transition;

public abstract class Agent {

    protected  Evironnement env;
    protected double gamma;
    protected double theta;

    public Agent( Evironnement env, double gamma, double theta) {
        this.env = env;
        this.gamma = gamma;
        this.theta = theta;
    }

    public abstract void learn();

    public abstract Transition chooseAction(State s, List<Transition> actions);

    public abstract double getQValue(State s, Transition a);
}
