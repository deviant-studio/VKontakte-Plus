package ds.vkplus.ui.view;

import ds.vkplus.App;
import ds.vkplus.model.PhotoData;
import ds.vkplus.utils.L;
import ds.vkplus.utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class LayoutUtils {

	private static float calculateMultiThumbsHeight(final List<Float> list, final float n, final float n2) {
		return (n - n2 * (-1 + list.size())) / sum(list);
	}


	private static int oi(final char c) {
		switch (c) {
			default: {
				return 0;
			}
			case 'n': {
				return 1;
			}
			case 'q': {
				return 2;
			}
		}
	}


	public static void processThumbs(final int areaWidth, final int areaHeight, final List<PhotoData> photos) {

		if (photos.size() != 0) {
			String string = "";
			//final int[] orientations = new int[3];
			final ArrayList<Float> ratios = new ArrayList<>();
			final int size = photos.size();
			boolean bad = false;
			for (final PhotoData photo : photos) {
				final float ratio = photo.ratio;
				if (ratio == -1.0f) {
					bad = true;
				}
				char ratioChar;
				if (ratio > 1.2) {
					ratioChar = 'w';
				} else if (ratio < 0.8) {
					ratioChar = 'n';
				} else {
					ratioChar = 'q';
				}
				string += ratioChar;
				//final int oi = oi(ratioChar);
				//orientations[oi] += 1;
				ratios.add(ratio);
			}
			if (bad) {
				L.e("layout error");
				for (final PhotoData p : photos) {
					p.setViewSize(scale(135.0f), scale(100.0f), false, false);
				}
				photos.get(photos.size() - 1).paddingBottom = true;
				return;
			}
			for (final PhotoData p : photos) {
				p.paddingBottom = false;
			}
			photos.get(photos.size() - 1).paddingBottom = true;
			float ratioAverage;
			if (!ratios.isEmpty()) {
				ratioAverage = sum(ratios) / ratios.size();
			} else {
				ratioAverage = 1.0f;
			}
			final float paddingHorizontal = scale(2.0f);
			final float paddingVertical = scale(2.0f);
			float width;
			float height;
			if (areaWidth > 0) {
				width = areaWidth;
				height = areaHeight;
			} else {
				width = Utils.dp(App.instance(),240);
				height = Utils.dp(App.instance(),160);
			}
			final float areaRatio = width / height;
			if (size == 1) {
				final float viewWidth = Math.min(width, scale(photos.get(0).width));
				if (ratios.get(0) > 0.5) {
					photos.get(0).setViewSize(viewWidth, viewWidth / ratios.get(0), true, false);
					return;
				}
				photos.get(0).setViewSize(viewWidth, 2.0f * viewWidth, true, false);
			} else if (size == 2) {
				if (string.equals("ww") && ratioAverage > 1.4 * areaRatio && ratios.get(1) - ratios.get(0) < 0.2) {
					final float viewWidth = width;
					final float viewHeight = Math.min(viewWidth / ratios.get(0), Math.min(viewWidth / ratios.get(1), (height - paddingVertical) / 2.0f));
					photos.get(0).setViewSize(viewWidth, viewHeight, true, false);
					photos.get(1).setViewSize(viewWidth, viewHeight, false, false);
					return;
				}
				if (string.equals("ww") || string.equals("qq")) {
					final float viewWidth1 = (width - paddingHorizontal) / 2.0f;
					final float viewHeight = Math.min(viewWidth1 / ratios.get(0), Math.min(viewWidth1 / ratios.get(1), height));
					photos.get(0).setViewSize(viewWidth1, viewHeight, false, false);
					photos.get(1).setViewSize(viewWidth1, viewHeight, false, false);
					return;
				}
				final float viewWidth2 = (width - paddingHorizontal) / ratios.get(1) / (1.0f / ratios.get(0) + 1.0f / ratios.get(1));
				final float viewWidth1 = width - viewWidth2 - paddingHorizontal;
				final float viewHeight = Math.min(height, Math.min(viewWidth2 / ratios.get(0), viewWidth1 / ratios.get(1)));
				photos.get(0).setViewSize(viewWidth2, viewHeight, false, false);
				photos.get(1).setViewSize(viewWidth1, viewHeight, false, false);
			} else if (size == 3) {
				if (string.equals("www")) {
					final float viewHeight1 = Math.min(width / ratios.get(0), 0.66f * (height - paddingVertical));
					photos.get(0).setViewSize(width, viewHeight1, true, false);
					final float viewWidth = (width - paddingHorizontal) / 2.0f;
					final float viewHeight2 = Math.min(height - viewHeight1 - paddingVertical, Math.min(viewWidth / ratios.get(1), viewWidth / ratios.get(2)));
					photos.get(1).setViewSize(viewWidth, viewHeight2, false, false);
					photos.get(2).setViewSize(viewWidth, viewHeight2, false, false);
					return;
				}
				final int totalHeight = (int) height;
				final int viewWidth1 = (int) Math.min(totalHeight * ratios.get(0), 0.75 * (width - paddingHorizontal));
				photos.get(0).setViewSize(viewWidth1, totalHeight, false, false);
				final float viewHeight2 = ratios.get(1) * (height - paddingVertical) / (ratios.get(2) + ratios.get(1));
				final float viewHeight1 = height - viewHeight2 - paddingVertical;
				final float min7 = Math.min(width - viewWidth1 - paddingHorizontal, Math.min(viewHeight2 * ratios.get(2), viewHeight1 * ratios.get(1)));
				photos.get(1).setViewSize(min7, viewHeight1, false, true);
				photos.get(2).setViewSize(min7, viewHeight2, false, true);
			} else if (size == 4) {
				if (string.equals("wwww")) {
					final int viewWidth1 = (int) width;
					final int viewHeight1 = (int) Math.min(viewWidth1 / ratios.get(0), 0.66 * (height - paddingVertical));
					photos.get(0).setViewSize(viewWidth1, viewHeight1, true, false);
					final int subgroupHeight = (int) ((width - 2 * paddingHorizontal) / (ratios.get(1) + ratios.get(2) + ratios.get(3)));
					final int viewWidth2 = (int) (subgroupHeight * ratios.get(1));
					final int viewWidth3 = (int) (subgroupHeight * ratios.get(2));
					final int viewWidth4 = (int) (subgroupHeight * ratios.get(3));
					final int viewHeight2 = (int) Math.min(height - viewHeight1 - paddingVertical, subgroupHeight);
					photos.get(1).setViewSize(viewWidth2, viewHeight2, false, false);
					photos.get(2).setViewSize(viewWidth3, viewHeight2, false, false);
					photos.get(3).setViewSize(viewWidth4, viewHeight2, false, false);
					return;
				}

				final int totalHeight = (int) height;
				final int viewWidth1 = (int) Math.min(totalHeight * ratios.get(0), 0.66 * (width - paddingHorizontal));
				photos.get(0).setViewSize(viewWidth1, totalHeight, false, false);
				final int n28 = (int) ((height - 2.0f * paddingVertical) / (1.0f / ratios.get(1) + 1.0f / ratios.get(2) + 1.0f / ratios.get(3)));
				final int viewHeight2 = (int) (n28 / ratios.get(1));
				final int viewHeight3 = (int) (n28 / ratios.get(2));
				final int viewHeight4 = (int) (paddingVertical + n28 / ratios.get(3));
				final int viewWidth2 = (int) Math.min(width - viewWidth1 - paddingHorizontal, n28);
				photos.get(1).setViewSize(viewWidth2, viewHeight2, false, true);
				photos.get(2).setViewSize(viewWidth2, viewHeight3, false, true);
				photos.get(3).setViewSize(viewWidth2, viewHeight4, false, true);
			} else {
				final ArrayList<Float> horRatios = new ArrayList<Float>();
				if (ratioAverage > 1.1) {
					for (final Float ratio : ratios) {
						horRatios.add(Math.max(1.0f, ratio));
					}
				} else {
					for (final Float ratio : ratios) {
						horRatios.add(Math.min(1.0f, ratio));
					}
				}
				final HashMap<String, float[]> hashMap = new HashMap<>();
				hashMap.put(size + "", new float[] {calculateMultiThumbsHeight(horRatios, width, paddingHorizontal)});
				for (int i = 1; i <= size - 1; ++i) {
					hashMap.put(i + "," + (size - i),
							new float[] {calculateMultiThumbsHeight(horRatios.subList(0, i), width, paddingHorizontal),
									calculateMultiThumbsHeight(horRatios.subList(i, horRatios.size()), width, paddingHorizontal)}
					);
				}
				for (int j = 1; j <= size - 2; ++j) {
					for (int k = 1; k <= -1 + (size - j); ++k) {
						hashMap.put(j + "," + k + "," + (size - j - k),
								new float[] {calculateMultiThumbsHeight(horRatios.subList(0, j), width, paddingHorizontal),
										calculateMultiThumbsHeight(horRatios.subList(j, j + k), width, paddingHorizontal),
										calculateMultiThumbsHeight(horRatios.subList(j + k, horRatios.size()), width, paddingHorizontal)}
						);
					}
				}
				String s = null;
				float n33 = 0.0f;
				for (final String s2 : hashMap.keySet()) {
					final float[] array2 = hashMap.get(s2);
					float totalVertPadding = paddingVertical * (array2.length - 1);
					for (int length = array2.length, l = 0; l < length; ++l) {
						totalVertPadding += array2[l];
					}
					float abs = Math.abs(totalVertPadding - height);
					if (s2.indexOf(44) != -1) {
						final String[] split = s2.split(",");
						if (Integer.parseInt(split[0]) > Integer.parseInt(split[1]) || (split.length > 2 && Integer.parseInt(split[1]) > Integer.parseInt(split[2]))) {
							abs *= (float) 1.1;
						}
					}
					if (s == null || abs < n33) {
						s = s2;
						n33 = abs;
					}
				}
				final ArrayList<PhotoData> photosCopy = new ArrayList<>(photos);
				final ArrayList<Float> horRatiosCopy = new ArrayList<>(horRatios);
				final String[] split2 = s.split(",");
				final float[] array3 = hashMap.get(s);
				int index = 0;
				for (final String aSplit2 : split2) {
					final int int1 = Integer.parseInt(aSplit2);
					final ArrayList<PhotoData> tempPhotos = new ArrayList<>();
					for (int i = 0; i < int1; ++i) {
						tempPhotos.add(photosCopy.remove(0));
					}
					final float val = array3[index];
					++index;
					final int n40 = -1 + tempPhotos.size();
					for (int j = 0; j < tempPhotos.size(); ++j) {
						tempPhotos.get(j).setViewSize((int) (val * horRatiosCopy.remove(0)), (int) val, j == n40, false);
					}
				}
			}
		}
	}


	private static float sum(final List<Float> list) {
		float n = 0.0f;
		final Iterator<Float> iterator = list.iterator();
		while (iterator.hasNext()) {
			n += iterator.next();
		}
		return n;
	}


	public static int scale(float paramFloat) {
		float displayDensity = App.instance().getResources().getDisplayMetrics().density;
		return Math.round(paramFloat * displayDensity);
	}
}