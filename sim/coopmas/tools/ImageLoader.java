package sim.coopmas.tools;
/**
 * Class for loading an image into an array.
 * @author Marcel Miebach
 */
public class ImageLoader {
	private String current_image = null;
	private String last_image = null;

	private int width = -1;
	private int height = -1;
	private int[] data = null;

	/**
	 * Loads an image from the given path.
	 * @return true if the image could be loaded, false else
	 */
	public boolean loadImage(String image) {
		java.awt.image.BufferedImage bi = null;
		try {
			bi = javax.imageio.ImageIO.read(new java.io.File(image));
		} catch (java.io.IOException e) {
			System.err.println("Could not load the image: "+image);
			return false;
		}
		if (bi != null) {
		    synchronized(this) {
			this.current_image = image;
			this.last_image = image;
			this.height = bi.getHeight();
			this.width = bi.getWidth();
			data = new int[this.height*this.width];
			bi.getRGB(0,0,width,height,data,0,this.width);
			bi = null;
		    }
		}
		return true;
	}
	/**
	 * Returns the 8byte color-value for a given position in the image.
	 * 2byte alpha, 2byte red, 2byte green, 2byte blue (0xAARRGGBB)
	 * @param x the column
	 * @param y the row
	 * @return the color-value
	 */
	public int getValue(int x, int y) {
		if (this.last_image == null || !this.last_image.equals(this.current_image)) {
		   loadImage(this.current_image);
		}
		if (x < this.width && y < this.height && x >= 0 && y >= 0)
			return data[this.width*y+x];
		else
			return -1;
	}
	/**
	 * The alpha value at a given point.
	 * @param x the column
	 * @param y the row
	 * @return the alpha value (0..255), 0 = full transparency, 255 = no transparency
	 */
	public int getA(int x, int y) {
		int val = getValue(x,y);
		return val == -1 ? -1 : ((val >> 24)&0xff);
	}
	/**
	 * The red value at a given point.
	 * @param x the column
	 * @param y the row
	 * @return the red value (0..255)
	 */
	public int getR(int x, int y) {
		int val = getValue(x,y);
		return val == -1 ? -1 : ((val >> 16)&0xff);
	}
	/**
	 * The green value at a given point.
	 * @param x the column
	 * @param y the row
	 * @return the green value (0..255)
	 */
	public int getG(int x, int y) {
		int val = getValue(x,y);
		return val == -1 ? -1 : ((val >> 8)&0xff);
	}
	/**
	 * The blue value at a given point.
	 * @param x the column
	 * @param y the row
	 * @return the blue value (0..255)
	 */
	public int getB(int x, int y) {
		int val = getValue(x,y);
		return val == -1 ? -1 : ((val >> 0)&0xff);
	}
	/**
	 * The alpha value at a given point, rounded to the next int values.
	 * @param x the column (gets rounded to the next int)
	 * @param y the row (gets rounded to the next int)
	 * @return the alpha value (0..255), 0 = full transparency, 255 = no transparency
	 */
	public int getA(double x, double y) {
		return getA((int)Math.round(x), (int)Math.round(y));
	}
	/**
	 * The red value at a given point, rounded to the next int values.
	 * @param x the column (gets rounded to the next int)
	 * @param y the row (gets rounded to the next int)
	 * @return the red value (0..255)
	 */
	public int getR(double x, double y) {
		return getR((int)Math.round(x), (int)Math.round(y));
	}	
	/**
	 * The green value at a given point, rounded to the next int values.
	 * @param x the column (gets rounded to the next int)
	 * @param y the row (gets rounded to the next int)
	 * @return the green value (0..255)
	 */
	public int getG(double x, double y) {
		return getG((int)Math.round(x), (int)Math.round(y));
	}
	/**
	 * The blue value at a given point, rounded to the next int values.
	 * @param x the column (gets rounded to the next int)
	 * @param y the row (gets rounded to the next int)
	 * @return the blue value (0..255)
	 */
	public int getB(double x, double y) {
		return getB((int)Math.round(x), (int)Math.round(y));
	}

	/**
	 * Black 000000 = Wall.
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return true if the given point is a wall
	 */
	public boolean isWall(int x, int y) {
		return (getValue(x,y)&0xffffff) == 0;
	}
	/**
	 * Red ff0000 = Point of Interest.
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return true if the given point is a point of interest
	 */
	public boolean isPOI(int x, int y) {
		return getR(x,y) == 255 && getG(x,y) == 0 && getB(x,y) == 0;
	}
	/**
	 * Green 00ff00 = Visited Point of Interest.
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return true if the given point is a visited point of interest
	 */
	public boolean isVisitedPOI(int x, int y) {
		return getR(x,y) == 0 && getG(x,y) == 255 && getB(x,y) == 0;
	}
	/**
	 * Blue 0000ff = Startpoint.
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return true if the given point is a startpoint
	 */
	public boolean isStart(int x, int y) {
		return getR(x,y) == 0 && getG(x,y) == 0 && getB(x,y) == 255;
	}
	/**
	 * Purple 800080 = Searcher.
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return true if the given point is a searcher
	 */
	public boolean isSearcher(int x, int y) {
		return getR(x,y) == 128 && getG(x,y) == 0 && getB(x,y) == 128;
	}
	/**
	 * Orange FFA500 = Caller.
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return true if the given point is a caller
	 */
	public boolean isCaller(int x, int y) {
		return getR(x,y) == 255 && getG(x,y) == 165 && getB(x,y) == 0;
	}

	/**
	 * Gets the model representation of a point in the image.
	 * E.g. it returns PointRepresentation.WALL if the given point is black (= wall).
	 * The transparency value is NOT used.
	 * @param x the column
	 * @param y the row
	 * @return the PointRepresentation that fits to the position in the image
	 */
	public PointRepresentation getPointRepresentation(int x, int y) {
		int val = getValue(x,y);
		val = val&0xffffff;
		switch (val) {
			case 0 : return PointRepresentation.WALL;
			case 255 : return PointRepresentation.START;
			case 65280 : return PointRepresentation.VPOI;
			case 16711680 : return PointRepresentation.POI;
			case 8388736 : return PointRepresentation.SEARCHER;
			case 16753920 : return PointRepresentation.CALLER;
			case 16777215: return PointRepresentation.WAY;
			default: return PointRepresentation.UNKNOWN;
		}
	}

	@Override public String toString() {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<this.height; ++i) {
			for (int j=0; j<this.width; ++j) {
				//int val = data[this.width * i + j];
				//sb.append(isWall(j,i)?"#":(isPOI(j,i)?"X":(isVisitedPOI(j,i)?"V":(isSearcher(j,i)?"S":(isCaller(j,i)?"C":(isStart(j,i)?"E":"."))))));
				PointRepresentation pr = getPointRepresentation(j,i);
				String x = "";
				switch (pr) {
					case WALL: x = "#"; break;
					case START: x = "E"; break;
					case POI: x = "P"; break;
					case VPOI: x = "V"; break;
					case SEARCHER: x = "S"; break;
					case CALLER: x = "C"; break;
					case WAY : x = "."; break;
					default : x = "?"; break;
				}
				sb.append(x);
			}
			sb.append('\n');
		}
		sb.append("Image: ");
		sb.append(current_image);
		sb.append('\n');
		sb.append("# -- Wall\n");
		sb.append("E -- Start-/Entrypoint\n");
		sb.append("P -- Point Of Interest\n");
		sb.append("V -- Visited Point Of Interest\n");
		sb.append("S -- Searcher\n");
		sb.append("C -- Caller\n");
		sb.append(". -- Way / Free path\n");
		return sb.toString();
	}
	/**
	 * To test the behaviour.
	 * @param args The first argument is taken as the imagepath, else files/map.png is taken
	 */
	public static void main(String[] args) {
		ImageLoader il = new ImageLoader();
		//System.out.println(il);
		il.loadImage(args.length>0?args[0]:"files" + java.io.File.separator + "map.png");
		System.out.println(il);
	}
}
/**
 * A representation for a given point in the image.
 * E.g. a wall or a point of interest.
 */
enum PointRepresentation{ 
    /** A wall - no chance to pass. */
    WALL, 
    /** A point of interest - you have to find them all. */
    POI, 
    /** An already visited point of interest - no longer interesting. */
    VPOI, 
    /** The point you start from and enter the area to search for POIs. */
    START, 
    /** A searcher that is able to find POIs. */
    SEARCHER, 
    /** A caller that is able to communicate within larger areas with searchers to acknowledge POIs. */
    CALLER, 
    /** A way or free path - you can go there! */
    WAY, 
    /** Something strange - maybe a pit. Hopefully no Wumpus... */
    UNKNOWN;
};

