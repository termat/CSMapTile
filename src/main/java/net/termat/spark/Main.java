package net.termat.spark;

import static spark.Spark.*;

import java.io.IOException;
import java.util.Optional;

import net.termat.gsi.tile.Tile;
import net.termat.gsi.tile.TileDB;
import spark.Spark;

public class Main {
	private static TileDB db;

	public static void main(String[] args){
		db=new TileDB();
		try{
			db.connectDB("tile.db", true);
		}catch(Exception e){
			e.printStackTrace();
		}
		Optional<String> optionalPort = Optional.ofNullable(System.getenv("PORT"));
		optionalPort.ifPresent( p -> {
			int port = Integer.parseInt(p);
			Spark.port(port);
		});
		Spark.staticFileLocation("/public");

		get("/csmap/:z/:x/:y", (request, response) -> {
			try{
				Integer zz=Integer.parseInt(request.params("z"));
				if(zz>15)return null;
				Integer xx=Integer.parseInt(request.params("x"));
				Integer yy=Integer.parseInt(request.params("y"));
				byte[] rawImage =db.getTile(Tile.TYPE_CS, zz, xx, yy);
				if(rawImage!=null){
					response.status(200);
					response.header("Content-Type", "image/png");
					return rawImage;
				}else{
					response.status(400);
					return null;
				}
			}catch(IOException e){
				e.printStackTrace();
				response.status(400);
				return null;
			}
		});

		get("/jshis/:z/:x/:y", (request, response) -> {
			try{
				Integer zz=Integer.parseInt(request.params("z"));
				if(zz>15)return null;
				Integer xx=Integer.parseInt(request.params("x"));
				Integer yy=Integer.parseInt(request.params("y"));
				byte[] rawImage =db.getTile(Tile.TYPE_JSHIS, zz, xx, yy);
				if(rawImage!=null){
					response.status(200);
					response.header("Content-Type", "image/png");
					return rawImage;
				}else{
					response.status(400);
					return null;
				}
			}catch(Exception e){
				e.printStackTrace();
				response.status(400);
				return null;
			}
		});

		get("/compo/:z/:x/:y", (request, response) -> {
			try{
				Integer zz=Integer.parseInt(request.params("z"));
				if(zz>15)return null;
				Integer xx=Integer.parseInt(request.params("x"));
				Integer yy=Integer.parseInt(request.params("y"));
				byte[] rawImage =db.getTile(Tile.TYPE_COMPO, zz, xx, yy);
				if(rawImage!=null){
					response.status(200);
					response.header("Content-Type", "image/jpeg");
					return rawImage;
				}else{
					response.status(400);
					return null;
				}
			}catch(Exception e){
				e.printStackTrace();
				response.status(400);
				return null;
			}
		});
	}

}