package ds.vkplus.ui.view;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class FlowLayout extends ViewGroup {

	private Vector<Integer> lineHeights = new Vector<>();
	private List<LayoutParams> lparams;
	private int measuredHeight = 0;
	public int pwidth;


	public FlowLayout(Context context) {
		super(context);
	}


	public FlowLayout(Context context, AttributeSet attributeSet) {
		super(context, attributeSet);

		pwidth = scale(context, 5);
	}

	@Override
	protected boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
		return layoutParams instanceof LayoutParams;
	}


	@Override
	protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
		return new LayoutParams(scale(getContext(), 2.0f), scale(getContext(), 2.0f));
	}


	public int getFullHeight() {
		int n = 0;
		for (final Integer lineHeight : this.lineHeights) {
			n += lineHeight;
		}
		return n;
	}


	/*
	 * Enabled aggressive block sorting
	 */
	public List<Rect> layoutWithParams(List<LayoutParams> list, int n, int n2) {
		this.lparams = list;
		ArrayList<Rect> arrayList = new ArrayList<>();
		int count = list.size();
		int paddingLeft = this.getPaddingLeft();
		int paddingTop = this.getPaddingTop();
		boolean bl = false;
		this.lineHeights.clear();
		int n6 = 0;
		int n7 = 0;
		int n8 = 0;
		for (LayoutParams lp : list) {
			int n9;
			LayoutParams layoutParams = lp;
			int n10 = layoutParams.width <= 0 ? n : layoutParams.width;
			if ((n9 = layoutParams.height) < 0) {
				throw new IllegalArgumentException("Height should be constant");
			}
			if (bl || paddingLeft + n10 > n + this.pwidth) {
				paddingLeft = this.getPaddingLeft();
				paddingTop += Math.max(n7, n6);
				this.lineHeights.add(Math.max(n7, n6));
				n7 = 0;
				n6 = 0;
			}
			n7 = Math.max(n7, n9 + layoutParams.vertical_spacing);
			if (layoutParams.floating) {
				paddingTop += n9 + layoutParams.vertical_spacing;
				n6 += n9 + layoutParams.vertical_spacing;
				n8 = Math.max(n8, paddingLeft + n10);
			} else {
				paddingLeft += n10 + layoutParams.horizontal_spacing;
				n6 = 0;
			}
			bl = layoutParams.breakAfter;
			n8 = Math.max(n8, paddingLeft - layoutParams.horizontal_spacing);
		}
		if (n7 > 0) {
			this.lineHeights.add(n7);
		}
		int n11 = this.getPaddingLeft();
		int n12 = this.getPaddingTop();
		int n13 = 0;
		boolean bl2 = false;
		boolean bl3 = false;
		int n14 = 0;
		for (LayoutParams lp : list) {
			int n15;
			LayoutParams layoutParams = lp;
			int n16 = layoutParams.width <= 0 ? n : layoutParams.width;
			if ((n15 = layoutParams.height) < 0) {
				throw new IllegalArgumentException("Height should be constant");
			}
			if (!layoutParams.floating && bl2) {
				n12 = n13;
			}
			if (bl3 || n11 + n16 > n + this.pwidth) {
				n11 = this.getPaddingLeft();
				n12 += this.lineHeights.elementAt(n14);
				++n14;
			}
			if (layoutParams.center) {
				n11 = this.getWidth() / 2 - n16 / 2;
			}
			Log.v("vk", "" + n11 + ";" + n12 + ";" + n16 + ";" + n15);
			int n17 = n11 + n16;
			int n18 = n12 + n15;
			Rect rect = new Rect(n11, n12, n17, n18);
			arrayList.add(rect);
			if (layoutParams.floating) {
				if (!bl2) {
					n13 = n12;
					bl2 = true;
				}
				n12 += n15 + layoutParams.vertical_spacing;
			} else {
				n11 += n16 + layoutParams.horizontal_spacing;
				bl2 = false;
			}
			bl3 = layoutParams.breakAfter;
		}
		this.measuredHeight = this.getFullHeight();
		return arrayList;
	}


	/*
	 * Enabled aggressive block sorting
	 */
	protected void onLayout(boolean bl, int n, int n2, int n3, int n4) {
		int n5 = getChildCount();
		int n6 = n3 - n;
		int n7 = getPaddingLeft();
		int n8 = getPaddingTop();
		int n9 = 0;
		boolean bl2 = false;
		boolean bl3 = false;
		int n10 = 0;
		for (int i = 0; i < n5; ++i) {
			View view = this.getChildAt(i);
			if (view.getVisibility() == GONE) continue;
			LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
			int n11 = layoutParams.width <= 0 ? view.getMeasuredWidth() : layoutParams.width;
			int n12 = layoutParams.height <= 0 ? view.getMeasuredHeight() : layoutParams.height;
			if (!layoutParams.floating && bl3) {
				n8 = n10;
			}
			if (bl2 || n7 + n11 > n6 + this.pwidth) {
				n7 = getPaddingLeft();
				n8 += lineHeights.elementAt(n9);
				++n9;
			}
			if (layoutParams.center) {
				n7 = this.getWidth() / 2 - n11 / 2;
			}
			view.layout(n7, n8, n7 + n11, n8 + n12);
			if (layoutParams.floating) {
				if (!bl3) {
					n10 = n8;
					bl3 = true;
				}
				n8 += n12 + layoutParams.vertical_spacing;
			} else {
				n7 += n11 + layoutParams.horizontal_spacing;
				bl3 = false;
			}
			bl2 = layoutParams.breakAfter;
		}
	}


	/*
	 * Enabled aggressive block sorting
	 */
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = MeasureSpec.getSize(widthMeasureSpec) - this.getPaddingLeft() - this.getPaddingRight();
		int height = MeasureSpec.getSize(heightMeasureSpec) - this.getPaddingTop() - this.getPaddingBottom();
		int childsCount = getChildCount();
		int n6 = 0;
		int paddingLeft = getPaddingLeft();
		int paddingTop = getPaddingTop();
		int n9 = 0;
		int n10 = View.MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST ? View.MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST)
				: View.MeasureSpec.makeMeasureSpec(0, 0);
		this.lineHeights.clear();
		boolean bl = false;
		int n11 = 0;
		for (int i = 0; i < Math.max(childsCount, lparams != null ? lparams.size() : 0); ++i) {
			View view;
			if (((view = this.getChildAt(i)) == null || view.getVisibility() == GONE) && view != null) continue;
			LayoutParams layoutParams = view != null ? (LayoutParams) view.getLayoutParams() : this.lparams.get(i);
			if (view != null) {
				int n12 = layoutParams.width <= 0 ? View.MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST)
						: View.MeasureSpec.makeMeasureSpec(layoutParams.width, MeasureSpec.EXACTLY);
				view.measure(n12, n10);
			}
			int n13 = layoutParams.width <= 0 ? (view != null ? view.getMeasuredWidth() : width) : layoutParams.width;
			int n14 = layoutParams.height <= 0 ? (view != null ? view.getMeasuredHeight() : 0) : layoutParams.height;
			if (bl || paddingLeft + n13 > width + this.pwidth) {
				paddingLeft = this.getPaddingLeft();
				paddingTop += Math.max(n6, n11);
				this.lineHeights.add(Math.max(n6, n11));
				n6 = 0;
				n11 = 0;
			}
			n6 = Math.max(n6, n14 + layoutParams.vertical_spacing);
			if (layoutParams.floating) {
				paddingTop += n14 + layoutParams.vertical_spacing;
				n11 += n14 + layoutParams.vertical_spacing;
				n9 = Math.max(n9, paddingLeft + n13);
			} else {
				paddingLeft += n13 + layoutParams.horizontal_spacing;
				n11 = 0;
			}
			bl = layoutParams.breakAfter;
			n9 = Math.max(n9, paddingLeft - layoutParams.horizontal_spacing);
		}
		if (View.MeasureSpec.getMode(heightMeasureSpec) == 0) {
			height = Math.max(n6, n11);
			for (final Integer lineHeight : this.lineHeights) {
				height += lineHeight;
			}
		} else if (View.MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST && paddingTop + n6 < height) {
			height = n6;
			for (final Integer lineHeight : this.lineHeights) {
				height += lineHeight;
			}
		}
		if (View.MeasureSpec.getMode(widthMeasureSpec) == MeasureSpec.EXACTLY) {
			this.setMeasuredDimension(width, height);
			return;
		}
		this.setMeasuredDimension(n9, height);
	}


	public void resetParams() {
		this.lparams = null;
	}


	public static class LayoutParams
			extends ViewGroup.LayoutParams {

		public boolean breakAfter;
		public boolean center;
		public boolean floating;
		public int height;
		public int horizontal_spacing;
		public int vertical_spacing;
		public int width;


		public LayoutParams() {
			super(0, 0);
		}


		public LayoutParams(int n, int n2) {
			super(0, 0);
			this.horizontal_spacing = n;
			this.vertical_spacing = n2;
		}
	}


	public static int scale(Context ctx, float paramFloat) {
		float displayDensity = ctx.getResources().getDisplayMetrics().density;
		return Math.round(paramFloat * displayDensity);
	}
}