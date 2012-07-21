package sim.app.snr.message;

import sim.app.snr.agent.AbstractAgent;
import sim.field.grid.SparseGrid2D;
import sim.util.Int2D;

public class MapUpdate extends Message {
    /** message number **/
	public int number = -1;
	/** local map of the sender */
    public SparseGrid2D map = null;
	/** sender position on map */
	public Int2D agentpos = null;
	/** true if this MapUpdate is an answer from receiver to sender
	 * from a previous MapUpdate message */
    public boolean answer = false;

    public MapUpdate(AbstractAgent s, AbstractAgent r, int msgNum, SparseGrid2D map, Int2D agentpos, boolean answer) {
		super(s, r);
		this.number = msgNum;
		this.map = map;
		this.agentpos = agentpos;
		this.answer = answer;
    }

    @Override public void transmit(AbstractAgent aa) {
		int r = sender.getCommunicationRadius();
		sim.util.Int2D spos = sender.getPos();
		sim.util.Int2D rpos = aa.getPos();
		int x = spos.x - rpos.x; x=x<0?-x:x;
		int y = spos.y - rpos.y; y=y<0?-y:y;
		double z = Math.sqrt(x*x + y*y);
		if (z <= r) {
		    aa.receiveMessage(this);
		} else {
		    System.out.printf("%s is too far away from %s to receive the MapUpdate.\n",aa,sender);
		}
    }
}
