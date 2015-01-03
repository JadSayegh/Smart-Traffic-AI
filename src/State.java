

public class State {
	Intersection[][] grid;
	int reward = 0;
	
	public State(int width, int height){
		grid = new Intersection[width][height];
	}
	
	/**
	 * Used by getNextState method
	 * @return a deep copy of the state
	 */
	public State copyState(){
		State newState = new State(this.grid.length, this.grid[0].length);
		newState.grid = this.copyGrid();
		return newState;
	}
	
	/**
	 * return a seperate deep copy of the current grid
	 */
	public Intersection[][] copyGrid(){
		Intersection[][] newGrid = new Intersection[this.grid.length][this.grid[0].length];
		for(int i = 0; i < newGrid.length; i++){
			for(int j = 0; j < newGrid.length; j++){
				newGrid[i][j] = new Intersection(i, j, this.grid[i][j].NScars, this.grid[i][j].EWcars);
				newGrid[i][j].isNSInput = this.grid[i][j].isNSInput;
				newGrid[i][j].isEWInput = this.grid[i][j].isEWInput;
			}
		}
		return newGrid;
	}
}
	