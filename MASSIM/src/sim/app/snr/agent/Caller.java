package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.app.snr.message.MapUpdate;
import sim.engine.SimState;
import sim.util.Bag;
import sim.util.Int2D;
import sim.field.grid.IntGrid2D;

public class Caller extends AbstractAgent {
	private static int callers = 0;
	private int callerNr;
	private int teamNr;
	private int neededPositions = 3;

	private Bag team = new Bag();
	private IntGrid2D baseMapGrid;

	/** all Searcher that are known with positions for MapUpdate */
	private Bag teamPosition = new Bag();

	
	public Caller(int teamNr, IntGrid2D baseMapGrid) {
		callerNr = ++callers;
		this.teamNr = teamNr;
		this.baseMapGrid = baseMapGrid;
		communicationRadius = 100;
	}

	public int getTeamNr() { return this.teamNr; }
	public int getNeededPositionsForMapUpdate() { return this.neededPositions; }
	
	public void addTeamMember(AbstractAgent a){
		team.add(a);
	}
	public void removeTeamMember(AbstractAgent a) {
		team.remove(a);
	}
	
	@Override
	public void step(SimState state) {
	    //System.out.println("Caller scheduled");
		updateInternalState(state);
		updatePosition();
		if (snr.schedule.getSteps() % 20 == 0)
			sendMessage(new Ping(this,(AbstractAgent)team.get(0)));
	}

	@Override
	public void receiveMessage(Message m) {
	    if (m instanceof MapUpdate) {
			int del = -1;
			for (int i=0; i<teamPosition.size(); ++i) {
				Bag b = (Bag)teamPosition.get(i);
				if (((MapUpdate)m).sender.equals(((MapUpdate)b.get(0)).sender)) {
					del = i;
					break;
				}
			}
			if (del != -1) {
				System.out.printf("Newer map received from sender. Already collected %d positions.\n",((Bag)teamPosition.get(del)).size()-1);
				teamPosition.remove(del);
			}

			System.out.printf("MapUpdate to Base\n");
			MapUpdate mu = (MapUpdate)m;
			// for position some more steps are needed -> wait for pings from sender while "traveling around"
			Bag b = new Bag();
			b.add(mu);
			b.add(new Int2D(super.pos.x,super.pos.y));
			teamPosition.add(b);
	    } else if (m instanceof Ping) {
			Ping p = (Ping)m;
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
							System.out.printf("New position collected.\n");
						} else {
							// all needed positions collected
							MapUpdate msg = (MapUpdate)b.get(0);
							System.out.printf("Collected all needed positions!\n");
							MapUpdate answer = new MapUpdate(this, msg.sender, msg.number, null, null, true);
							sendMessage(answer);
						}
					}
				}
			}
		}
	}
}
