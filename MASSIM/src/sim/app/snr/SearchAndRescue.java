package sim.app.snr;

import java.io.IOException;

import sim.app.snr.agent.Caller;
import sim.app.snr.agent.Searcher;
import sim.engine.*;
import sim.field.grid.SparseGrid2D;

public class SearchAndRescue extends SimState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final String imgFile = "img/map.png";
	public Terrain terrain;
	public SparseGrid2D agentGrid;
	public int numAgents = 1;
	
	public SearchAndRescue(long seed) {
		super(seed);
		try {
			terrain = new Terrain(imgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		agentGrid = new SparseGrid2D(terrain.area.getWidth(),terrain.area.getHeight());
	}

    public int getNumAngents() { return numAgents; }
    public void setNumAnts(int val) {if (val > 0) numAgents = val; }
	
	
	public void start() {
		super.start();
		int startx=-1;
		int starty=-1;
		for (int x = 0; x<terrain.area.getWidth(); x++) {
			for (int y = 0; y<terrain.area.getHeight();y++ ) {
				if (terrain.area.get(x, y) == Terrain.START){
					startx = x;
					starty = y;
					break;
				}
			}
		}
		Caller c = new Caller();
		agentGrid.setObjectLocation(c, startx,starty);
		schedule.scheduleRepeating(c);
		
		for (int i=0; i<numAgents; i++) {
			Searcher x = new Searcher(c);
			c.addTeamMember(x);
			agentGrid.setObjectLocation(x, startx,starty);
			schedule.scheduleRepeating(x);
		}
	}

	public static void main(String[] args) {
		doLoop(sim.app.snr.SearchAndRescue.class, args);
		System.exit(0);
	}
	
}
