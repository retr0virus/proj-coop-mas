package sim.app.snr.agent;

import sim.app.snr.message.Message;
import sim.app.snr.message.Ping;
import sim.engine.SimState;
import sim.util.Bag;

public class Searcher extends AbstractAgent {
	
	private int teamNr;
	private Bag team = new Bag();
	private Bag teamCaller = new Bag();
	private double dirInertia = 0.95;
	private double rightTurner = 0.5;
	
	public Searcher(int teamNr){
		this.teamNr = teamNr;
		communicationRadius = 15;
		viewRadius = 10;
	}
	public void addTeamCaller(Caller a) {
		teamCaller.add(a);
	}
	
	private void swarm() {
		if (nextIsFree() && snr.random.nextDouble() < dirInertia) {
			forward();
		}
		else {
			if (snr.random.nextDouble() < rightTurner) {
				turnRight();
			} else {
				turnLeft();
			}
		}
	}
	
	public double getDirInertia() { return this.dirInertia; }
	public void setDirInertia(double value) { this.dirInertia = value; }
	public Object domDirInertia() { return new sim.util.Interval(0.0, 1.0); }
	
	public double getRightTurner() { return this.rightTurner; }
	public void setRightTurner(double value) { this.rightTurner = value; }
	public Object domRightTurner() { return new sim.util.Interval(0.0, 1.0); }
	
	public int getInspectRadius() { return viewRadius; }
	public void setInspectRadius(int r) {
		if (r > 0)
			this.viewRadius = r;
	}
	
	@Override public void step(SimState state) {
		updateInternalState(state);
		updatePosition();
		swarm();
	}
	
	@Override public void receiveMessage(Message m) {
		if (m instanceof Ping) {
			this.receive((Ping)m);
		} else {
			System.out.println("No ping");
			this.receive(m);
		}
	} 
	
	@Override public void receivePing(Ping p) {
		System.out.println("ping");
	}
	
	private void receive(Message m) {
		System.out.println("WRONG!");
	}
	private void receive(Ping p) {
		System.out.println("RIGHT!");
	}
}
