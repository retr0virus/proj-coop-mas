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
	protected Int2D dir = new Int2D(0,1);
	protected Int2D pos;
	protected IntBag terrainValues;
	protected IntBag terrainXCoords;
	protected IntBag terrainYCoords;
	protected Bag agents = new Bag();
	protected IntBag agentsXCoords =  new IntBag();
	protected IntBag agentsYCoords = new IntBag();
	protected int viewRadius = 0;
	protected int communicationRadius = 0;
	
	protected void forward() {
		snr.agentGrid.setObjectLocation(this, pos.x+dir.x,pos.y+dir.y);
	}
	
	protected void turnLeft(){
		dir = new Int2D(dir.y,dir.x * -1);
	}
	
	protected void turnRight(){
		
	}
	
	protected void examineTerrain(){
		snr.terrain.area.getNeighborsHexagonalDistance(pos.x,pos.y,viewRadius,false,terrainValues,terrainXCoords,terrainYCoords);
	}
	
	protected boolean nextIsFree(){
		if (snr.terrain.area.get(pos.x+dir.x, pos.y+dir.y) != Terrain.WALL)
			return true;
		return false;
	}
	
	protected void updateInternalState(SimState state){
		snr = (SearchAndRescue) state;
	}
	
	protected void updatePosition() {
		pos = snr.agentGrid.getObjectLocation(this);
	}
	
	protected void sendMessage(Message m){
		lookupAgents();
		for (int i=0;i< agents.size();i++){
			if (agents.get(i) == m.receiver)
					m.transmit();
		}
	}
	
	private void lookupAgents(){
		snr.agentGrid.getNeighborsHexagonalDistance(pos.x, pos.y, communicationRadius, false, agents,agentsXCoords, agentsYCoords);
	}
	
	public abstract void receiveMessage(Message m);
	public abstract void receivePing(Ping p);
	
}
