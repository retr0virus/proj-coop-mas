package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.engine.SimState;

public class Searcher extends AbstractAgent {
	
	private Caller c;
	
	public Searcher(Caller c ){
		this.c = c;
		communicationRadius = 15;
	}
	
	private void swarm() {
		if (nextIsFree())
			forward();
		else turnLeft();
	}
	
	@Override
	public void step(SimState state) {
		updateInternalState(state);
		updatePosition();
		swarm();
	}
	
	@Override
	public void receiveMessage(Message m) {
		System.out.println("message");
	} 
	
	@Override
	public void receivePing(Ping p) {
		System.out.println("ping");
	}
}
