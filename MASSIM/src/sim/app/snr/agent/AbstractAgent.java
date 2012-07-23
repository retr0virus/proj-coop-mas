package sim.app.snr.agent;

import sim.app.snr.SearchAndRescue;
import sim.app.snr.Terrain;
import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.util.Bag;
import sim.util.Int2D;
import sim.util.IntBag;

public abstract class AbstractAgent implements Steppable {
	protected SearchAndRescue snr;
	protected Int2D[] directions = new Int2D[] { 
			new Int2D(1,0), new Int2D(1,-1), new Int2D(0,-1), new Int2D(-1,-1), 
			new Int2D(-1,0), new Int2D(-1,1), new Int2D(0,1), new Int2D(1,1)
	};
	protected int dir = 0;
	protected Int2D pos;
	
	protected int viewRadius = 0; // editable
	protected int communicationRadius = 0; // editable
	
	protected IntBag terrainValues;
	protected IntBag terrainXCoords;
	protected IntBag terrainYCoords;
	
	protected Bag agents = new Bag();
	protected IntBag agentsXCoords =  new IntBag();
	protected IntBag agentsYCoords = new IntBag();
	
	protected Int2D zeroPos = null; ///< for local map coords
	protected int scale = 1;

	protected void stepBack() {
		snr.agentGrid.setObjectLocation(this, pos.x-directions[dir].x,pos.y-directions[dir].y);
	}
	protected void forward() {
		snr.agentGrid.setObjectLocation(this, pos.x+directions[dir].x,pos.y+directions[dir].y);
	}

	protected void turnLeft(){
		//dir = new Int2D(dir.y,dir.x * -1);
		dir = (dir == directions.length-1) ? 0 : dir+1;
	}
	protected void turnRight(){
		//dir = new Int2D(dir.y * -1, dir.x);
		dir = (dir == 0) ? directions.length-1 : dir-1;
	}
	
	public int getCommunicationRadius() { return communicationRadius; }
	public void setCommunicationRadius(int c) {
		if (c > 0)
			this.communicationRadius = c;
	}
	public Int2D getPos() { return pos; }
	
	/** Lookup the terrain and get all values and coordinates within the viewRadius */
	protected void examineTerrain(){
		terrainValues = new IntBag();
		terrainXCoords = new IntBag();
		terrainYCoords = new IntBag();
		snr.terrain.area.getNeighborsHexagonalDistance(pos.x,pos.y,viewRadius,false,terrainValues,terrainXCoords,terrainYCoords);
		// here may be added some algorithm for fetching out objects "not in the line of sight"
	}
	
	protected boolean backIsFree() {
		int checkX = pos.x-directions[dir].x;
	    int checkY = pos.y-directions[dir].y;
	    if (snr.terrain.width > checkX && checkX >= 0 && snr.terrain.height > checkY && checkY >= 0) {
			if (snr.terrain.area.get(checkX, checkY) != Terrain.WALL) {
				return true;
			}
	    }
	    return false;
	}
	/** Lookup the terrain in move direction for walls in the next step.
	 * @return true if there is no wall in move direction, false if there is a wall in move direction
	 */
	protected boolean nextIsFree(){
	    int checkX = pos.x+directions[dir].x;
	    int checkY = pos.y+directions[dir].y;
	    if (snr.terrain.width > checkX && checkX >= 0 && snr.terrain.height > checkY && checkY >= 0) {
			if (snr.terrain.area.get(checkX, checkY) != Terrain.WALL) {
				return true;
			}
	    }
	    return false;
	}
	
	protected void updateInternalState(SimState state){
		snr = (SearchAndRescue) state;
	}
	
	protected void updatePosition() {
		pos = snr.agentGrid.getObjectLocation(this);
	}
	
	/**
	 * Messages are only transmitted to agents within the communication radius.
	 * @param m
	 */
	protected void sendMessage(Message m){
		lookupReceivingAgents();
		for (int i=0;i< agents.size();i++){
			if (agents.get(i) == m.receiver) {
				m.transmit();
			} else if (m.receiver == null) {
				m.transmit((AbstractAgent)agents.get(i));
			}
		}
	}
	
	private void lookupReceivingAgents(){
		snr.agentGrid.getNeighborsHexagonalDistance(pos.x, pos.y, communicationRadius, false, agents, agentsXCoords, agentsYCoords);
	}

	/** Calculates a local map position from an actual map position from the simulation model.
	 * @return the local map position for this agent - it may differ from other agents positions even if they are standing on the same actual map position
	 */
	protected Int2D posToLocalMapPos(Int2D mapPos) {
		return posToLocalMapPos(mapPos,this.scale);
	}
	
	protected Int2D posToLocalMapPos(Int2D mapPos, int scale) {
		return posToLocalMapPos(mapPos,scale,this.zeroPos);
	}
	protected static Int2D posToLocalMapPos(Int2D mapPos, int scale, Int2D zeroPos) {
		int x = mapPos.x - zeroPos.x;
		int y = mapPos.y - zeroPos.y;
		// -6 -5 -4 -3 -2 -1  0 +1 +2 +3 +4 +5
		// -3 -3 -2 -2 -1 -1  0  0 +1 +1 +2 +2 
		if (x < 0) {
			x = (x % scale == 0) ? (x/scale) : (x/scale-1);
		} else {
			x = x/scale;
		}
		if (y < 0) {
			y = (y % scale == 0) ? (y/scale) : (y/scale-1);
		} else {
			y = y/scale;
		}
		return new Int2D(x,y);
	}
	
	public abstract void receiveMessage(Message m);
	
	public int getLocalMapScale() { return scale; }

	@Override public String toString() { return "Nevermind|"+this.getClass().getName()+"@"+this.hashCode(); }
}
