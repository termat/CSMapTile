package net.termat.gsi.tile;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;

public class Tile {
	public static final int TYPE_CS=0;
	public static final int TYPE_JSHIS=1;
	public static final int TYPE_COMPO=2;

	@DatabaseField(generatedId=true)
	long id;

    @DatabaseField
	int type;

    @DatabaseField
	int z;

    @DatabaseField
	int x;

    @DatabaseField
	int y;

    @DatabaseField(dataType = DataType.BYTE_ARRAY)
    byte[] imageBytes;

}
