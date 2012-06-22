package sim.app.snr;

public class TerrainException extends RuntimeException {
    public TerrainException(String msg) {
	super(msg);
    }
    public TerrainException(Throwable c) {
	super(c);
    }
}
