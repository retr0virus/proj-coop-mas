package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.app.snr.message.MapUpdate;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Int2D;
import sim.field.grid.SparseGrid2D;

import sim.app.snr.Terrain;
import sim.app.snr.Value;

public class Searcher extends AbstractAgent {
	
	private int teamNr;
	private Bag team = new Bag();
	private Bag teamCaller = new Bag();
	private double dirInertia = 0.95;
	private double rightTurner = 0.5;
	private int mapUpdateInterval = 25;

	private SparseGrid2D localMapGrid = null;
	private int curMapUpdateSteps = 0;
	private int scale = -1;
	
	public enum InternalState { SWARM, MAPUPDATE }
	private InternalState internalState = InternalState.SWARM;
	private int maxWaitForMapUpdateTime = 10;
	private int waitForMapUpdate = 0; ///< max waiting-time
	private Int2D zeroPos = null; ///< for local map coords
	private int lastSentMUNumber = -1; ///< needed for MU-answer from Caller

	private Bag allLocalMapCoordCounts = null;

	public Searcher(int teamNr, SparseGrid2D localMapGrid, int scale){
		this.teamNr = teamNr;
		this.localMapGrid = localMapGrid;
		this.scale = scale; ///< needed for the information where the localMapGrid is filled -> position/scale
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
			//System.out.println("Not longer waiting...");
			this.internalState = InternalState.SWARM;
			waitForMapUpdate = 0;
			return;
		}
		waitForMapUpdate++;
		// send Ping to all Callers so that they can calculate the position of this searcher
		for (int i=0; i<teamCaller.size(); ++i) {
			Ping p = new Ping(this, (Caller)teamCaller.get(i));
			sendMessage(p);
		}
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

	public int getLocalMapScale() { return scale; }

	@Override public void step(SimState state) {
		//System.out.println("Searcher scheduled");
		updateInternalState(state); // get current state
		updatePosition(); // get current position
		if (zeroPos == null) {
			zeroPos = new Int2D(super.pos.x,super.pos.y);
		}
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
			curMapUpdateSteps = 0;
			// try to send map to caller
			internalState = InternalState.MAPUPDATE;
			for (int i=0; i<teamCaller.size(); ++i) {
				Caller c = (Caller)teamCaller.get(i);
				SparseGrid2D map = localMapGrid;
				MapUpdate mu = new MapUpdate(this, c, ++lastSentMUNumber, map, posToLocalMapPos(super.pos), false);
				super.sendMessage(mu);
			}
		}
	}

	/** Calculates a local map position from an actual map position from the simulation model.
	 * @return the local map position for this agent - it may differ from other agents positions even if they are standing on the same actual map position
	 */
	private Int2D posToLocalMapPos(Int2D mapPos) {
		int x = mapPos.x - this.zeroPos.x;
		int y = mapPos.y - this.zeroPos.y;
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
	private Bag localMapPosToPos(Int2D localPos) {
		Bag res = new Bag();
		int x = localPos.x * scale;
		int y = localPos.y * scale;
		for (int i=0; i<scale; ++i) {
			for (int j=0; j<scale; ++j) {
				int w = localPos.x*scale + i + this.zeroPos.x;
				int h = localPos.y*scale + j + this.zeroPos.y;
				if (w>= 0 && w < snr.terrain.width && h >= 0 && h < snr.terrain.height) {
					res.add(new Int2D(w, h));
				}
			}
		}
		return res;
	}
	private void buildLocalMapCoordCounts() {
		Bag localMapCoords = new Bag(); // contains all localPos/numberOfLocalPos-pairs (how many times a specific localMapCoord exists)
		for (int w=0; w<snr.terrain.width; ++w) {
			for (int h=0; h<snr.terrain.height; ++h) {
				Int2D localCoord = posToLocalMapPos(new Int2D(w,h));
				boolean exists = false;
				for (int j=0; j<localMapCoords.size(); ++j) {
					Int2D coord = (Int2D)(((Object[])localMapCoords.get(j))[0]);
					int count = (int)(((Object[])localMapCoords.get(j))[1]);
					if (coord.x == localCoord.x && coord.y == localCoord.y) {
						exists = true;
						localMapCoords.set(j, new Object[] { coord, count+1 });
						break;
					}
				}
				if (!exists) {
					localMapCoords.add(new Object[] { localCoord, 1 });
				}
			}
		}
		allLocalMapCoordCounts = localMapCoords;
	}
	/**
	 * Returns all localMapPositions that are fully overviewed from the current position.
	 * So this method returns all positions that can be updated.
	 */
	private Bag currentViewToLocalMapPos() {
		examineTerrain(); // get current terrain values
		if (allLocalMapCoordCounts == null) {
			buildLocalMapCoordCounts();
		}
		// collect all coords
		Bag allLocalCoords = new Bag(); // contains all pos/localPos-pairs
		Bag localMapCoords = new Bag(); // contains all localPos/numberOfLocalPos-pairs (how many times a specific localMapCoord exists)
		for (int i=0; i<terrainValues.size(); ++i) {
			Int2D mapCoord = new Int2D(terrainXCoords.get(i),terrainYCoords.get(i));
			Int2D localCoord = posToLocalMapPos(mapCoord);
			allLocalCoords.add(new Object[] { 
				mapCoord,
				localCoord
			});
			boolean exists = false;
			for (int j=0; j<localMapCoords.size(); ++j) {
				Int2D coord = (Int2D)(((Object[])localMapCoords.get(j))[0]);
				int count = (int)(((Object[])localMapCoords.get(j))[1]);
				if (coord.x == localCoord.x && coord.y == localCoord.y) {
					exists = true;
					localMapCoords.set(j, new Object[] { coord, count+1 });
					break;
				}
			}
			if (!exists) {
				localMapCoords.add(new Object[] { localCoord, 1 });
			}
		}
		Bag res = new Bag();
		for (int i=0; i<localMapCoords.size(); ++i) {
			Int2D coord = (Int2D)(((Object[])localMapCoords.get(i))[0]);
			int count = (int)(((Object[])localMapCoords.get(i))[1]);
			for (int j=0; j<allLocalMapCoordCounts.size(); ++j) {
				Int2D coord2 = (Int2D)(((Object[])allLocalMapCoordCounts.get(j))[0]);
				int count2 = (int)(((Object[])allLocalMapCoordCounts.get(j))[1]);
				if (coord.x == coord2.x && coord.y == coord2.y) {
					if (count == count2) {
						res.add(coord);
						//System.out.printf("Complete: [%d,%d] %d times.\n",coord.x,coord.y,count);
					} else { 
						//System.out.printf("Incomplete: [%d,%d] %d of %d times.\n",coord.x,coord.y,count,count2);
					}
					break;
				}
			}
		}
		return res;
	}

	/**
	 * Needs a Bag of Int2D and checks if at the given positions are at least one point of interest.
	 */
	private boolean containsPOI(Bag b) {
		/*for (int i=0; i<snr.terrain.width; ++i) {
			for (int j=0; j<snr.terrain.height; ++j) {
				if (snr.terrain.area.get(i,j) == Terrain.POI) {
					Int2D loc = posToLocalMapPos(new Int2D(i,j));
					System.out.printf("[GLOBAL]Found POI at %d,%d (Local %d,%d).\n",i,j,loc.x,loc.y);
					//System.out.printf("[GLOBAL]Later looking at ");
					for (int k=0; k<b.size(); ++k) {
						if (i==((Int2D)b.get(k)).x && j==((Int2D)b.get(k)).y) {
							System.out.printf("[GLOBAL]!!!!!POI!!!!\n");
						}
						//System.out.printf("(%d,%d)",((Int2D)b.get(k)).x,((Int2D)b.get(k)).y);
					}
					//System.out.printf("\n");
				}
			}
		}*/
		for (int i=0; i<b.size(); ++i) {
			Int2D coord = (Int2D)b.get(i);
			int val = snr.terrain.area.get(coord.x,coord.y);
			if (val == Terrain.POI) {
				return true;
			}
		}
		return false;
	}
	private void updateLocalMap() {
		// update needed
		Bag toUpdate = currentViewToLocalMapPos();
		for (int i=0; i<toUpdate.size(); ++i) {
			Int2D localCoord = (Int2D)toUpdate.get(i);
			if (localMapGrid.getObjectsAtLocation(localCoord.x,localCoord.y) == null || ((Value)localMapGrid.getObjectsAtLocation(localCoord.x,localCoord.y).get(0)).get().equals(Terrain.POI)) { // check only if null or poi (could be vpoi now)
				boolean poi = containsPOI(localMapPosToPos(localCoord));
				System.out.printf("%s POI at local position %d,%d.\n",(poi?"There is a":"No"),localCoord.x,localCoord.y);
				// A "Value"-object is needed because an Integer would be "moved" to the new coords but we need separate objects for each position
				if (poi) {
					boolean ok = localMapGrid.setObjectLocation(new Value(Terrain.POI), localCoord);
					//System.out.printf("Detected POI within localMapCoord %d,%d. %s\n",localCoord.x,localCoord.y, ok);
					Bag b = localMapGrid.getObjectsAtLocation(localCoord.x,localCoord.y);
					System.out.printf("\t--> %s\n",(b!=null?((Value)b.get(0)):"NULL"));
				} else {
					boolean ok = localMapGrid.setObjectLocation(new Value(Terrain.WAY), localCoord);
					//System.out.printf("Nothing interesting at %d,%d. %s\n",localCoord.x,localCoord.y, ok);
					Bag b = localMapGrid.getObjectsAtLocation(localCoord.x,localCoord.y);
					//System.out.printf("\t--> %s\n",(b!=null?((Value)b.get(0)):"NULL"));
				}
			}
		}
		//Int2D curPos = posToLocalMapPos(super.pos);
		//Bag curElem = localMapGrid.getObjectsAtLocation(curPos.x, curPos.y);
		//if (curElem == null) {
		//}
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
		// if mu data is null and mu answer is true and the current state is MapUpdate
		// then change to swarming
		// else do nothing(?) with the map (cannot locate position)
		if (mu.map == null && mu.answer == true && mu.number == lastSentMUNumber && internalState == InternalState.MAPUPDATE) {
			internalState = InternalState.SWARM;
			System.out.printf("MapUpdate answer received: %s. SWARMING now\n",mu);
		} else {
			System.out.printf("MapUpdate received - ignoring '%s'.\n",mu);
		}
	}
}
