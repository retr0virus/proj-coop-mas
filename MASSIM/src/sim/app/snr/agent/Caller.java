package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.app.snr.message.MapUpdate;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Int2D;
import sim.field.grid.IntGrid2D;
import sim.field.grid.SparseGrid2D;
import sim.app.snr.Value;

public class Caller extends AbstractAgent {
	private static Bag callers = new Bag();
	private static int callerSteps = 0;
	private int callerNr;
	private int teamNr;
	private int neededPositions = 3;

	private Bag team = new Bag();
	private IntGrid2D baseMapGrid; // the map that is known to the base

	/** all Searcher that are known with positions for MapUpdate */
	private Bag teamPosition = new Bag();

	private Int2D target = null;

	public Caller(int teamNr, IntGrid2D baseMapGrid) {
		this.teamNr = teamNr;
		this.baseMapGrid = baseMapGrid;
		communicationRadius = 100;
		zeroPos = new Int2D(0,0);
		callerNr = callers.size()+1;
		callers.add(this);
	}


	public int getTeamNr() { return this.teamNr; }
	public int getNeededPositionsForMapUpdate() { return this.neededPositions; }
	public void setNeededPositionsForMapUpdate(int value) { this.neededPositions = value; }
	
	public void addTeamMember(AbstractAgent a){
		team.add(a);
	}
	public void removeTeamMember(AbstractAgent a) {
		team.remove(a);
	}
	
	private void swarm() {
		if (nextIsFree() && snr.random.nextDouble() < 0.95) {
			forward();
		}
		else {
			if (snr.random.nextDouble() < 0.5) {
				turnRight();
			} else {
				turnLeft();
			}
		}
	}
	private static Bag targets = null;
	private static void updateTargets(IntGrid2D baseMapGrid, int scale, Int2D zeroPos) {
		if (targets == null) {
			targets = new Bag();
		}
		else {
			targets.clear();
		}
		java.util.HashMap<Integer,java.util.HashSet<Integer>> starget = new java.util.HashMap<Integer,java.util.HashSet<Integer>>();
		for (int i=0; i<baseMapGrid.getWidth(); ++i) {
			for (int j=0; j<baseMapGrid.getHeight(); ++j) {
				int val = baseMapGrid.get(i,j);
				if (val == 0) {
					Int2D locPos = posToLocalMapPos(new Int2D(i,j),scale,zeroPos);
					if(!starget.containsKey(locPos.x)) { starget.put(locPos.x,new java.util.HashSet<Integer>()); }
					boolean ok = starget.get(locPos.x).add(locPos.y);
					if (ok) { targets.add(locPos); }
				}
			}
		}
	}
	private void chooseTarget() {
		if (target == null) {
			int num = callers.size();
			int tc = targets.size();
			int tspan = tc / num;
			target = (Int2D)targets.get(tspan*(callerNr-1));
		} else {
			// check if target was already found
			if (baseMapGrid.get(target.x,target.y) != 0) {
				target = null;
				chooseTarget();
			}
		}
	}

	@Override
	public void step(SimState state) {
	    //System.out.println("Caller scheduled");
		updateInternalState(state);
		updatePosition();

		if (callerSteps % callers.size() == 0) {
			updateTargets(this.baseMapGrid,scale,zeroPos);
		}
		chooseTarget();
		if (target == null) {
			swarm();
		} else {
			// move to target
			boolean top = false;
			boolean left = false;
			if (target.x < pos.x) {
				left = true;
			}
			if (target.y < pos.y) {
				top = true;
			}
			// TODO
		}
		if (snr.schedule.getSteps() % 20 == 0)
			sendMessage(new Ping(this,(AbstractAgent)team.get(0)));
		callerSteps++;
	}

	@Override
	public void receiveMessage(Message m) {
	    if (m instanceof MapUpdate) {
			//int del = -1;
			for (int i=0; i<teamPosition.size(); ++i) {
				Bag b = (Bag)teamPosition.get(i);
				if (((MapUpdate)m).sender.equals(((MapUpdate)b.get(0)).sender)) {
					//del = i;
					System.out.printf("Newer map received from sender. Already collected %d positions.\n",((Bag)teamPosition.get(i)).size()-1);
					teamPosition.remove(i);
					break;
				}
			}
			//if (del != -1) {
			//}

			MapUpdate mu = (MapUpdate)m;
			// for position some more steps are needed -> wait for pings from sender while "traveling around"
			Bag b = new Bag();
			b.add(mu);
			b.add(new Int2D(super.pos.x,super.pos.y));
			teamPosition.add(b);
	    } else if (m instanceof Ping) {
			Ping p = (Ping)m;
			// Look for all teams that want to update the map
			for (int i=0; i<teamPosition.size(); ++i) {
				Bag b = (Bag)teamPosition.get(i);
				if (p.sender.equals(((MapUpdate)b.get(0)).sender)) {
					boolean newPos = true;
					for (int j=1; j<b.size(); ++j) {
						Int2D pos = (Int2D)b.get(j);
						if (pos.x == super.pos.x && pos.y == super.pos.y) { newPos = false; }
					}
					if (newPos) {
						if (b.size() < 1+neededPositions-1) {
							b.add(new Int2D(super.pos.x,super.pos.y));
							//System.out.printf("New position collected.\n");
						} else {
							// all needed positions collected
							MapUpdate msg = (MapUpdate)b.get(0);
							System.out.printf("Collected all needed positions!\n");
							MapUpdate answer = new MapUpdate(this, msg.sender, msg.number, null, null, true);
							sendMessage(answer);
							//System.out.printf("Sender position: %d,%d.\n",msg.sender.getPos().x,msg.sender.getPos().y);
							updateBaseMap(msg.map, msg.agentpos, posToLocalMapPos(msg.sender.getPos(),msg.sender.getLocalMapScale()));
						}
					}
				}
			}
		}
	}
	private void updateBaseMap(SparseGrid2D localMap, Int2D localPos, Int2D actualPos) {
		int diffx = 0;//actualPos.x - localPos.x;
		int diffy = 0;//actualPos.y - localPos.y;
		//System.out.printf("BaseGridSize: %d,%d.\n",baseMapGrid.getWidth(),baseMapGrid.getHeight());
		//System.out.printf("Actual: %d,%d, Local: %d,%d; Diff: %d,%d\n",actualPos.x,actualPos.y,localPos.x,localPos.y,diffx,diffy);
		Bag all = localMap.getAllObjects();
		for (int i=0; i<all.size(); ++i) {
			Int2D localLoc = localMap.getObjectLocation(all.get(i));
			Int2D actualLoc = new Int2D(localLoc.x + diffx,localLoc.y + diffy);
			//System.out.printf("Bag: local: %d,%d and actual %d,%d.\n",localLoc.x,localLoc.y, actualLoc.x,actualLoc.y);
			baseMapGrid.set(actualLoc.x,actualLoc.y,(Integer)((Value)all.get(i)).get());
		}
	}
}
