package sim.app.snr;

import java.io.IOException;

import sim.app.snr.agent.Caller;
import sim.app.snr.agent.Searcher;
import sim.engine.SimState;
import sim.field.grid.SparseGrid2D;
import sim.field.grid.IntGrid2D;
import sim.util.Bag;

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
	public boolean started = false;
	
	public SearchAndRescue(long seed, String imgfile) {
		super(seed);
		this.imgFile = imgfile;
		loadMap(imgfile);
	}

    public int getNumTeams() { return numTeams; }
    public void setNumTeams(int val) {if (val > 0) numTeams = val; }
    public int getNumCallerPerTeam() { return numCallerPerTeam; }
    public void setNumCallerPerTeam(int val) {if (val > 0) numCallerPerTeam = val; }
    public int getNumAgentsPerTeam() { return numAgentsPerTeam; }
    public void setNumAgentsPerTeam(int val) {if (val > 0) numAgentsPerTeam = val; }
    public void setMap(String val) { if (loadMap(val)) this.imgFile = val; }
    public String getMap() { return this.imgFile; }

	public boolean loadMap(String imgFile) {
	    try {
		terrain = new Terrain(imgFile);
	    } catch (TerrainException e) {
		System.err.printf("Cannot load map %s.\n",imgFile);
		//e.printStackTrace();
		return false;
	    }
	    this.agentGrid = new SparseGrid2D(terrain.area.getWidth(),terrain.area.getHeight());
	    int scale = 10;
	    this.baseMapGrid = new IntGrid2D(terrain.area.getWidth()/scale,terrain.area.getHeight()/scale);
	    return true;
	}
	
	public void start() {
	    this.started = false;
		if (!loadMap(imgFile)) {
		    System.err.println("Cannot start without an appropriate map file.");
		    return;
		}
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
		// add all teams
		for (int i=0; i<numTeams; ++i) {
		    Bag callers = new Bag();
		    // add all callers of the team
		    for (int j=0; j<numCallerPerTeam; j++) {
			Caller c = new Caller(i, this.baseMapGrid);
			agentGrid.setObjectLocation(c, startx,starty);
			schedule.scheduleRepeating(c,2);
			callers.add(c);
		    }
		    // add all agents of the team
		    for (int j=0; j<numAgentsPerTeam; j++) {
			int scale = 10;
			int width = terrain.area.getWidth();
			int height = terrain.area.getHeight();
			Searcher x = new Searcher(i, new SparseGrid2D(width/scale,height/scale),scale);
			agentGrid.setObjectLocation(x, startx,starty);
			schedule.scheduleRepeating(x);
			for (int k=0; k<numCallerPerTeam; k++) {
			    ((Caller)callers.get(k)).addTeamMember(x);
			    x.addTeamCaller((Caller)callers.get(k));
			}
		    }
		}
	    this.started = true;
	}

	public static void main(String[] args) {
		doLoop(sim.app.snr.SearchAndRescue.class, args);
		System.exit(0);
	}
	
}
