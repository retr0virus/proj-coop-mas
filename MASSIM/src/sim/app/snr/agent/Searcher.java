package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.app.snr.message.MapUpdate;
import sim.engine.SimState;
import sim.util.Bag;
import sim.field.grid.SparseGrid2D;

public class Searcher extends AbstractAgent {
	
	private int teamNr;
	private Bag team = new Bag();
	private Bag teamCaller = new Bag();
	private double dirInertia = 0.95;
	private double rightTurner = 0.5;
	private int mapUpdateInterval = 25;

	private SparseGrid2D localMapGrid = null;
	private int curMapUpdateSteps = 0;
	private int scale;
	
	public enum InternalState { SWARM, MAPUPDATE }
	private InternalState internalState = InternalState.SWARM;
	private int maxWaitForMapUpdateTime = 10;
	private int waitForMapUpdate = 0;

	public Searcher(int teamNr, SparseGrid2D localMapGrid, int scale){
		this.teamNr = teamNr;
		this.localMapGrid = localMapGrid;
		this.scale = scale; // needed for the information where the localMapGrid is filled -> position/scale
		communicationRadius = 15;
		viewRadius = 10;
	}
	public void addTeamCaller(Caller a) {
		teamCaller.add(a);
	}
	
	private void swarm() {
		if (nextIsFree() && snr.random.nextDouble() < dirInertia) {
			forward();
			curMapUpdateSteps++;
		}
		else {
			if (snr.random.nextDouble() < rightTurner) {
				turnRight();
			} else {
				turnLeft();
			}
		}
	}
	/**
	 * When updating the map, the caller has to locate the position
	 * of the searcher.
	 * The searcher has to wait for the "ok" of the caller
	 * and hold its position while sending ping messages.
	 */
	private void waitForMapUpdateSignal() {
	    //maximal waiting time: 10
	    if (waitForMapUpdate > maxWaitForMapUpdateTime) {
		this.internalState = InternalState.SWARM;
		return;
	    }
	    ///for (int i=0; i<teamCaller.size(); ++i) {
		//Ping p = new Ping(this, teamCaller.get(i));
		//sendMessage(p);
	    //}
	    Ping p = new Ping(this, null);
	    sendMessage(p);
	}

	public int getMapUpdateInterval() { return this.mapUpdateInterval; }
	public void setMapUpdateInterval(int steps) { this.mapUpdateInterval = steps; }
	
	public double getDirInertia() { return this.dirInertia; }
	public void setDirInertia(double value) { this.dirInertia = value; }
	public Object domDirInertia() { return new sim.util.Interval(0.0, 1.0); }
	
	public double getRightTurner() { return this.rightTurner; }
	public void setRightTurner(double value) { this.rightTurner = value; }
	public Object domRightTurner() { return new sim.util.Interval(0.0, 1.0); }
	
	public int getInspectRadius() { return viewRadius; }
	public void setInspectRadius(int r) {
		if (r > 0)
			this.viewRadius = r;
	}

	@Override public void step(SimState state) {
	    //System.out.println("Searcher scheduled");
		updateInternalState(state);
		updatePosition();
		switch (internalState) {
		    case SWARM : swarm(); break;
		    case MAPUPDATE : waitForMapUpdateSignal(); break;
		    default :
			System.out.printf("Unknown internal state %s.\n",internalState);
			swarm();
			break;
		}
		updateLocalMap();
		if (curMapUpdateSteps == mapUpdateInterval) {
		    // try to send map to caller
		    // TODO
		}
	}

	private void updateLocalMap() {
	    int x = super.pos.x;
	    int y = super.pos.y;
	    // TODO
	}
	
	@Override public void receiveMessage(Message m) {
		if (m instanceof Ping) {
			this.receive((Ping)m);
		} else if (m instanceof MapUpdate) {
			this.receive((MapUpdate)m);
		} else {
			System.out.printf("Unknown Message: %s",m);
		}
	} 
	
	private void receive(Ping p) {
		System.out.printf("PING: %s\n",p);
	}
	private void receive(MapUpdate mu) {
		// if mu data is null and mu positionOk is true and the current state is MapUpdate
		// then change to swarming
		// else do nothing(?) with the map (cannot locate position)
		System.out.printf("MapUpdate: %s\n",mu);
	}
}
