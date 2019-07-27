package net.termat.geo.xmap;

import java.net.URL;

public interface TileProvider {

	public URL getTileURL(int x,int y,int zoom);
	public String getName();
}
