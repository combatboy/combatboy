package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

import org.zywx.wbpalmstar.engine.universalex.EUExUtil;
import org.zywx.wbpalmstar.plugin.uexappmarket.bitmap.util.ImageCache.ImageCacheParams;
import org.zywx.wbpalmstar.plugin.uexappmarket.activity.AppMarketMainActivity;
import org.zywx.wbpalmstar.plugin.uexappmarket.bitmap.util.ImageFetcher;

import android.content.Context;
import android.util.DisplayMetrics;

public class ImageFetcherFactory {

	private static ImageFetcher getImageFetcher(Context context,
			ImageFetcher imageFetcher, int imageSize, int loadImageId) {
		if (imageFetcher == null) {
			imageFetcher = newImageFetcher(context, imageSize, loadImageId);
		}
		return imageFetcher;
	}

	public static ImageFetcher newImageFetcher(Context context, int imageSize,
			int loadImageId) {

		ImageCacheParams cacheParams = new ImageCacheParams(context,
				AppMarketMainActivity.TAG);
		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to
													// 25% of
													// app memory
		// The ImageFetcher takes care of loading images into our ImageView
		// children asynchronously
		ImageFetcher imageFethcer = new ImageFetcher(context, imageSize);
		imageFethcer.setLoadingImage(loadImageId);
		imageFethcer.addImageCache(cacheParams);
		imageFethcer.setImageFadeIn(false);
		return imageFethcer;
	}

	public static ImageFetcher getADImageFethcer(Context context) {
		final String adDrawableName = "plugin_appmarket_ex_ad_default";
		return getImageFetcher(context, mADImageFethcer,
				getADImageSize(context),
				EUExUtil.getResDrawableID(adDrawableName));
	}

	public static ImageFetcher getAppImageFetcher(Context context) {
		final String adDrawableName = "plugin_appmarket_ex_app_icon_default";
		return getImageFetcher(context, mAppImageFethcer,
				getADImageSize(context),
				EUExUtil.getResDrawableID(adDrawableName));
	}

	private static int getADImageSize(Context context) {
		return getMaxScreenSize(context);
	}

	private static int getMaxScreenSize(Context context) {
		DisplayMetrics displayMetrics = context.getResources()
				.getDisplayMetrics();
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;
		return width > height ? width : height;
	}

	public static ImageFetcher mADImageFethcer;
	public static ImageFetcher mAppImageFethcer;

}
