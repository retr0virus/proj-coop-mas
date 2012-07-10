package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.app.snr.message.MapUpdate;
import sim.engine.SimState;
import sim.util.Bag;
import sim.field.grid.IntGrid2D;

public class Caller extends AbstractAgent {
	private int teamNr;
	private Bag team = new Bag();
	private IntGrid2D baseMapGrid;
	
	public Caller(int teamNr, IntGrid2D baseMapGrid) {
		this.teamNr = teamNr;
		this.baseMapGrid = baseMapGrid;
		communicationRadius = 100;
	}
	
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
		System.out.printf("MapUpdate to Base\n");
		MapUpdate mu = (MapUpdate)m;
		// TODO
		// add info to baseMapGrid at valid position
	    }
	}
}
