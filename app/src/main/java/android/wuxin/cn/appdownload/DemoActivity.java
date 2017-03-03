package android.wuxin.cn.appdownload;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * 类名：MainActivity  使用请注册广播接收和加上权限
 *
 * @author wuxin<br/>
 */
public class DemoActivity extends Activity implements UpdateDownLoaderUtils.DownloaderCallbackListener {
    private Button download_start;
    private ProgressBar downloadProgress;
    private TextView downloadSize;
    private TextView downloadPrecent;
    private ImageView downloadCancel;
    private UpdateHandler handler;
    private UpdateDownLoaderUtils updateDownLoaderUtils;

    @SuppressLint ("InlinedApi")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new UpdateHandler();
        setContentView(R.layout.activity_main);
        updateDownLoaderUtils = UpdateDownLoaderUtils.getInstance(this);
        updateDownLoaderUtils.setDownloaderCallbackListener(DemoActivity.this);
        updateDownLoaderUtils.setDescription("正在下载App");
        updateDownLoaderUtils.setDescriptionTitle("正在下载App");
        updateDownLoaderUtils.setDownload_filename("wuxin_" + System.currentTimeMillis());
        initView();
        download_start.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick (View v) {
                updateDownLoaderUtils.openUpdateDownLoader("http://www.ichsy.cn/apps/Hui_Jia_You_ichsy.apk");
            }
        });

        downloadCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick (View v) {
                updateDownLoaderUtils.cancelCurrentTask(updateDownLoaderUtils.getDownloadId());
            }
        });
    }

    private void initView () {
        download_start = (Button) findViewById(R.id.download_start);
        downloadCancel = (ImageView) findViewById(R.id.download_cancel);
        downloadProgress = (ProgressBar) findViewById(R.id.download_progress);
        downloadSize = (TextView) findViewById(R.id.download_size);
        downloadPrecent = (TextView) findViewById(R.id.download_precent);
    }

    @SuppressLint ("HandlerLeak")
    private class UpdateHandler extends Handler {

        @Override
        public void handleMessage (Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case 0:
                    int status = (Integer) msg.obj;
                    if (status != DownloadManager.STATUS_FAILED) {
                        downloadProgress.setVisibility(View.VISIBLE);
                        downloadProgress.setMax(0);
                        downloadProgress.setProgress(0);
                        downloadSize.setVisibility(View.VISIBLE);
                        downloadPrecent.setVisibility(View.VISIBLE);
                        downloadCancel.setVisibility(View.VISIBLE);

                        if (msg.arg2 < 0) {
                            downloadProgress.setIndeterminate(true);
                            downloadPrecent.setText("当前已下载：" + "0%");
                            downloadSize.setText("0M/0M");
                        } else {
                            downloadProgress.setIndeterminate(false);
                            downloadProgress.setMax(msg.arg2);
                            downloadProgress.setProgress(msg.arg1);
                            downloadPrecent.setText("当前已下载： " + UpdateDownLoaderUtils.getNotiPercent(msg.arg1, msg.arg2));
                            downloadSize.setText(UpdateDownLoaderUtils.getAppSize(msg.arg1) + "/" + UpdateDownLoaderUtils.getAppSize(msg.arg2));
                        }
                    } else {
                        Log.d("TAG", "下载失败,请检查网络!");
                        downloadProgress.setProgress(0);
                        downloadPrecent.setText("当前已下载： 0%");
                        downloadSize.setText("0K/" + UpdateDownLoaderUtils.getAppSize(msg.arg2));
                    }
                    break;
            }
        }
    }

    @Override
    protected void onResume () {
        super.onResume();
    }

    @Override
    public void downloadComplete (String apkFilePath) {
        updateDownLoaderUtils.install(apkFilePath);
    }

    @Override
    public void downloadSizeChange (int progress, int max, int state) {
        handler.sendMessage(handler.obtainMessage(0, progress, max, state));
    }
}
