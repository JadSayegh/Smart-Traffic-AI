

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class Agent {
	PrintWriter writer;
	
	int turn = 0, totalReward = 0;
	double averageReward = 0;
	int policyCount;
	int width, height;
	
	final int CLUSTER_COUNT = 200; //number of clusters
	final int MAX_ITERATION = 40000; //number of iterations to simulate over
	final double MAX_TEMP = 300; //max temperature used in the Boltzmann exploration function
	final double EPS = 0.98, BETA = 0.8, GAMMA = 0.7; //Respectively, the exploration rate, learning rate and discount rate

	State[] centroids = new State[CLUSTER_COUNT]; // the list of centroid for each cluster
	int[] stateCount = new int[CLUSTER_COUNT]; // number of state subscribed to each cluster
	double[][] QValues; // Matrix of Qvalues 
	int[][] QVisitCount; //How many times has each QValue been visited
	boolean[][][] policies = {{{false, false}, {false, false}},{{false, false}, {false, true}},{{false, false}, {true, false}},{{false, false}, {true, true}}, {{false, true}, {false, false}},{{false, true}, {false, true}},{{false, true}, {true, false}},{{false, true}, {true, true}},{{true, false}, {false, false}},{{true, false}, {false, true}},{{true, false}, {true, false}},{{true, false}, {true, true}},{{true, true}, {false, false}},{{true, true}, {false, true}},{{true, true}, {true, false}},{{true, true}, {true, true}}};


	Model model;


	public Agent(String name){
		model = new Model();
		try {
			writer = new PrintWriter("" + name + ".txt", "UTF-8");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

		model.currentState = model.buildState();

		width = model.getWidth();
		height = model.getHeight();

		this.policyCount = (int)Math.pow(2, width*height);

		QValues = new double[policyCount][CLUSTER_COUNT];
		QVisitCount = new int[policyCount][CLUSTER_COUNT];
	}


	/**
	 * Applies the Q-learning algorithm over the course of the simulation
	 */
	public void doQLearning(){
		//initialise clusters
		for(int i = 0; i < centroids.length ; i++){
			centroids[i] = model.buildCentroidState();
			stateCount[i] = 0;
		}

		for( turn = 0; turn < MAX_ITERATION; turn ++){
			boolean[][] nextNSGreen = new boolean[width][height];
			int index = 0;
			int currentCluster = getBestClusterIndex(model.currentState);
			//Choose the desired exploration policy
			index = choosePolicyIndexLinear(currentCluster);
	//		index = choosePolicyIndexBoltzmann(currentCluster);
			
			// Extract the selected policy given the selection index
			nextNSGreen = policies[index];
			State nextState = Model.getNextState(model.currentState, nextNSGreen);
			int nextCluster = getBestClusterIndex(nextState);

			double optimalNextValue = Double.NEGATIVE_INFINITY;
			
			// Get the best Qvalue given the next state
			for(int j = 0; j < QValues.length; j++){
				if(QValues[j][nextCluster] > optimalNextValue){
					optimalNextValue = QValues[j][nextCluster];
				}
			}
			//Add the reward to the total reward and update average reward
			totalReward += nextState.reward;				
			//			updateAvgReward(nextState.reward);
			writer.write("" + nextState.reward + "\n");


			//Update QValue for the current state
			QVisitCount[index][currentCluster]++;
			QValues[index][currentCluster] += BETA*(nextState.reward + GAMMA*optimalNextValue - QValues[index][currentCluster]);

			//Update currentState and clusters
			model.currentState = nextState;
			Model.printState(model.currentState);

			updateCluster(nextState, nextCluster);
		}
		System.out.println("======================");
	}

	/**
	 * Update the centroid at centroid index to accomodate newState by applying a weighted average
	 */
	private void updateCluster(State newState, int clusterIndex){
		int count = stateCount[clusterIndex];
		if(count == 0){
			centroids[clusterIndex] = newState.copyState();
		}else{
			for(int i =0; i < newState.grid.length; i++){
				for(int j =0; j < newState.grid[0].length; j++){
					centroids[clusterIndex].grid[i][j].NScars = (count*centroids[clusterIndex].grid[i][j].NScars + newState.grid[i][j].NScars)/(count + 1) + 1; 
					centroids[clusterIndex].grid[i][j].EWcars = (count*centroids[clusterIndex].grid[i][j].EWcars + newState.grid[i][j].EWcars)/(count + 1) + 1; 
				}
			}
		}
		stateCount[clusterIndex]++;
	}

	/**
	 * Returns the index of the closest cluster by Euclidean distance
	 */
	public int getBestClusterIndex(State s){
		double minDistance = Double.POSITIVE_INFINITY;
		double distance = 0;
		int index = 0;
		for(int k = 0 ; k < centroids.length; k++){
			distance = 0;
			for(int i = 0; i < width ; i++){
				for(int j = 0; j < height ; j++){
					distance += Math.pow(s.grid[i][j].NScars - centroids[k].grid[i][j].NScars, 2);
					distance += Math.pow(s.grid[i][j].EWcars - centroids[k].grid[i][j].EWcars, 2);
				}
			}
			if(distance < minDistance){
				minDistance = distance;
				index = k;
			}
		}
		return index;
	}

	/**
	 * Applies a handcoded naive policy over the course of the simulation
	 */
	public void doIntuitiveStrategy(){
		//PoissonDistribution inp;
		for(turn = 0; turn < MAX_ITERATION; turn++){
			State nextState;
			
			if(turn % 2 == 0){
				boolean[][] NSGreen = {{false, false} , {false, false}}; 
				nextState = Model.getNextState(model.currentState, NSGreen);
			}else{
				boolean[][] NSGreen = {{true, true} , {true, true}};
				nextState = Model.getNextState(model.currentState, NSGreen);
			}

			//Add the reward to the total reward
			totalReward += nextState.reward;
			writer.write("" + nextState.reward + "\n");
			//Update currentState
			model.currentState = nextState;
			Model.printState(model.currentState);
		}

		System.out.println("=========================================");
	}

	/**
	 * Applies random policies over the course of the simulation
	 */
	public void doRandomStrategy(){
		for(turn = 0; turn < MAX_ITERATION; turn++){
			State nextState;
			double temp =  Math.random();
			int index = (int)(temp*policyCount);
			boolean[][] NSGreen = policies[index]; 
			nextState = Model.getNextState(model.currentState, NSGreen);
			//Add the reward to the total reward 
			totalReward += nextState.reward;
			writer.write("" + nextState.reward + "\n");

			//Update currentState
			model.currentState = nextState;
			Model.printState(model.currentState);
		}
	}

	public void testCentroidUpdate(){
		centroids[0] = model.buildState();
		State testState  = model.buildState();
		System.out.println("centroid is at " + centroids[0].grid[0][0].NScars + " with " + stateCount[0] + " states and testState is at " + testState.grid[0][0].NScars);
		updateCluster(testState, 0);
		testState = model.buildState();
		System.out.println("centroid is at " + centroids[0].grid[0][0].NScars + " with " + stateCount[0] + " states and testState is at " + testState.grid[0][0].NScars);
		updateCluster(testState, 0);
		testState = model.buildState();
		System.out.println("centroid is at " + centroids[0].grid[0][0].NScars + " with " + stateCount[0] + " states and testState is at " + testState.grid[0][0].NScars);
		updateCluster(testState, 0);
		testState = model.buildState();
		System.out.println("centroid is at " + centroids[0].grid[0][0].NScars + " with " + stateCount[0] + " states and testState is at " + testState.grid[0][0].NScars);

	}


	public static void main(String[] args){
		Agent agent1 = new Agent("Qlearning");
		Agent agent2 = new Agent("Intuitive");
		Agent agent3 = new Agent("Random");
		agent1.doQLearning();
		agent2.doIntuitiveStrategy();
		agent3.doRandomStrategy();

		System.out.println("Total Q reward is " + agent1.totalReward);
		System.out.println("Total intuitive reward is " + agent2.totalReward);
		System.out.println("Total random reward is " + agent3.totalReward);
		agent1.writer.close();
		agent2.writer.close();
		agent3.writer.close();
	}
	
	/**
	 * Applies the linear exploration function to chose the next policy given the current cluster
	 */
	private int choosePolicyIndexLinear(int currentCluster){
		int index = 0;
		
		//Explores with linear decay until 7/8th of simulation
		System.out.println("EPS is at " +  (1 - (EPS - (double)turn/(double)(MAX_ITERATION - MAX_ITERATION/8)*EPS)));
		if(Math.random() < 1 - (EPS - (double)turn/(double)(MAX_ITERATION- MAX_ITERATION/8)*EPS)){//Be greedy and choose the optimal policy and decrease exploration with time. Allow 1/8 of the iteration for pure exploitation.
			double maxValue = Double.NEGATIVE_INFINITY;
			for(int j = 0; j < QValues.length; j++){
				if(QValues[j][currentCluster] > maxValue){
					maxValue = QValues[j][currentCluster];
					index = j;
				}
			}
		}else{//Explore and choose a random action
			double temp =  Math.random();
			index = (int)(temp*policyCount);
		}
		return index;
	}
	
	/**
	 * Applies the Boltzmann exploration function to chose the next policy given the current cluster
	 */
	private int choosePolicyIndexBoltzmann(int currentCluster){
		double temperature = Math.exp(-1*turn)*MAX_TEMP + 1;
				
	    double valueSum = 0;
		int index = 0;
		double[] probability = new double[policyCount];
		for(int i = 0; i < policyCount; i++){
			valueSum += Math.exp(QValues[i][currentCluster]/temperature);
		}
		for(int i = 0; i < policyCount; i++){
			probability[i] = Math.exp((QValues[i][currentCluster]/temperature))/valueSum;
		}
		return index = discreteDistribution(probability);
	}
	
	/**
	 * Takes an probability distribution array and return a random value according to it. Used by choosePolicyIndexBoltzmann()
	 * @return index of the value;
	 */
	public int discreteDistribution(double[] probability){	
		double var = Math.random(), cumulative = 0;
		int index = 0;
		for(int i =0 ; i < probability.length; i++){
			if(var < probability[i] + cumulative){
				index = i;
				break;
			}
			cumulative += probability[i]; 
		}
		return index;
	}
}
