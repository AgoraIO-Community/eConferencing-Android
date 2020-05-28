package io.agora.meeting.viewmodel;

import androidx.annotation.NonNull;

import io.agora.meeting.annotaion.room.AudioRoute;
import io.agora.rtc.Constants;
import io.agora.sdk.listener.RtcEventListener;

public class RtcEventHandler extends RtcEventListener {
    private RtcViewModel rtcVM;

    public RtcEventHandler(@NonNull RtcViewModel viewModel) {
        this.rtcVM = viewModel;
    }

    @Override
    public void onAudioRouteChanged(@io.agora.sdk.annotation.AudioRoute int routing) {
        super.onAudioRouteChanged(routing);
        if (routing == Constants.AUDIO_ROUTE_HEADSET
                || routing == Constants.AUDIO_ROUTE_HEADSETNOMIC
                || routing == Constants.AUDIO_ROUTE_HEADSETBLUETOOTH) {
            rtcVM.audioRoute.postValue(AudioRoute.HEADSET);
        } else if (routing == Constants.AUDIO_ROUTE_EARPIECE) {
            rtcVM.audioRoute.postValue(AudioRoute.EARPIECE);
        } else {
            rtcVM.audioRoute.postValue(AudioRoute.SPEAKER);
        }
    }
}
