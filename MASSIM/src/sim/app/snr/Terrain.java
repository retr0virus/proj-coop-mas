package sim.app.snr;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import sim.field.grid.IntGrid2D;

public class Terrain {
	public static final int WALL = 0;
	public static final int WAY = 1;
	public static final int POI = 2;
	public static final int VPOI = 3;
	public static final int START = 4;
	public final Color[] colorTable = {Color.black,Color.white,Color.red,Color.green,Color.blue};
	public IntGrid2D area;

	public int width = -1;
	public int height = -1;

	public Terrain(String imgFile) throws TerrainException {
		if (imgFile == null) throw new TerrainException("NULL-Parameter");
		BufferedImage bi = null;
		try {
		    bi = ImageIO.read(new File(imgFile));
		} catch (IOException e) {
		    throw new TerrainException(e);
		}
		area = new IntGrid2D(bi.getWidth(), bi.getHeight());
		width = bi.getWidth();
		height = bi.getHeight();
		for (int x = 0; x<bi.getWidth(); x++) {
			for (int y = 0; y<bi.getHeight();y++ ) {
				int c = bi.getRGB(x, y);
				if( c == Color.black.getRGB())
					area.set(x, y, WALL);
				else if( c == Color.blue.getRGB())
					area.set(x, y, START);
				else if ( c == Color.red.getRGB())
					area.set(x, y, POI);
				else 
					area.set(x, y, WAY);
			}
		}
	}
}
