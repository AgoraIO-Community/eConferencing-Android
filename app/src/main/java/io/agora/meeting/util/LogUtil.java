package io.agora.meeting.util;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import io.agora.base.ToastManager;
import io.agora.base.callback.ThrowableCallback;
import io.agora.log.LogManager;
import io.agora.log.UploadManager;
import io.agora.meeting.BuildConfig;
import io.agora.meeting.MainApplication;
import io.agora.meeting.R;

public class LogUtil {
    public static void upload(@NonNull Activity activity) {
        UploadManager.upload(activity, new UploadManager.UploadParam() {{
            host = BuildConfig.API_BASE_URL;
            if (TextUtils.isEmpty(MainApplication.getAppId())) {
                appId = "default";
            } else {
                appId = MainApplication.getAppId();
            }
            appCode = BuildConfig.CODE;
            appVersion = BuildConfig.VERSION_NAME;
            uploadPath = LogManager.path.getAbsolutePath();
        }}, new ThrowableCallback<String>() {
            @Override
            public void onSuccess(String res) {
                activity.runOnUiThread(() ->
                        new AlertDialog.Builder(activity)
                                .setTitle(R.string.upload_success)
                                .setMessage(res)
                                .setPositiveButton(R.string.know, (dialog, which) -> {
                                    ClipboardManager manager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
                                    if (manager != null) {
                                        manager.setPrimaryClip(ClipData.newPlainText(null, res));
                                        ToastManager.showShort(activity.getString(R.string.clipboard));
                                    }
                                })
                                .show()
                );
            }

            @Override
            public void onFailure(Throwable throwable) {
                ToastManager.showShort(throwable.getMessage());
            }
        });
    }
}
