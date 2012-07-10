package sim.app.snr.message;

import sim.app.snr.agent.AbstractAgent;
import sim.field.grid.SparseGrid2D;

public class MapUpdate extends Message {
    public int number = -1;
    public SparseGrid2D map = null;

    public MapUpdate(AbstractAgent s, AbstractAgent r, int msgNum, SparseGrid2D map) {
	super(s, r);
	this.number = msgNum;
	this.map = map;
    }

    @Override public void transmit() {
	int r = sender.getCommunicationRadius();
	sim.util.Int2D spos = sender.getPos();
	sim.util.Int2D rpos = receiver.getPos();
	int x = spos.x - rpos.x; x=x<0?-x:x;
	int y = spos.y - rpos.y; y=y<0?-y:y;
	double z = Math.sqrt(x*x + y*y);
	if (z <= r) {
	    receiver.receiveMessage(this);
	} else {
	    System.out.printf("%s is too far away from %s to receive the MapUpdate.\n",receiver,sender);
	}
    }
}
