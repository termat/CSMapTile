package net.termat.geo.xmap;

import java.net.MalformedURLException;
import java.net.URL;

public class OsmTileProvider implements TileProvider{
	private String baseURL="http://tile.openstreetmap.jp";

	@Override
	public URL getTileURL(int x, int y, int zoom) {
		String url = baseURL + "/" + zoom + "/" + x + "/" + y + ".png";
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public String getName() {
		return "OpenStreetMap.jp";
	}
}
