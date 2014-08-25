package ds.vkplus.ui.view;

import android.animation.AnimatorSet;
import android.animation.LayoutTransition;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import ds.vkplus.R;
import ds.vkplus.utils.L;

public class RefreshButton extends FrameLayout {

	private ImageView button;
	private TextView textLabel;
	private int notificationsCount;
	private ViewGroup root;
	private boolean rotating;


	public RefreshButton(final Context context) {
		super(context);
		init();
	}


	public RefreshButton(final Context context, final AttributeSet attrs) {
		super(context, attrs);
		init();
	}


	public RefreshButton(final Context context, final AttributeSet attrs, final int defStyle) {
		super(context, attrs, defStyle);
		init();
	}


	private void init() {
		root = (ViewGroup) LayoutInflater.from(getContext()).inflate(R.layout.refresh_button, this, true);
		LayoutParams p = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		setLayoutParams(p);
		textLabel = (TextView) root.findViewById(R.id.text_label);
		button = (ImageView) root.findViewById(R.id.button);

		new Handler().postDelayed(this::setupTransitions, 1000);   // god bless this line!

	}


	private void setupTransitions() {
		final LayoutTransition lt = new LayoutTransition();
		final AnimatorSet disappear = new AnimatorSet();
		final AnimatorSet appear = new AnimatorSet();
		disappear.playTogether(ObjectAnimator.ofFloat(null, View.SCALE_X, 1f, 0f), ObjectAnimator.ofFloat(null, View.SCALE_Y, 1f, 0f));
		appear.playTogether(ObjectAnimator.ofFloat(null, View.SCALE_X, 0f, 1f), ObjectAnimator.ofFloat(null, View.SCALE_Y, 0f, 1f));
		disappear.setInterpolator(new DecelerateInterpolator());
		appear.setInterpolator(new OvershootInterpolator(3f));
		lt.setAnimator(LayoutTransition.DISAPPEARING, disappear);
		lt.setAnimator(LayoutTransition.APPEARING, appear);
		lt.setStartDelay(LayoutTransition.APPEARING, 0);
		lt.setDuration(250);
		((ViewGroup) root.findViewById(R.id.root)).setLayoutTransition(lt);
	}


	public int getNotificationsCount() {
		return notificationsCount;
	}


	public void setNotificationsCount(final int count) {
		this.notificationsCount = count;
		textLabel.setText(String.valueOf(count));
		textLabel.setVisibility(count > 0 ? VISIBLE : GONE);

		if (count != 0)
			toggleVisibility(true);
	}


	@Override
	public void setOnClickListener(final OnClickListener l) {
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(final View v) {
				l.onClick(RefreshButton.this);
			}
		});
	}


	public void toggleProgress(boolean enable) {
		L.v("progress "+enable);
		if (enable == rotating && enable)
			return;

		rotating = enable;
		if (enable) {
			button.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.progress_rotation));
			toggleVisibility(true);
		} else {
			button.clearAnimation();
			toggleVisibility(false);
		}

	}


	public boolean isProgressEnabled() {
		return rotating;
	}


	private void toggleVisibility(boolean enabled) {
		//root.findViewById(R.id.root).setVisibility(enabled ? VISIBLE : GONE);
	}
}
