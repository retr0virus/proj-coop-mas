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
	
	protected void examineTerrain(){
		snr.terrain.area.getNeighborsHexagonalDistance(pos.x,pos.y,viewRadius,false,terrainValues,terrainXCoords,terrainYCoords);
	}
	
	protected boolean nextIsFree(){
	    int checkX = pos.x+directions[dir].x;
	    int checkY = pos.y+directions[dir].y;
	    if (snr.terrain.width > checkX && snr.terrain.height > checkY) {
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
	
	public abstract void receiveMessage(Message m);
	
	@Override public String toString() { return "Nevermind|"+this.getClass().getName()+"@"+this.hashCode(); }
}
