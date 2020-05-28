package io.agora.meeting.viewmodel;

import androidx.annotation.IntRange;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import io.agora.meeting.annotaion.room.AudioRoute;
import io.agora.sdk.listener.RtcEventListener;
import io.agora.sdk.manager.RtcManager;

public class RtcViewModel extends ViewModel {
    public final MutableLiveData<Integer> audioRoute = new MutableLiveData<>();

    private RtcEventListener rtcEventListener = new RtcEventHandler(this);

    public RtcViewModel() {
        RtcManager.instance().registerListener(rtcEventListener);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        RtcManager.instance().unregisterListener(rtcEventListener);
    }

    public void switchAudioRoute() {
        Integer audioRoute = this.audioRoute.getValue();
        if (audioRoute == null) return;

        if (audioRoute == AudioRoute.EARPIECE) {
            RtcManager.instance().setEnableSpeakerphone(true);
        } else if (audioRoute == AudioRoute.SPEAKER) {
            RtcManager.instance().setEnableSpeakerphone(false);
        }
    }

    public void switchCamera() {
        RtcManager.instance().switchCamera();
    }

    public void muteLocalAudioStream(boolean mute) {
        RtcManager.instance().muteLocalAudioStream(mute);
    }

    public void muteLocalVideoStream(boolean mute) {
        RtcManager.instance().muteLocalVideoStream(mute);
    }

    public void rate(@IntRange(from = 1, to = 5) int rating) {
        RtcManager.instance().rate(rating, null);
    }
}
