package com.dalong.recordlib.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Rect;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.WindowManager;

/**
 * android工具类
 * 
 * @author Aaron
 * 
 */
public class AndroidUtil {

	/**
	 * 获取设备编号
	 * 
	 * @param context
	 * @return
	 */
	public static String getDeviceId(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}
	
	/**
	 * 获得屏幕宽度
	 * @param context
	 * @return
	 */
	public static int getScreenWidth(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);

		int width = wm.getDefaultDisplay().getWidth();// 屏幕宽度
		return width;
	}
	
	/**
	 * 获得屏幕高度
	 * @param context
	 * @return
	 */
	public static int getScreenHeight(Context context) {
		WindowManager wm = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		
		int height = wm.getDefaultDisplay().getHeight();// 屏幕高度
		return height;
	}

	/**
	 * 获取手机状态栏
	 * @param context
	 * @return
     */
	public static int getPandaStatusBarHeight(Context context){
		int statusBarHeight1 = -1;
		//获取status_bar_height资源的ID
		int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			//根据资源ID获取响应的尺寸值
			statusBarHeight1 = context.getResources().getDimensionPixelSize(resourceId);
		}
		return statusBarHeight1;
	}
	
	/**
	 * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
	 */
	public static int dip2px(Context context, float dpValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (dpValue * scale + 0.5f);
	}

	/**
	 * 根据手机的分辨率从 px(像素) 的单位 转成为 dp
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	/** 
     * 将px值转换为sp值，保证文字大小不变 
     *  
     * @param pxValue 
     *            （DisplayMetrics类中属性scaledDensity）
     * @return 
     */  
    public static int px2sp(Context context, float pxValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (pxValue / fontScale + 0.5f);  
    }  
  
    /** 
     * 将sp值转换为px值，保证文字大小不变 
     *  
     * @param spValue 
     *            （DisplayMetrics类中属性scaledDensity）
     * @return 
     */  
    public static int sp2px(Context context, float spValue) {  
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;  
        return (int) (spValue * fontScale + 0.5f);  
    }  
    
    
    /**
     * 获取状态栏高度
     * @param context
     * @return
     */
    public static final int getStatusBarHeight(Context context){
    	Rect frame = new Rect();
    	((Activity)context).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
    	int statusBarHeight = frame.top;
    	return statusBarHeight;
    }
    
    public static int getVerCode(Context ctx) {
		int verCode = -1;
		try {
			verCode = ctx.getPackageManager().getPackageInfo(
					ctx.getPackageName(), 0).versionCode;
		} catch (NameNotFoundException e) {
			Log.e("", e.getMessage());
		}
		return verCode;
	}

}
