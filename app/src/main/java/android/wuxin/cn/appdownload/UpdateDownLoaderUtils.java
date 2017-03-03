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
	public static final String KEY_NAME_DOWNLOAD_ID = "downloadId";
	/**
	 * apk 存储文件名
	 */
	public static String download_filename = "wuxin";
	/**
	 * apk 存储文件夹名
	 */
	public static String download_floder_name = "cn.wuxin.android";
	private static DownloadChangeObserver downloadObserver;
	private DownloadManager downloadManager;
	private static DownloadManagerPro downloadManagerPro;
	private static long downloadId = 0;
	private  Context context;
	private String description;
	private String descriptionTitle;
	private boolean onlyOneTask;
	private int notifycationVisible = DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED;

	public UpdateDownLoaderUtils(Context context) {
		super();
		this.context = context;
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
		downloadManager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
		downloadManagerPro = new DownloadManagerPro(downloadManager);
		downloadId = getLong();
		downloadObserver = new DownloadChangeObserver(null);
		context.getContentResolver().registerContentObserver(DownloadManagerPro.CONTENT_URI, true, downloadObserver);
		downLoaderStartAndSetNotifycation(apk_urls);
	}

	private long getLong() {
		Long downloadId = context.getSharedPreferences("downloadConfig",
				Context.MODE_PRIVATE).getLong(KEY_NAME_DOWNLOAD_ID, 0);
		return downloadId;
	}

	private void putLong() {
		context.getSharedPreferences("downloadConfig", Context.MODE_PRIVATE)
				.edit().putLong(KEY_NAME_DOWNLOAD_ID, downloadId).commit();
	}

	/**
	 * 开启通知栏
	 * 
	 * @param apk_urls
	 */
	private void downLoaderStartAndSetNotifycation(String apk_urls) {
		DownloadManager.Request request = new DownloadManager.Request(Uri.parse(apk_urls));
		if (downloadId > 0 && onlyOneTask) {
			downloadManager.remove(downloadId);
		}
		request.setDestinationInExternalPublicDir(download_floder_name,download_filename);
		request.setTitle(descriptionTitle);
		request.setDescription(description);
		request.setNotificationVisibility(notifycationVisible);
		request.setVisibleInDownloadsUi(false);
		request.setMimeType("application/vnd.android.package-archive");
		downloadId = downloadManager.enqueue(request);
		putLong();
	}

	/**
	 * 获取downloadId 如果downloadId大于0说明已经下载
	 */
	public long getDownloadId() {
		downloadId = getLong();
		return downloadId;
	}

	/**
	 * 是否需要删除上一次任务
	 * 
	 * @param onlyOneTask
	 *            true->删除 false->不删除
	 */
	public void setOnlyOneTask(boolean onlyOneTask) {
		this.onlyOneTask = onlyOneTask;
	}

	/**
	 * 暂停下载
	 * 
	 * @param ids
	 * @return -1 表示出现异常或者没有这个方法
	 */
	public int pauseDownload(long... ids) {
		if (downloadManager == null)
			return -1;
		return downloadManagerPro.pauseDownload(ids);
	}

	/**
	 * 取消当前任务
	 * 
	 * @param ids
	 *            下载编号
	 * @return 实际取消的编号
	 */
	public int cancelCurrentTask(long... ids) {
		if (downloadManager == null)
			return 0;
		return downloadManager.remove(ids);
	}

	/**
	 * 恢复下载
	 * 
	 * @param ids
	 * @return-1 表示出现异常或者没有这个方法
	 */
	public int resumeDownload(long... ids) {
		if (downloadManager == null)
			return -1;
		return downloadManagerPro.resumeDownload(ids);
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
	 * @param notifycationVisible
	 *            </br> DownloadManager.Request.
	 *            VISIBILITY_VISIBLE_NOTIFY_COMPLETED表示下载完成后显示通知栏提示</br>
	 *            DownloadManager.Request.VISIBILITY_HIDDEN反之，需要添加权限
	 */
	public void setNotifycationVisible(int notifycationVisible) {
		this.notifycationVisible = notifycationVisible;
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
			context.startActivity(i);
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
			 * 获取安装成功的 apk downloadId，然后安装
			 **/
			long completeDownloadId = intent.getLongExtra(
					DownloadManager.EXTRA_DOWNLOAD_ID, -1);
			if (completeDownloadId == downloadId) {
				if (downloadManagerPro.getStatusById(downloadId) == DownloadManager.STATUS_SUCCESSFUL) {
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
		if (context != null && downloadObserver != null) {
			context.getContentResolver().unregisterContentObserver(
					downloadObserver);
		}
	}

	/**
	 * 设置通知栏描述
	 * 
	 * @param description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * 设置通知栏头
	 * 
	 * @param descriptionTitle
	 */
	public void setDescriptionTitle(String descriptionTitle) {
		this.descriptionTitle = descriptionTitle;
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
		int[] bytesAndStatus = downloadManagerPro.getBytesAndStatus(downloadId);
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
		abstract void downloadComplete (String apkFilePath);

		/**
		 * 下载大小,状态改变监听
		 * @param progress
		 * @param max
		 * @param state
		 */
		abstract void downloadSizeChange (int progress, int max, int state);

	};
}
