package sim.app.snr;

import java.io.IOException;

import sim.app.snr.agent.Caller;
import sim.app.snr.agent.Searcher;
import sim.engine.*;
import sim.field.grid.SparseGrid2D;
import sim.field.grid.IntGrid2D;

public class SearchAndRescue extends SimState {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String imgFile = "img/map.png";
	public Terrain terrain;
	public SparseGrid2D agentGrid;
	public IntGrid2D baseMapGrid;
	public int numTeams = 1;
	public int numAgentsPerTeam = 1;
	public int numCallerPerTeam = 1;
	
	public SearchAndRescue(long seed, String imgfile) {
		super(seed);
		try {
		    if (imgfile != null) imgFile = imgfile;
		    terrain = new Terrain(imgFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		agentGrid = new SparseGrid2D(terrain.area.getWidth(),terrain.area.getHeight());
		int scale = 10;
		baseMapGrid = new IntGrid2D(terrain.area.getWidth()/scale,terrain.area.getHeight()/scale);
	}

    public int getNumTeams() { return numTeams; }
    public void setNumTeams(int val) {if (val > 0) numTeams = val; }
    public int getNumCallerPerTeam() { return numCallerPerTeam; }
    public void setNumCallerPerTeam(int val) {if (val > 0) numCallerPerTeam = val; }
    public int getNumAgentsPerTeam() { return numAgentsPerTeam; }
    public void setNumAgentsPerTeam(int val) {if (val > 0) numAgentsPerTeam = val; }
	
	
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
		for (int i=0; i<numTeams; ++i) {
		    Caller c = new Caller();
		    agentGrid.setObjectLocation(c, startx,starty);
		    schedule.scheduleRepeating(c);
		
		    for (int j=0; j<numAgentsPerTeam; j++) {
			Searcher x = new Searcher(c);
			c.addTeamMember(x);
			agentGrid.setObjectLocation(x, startx,starty);
			schedule.scheduleRepeating(x);
		    }
		}
		
	}

	public static void main(String[] args) {
		doLoop(sim.app.snr.SearchAndRescue.class, args);
		System.exit(0);
	}
	
}
