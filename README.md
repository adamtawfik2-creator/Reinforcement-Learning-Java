#  Reinforcement Learning in Java

**Authors:** Adam Tawfik & Abdallah Abdelsadek  
**Project:** Méthodes Numériques – Polytech Grenoble  
**Language:** Java  
**Subject:** Reinforcement Learning (RL)

---

## Project Overview

This project implements a **modular Reinforcement Learning library** in Java.  
It supports both **tabular** and **policy-based** methods, and can be applied to various environments like:
- a **Simple static environment** (multi-armed bandits)
- a **TicTacToe environment** (sequential decision problem)

The library allows easy extension with new algorithms and environments.

---

##  Implemented Algorithms

| Category | Algorithms |
|-----------|-------------|
| **Bandit Problems** | Epsilon-Greedy, UCB, Gradient Bandit |
| **Markov Decision Processes (MDP)** | Q-Learning, Policy Iteration, Value Iteration |

Each agent implements the `Agent` interface, which defines:
- `learn()`
- `chooseAction(...)`
- `getQValue(...)`

---

##  Environments

###  Simple Environment
Single-state environment for testing **bandit algorithms**.  
The agent learns which action maximizes expected reward.

###  TicTacToe Environment
Two-player board game with:
- Reward = +1 (win), -1 (loss), 0 (draw), -0.25 (non-optimal move)
- Supports both **Agent vs Human** and **Agent vs Agent** modes
- Demonstrates the application of Q-Learning and policy iteration in discrete spaces

---

##  Project Structure

# Project

- First, clone this repository

- Check if maven is installed : mvn -version

- If maven is not installed, run sudo apt install maven -y

## Run the Project

- In the same folder as pom.xml, run :

mvn clean

mvn compile

mvn exec:java
