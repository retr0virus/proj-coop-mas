package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.engine.SimState;
import sim.util.Bag;

public class Caller extends AbstractAgent {
	private Bag team = new Bag();
	
	public Caller(){
		communicationRadius = 100;
	}
	
	public void addTeamMember(AbstractAgent a ){
		team.add(a);
	}

	
	@Override
	public void step(SimState state) {
		updateInternalState(state);
		updatePosition();
		if (snr.schedule.getSteps() % 20 == 0)
			sendMessage(new Ping(this,(AbstractAgent)team.get(0)));
	}

	@Override
	public void receiveMessage(Message m) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void receivePing(Ping p) {
		// TODO Auto-generated method stub
		
	}

}