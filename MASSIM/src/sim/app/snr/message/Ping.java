package sim.app.snr.message;

import sim.app.snr.agent.AbstractAgent;

public class Ping extends Message {

	public Ping(AbstractAgent s, AbstractAgent r) {
		super(s, r);
	}

	@Override public void transmit(AbstractAgent aa) {
	    aa.receiveMessage(this);
	}
}
