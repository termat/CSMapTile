package net.termat.gsi;

import java.awt.image.BufferedImage;
import java.net.URL;

import javax.imageio.ImageIO;

public class JShisWMS {
	private static final String baseURL="http://www.j-shis.bosai.go.jp/map/wms/landslide?SERVICE=WMS&VERSION=1.3.0&REQUEST=GetMap&";
	private static final double L=(180.0/Math.PI)*Math.asin(Math.tanh(Math.PI));

/*
 * http://www.j-shis.bosai.go.jp/wms-landslide
 *
 * 4301 Tokyo
 * 4326 WGS84
 * 4612 JGS2000
 *
 * 輪郭構造（滑落崖と側方崖）	L-V3-S100
 * 輪郭構造（移動体の輪郭・境界）（移動体アーク）	L-V3-S200
 * 輪郭構造（移動体の輪郭・境界）（移動体ポリゴン）	L-V3-S300
 * 内部構造	L-V3-S400
 * 移動方向等移動体の主移動方向	L-V3-S500
 * 移動体の重心	L-V3-CENTER
 * 上記全ての地すべり地形	L-V3-ALL
 *
 */

	public static BufferedImage getJShis(int zoom,int tileX,int tileY){
		try{
			String url=getURL(zoom,tileX,tileY);
			BufferedImage img=ImageIO.read(new URL(url));
			return img;
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	private static String getURL(int zoom,int tileX,int tileY){
		String ret=baseURL+getBBOX(zoom,tileX,tileY)+"&CRS=EPSG:4612"+"&WIDTH=256&HEIGHT=256&LAYERS=L-V3-ALL&FORMAT=image/png&TRANSPARENT=TRUE";
		return ret;
	}

	public static String getBBOX(int zoom,int tileX,int tileY){
		String ret="BBOX=";
		float[][] b=getTileBounds(zoom,tileX,tileY);
		ret=ret+Float.toString(b[1][2])+","+Float.toString(b[0][1])+","+Float.toString(b[0][2])+","+Float.toString(b[1][1]);
		return ret;
	}

	private static float[][] getTileBounds(int zoom,long tileX,long tileY){
		float[] ll1=pixelToLonLatCoord(zoom,tileX*256,tileY*256);
		float[] ll2=pixelToLonLatCoord(zoom,tileX*256+255,tileY*256+255);
		return new float[][]{ll1,ll2};
	}

	private static float[] pixelToLonLatCoord(int zoom,long x,long y){
		double lon=180.0*(x/Math.pow(2, zoom+7)-1);
		double tmp0=((-Math.PI/Math.pow(2, zoom+7))*y);
		double tmp1=atanh(Math.sin(L*Math.PI/180.0));
		double lat=(180.0/Math.PI)*Math.asin(Math.tanh(tmp0+tmp1));
		return new float[]{(float)zoom,(float)lon,(float)lat};
	}

	private static double atanh(double v){
		return 0.5*Math.log((1.0+v)/(1.0-v));
	}
}
