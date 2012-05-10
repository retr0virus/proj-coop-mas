package sim.app.snr.message;

import sim.app.snr.agent.AbstractAgent;

public abstract class Message {
	
	public AbstractAgent sender;
	public AbstractAgent receiver;
	
	public Message(AbstractAgent s,AbstractAgent r){
		sender = s;
		receiver = r;
	}
	
	public abstract void transmit();
}
