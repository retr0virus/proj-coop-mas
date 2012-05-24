package sim.app.snr.message;

import sim.app.snr.agent.AbstractAgent;

public class Ping extends Message {

	public Ping(AbstractAgent s, AbstractAgent r) {
		super(s, r);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void transmit() {
		receiver.receivePing(this);
		//receiver.receiveMessage(this);
	}



	
}
