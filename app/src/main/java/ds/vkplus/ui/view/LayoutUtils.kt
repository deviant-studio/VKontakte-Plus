package ds.vkplus.ui.view

import ds.vkplus.App
import ds.vkplus.model.PhotoData
import ds.vkplus.utils.L
import ds.vkplus.utils.Utils

import java.util.ArrayList
import java.util.HashMap

object LayoutUtils {
	
	private fun calculateMultiThumbsHeight(list: List<Float>, n: Float, n2: Float): Float {
		return (n - n2 * (-1 + list.size())) / sum(list)
	}
	
	
	fun processThumbs(areaWidth: Int, areaHeight: Int, photos: List<PhotoData>) {
		
		if (photos.size() != 0) {
			var string = ""
			val ratios = ArrayList<Float>()
			val size = photos.size()
			var bad = false
			for (photo in photos) {
				val ratio = photo.ratio
				if (ratio == -1.0f) {
					bad = true
				}
				val ratioChar: Char
				if (ratio > 1.2) {
					ratioChar = 'w'
				} else if (ratio < 0.8) {
					ratioChar = 'n'
				} else {
					ratioChar = 'q'
				}
				string += ratioChar
				ratios.add(ratio)
			}
			if (bad) {
				L.e("layout error")
				for (p in photos) {
					p.setViewSize(scale(135.0f).toFloat(), scale(100.0f).toFloat(), false, false)
				}
				return
			}
			val ratioAverage: Float
			if (!ratios.isEmpty()) {
				ratioAverage = sum(ratios) / ratios.size()
			} else {
				ratioAverage = 1.0f
			}
			val paddingHorizontal = scale(2f).toFloat()
			val paddingVertical = scale(2f).toFloat()
			val width: Float
			val height: Float
			if (areaWidth > 0) {
				width = areaWidth.toFloat()
				height = areaHeight.toFloat()
			} else {
				width = Utils.dp(App.instance, 240).toFloat()
				height = Utils.dp(App.instance, 160).toFloat()
			}
			val areaRatio = width / height
			if (size == 1) {
				val viewWidth = Math.min(width, scale(photos.get(0).width.toFloat()).toFloat())
				if (ratios.get(0) > 0.5) {
					photos.get(0).setViewSize(viewWidth, viewWidth / ratios.get(0), true, false)
					return
				}
				photos.get(0).setViewSize(viewWidth, 2.0f * viewWidth, true, false)
			} else if (size == 2) {
				if (string == "ww" && ratioAverage > 1.4 * areaRatio && ratios.get(1) - ratios.get(0) < 0.2) {
					val viewWidth = width
					val viewHeight = Math.min(viewWidth / ratios.get(0), Math.min(viewWidth / ratios.get(1), (height - paddingVertical) / 2.0f))
					photos.get(0).setViewSize(viewWidth, viewHeight, true, false)
					photos.get(1).setViewSize(viewWidth, viewHeight, false, false)
					return
				}
				if (string == "ww" || string == "qq") {
					val viewWidth1 = (width - paddingHorizontal) / 2.0f
					val viewHeight = Math.min(viewWidth1 / ratios.get(0), Math.min(viewWidth1 / ratios.get(1), height))
					photos.get(0).setViewSize(viewWidth1, viewHeight, false, false)
					photos.get(1).setViewSize(viewWidth1, viewHeight, false, false)
					return
				}
				val viewWidth2 = (width - paddingHorizontal) / ratios.get(1) / (1.0f / ratios.get(0) + 1.0f / ratios.get(1))
				val viewWidth1 = width - viewWidth2 - paddingHorizontal
				val viewHeight = Math.min(height, Math.min(viewWidth2 / ratios.get(0), viewWidth1 / ratios.get(1)))
				photos.get(0).setViewSize(viewWidth2, viewHeight, false, false)
				photos.get(1).setViewSize(viewWidth1, viewHeight, false, false)
			} else if (size == 3) {
				if (string == "www") {
					val viewHeight1 = Math.min(width / ratios.get(0), 0.66f * (height - paddingVertical))
					photos.get(0).setViewSize(width, viewHeight1, true, false)
					val viewWidth = (width - paddingHorizontal) / 2.0f
					val viewHeight2 = Math.min(height - viewHeight1 - paddingVertical, Math.min(viewWidth / ratios.get(1), viewWidth / ratios.get(2)))
					photos.get(1).setViewSize(viewWidth, viewHeight2, false, false)
					photos.get(2).setViewSize(viewWidth, viewHeight2, false, false)
					return
				}
				val totalHeight = height.toInt()
				val viewWidth1 = Math.min((totalHeight * ratios.get(0)).toDouble(), 0.75 * (width - paddingHorizontal)).toInt()
				photos.get(0).setViewSize(viewWidth1.toFloat(), totalHeight.toFloat(), false, false)
				val viewHeight2 = ratios.get(1) * (height - paddingVertical) / (ratios.get(2) + ratios.get(1))
				val viewHeight1 = height - viewHeight2 - paddingVertical
				val min7 = Math.min(width - viewWidth1.toFloat() - paddingHorizontal, Math.min(viewHeight2 * ratios.get(2), viewHeight1 * ratios.get(1)))
				photos.get(1).setViewSize(min7, viewHeight1, false, true)
				photos.get(2).setViewSize(min7, viewHeight2, false, true)
			} else if (size == 4) {
				if (string == "wwww") {
					val viewWidth1 = width.toInt()
					val viewHeight1 = Math.min((viewWidth1 / ratios.get(0)).toDouble(), 0.66 * (height - paddingVertical)).toInt()
					photos.get(0).setViewSize(viewWidth1.toFloat(), viewHeight1.toFloat(), true, false)
					val subgroupHeight = ((width - 2 * paddingHorizontal) / (ratios.get(1) + ratios.get(2) + ratios.get(3))).toInt()
					val viewWidth2 = (subgroupHeight * ratios.get(1)).toInt()
					val viewWidth3 = (subgroupHeight * ratios.get(2)).toInt()
					val viewWidth4 = (subgroupHeight * ratios.get(3)).toInt()
					val viewHeight2 = Math.min(height - viewHeight1.toFloat() - paddingVertical, subgroupHeight.toFloat()).toInt()
					photos.get(1).setViewSize(viewWidth2.toFloat(), viewHeight2.toFloat(), false, false)
					photos.get(2).setViewSize(viewWidth3.toFloat(), viewHeight2.toFloat(), false, false)
					photos.get(3).setViewSize(viewWidth4.toFloat(), viewHeight2.toFloat(), false, false)
					return
				}
				
				val totalHeight = height.toInt()
				val viewWidth1 = Math.min((totalHeight * ratios.get(0)).toDouble(), 0.66 * (width - paddingHorizontal)).toInt()
				photos.get(0).setViewSize(viewWidth1.toFloat(), totalHeight.toFloat(), false, false)
				val n28 = ((height - 2.0f * paddingVertical) / (1.0f / ratios.get(1) + 1.0f / ratios.get(2) + 1.0f / ratios.get(3))).toInt()
				val viewHeight2 = (n28 / ratios.get(1)).toInt()
				val viewHeight3 = (n28 / ratios.get(2)).toInt()
				val viewHeight4 = (paddingVertical + n28 / ratios.get(3)).toInt()
				val viewWidth2 = Math.min(width - viewWidth1.toFloat() - paddingHorizontal, n28.toFloat()).toInt()
				photos.get(1).setViewSize(viewWidth2.toFloat(), viewHeight2.toFloat(), false, true)
				photos.get(2).setViewSize(viewWidth2.toFloat(), viewHeight3.toFloat(), false, true)
				photos.get(3).setViewSize(viewWidth2.toFloat(), viewHeight4.toFloat(), false, true)
			} else {
				val horRatios = ArrayList<Float>()
				if (ratioAverage > 1.1) {
					for (ratio in ratios) {
						horRatios.add(Math.max(1.0f, ratio))
					}
				} else {
					for (ratio in ratios) {
						horRatios.add(Math.min(1.0f, ratio))
					}
				}
				val hashMap = HashMap<String, FloatArray>()
				hashMap.put("$size", floatArrayOf(calculateMultiThumbsHeight(horRatios, width, paddingHorizontal)))
				for (i in 1..size - 1) {
					hashMap.put("$i,${size - i}",
						floatArrayOf(
							calculateMultiThumbsHeight(horRatios.subList(0, i), width, paddingHorizontal),
							calculateMultiThumbsHeight(horRatios.subList(i, horRatios.size()), width, paddingHorizontal)))
				}
				for (j in 1..size - 2) {
					for (k in 1..-1 + (size - j)) {
						hashMap.put("$j,$k,${size - j - k}",
							floatArrayOf(
								calculateMultiThumbsHeight(horRatios.subList(0, j), width, paddingHorizontal),
								calculateMultiThumbsHeight(horRatios.subList(j, j + k), width, paddingHorizontal),
								calculateMultiThumbsHeight(horRatios.subList(j + k, horRatios.size()), width, paddingHorizontal)))
					}
				}
				var s: String? = null
				var n33 = 0.0f
				for (s2 in hashMap.keySet()) {
					val array2 = hashMap.get(s2)
					var totalVertPadding = paddingVertical * (array2!!.size() - 1)
					val length: Int = array2.size()
					var l = 0
					while (l < length) {
						totalVertPadding += array2[l]
						++l
					}

					var abs = Math.abs(totalVertPadding - height)
					if (s2.indexOf(44.toChar(),0,false) != -1) {
						val split = s2.split(",")
						if (Integer.parseInt(split[0]) > Integer.parseInt(split[1]) || (split.size() > 2 && Integer.parseInt(split[1]) > Integer.parseInt(split[2]))) {
							abs *= 1.1.toFloat()
						}
					}
					if (s == null || abs < n33) {
						s = s2
						n33 = abs
					}
				}
				val photosCopy = ArrayList(photos)
				val horRatiosCopy = ArrayList(horRatios)
				val split2 = s!!.split(",")
				val array3 = hashMap.get(s)
				var index = 0
				for (aSplit2 in split2) {
					val int1 = Integer.parseInt(aSplit2)
					val tempPhotos = ArrayList<PhotoData>()
					for (i in 0..int1 - 1) {
						tempPhotos.add(photosCopy.remove(0))
					}
					val value = array3!![index]
					++index
					val n40 = -1 + tempPhotos.size()
					for (j in tempPhotos.indices) {
						tempPhotos.get(j).setViewSize((value * horRatiosCopy.remove(0)).toInt().toFloat(), value.toInt().toFloat(), j == n40, false)
					}
				}
			}
		}
	}
	
	
	private fun sum(list: List<Float>): Float {
		var n = 0.0f
		for (item in list) {
			n += item
		}
		return n
	}
	
	
	fun scale(paramFloat: Float): Int {
		val displayDensity = App.instance.resources.displayMetrics.density
		return Math.round(paramFloat * displayDensity)
	}

	//fun String.indexOf(char:Int)=
}