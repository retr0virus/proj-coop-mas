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
	public Display2D display;
	public JFrame displayFrame;

	public SearchAndRescueWithUI() {
		super(new SearchAndRescue(System.currentTimeMillis()));
	}

	public SearchAndRescueWithUI(SimState state) {
		super(state);
	}

	public static String getName() {
		return "Search And Rescue MAS";
	}

	public void start() {
		super.start();
		SearchAndRescue snr = (SearchAndRescue) state;
		terrainPortrayal.setField(snr.terrain.area);
		terrainPortrayal.setMap(new sim.util.gui.SimpleColorMap(
				snr.terrain.colorTable));
		agentPortrayal.setField(snr.agentGrid);
		agentPortrayal.setPortrayalForAll(new OvalPortrayal2D(10));

		display.reset();
		display.setBackdrop(Color.white);

		display.repaint();
	}
	
    public void load(SimState state)
    {
    super.load(state);

    }
    
	public void init(Controller c) {
		super.init(c);
		SearchAndRescue snr = (SearchAndRescue) state;
		display = new Display2D(snr.terrain.area.getWidth(),snr.terrain.area.getHeight(),  this); 
		displayFrame = display.createFrame();
		c.registerFrame(displayFrame);
		displayFrame.setVisible(true);
		display.attach(terrainPortrayal, "Terrain");
		display.attach(agentPortrayal,"Agents");
	}

	public void quit() {
		super.quit();

		if (displayFrame != null)
			displayFrame.dispose();
		displayFrame = null; 
		display = null;
	}

	public static void main(String[] args) {
		new SearchAndRescueWithUI().createController();
	}
}
