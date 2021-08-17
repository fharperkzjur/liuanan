package net.oschina.gitapp.ui.baseactivity;

import java.lang.reflect.Field;

import net.oschina.gitapp.AppContext;
import net.oschina.gitapp.interfaces.ActivityHelperInterface;
import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.ViewConfiguration;

/**
 * 类名 BaseActionBarActivity.java</br>
 * 创建日期 2014年4月20日</br>
 * @author LeonLee (http://my.oschina.net/lendylongli)</br>
 * Email lendylongli@gmail.com</br>
 * 更新时间 2014年4月20日 下午11:39:14</br>
 * 最后更新者 LeonLee</br>
 * 
 * 说明 ActionBar的基类
 */
public class BaseActionBarActivity extends ActionBarActivity 
	implements ActivityHelperInterface{

	ActivityHelper mHelper = new ActivityHelper(this);
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHelper.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	
	@Override
	public void onPause() {
		super.onPause();
	}
	
	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
		mHelper.onAttachedToWindow();
	}
	
	@Override
	public void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		mHelper.onDetachedFromWindow();
	}
	
	@Override
	public AppContext getOsChinaApplication() {
		return mHelper.getOsChinaApplication();
	}

	@Override
	public Activity getActivity() {
		return mHelper.getActivity();
	}
	
	/** 将菜单显示在actionbar上，而不是在底部*/
	protected void requestActionBarMenu() {
		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class
					.getDeclaredField("sHasPermanentMenuKey");

			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception e) {
			// presumably, not relevant
		}
	}
}
