package ds.vkplus.eventbus.events;

public class CroutonEvent {

	public String message;
	public int style;
	public int duration;


	public CroutonEvent(final String message, final int style, final int duration) {
		this.message = message;
		this.style = style;
		this.duration = duration;
	}
}
