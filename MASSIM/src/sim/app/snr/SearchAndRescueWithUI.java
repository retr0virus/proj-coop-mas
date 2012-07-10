package sim.app.snr;

import java.awt.Color;

import javax.swing.JFrame;
import sim.display.Controller;
import sim.display.Display2D;
import sim.display.GUIState;
import sim.engine.SimState;
import sim.portrayal.grid.FastValueGridPortrayal2D;
import sim.portrayal.grid.SparseGridPortrayal2D;
import sim.portrayal.simple.OvalPortrayal2D;

public class SearchAndRescueWithUI extends GUIState {

	FastValueGridPortrayal2D terrainPortrayal = new FastValueGridPortrayal2D("Terrain");
	SparseGridPortrayal2D agentPortrayal = new SparseGridPortrayal2D();
	FastValueGridPortrayal2D baseMapPortrayal = new FastValueGridPortrayal2D();
	public Display2D display;
	public JFrame displayFrame;

	public SearchAndRescueWithUI(String imgfile) {
		super(new SearchAndRescue(System.currentTimeMillis(),imgfile));
	}

	public SearchAndRescueWithUI(SimState state) {
		super(state);
	}

	public static String getName() {
		return "Search And Rescue MAS";
	}
	
	public Object getSimulationInspectedObject() { return state; }
	/*public sim.portrayal.Inspector getInspector() {
	    sim.portrayal.Inspector i = super.getInspector();
	    i.setVolatile(true);
	    return i;
	}*/

	public void start() {
	    System.out.println("START");
		super.start(); // starts the state too
		SearchAndRescue snr = (SearchAndRescue) state;
		if (!snr.started) return;
		if (snr.terrain != null) { initDisplay(); }
		terrainPortrayal.setField(snr.terrain.area);
		terrainPortrayal.setMap(new sim.util.gui.SimpleColorMap(
				snr.terrain.colorTable));
		agentPortrayal.setField(snr.agentGrid);
		agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(10));

		baseMapPortrayal.setField(snr.baseMapGrid);
		baseMapPortrayal.setMap(new sim.util.gui.SimpleColorMap(snr.terrain.colorTable));
		baseMapPortrayal.setPortrayalForAll(new OvalPortrayal2D(5));

		display.reset();
		display.setBackdrop(Color.white);

		display.repaint();
	}
	
	public void load(SimState state) {
	    super.load(state);
	}
    
	public void init(Controller c) {
		super.init(c);
		initDisplay();
		
	}
	public void initDisplay() {
		SearchAndRescue snr = (SearchAndRescue) state;
		if (snr.terrain == null) return;
		if (display != null) {
		    JFrame f = (JFrame)display.getFrame();
		    display.detatchAll();
		    display.quit();
		    super.controller.unregisterFrame(f);
		    f.setVisible(false);
		    f.dispose();
		} 
		display = new Display2D(snr.terrain.area.getWidth(),snr.terrain.area.getHeight(),  this); 
		displayFrame = display.createFrame();
		super.controller.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		display.attach(terrainPortrayal, "Terrain");
		display.attach(agentPortrayal,"Agents");
		display.attach(baseMapPortrayal,"Base Map", false);
	}

	public void quit() {
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null; 
		display = null;
	}

	public static void main(String[] args) {
	    String imgfile = null;
	    if (args.length > 0) {
		imgfile = args[0];
	    }
	    new SearchAndRescueWithUI(imgfile).createController();
	}
}
