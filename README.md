# Smart-Traffic-AI

An AI project in which an AI agent controls traffic lights on 4 simulated street intersections. 
The street simulation was for formulated as 2 North-South 1-way streets intersecting with 2 East-West 1-way streets. Time is discreet in this simulation and on each time step, a number cars appear stochastically at the entrace of each street. Street may have varrying distribution of how many cars will appear. Cars also have a likelihood of going forward vs making a turn on the light. The "state" in this context is the number of cars waiting at each direction of each intersection.
To deal with the high number of possible individual states, the AI uses K-means clutering. This allows the AI to identify a particular state with its nearest cluster. This allows it to deal with states it has never encountered before, by using the nearest cluster as an estimate, then adjusting the cluster.

The AI then uses the Q-learning algorithm to learn from previous experiences. By mapping each state-cluster to a "best action" through an outcome scoring system, it's able to use the scores of previous state-action pairs to determine the current best outcome. This score is then updated according to the outcome that follows.
An decaying exploration coefficient was used, which causes the AI to explore alternatives to known "best actions" at the begining of the simulation.

