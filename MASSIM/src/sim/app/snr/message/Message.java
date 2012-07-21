package sim.app.snr.message;

import sim.app.snr.agent.AbstractAgent;

public abstract class Message {
	
	public AbstractAgent sender;
	public AbstractAgent receiver;
	
	public Message(AbstractAgent s,AbstractAgent r){
		sender = s;
		receiver = r;
	}
	
	public void transmit() {
	    if (receiver == null) {
		throw new RuntimeException("Transmission without given receiver is not allowed.");
	    }
	    transmit(receiver);
	}
	public abstract void transmit(AbstractAgent r);

	@Override public String toString() { 
	    return String.format("MESSAGE from %s to %s.\n",sender,receiver);
	}
}
