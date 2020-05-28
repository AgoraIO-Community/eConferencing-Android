package io.agora.meeting.adapter;

import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.databinding.BindingAdapter;

import io.agora.rtc.video.VideoCanvas;
import io.agora.sdk.manager.RtcManager;

public class BindingAdapters {
    @BindingAdapter("android:layout_alignParentEnd")
    public static void bindAlignParentEnd(View view, Boolean alignParentEnd) {
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();

        if (layoutParams instanceof RelativeLayout.LayoutParams) {
            if (alignParentEnd) {
                ((RelativeLayout.LayoutParams) layoutParams).addRule(RelativeLayout.ALIGN_PARENT_END);
            } else {
                ((RelativeLayout.LayoutParams) layoutParams).removeRule(RelativeLayout.ALIGN_PARENT_END);
            }
            view.setLayoutParams(layoutParams);
        }
    }

    @BindingAdapter("isGone")
    public static void bindIsGone(View view, Boolean isGone) {
        view.setVisibility(isGone ? View.GONE : View.VISIBLE);
    }

    @BindingAdapter("activated")
    public static void bindActivated(View view, Boolean activated) {
        view.setActivated(activated);
    }

    @BindingAdapter({
            "video_enable",
            "video_uid",
            "video_overlay"
    })
    public static void bindVideo(View view, Boolean enable, Integer uid, Boolean overlay) {
        if (view instanceof ViewGroup) {
            if (enable) {
                SurfaceView surfaceView = RtcManager.instance().createRendererView(view.getContext());
                surfaceView.setZOrderMediaOverlay(overlay);
                if (uid == 0) {
                    RtcManager.instance().setupLocalVideo(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN);
                } else {
                    RtcManager.instance().setupRemoteVideo(surfaceView, VideoCanvas.RENDER_MODE_HIDDEN, uid);
                }
                ((ViewGroup) view).removeAllViews();
                ((ViewGroup) view).addView(surfaceView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            } else {
                if (uid == 0) {
                    RtcManager.instance().setupLocalVideo(null, VideoCanvas.RENDER_MODE_HIDDEN);
                } else {
                    RtcManager.instance().setupRemoteVideo(null, VideoCanvas.RENDER_MODE_HIDDEN, uid);
                }
                ((ViewGroup) view).removeAllViews();
            }
        }
    }
}
