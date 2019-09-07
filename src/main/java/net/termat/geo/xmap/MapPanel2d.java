package net.termat.geo.xmap;

import java.awt.BorderLayout;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import org.jxmapviewer.JXMapKit;
import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.CompoundPainter;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.DefaultTileFactory;
import org.jxmapviewer.viewer.GeoPosition;
import org.jxmapviewer.viewer.TileFactoryInfo;

public class MapPanel2d extends JPanel {

	private static final long serialVersionUID = 1L;
	private JXMapKit mapkit;
	private MultiTileFactoryInfo tileInfo;
	private CompoundPainter<JXMapViewer> painter;

	public MapPanel2d(int zoom,double lat,double lng){
		super(new BorderLayout());
		init(zoom,lat, lng);
		painter=new CompoundPainter<JXMapViewer>();
		mapkit.getMainMap().setOverlayPainter(painter);
	}

	public void addPainter(Painter<JXMapViewer> p){
		painter.addPainter(p);
	}

	public MultiTileFactoryInfo getTileInfo() {
		return tileInfo;
	}

	public void setTileInfo(MultiTileFactoryInfo tileInfo) {
		this.tileInfo = tileInfo;
	}

	private void init(int zoom,double lat,double lng){
		mapkit = new JXMapKit();
		add(mapkit,BorderLayout.CENTER);
		tileInfo=new MultiTileFactoryInfo();
		DefaultTileFactory tileFactory = new DefaultTileFactory(tileInfo);
		mapkit.setTileFactory(tileFactory);
		final GeoPosition gp = new GeoPosition(lat, lng);
		mapkit.setZoom(zoom);
		mapkit.setCenterPosition(gp);
		mapkit.setZoomSliderVisible(false);
		mapkit.setZoomButtonsVisible(false);
		mapkit.setMiniMapVisible(false);
	}

	public void setTilefactoryInfo(TileFactoryInfo info){
		DefaultTileFactory tileFactory=new DefaultTileFactory(info);
		mapkit.setTileFactory(tileFactory);
		mapkit.updateUI();
	}

	public BufferedImage getImage(){
		BufferedImage img=new BufferedImage(mapkit.getWidth(),mapkit.getHeight(),BufferedImage.TYPE_INT_RGB);
		Graphics2D g=img.createGraphics();
		this.paint(g);
		g.dispose();
		getRectangleLonLat();
		return img;
	}

	private void getRectangleLonLat(){
		System.out.println(mapkit.getMainMap().getViewportBounds());
		GeoPosition topleft=mapkit.getMainMap().convertPointToGeoPosition(new Point2D.Double(0,0));
		GeoPosition bottomright=mapkit.getMainMap().convertPointToGeoPosition(new Point2D.Double(mapkit.getWidth()-1,mapkit.getHeight()-1));
		System.out.println(topleft.getLatitude()+","+topleft.getLongitude()+","+bottomright.getLatitude()+","+bottomright.getLongitude());
	}

}
