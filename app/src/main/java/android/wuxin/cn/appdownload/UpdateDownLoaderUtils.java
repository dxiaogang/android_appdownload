package android.wuxin.cn.appdownload;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;

import java.io.File;
import java.text.DecimalFormat;

/**
 * 类名：UpdateDownLoaderUtils
 * 
 * @author wuxin<br/>
 */
public class UpdateDownLoaderUtils {
	public static final String KEY_NAME_DOWNLOAD_ID = "download_id";
	/**
	 * apk 存储文件名
	 */
	public static String download_filename = "wuxin";
	/**
	 * apk 存储文件夹名
	 */
	public static String download_floder_name = "cn.wuxin.android";
	private static DownloadChangeObserver download_observer;
	private static DownloadManagerPro download_manager_pro;
	private static long download_id = 0;
	private DownloadManager mDownloadManager;
	private  Context mContext;
	private String mDescription;
	private String mDescriptionTitle;
	private boolean mOnlyOneTask;
	private int mVisibilityVisibleNotifyCompleted = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

	public UpdateDownLoaderUtils(Context context) {
		super();
		this.mContext = context;
	}

	public static final UpdateDownLoaderUtils getInstance(Context context) {
		return new UpdateDownLoaderUtils(context);
	}

	/**
	 * 开启下载
	 * 
	 * @param apk_urls
	 */
	public void openUpdateDownLoader(String apk_urls) {
		mDownloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
		download_manager_pro = new DownloadManagerPro(mDownloadManager);
		download_id = getLong();
		download_observer = new DownloadChangeObserver(null);
		mContext.getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, download_observer);
		downLoaderStartAndSetNotifycation(apk_urls);
	}

	private long getLong() {
		Long downloadId = mContext.getSharedPreferences("downloadConfig",
				Context.MODE_PRIVATE).getLong(KEY_NAME_DOWNLOAD_ID, 0);
		return downloadId;
	}

	private void putLong() {
		mContext.getSharedPreferences("downloadConfig", Context.MODE_PRIVATE)
				.edit().putLong(KEY_NAME_DOWNLOAD_ID, download_id).commit();
	}

	/**
	 * 开启通知栏
	 * 
	 * @param apk_urls
	 */
	private void downLoaderStartAndSetNotifycation(String apk_urls) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apk_urls));
		if (download_id > 0 && mOnlyOneTask) {
			mDownloadManager.remove(download_id);
		}
		request.setDestinationInExternalPublicDir(download_floder_name,download_filename);
		request.setTitle(mDescriptionTitle);
		request.setDescription(mDescription);
		request.setNotificationVisibility(mVisibilityVisibleNotifyCompleted);
		request.setVisibleInDownloadsUi(false);
		request.setMimeType("application/vnd.android.package-archive");
		download_id = mDownloadManager.enqueue(request);
		putLong();
	}

	/**
	 * 获取downloadId 如果downloadId大于0说明已经下载
	 */
	public long getDownloadId() {
		download_id = getLong();
		return download_id;
	}

	/**
	 * 是否需要删除上一次任务
	 * 
	 * @param onlyOneTask
	 *            true->删除 false->不删除
	 */
	public void setOnlyOneTask(boolean onlyOneTask) {
		this.mOnlyOneTask = onlyOneTask;
	}

	/**
	 * 暂停下载
	 * 
	 * @param ids
	 * @return -1 表示出现异常或者没有这个方法
	 */
	public int pauseDownload(long... ids) {
		if (mDownloadManager == null)
			return -1;
		return download_manager_pro.pauseDownload(ids);
	}

	/**
	 * 取消当前任务
	 * 
	 * @param ids
	 *            下载编号
	 * @return 实际取消的编号
	 */
	public int cancelCurrentTask(long... ids) {
		if (mDownloadManager == null)
			return 0;
		return mDownloadManager.remove(ids);
	}

	/**
	 * 恢复下载
	 * 
	 * @param ids
	 * @return-1 表示出现异常或者没有这个方法
	 */
	public int resumeDownload(long... ids) {
		if (mDownloadManager == null)
			return -1;
		return download_manager_pro.resumeDownload(ids);
	}

	/**
	 * 设置存储文件夹的名字
	 * 
	 * @param download_floder_name
	 */
	public static void setDownloadFloderName(String download_floder_name) {
		UpdateDownLoaderUtils.download_floder_name = download_floder_name;
	}

	/**
	 * 设置是否有通知栏</br>
	 * 如果要取消通知栏，需要添加权限android.permission.DOWNLOAD_WITHOUT_NOTIFICATION
	 * 
	 * @param visibilityVisibleNotifyCompleted
	 *            </br> DownloadManager.Request.
	 *            VISIBILITY_VISIBLE_NOTIFY_COMPLETED表示下载完成后显示通知栏提示</br>
	 *            DownloadManager.Request.VISIBILITY_HIDDEN反之，需要添加权限
	 */
	public void setVisibilityVisibleNotifyCompleted (int visibilityVisibleNotifyCompleted) {
		this.mVisibilityVisibleNotifyCompleted = visibilityVisibleNotifyCompleted;
	}

	/**
	 * 安装 App
	 * 
	 * @param filePath
	 * @return 是否存在该 Apk
	 */
	public boolean install(String filePath) {
		Intent i = new Intent(Intent.ACTION_VIEW);
		File file = new File(filePath);
		if (file != null && file.length() > 0 && file.exists() && file.isFile()) {
			i.setDataAndType(Uri.parse("file://" + filePath),
					"application/vnd.android.package-archive");
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			mContext.startActivity(i);
			android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		}
		return false;
	}

	/**
	 * 获取下载改变 类名：DownloadChangeObserver
	 * 
	 * @author wuxin<br/>
	 */
	class DownloadChangeObserver extends ContentObserver {

		public DownloadChangeObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			updateView();
		}

	}

	public static class CompleteReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			/**
			 * 获取安装成功的 apk download_id，然后安装
			 **/
			long completeDownloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (completeDownloadId == download_id) {
				if (download_manager_pro.getStatusById(download_id) == DownloadManager.STATUS_SUCCESSFUL) {
					String apkFilePath = new StringBuilder(Environment
							.getExternalStorageDirectory().getAbsolutePath())
							.append(File.separator)
							.append(download_floder_name)
							.append(File.separator).append(download_filename)
							.toString();
					UpdateDownLoaderUtils.downloaderCallbackListener
							.downloadComplete(apkFilePath);
				}
			}
			unregisterContentObserver(context);
		}
	};

	/**
	 * 设置文件名，注意，文件名已经默认添加.apk 后缀
	 * 
	 * @param download_filename
	 *            文件名
	 */
	public void setDownload_filename(String download_filename) {
		if (TextUtils.isEmpty(download_filename)) {
			download_filename = System.currentTimeMillis() + ".apk";
		} else {
			UpdateDownLoaderUtils.download_filename = download_filename+ ".apk";
		}
	}

	public static void unregisterContentObserver(Context context) {
		if (context != null && download_observer != null) {
			context.getContentResolver().unregisterContentObserver(
					download_observer);
		}
	}

	/**
	 * 设置通知栏描述
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.mDescription = description;
	}

	/**
	 * 设置通知栏头
	 * 
	 * @param descriptionTitle
	 */
	public void setDescriptionTitle(String descriptionTitle) {
		this.mDescriptionTitle = descriptionTitle;
	}
	
	static final DecimalFormat DOUBLE_DECIMAL_FORMAT = new DecimalFormat("0.##");

	public static final int MB_2_BYTE = 1024 * 1024;
	public static final int KB_2_BYTE = 1024;

	/**
	 * @param size
	 * @return
	 */
	public static CharSequence getAppSize(long size) {
		if (size <= 0) {
			return "0M";
		}

		if (size >= MB_2_BYTE) {
			return new StringBuilder(16).append(
					DOUBLE_DECIMAL_FORMAT.format((double) size / MB_2_BYTE))
					.append("M");
		} else if (size >= KB_2_BYTE) {
			return new StringBuilder(16).append(
					DOUBLE_DECIMAL_FORMAT.format((double) size / KB_2_BYTE))
					.append("K");
		} else {
			return size + "B";
		}
	}
	
	/**
	 * @param progress
	 * @param max
	 * @return
	 */
	public static String getNotiPercent(long progress, long max) {
		int rate = 0;
		if (progress <= 0 || max <= 0) {
			rate = 0;
		} else if (progress > max) {
			rate = 100;
		} else {
			rate = (int) ((double) progress / max * 100);
		}
		return new StringBuilder(16).append(rate).append("%").toString();
	}

	private void updateView() {
		int[] bytesAndStatus = download_manager_pro.getBytesAndStatus(download_id);
		UpdateDownLoaderUtils.downloaderCallbackListener.downloadSizeChange(bytesAndStatus[0], bytesAndStatus[1], bytesAndStatus[2]);
	}

	private static DownloaderCallbackListener downloaderCallbackListener;

	/**
	 * 设置下载监听
	 * 
	 * @param downloaderCallbackListener
	 */
	public void setDownloaderCallbackListener(
			DownloaderCallbackListener downloaderCallbackListener) {
		UpdateDownLoaderUtils.downloaderCallbackListener = downloaderCallbackListener;
	}

	/**
	 * 类名：DownloaderCallbackListener
	 * 
	 * @author wuxin<br/>
	 */
	interface DownloaderCallbackListener {
		/**
		 *  下载完成的监听
		 * @param apkFilePath
		 */
		void downloadComplete (String apkFilePath);

		/**
		 * 下载大小,状态改变监听
		 * @param progress
		 * @param max
		 * @param state
		 */
		void downloadSizeChange (int progress, int max, int state);

	}
}
