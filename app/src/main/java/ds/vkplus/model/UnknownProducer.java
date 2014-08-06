package ds.vkplus.model;

public class UnknownProducer implements Producer {

	@Override
	public String getName() {
		return "[Unknown]";
	}


	@Override
	public String getThumb() {
		return "android.resource://ds.vkplus/drawable/ic_launcher";
	}


	@Override
	public long getId() {
		return 0;
	}
}
