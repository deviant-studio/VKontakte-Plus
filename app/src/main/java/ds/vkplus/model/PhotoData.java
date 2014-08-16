package ds.vkplus.model;

public class PhotoData {

	public static final int TYPE_PHOTO = 0;
	public static final int TYPE_VIDEO = 1;
	public static final int TYPE_LINK = 2;

	public final long id;
	public int width;
	public int height;
	public String url;
	public float ratio;
	public int type;
	public boolean breakAfter;
	public boolean floating;
	public boolean paddingBottom;
	public String extra;


	public PhotoData(final String url, final int width, final int height, final int t, final long id) {
		this.width = width != 0 ? width : 1600;
		this.height = height != 0 ? height : 1200;
		this.url = url;
		ratio = (float) width / height;
		type = t;
		this.id = id;
	}


	public PhotoData(final String url, final String urlBig, final long id) {
		this.url = url;
		extra = urlBig;
		this.id = id;
	}


	public void setViewSize(final float w, final float h, final boolean breakAfter, final boolean floating) {
		width = (int) w;
		height = (int) h;
		this.breakAfter = breakAfter;
		this.floating = floating;


	}
}
