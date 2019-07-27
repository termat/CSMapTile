package net.termat.geo.xmap;

import java.net.MalformedURLException;
import java.net.URL;

public class JpTileProvider implements TileProvider{
//	private String baseURL="https://cyberjapandata.gsi.go.jp/xyz/std";
	private String baseURL="http://localhost:4567";

	@Override
	public URL getTileURL(int x, int y, int zoom) {
		String url= this.baseURL + "/photo/" + zoom + "/" + x + "/" + y + ".png";
		try {
			return new URL(url);
		} catch (MalformedURLException e) {
			return null;
		}
	}


	@Override
	public String getName() {
		return "電子国土基本図（標準）";
	}
}
