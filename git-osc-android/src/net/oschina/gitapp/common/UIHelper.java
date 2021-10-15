package net.oschina.gitapp.common;

import java.io.File;
import java.io.IOException;
import java.util.regex.Pattern;

import static net.oschina.gitapp.common.Contanst.*;
import net.oschina.gitapp.AppContext;
import net.oschina.gitapp.AppException;
import net.oschina.gitapp.AppManager;
import net.oschina.gitapp.R;
import net.oschina.gitapp.api.ApiClient;
import net.oschina.gitapp.bean.Commit;
import net.oschina.gitapp.bean.CommitDiff;
import net.oschina.gitapp.bean.Event;
import net.oschina.gitapp.bean.Issue;
import net.oschina.gitapp.bean.Project;
import net.oschina.gitapp.bean.User;
import net.oschina.gitapp.ui.CommitDetailActivity;
import net.oschina.gitapp.ui.CommitFileDetailActivity;
import net.oschina.gitapp.ui.IssueDetailActivity;
import net.oschina.gitapp.ui.IssueEditActivity;
import net.oschina.gitapp.ui.LoginActivity;
import net.oschina.gitapp.ui.MainActivity;
import net.oschina.gitapp.ui.ProjectActivity;
import net.oschina.gitapp.ui.SearchActivity;
import net.oschina.gitapp.ui.MySelfInfoActivity;
import net.oschina.gitapp.ui.UserInfoActivity;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * 应用程序UI工具包：封装UI相关的一些操作
 * 
 * @author liux (http://my.oschina.net/liux)
 * @version 1.0
 * @created 2012-3-21
 */
public class UIHelper {

	/** 表情图片匹配 */
	private static Pattern facePattern = Pattern
			.compile("\\[{1}([0-9]\\d*)\\]{1}");

	/** 全局web样式 */
	public final static String WEB_STYLE =  "<style>* {font-size:14px;line-height:20px;} p {color:#333;} a {color:#3E62A6;} img {max-width:310px;} "
			+ "img.alignleft {float:left;max-width:120px;margin:0 10px 5px 0;border:1px solid #ccc;background:#fff;padding:2px;} "
			+ "a.tag {font-size:15px;text-decoration:none;background-color:#bbd6f3;border-bottom:2px solid #3E6D8E;border-right:2px solid #7F9FB6;color:#284a7b;margin:2px 2px 2px 0;padding:2px 4px;white-space:nowrap;}</style>";

	/**
	 * 发送App异常崩溃报告
	 * 
	 * @param cont
	 * @param crashReport
	 */
	public static void sendAppCrashReport(final Context context,
			final String crashReport) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setIcon(android.R.drawable.ic_dialog_info);
		builder.setCancelable(false);
		builder.setTitle(R.string.app_error);
		builder.setMessage(R.string.app_error_message);
		builder.setPositiveButton(R.string.submit_report,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 发送异常报告
						Intent i = new Intent(Intent.ACTION_SEND);
						// i.setType("text/plain"); //模拟器
						i.setType("message/rfc822"); // 真机
						// 接收错误报告的邮箱地址
						i.putExtra(Intent.EXTRA_EMAIL,
								new String[] { "zhangdeyi@oschina.net" });
						i.putExtra(Intent.EXTRA_SUBJECT,
								"GIT@OSC,Android客户端 - 错误报告");
						i.putExtra(Intent.EXTRA_TEXT, crashReport);
						context.startActivity(Intent.createChooser(i, "发送错误报告"));
						// 退出
						AppManager.getAppManager().AppExit(context);
					}
				});
		builder.setNegativeButton(R.string.sure,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
						// 退出
						AppManager.getAppManager().AppExit(context);
					}
				});
		builder.show();
	}
	
	/**
	 * 点击返回监听事件
	 * 
	 * @param activity
	 * @return
	 */
	public static View.OnClickListener finish(final Activity activity) {
		return new View.OnClickListener() {
			public void onClick(View v) {
				activity.finish();
			}
		};
	}
	
	/**
	 * 弹出Toast消息
	 * 
	 * @param msg
	 */
	public static void ToastMessage(Context cont, String msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, int msg) {
		Toast.makeText(cont, msg, Toast.LENGTH_SHORT).show();
	}

	public static void ToastMessage(Context cont, String msg, int time) {
		Toast.makeText(cont, msg, time).show();
	}
	
	/**
	 * 分析并组合动态的标题
	 * @param author_name 动态作者的名称
	 * @param pAuthor_And_pName 项目的作者和项目名
	 * @param eventTitle 事件的title（Issue或者pr或分支）
	 * @return
	 */
	public static SpannableString parseEventTitle(String author_name, 
			String pAuthor_And_pName, Event event) {
		String title = "";
		String eventTitle = "";
		int action = event.getAction();
		switch (action) {
		case Event.EVENT_TYPE_CREATED:// 创建了issue
			eventTitle = event.getTarget_type();
			if (event.getIssue() != null) { 
				eventTitle = eventTitle + " #" + event.getIssue().getIid();
			}
			title = "在项目 " + pAuthor_And_pName + " 创建了 " + eventTitle;
			break;
		case Event.EVENT_TYPE_UPDATED:// 更新项目
			title = "更新了项目 " + pAuthor_And_pName;
			break;
		case Event.EVENT_TYPE_CLOSED:// 关闭项目
			eventTitle = event.getTarget_type();
			title = "关闭了项目 " + pAuthor_And_pName + " 的" + eventTitle;
			break;
		case Event.EVENT_TYPE_REOPENED:// 重新打开了项目
			eventTitle = event.getTarget_type();
			title = "重新打开了项目 " + pAuthor_And_pName + " 的" + eventTitle;
			break;
		case Event.EVENT_TYPE_PUSHED:// push
			eventTitle = event.getData().getRef().substring(event.getData().getRef().lastIndexOf("/") + 1);
			title = "推送到了项目 " + pAuthor_And_pName + " 的 " + eventTitle + " 分支";
			break;
		case Event.EVENT_TYPE_COMMENTED:// 评论
			if (event.getNote().getNoteable_type() != null) {
				eventTitle = event.getNote().getNoteable_type();
			} else {
				eventTitle = "Issues";
			}
			title = "评论了项目 " + pAuthor_And_pName + " 的" + eventTitle;
			break;
		case Event.EVENT_TYPE_MERGED:// 合并
			eventTitle = event.getTarget_type();
			title = "接受了项目 " + pAuthor_And_pName + " 的 " + eventTitle;
			break;
		case Event.EVENT_TYPE_JOINED://# User joined project
			title = "加入了项目 " + pAuthor_And_pName;
			break;
		case Event.EVENT_TYPE_LEFT://# User left project
			title = "离开了项目 " + pAuthor_And_pName;
			break;
		case Event.EVENT_TYPE_FORKED:// fork了项目
			title = "Fork了项目 " + pAuthor_And_pName;
			break;
		default:
			title = "更新了动态：";
			break;
		}
		title = author_name + " " + title;
		SpannableString sps = new SpannableString(title);
		
		// 设置用户名字体大小、加粗、高亮
		sps.setSpan(new AbsoluteSizeSpan(14, true), 0, author_name.length(),
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sps.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0,
				author_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sps.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")), 0,
				author_name.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		// 设置项目名字体大小和高亮
		int start = title.indexOf(pAuthor_And_pName);
		int end = start + pAuthor_And_pName.length();
		sps.setSpan(new AbsoluteSizeSpan(14, true), start, end,
			Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		sps.setSpan(new ForegroundColorSpan(Color.parseColor("#0e5986")),
			start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		
		// 设置动态的title字体大小和高亮
		if (!StringUtils.isEmpty(eventTitle) && eventTitle != null) {
			start = title.indexOf(eventTitle);
			end = start + eventTitle.length();
			if (start > 0) {
				sps.setSpan(new AbsoluteSizeSpan(14, true), start, end,
						Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				sps.setSpan(
						new ForegroundColorSpan(Color.parseColor("#0e5986")),
						start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return sps;
	}
	
	/**
	 * 加载显示用户头像
	 * 
	 * @param imgFace
	 * @param faceURL
	 */
	public static void showUserFace(final ImageView imgFace,
			final String faceURL) {
		showLoadImage(imgFace, faceURL,
				imgFace.getContext().getString(R.string.msg_load_userface_fail));
	}

	/**
	 * 加载显示图片
	 * 
	 * @param imgFace
	 * @param faceURL
	 * @param errMsg
	 */
	public static void showLoadImage(final ImageView imgView,
			final String imgURL, final String errMsg) {
		// 读取本地图片
		if (StringUtils.isEmpty(imgURL) || imgURL.endsWith("portrait.gif")) {
			Bitmap bmp = BitmapFactory.decodeResource(imgView.getResources(),
					R.drawable.mini_avatar);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 是否有缓存图片
		final String filename = FileUtils.getFileName(imgURL);
		// Environment.getExternalStorageDirectory();返回/sdcard
		String filepath = imgView.getContext().getFilesDir() + File.separator
				+ filename;
		File file = new File(filepath);
		if (file.exists()) {
			Bitmap bmp = ImageUtils.getBitmap(imgView.getContext(), filename);
			imgView.setImageBitmap(bmp);
			return;
		}

		// 从网络获取&写入图片缓存
		String _errMsg = imgView.getContext().getString(
				R.string.msg_load_image_fail);
		if (!StringUtils.isEmpty(errMsg))
			_errMsg = errMsg;
		final String ErrMsg = _errMsg;
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1 && msg.obj != null) {
					imgView.setImageBitmap((Bitmap) msg.obj);
					try {
						// 写图片缓存
						ImageUtils.saveImage(imgView.getContext(), filename,
								(Bitmap) msg.obj);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					ToastMessage(imgView.getContext(), ErrMsg);
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					Bitmap bmp = ApiClient.getNetBitmap(imgURL);
					msg.what = 1;
					msg.obj = bmp;
				} catch (AppException e) {
					e.printStackTrace();
					msg.what = -1;
					msg.obj = e;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	/**
	 * 清除app缓存
	 * 
	 * @param activity
	 */
	public static void clearAppCache(Activity activity) {
		final AppContext ac = (AppContext) activity.getApplication();
		final Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				if (msg.what == 1) {
					ToastMessage(ac, "缓存清除成功");
				} else {
					ToastMessage(ac, "缓存清除失败");
				}
			}
		};
		new Thread() {
			public void run() {
				Message msg = new Message();
				try {
					ac.clearAppCache();
					msg.what = 1;
				} catch (Exception e) {
					e.printStackTrace();
					msg.what = -1;
				}
				handler.sendMessage(msg);
			}
		}.start();
	}
	
	/**
	 * 显示登录的界面
	 * @param context
	 */
	public static void showLoginActivity(Activity context) {
		Intent intent = new Intent(context, LoginActivity.class);
		context.startActivityForResult(intent, LOGIN_REQUESTCODE);
	}
	
	/**
	 * 显示项目的详情
	 * @param context
	 * @param project
	 * @param projectId
	 */
	public static void showProjectDetail(Context context, Project project, String projectId, int currentItem) {
		Intent intent = new Intent(context, ProjectActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putSerializable(PROJECT, project);
		bundle.putString(PROJECTID, projectId);
		bundle.putInt(CURRENTITEM, currentItem);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	/**
	 * 显示commit详情
	 * @param context
	 * @param project
	 * @param commit
	 */
	public static void showCommitDetail(Context context, Project project, Commit commit) {
		Intent intent = new Intent(context, CommitDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putSerializable(Contanst.PROJECT, project);
		bundle.putSerializable(Contanst.COMMIT, commit);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	/**
	 * 显示commit的Diff详情
	 * @param context
	 * @param project
	 * @param commit
	 * @param commitDiff
	 */
	public static void showCommitDiffFileDetail(Context context, Project project, Commit commit, CommitDiff commitDiff) {
		Intent intent = new Intent(context, CommitFileDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		Bundle bundle = new Bundle();
		bundle.putSerializable(Contanst.PROJECT, project);
		bundle.putSerializable(Contanst.COMMIT, commit);
		bundle.putSerializable(Contanst.COMMITDIFF, commitDiff);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	/**
	 * 显示issue的详情
	 * @param context
	 * @param project
	 * @param issue
	 */
	public static void showIssueDetail(Context context, Project project, Issue issue) {
		Intent intent = new Intent(context, IssueDetailActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
		Bundle bundle = new Bundle();
		bundle.putSerializable(Contanst.PROJECT, project);
		bundle.putSerializable(Contanst.ISSUE, issue);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	/**
	 * 显示issue的编辑或者新增issue的界面
	 * @param context
	 * @param project
	 * @param issue
	 */
	public static void showIssueEditOrCreate(Context context, Project project, Issue issue) {
		Intent intent = new Intent(context, IssueEditActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putSerializable(Contanst.PROJECT, project);
		bundle.putSerializable(Contanst.ISSUE, issue);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
	
	/**
	 * 显示用户信息详情
	 * @param context
	 */
	public static void showMySelfInfoDetail(Context context) {
		Intent intent = new Intent(context, MySelfInfoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	/**
	 * 显示搜索界面
	 * @param context
	 */
	public static void showSearch(Context context) {
		Intent intent = new Intent(context, SearchActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
	
	/**
	 * 显示用户详情界面
	 * @param context
	 * @param user
	 */
	public static void showUserInfoDetail(Context context, User user) {
		Intent intent = new Intent(context, UserInfoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		Bundle bundle = new Bundle();
		bundle.putSerializable(Contanst.USER, user);
		intent.putExtras(bundle);
		context.startActivity(intent);
	}
}
