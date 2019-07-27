package net.termat.geo.xmap;

import java.net.MalformedURLException;
import java.net.URL;

public class JpOrtTileProvider implements TileProvider{
	private String baseURL="https://cyberjapandata.gsi.go.jp/xyz/seamlessphoto";

	@Override
	public URL getTileURL(int x, int y, int zoom) {
		String url= this.baseURL + "/ort/" + zoom + "/" + x + "/" + y + ".jpg";
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}

	@Override
	public String getName() {
		return "電子国土基本図（オルソ）";
	}
}
