package sim.app.snr;
public class Value {
	private Object value = null;
	public Value(Object value) {
		this.value = value;
	}
	public Object get() { return value; }
	public void set(Object val) { this.value = val; }
	@Override public String toString() { return "[Value]"+String.valueOf(value); }
}
