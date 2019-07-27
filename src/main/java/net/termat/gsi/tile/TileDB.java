package net.termat.gsi.tile;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JLabel;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import net.termat.geo.tool.Gradient;
import net.termat.geo.tool.GradientFactory;
import net.termat.geo.tool.RangeMap;
import net.termat.gsi.JShisWMS;

public class TileDB {
	private static String slope="https://cyberjapandata.gsi.go.jp/xyz/slopemap";
	private static String dem14="https://cyberjapandata.gsi.go.jp/xyz/dem_png";
	private static String dem15a="https://cyberjapandata.gsi.go.jp/xyz/dem5a_png";
	private static String dem15b="https://cyberjapandata.gsi.go.jp/xyz/dem5b_png";
	private static final int P8=(int)Math.pow(2,8);
	private static final int P16=(int)Math.pow(2,16);
	private static final int P23=(int)Math.pow(2,23);
	private static final int P24=(int)Math.pow(2,24);
	private static final double U=0.01;

	private static double GEO_R=6378137;
	private static RangeMap range=new RangeMap(-1.5,1.5);
	private static Gradient grad1=GradientFactory.createGradient(new Color[]{new Color(255,200,200),Color.WHITE,new Color(200,200,255)});
	private static JComponent comp=new JLabel("");

	private ConnectionSource connectionSource = null;
	private Dao<Tile,Long> tileDao;
	private static int[] col=new int[]{255,232,197};

	private float transparency=0.9f;

	/**
	 * DB接続
	 *
	 * @param dbName DBのパス
	 * @param create DBが無い場合、自動生成
	 * @throws SQLException
	 */
	public void connectDB(String dbName,boolean create) throws SQLException{
		try{
			if(!dbName.endsWith(".db"))dbName=dbName+".db";
			Class.forName("org.sqlite.JDBC");
			connectionSource = new JdbcConnectionSource("jdbc:sqlite:"+dbName);
			tileDao= DaoManager.createDao(connectionSource, Tile.class);
			if(create)TableUtils.createTableIfNotExists(connectionSource, Tile.class);
		}catch(SQLException e){
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	/**
	 * 画像タイルを取得
	 *
	 * @param type タイルの種類
	 * @param zoom ズームレベル
	 * @param tileX タイルX座標
	 * @param tileY タイルY座標
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public byte[] getTile(int type,int zoom,int tileX,int tileY) throws SQLException,IOException{
		QueryBuilder<Tile, Long> query=tileDao.queryBuilder();
		query.where().eq("type", type).and().eq("z", zoom).and().eq("x", tileX).and().eq("y", tileY);
		List<Tile> list=tileDao.query(query.prepare());
		if(list.size()==0){
			Tile t=new Tile();
			t.z=zoom;
			t.x=tileX;
			t.y=tileY;
			t.type=type;
			switch(type){
			case Tile.TYPE_CS:
				t.imageBytes=bi2Bytes(getCSImage(zoom,tileX,tileY));
				break;
			case Tile.TYPE_JSHIS:
				t.imageBytes=bi2Bytes(getJSImage(zoom,tileX,tileY));
				break;
			case Tile.TYPE_COMPO:
				t.imageBytes=bi2Bytes(getCompImage(zoom,tileX,tileY));
				break;
			}
			if(t.imageBytes!=null&&t.imageBytes.length>0)tileDao.createIfNotExists(t);
			return t.imageBytes;
		}else{
			Tile t=list.get(0);
			return t.imageBytes;
		}
	}

	/*
	 * BufferedImage -> byte[]
	 */
	private static BufferedImage bytes2Bi(byte[] raw)throws IOException{
		ByteArrayInputStream bais = new ByteArrayInputStream(raw);
		BufferedImage img=ImageIO.read(bais);
		return img;
	}

	/*
	 * byte[] -> BufferedImage
	 */
	private static byte[] bi2Bytes(BufferedImage img)throws IOException{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write( img, "png", baos );
		baos.flush();
		return baos.toByteArray();
	}

	/*
	 * CSタイル画像取得
	 */
	public BufferedImage getCSImage(int zoom,int tileX,int tileY)throws IOException{
		String param="/"+zoom+"/"+tileX+"/"+tileY;
		BufferedImage s_img=ImageIO.read(new URL(slope+param+".png"));
		s_img=getColorTransSlope(s_img);
		BufferedImage d_img=null;
		d_img=createDemTile(zoom,tileX,tileY);
		/*
		if(zoom<=14){
			d_img=ImageIO.read(new URL(dem14+param+".png"));
		}else{
			try{
				d_img=ImageIO.read(new URL(dem15a+param+".png"));
			}catch(IOException e){
				d_img=ImageIO.read(new URL(dem15b+param+".png"));
			}
		}
		*/
		BufferedImage c_img=getCurve(d_img,zoom);
		BufferedImage img=mul(c_img,s_img);
		return img;
	}

	private static BufferedImage createDemTile(int zoom,int tileX,int tileY)throws IOException{
		BufferedImage bc=getDemImage(zoom,tileX,tileY);
		BufferedImage ret=new BufferedImage(bc.getWidth()+2,bc.getHeight()+2,bc.getType());
		Graphics2D g=ret.createGraphics();
		g.drawImage(bc, 1, 1, comp);
		try{
			BufferedImage bu=getDemImage(zoom,tileX,tileY-1);
			g.drawImage(bu, 1, -255, comp);
		}catch(IOException e){}
		try{
			BufferedImage bd=getDemImage(zoom,tileX,tileY+1);
			g.drawImage(bd, 1, 257, comp);
		}catch(IOException e){}
		try{
			BufferedImage bl=getDemImage(zoom,tileX-1,tileY);
			g.drawImage(bl, -255, 1, comp);
		}catch(IOException e){}

		try{
			BufferedImage br=getDemImage(zoom,tileX+1,tileY);
			g.drawImage(br, 257, 1, comp);
		}catch(IOException e){}
		g.dispose();
		return ret;
	}

	private static BufferedImage getDemImage(int zoom,int tileX,int tileY)throws IOException{
		String param="/"+zoom+"/"+tileX+"/"+tileY;
		BufferedImage d_img=null;
		if(zoom<=14){
			d_img=ImageIO.read(new URL(dem14+param+".png"));
		}else{
			try{
				d_img=ImageIO.read(new URL(dem15a+param+".png"));
			}catch(IOException e){
				d_img=ImageIO.read(new URL(dem15b+param+".png"));
			}
		}
		return d_img;
	}

	/*
	 * 傾斜量図の色変換
	 */
	private static BufferedImage getColorTransSlope(BufferedImage img){
		int w=img.getWidth();
		int h=img.getHeight();
		BufferedImage ret=new BufferedImage(w,h,img.getType());
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				float cv=(float)(img.getRGB(i, j)&0x0000ff)/255f;
				ret.setRGB(i, j, new Color((int)(col[0]*cv),(int)(col[1]*cv),(int)(col[2]*cv)).getRGB());
			}
		}
		return ret;
	}

	/*
	 * 地すべり等タイル画像取得
	 */
	public BufferedImage getJSImage(int zoom,int tileX,int tileY)throws IOException{
		BufferedImage img=JShisWMS.getJShis(zoom,tileX,tileY);
		return img;
	}

	/*
	 * CS+地すべり等タイル画像取得
	 */
	public BufferedImage getCompImage(int zoom,int tileX,int tileY)throws SQLException,IOException{
		QueryBuilder<Tile, Long> query=tileDao.queryBuilder();
		query.where().eq("type", Tile.TYPE_CS).and().eq("z", zoom).and().eq("x", tileX).and().eq("y", tileY);
		List<Tile> list=tileDao.query(query.prepare());
		BufferedImage img1=null;
		if(list.size()==0){
			img1=getCSImage(zoom,tileX,tileY);
		}else{
			img1=bytes2Bi(list.get(0).imageBytes);
		}
		query=tileDao.queryBuilder();
		query.where().eq("type", Tile.TYPE_JSHIS).and().eq("z", zoom).and().eq("x", tileX).and().eq("y", tileY);
		list=tileDao.query(query.prepare());
		BufferedImage img2=null;
		if(list.size()==0){
			img2=getJSImage(zoom,tileX,tileY);
		}else{
			img2=bytes2Bi(list.get(0).imageBytes);
		}
		Graphics2D g=img1.createGraphics();
		if(transparency<1.0f)
			g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER,transparency));
		g.drawImage(img2,0,0,comp);
		g.dispose();
		return img1;
	}

	/*
	 * 画素乗算
	 */
	private static BufferedImage mul(BufferedImage im1,BufferedImage im2){
		int w=im1.getWidth();
		int h=im1.getHeight();
		BufferedImage ret=new BufferedImage(w,h,BufferedImage.TYPE_INT_RGB);
		for(int i=0;i<w;i++){
			for(int j=0;j<h;j++){
				int rgb1=im1.getRGB(i, j);
				int rgb2=im2.getRGB(i, j);
				ret.setRGB(i, j, mulRGB(rgb1,rgb2));
			}
		}
		return ret;
	}

	/*
	 * RGB乗算
	 */
	private static int mulRGB(int rgb1,int rgb2){
		float r1=(float)(rgb1 >> 16 & 0xff)/255f;
		float g1=(float)(rgb1&0x00ff00 >> 8 & 0xff)/255f;
		float b1=(float)(rgb1&0x0000ff & 0xff)/255f;
		float r2=(float)(rgb2 >> 16 & 0xff)/255f;
		float g2=(float)(rgb2&0x00ff00 >> 8 & 0xff)/255f;
		float b2=(float)(rgb2&0x0000ff & 0xff)/255f;
		return new Color(r1*r2,g1*g2,b1*b2).getRGB();
	}

	/*
	 * 曲率画像取得
	 */
	public static BufferedImage getCurve(BufferedImage dem,int zoom){
		BufferedImage ret=new BufferedImage(dem.getWidth(),dem.getHeight(),BufferedImage.TYPE_INT_RGB);
		double[][] dd=new double[dem.getWidth()][dem.getHeight()];
		for(int i=0;i<dd.length;i++){
			for(int j=0;j<dd[i].length;j++){
				dd[i][j]=getZ(dem.getRGB(i, j));
			}
		}
		double ll=getL(zoom);
		for(int i=1;i<dd.length-1;i++){
			for(int j=1;j<dd[i].length-1;j++){
				double[][] p=new double[][]{
						{dd[i-1][j-1],dd[i-1][j],dd[i-1][j+1]},
						{dd[i][j-1],dd[i][j],dd[i][j+1]},
						{dd[i+1][j-1],dd[i+1][j],dd[i+1][j+1]}};
				double cu=getCurveVal(p,ll);
				Color col=grad1.getColor(range.getNormalValue(cu));
				ret.setRGB(i, j, col.getRGB());
			}
		}
		return ret.getSubimage(1, 1, 256, 256);
//		return ret;
	}

	/*
	 * 標高取得
	 */
	private static double getZ(int intColor){
		Color c=new Color(intColor);
		int r=c.getRed();
		int g=c.getGreen();
		int b=c.getBlue();
		int x=r*P16+g*P8+b;
		if(x<P23){
			return U*(double)x;
		}else if(x>P23){
			return 0;
		}else{
			return U*(double)(x-P24);
		}
	}

	/*
	 * ズームレベルの1pixel長さ(m)
	 */
	private static double getL(int zoom){
		return 2*GEO_R*Math.PI/256/Math.pow(2, zoom);
	}

	/*
	 * 曲率計算
	 */
	private static double getCurveVal(double[][] p,double ll){
		double ex1=p[1][0]+p[1][2]-2*p[1][1];
		double ex2=p[0][1]+p[2][1]-2*p[1][1];
		return (ex1+ex2)/(ll*ll)*100;
	}

}
