package net.termat.geo.xmap;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.jxmapviewer.JXMapViewer;
import org.jxmapviewer.painter.Painter;
import org.jxmapviewer.viewer.GeoPosition;

public class RectPainter implements  Painter<JXMapViewer> {
	private GeoPosition[] node;
	private Stroke stroke=new BasicStroke(0.0f);
	private Color col=new Color(0.2f,0.2f,0.8f,0.5f);

	public RectPainter(Rectangle2D rect){
		List<GeoPosition> list=new ArrayList<GeoPosition>();
		list.add(new GeoPosition(rect.getY(),rect.getX()));
		list.add(new GeoPosition(rect.getY(),rect.getX()+rect.getWidth()));
		list.add(new GeoPosition(rect.getY()+rect.getHeight(),rect.getX()+rect.getWidth()));
		list.add(new GeoPosition(rect.getY()+rect.getHeight(),rect.getX()));
		node=list.toArray(new GeoPosition[list.size()]);
	}

	@Override
	public void paint(Graphics2D g, JXMapViewer map, int width, int height) {
		g = (Graphics2D) g.create();
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);
		g.setColor(col);
		g.setStroke(stroke);
		Point2D p1=map.convertGeoPositionToPoint(node[0]);
		Point2D p2=map.convertGeoPositionToPoint(node[1]);
		Point2D p3=map.convertGeoPositionToPoint(node[2]);
		Point2D p4=map.convertGeoPositionToPoint(node[3]);
		Point2D p5=map.convertGeoPositionToPoint(node[0]);
		Polygon poly=new Polygon();
		poly.addPoint((int)p1.getX(), (int)p1.getY());
		poly.addPoint((int)p2.getX(), (int)p2.getY());
		poly.addPoint((int)p3.getX(), (int)p3.getY());
		poly.addPoint((int)p4.getX(), (int)p4.getY());
		poly.addPoint((int)p5.getX(), (int)p5.getY());
		g.fillPolygon(poly);
	}

}
