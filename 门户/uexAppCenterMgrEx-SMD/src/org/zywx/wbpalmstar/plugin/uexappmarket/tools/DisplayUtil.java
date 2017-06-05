package org.zywx.wbpalmstar.plugin.uexappmarket.tools;

public class DisplayUtil {

	/**
	 * ��pxֵת��Ϊdip��dpֵ����֤�ߴ��С����
	 * 
	 * @param pxValue
	 * @param scale
	 *            ��DisplayMetrics��������density��
	 * @return
	 */
	public static int px2dip(float pxValue, float scale) {
		return (int) (pxValue / scale + 0.5f);
	}

	/**
	 * ��dip��dpֵת��Ϊpxֵ����֤�ߴ��С����
	 * 
	 * @param dipValue
	 * @param scale
	 *            ��DisplayMetrics��������density��
	 * @return
	 */
	public static int dip2px(float dipValue, float scale) {
		return (int) (dipValue * scale + 0.5f);
	}

	/**
	 * ��pxֵת��Ϊspֵ����֤���ִ�С����
	 * 
	 * @param pxValue
	 * @param fontScale
	 *            ��DisplayMetrics��������scaledDensity��
	 * @return
	 */
	public static int px2sp(float pxValue, float fontScale) {
		return (int) (pxValue / fontScale + 0.5f);
	}

	/**
	 * ��spֵת��Ϊpxֵ����֤���ִ�С����
	 * 
	 * @param spValue
	 * @param fontScale
	 *            ��DisplayMetrics��������scaledDensity��
	 * @return
	 */
	public static int sp2px(float spValue, float fontScale) {
		return (int) (spValue * fontScale + 0.5f);
	}
}
