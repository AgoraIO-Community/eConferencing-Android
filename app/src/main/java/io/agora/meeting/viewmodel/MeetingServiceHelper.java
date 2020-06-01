package io.agora.meeting.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.HashMap;
import java.util.List;

import io.agora.base.Callback;
import io.agora.base.ThrowableCallback;
import io.agora.base.ToastManager;
import io.agora.base.network.RetrofitManager;
import io.agora.meeting.BuildConfig;
import io.agora.meeting.MainApplication;
import io.agora.meeting.annotaion.member.Role;
import io.agora.meeting.annotaion.message.ChatType;
import io.agora.meeting.base.BaseCallback;
import io.agora.meeting.data.Me;
import io.agora.meeting.data.Member;
import io.agora.meeting.data.Room;
import io.agora.meeting.data.RoomState;
import io.agora.meeting.data.ShareBoard;
import io.agora.meeting.data.ShareScreen;
import io.agora.meeting.service.MeetingService;
import io.agora.meeting.service.body.req.ApplyReq;
import io.agora.meeting.service.body.req.BoardReq;
import io.agora.meeting.service.body.req.ChatReq;
import io.agora.meeting.service.body.req.InviteReq;
import io.agora.meeting.service.body.req.MemberReq;
import io.agora.meeting.service.body.req.RoomEntryReq;
import io.agora.meeting.service.body.req.RoomReq;
import io.agora.meeting.service.body.req.ScreenReq;
import io.agora.meeting.service.body.res.RoomBoardRes;
import io.agora.sdk.manager.RtcManager;
import io.agora.sdk.manager.RtmManager;
import io.agora.sdk.manager.SdkManager;

public class MeetingServiceHelper {
    public static final int LIMIT = 100;

    private String appId;
    private MeetingService service;
    private MeetingViewModel meetingVM;
    private String nextId;

    public MeetingServiceHelper(MeetingViewModel viewModel) {
        this.appId = MainApplication.getAppId();
        this.service = RetrofitManager.instance().getService(BuildConfig.API_BASE_URL, MeetingService.class);
        this.meetingVM = viewModel;
    }

    public void entryRoom(@NonNull RoomEntryReq req, @NonNull Callback<String> callback) {
        service.roomEntry(appId, req)
                .enqueue(new BaseCallback<>(data -> {
                    RetrofitManager.instance().addHeader("token", data.userToken);
                    callback.onSuccess(data.roomId);
                }));
    }

    public void roomBoard(@NonNull String roomId, @NonNull BaseCallback<RoomBoardRes> callback) {
        service.roomBoard(appId, roomId).enqueue(callback);
    }

    public void getRoomInfo(@NonNull String roomId) {
        service.room(appId, roomId)
                .enqueue(new BaseCallback<>(data -> {
                    // must set room and me before everything
                    meetingVM.room.setValue(new Room(data.room));
                    meetingVM.me.setValue(data.user);

                    meetingVM.updateRoomState(new RoomState(data.room));
                    meetingVM.updateHosts(data.room.hosts);
                    meetingVM.updateShareBoard(new ShareBoard() {{
                        shareBoard = data.room.shareBoard;
                        createBoardUserId = data.room.createBoardUserId;
                        shareBoardUsers = data.room.shareBoardUsers;
                    }});
                    meetingVM.updateShareScreen(new ShareScreen() {{
                        shareScreen = data.room.shareScreen;
                        shareScreenUsers = data.room.shareScreenUsers;
                    }});
                    initAudiences(roomId, meetingVM.getAudiencesValue());
                }));
    }

    public void initAudiences(String roomId, @NonNull List<Member> audiences) {
        service.userPage(appId, roomId, Role.AUDIENCE, nextId, LIMIT)
                .enqueue(new BaseCallback<>(data -> {
                    nextId = data.nextId;
                    if (data.list.size() == 0) {
                        meetingVM.updateAudiences(audiences);
                        joinChannel(meetingVM.room.getValue(), meetingVM.getMeValue());
                        return;
                    }
                    audiences.addAll(data.list);
                    initAudiences(roomId, audiences);
                }));
    }

    public void joinChannel(@Nullable Room room, @Nullable Me me) {
        if (room == null || me == null) return;

        String channelId = room.channelName;
        RtmManager.instance().login(me.rtmToken, me.uid, new ThrowableCallback<Void>() {
            @Override
            public void onSuccess(Void res) {
                RtmManager.instance().joinChannel(new HashMap<String, String>() {{
                    put(SdkManager.CHANNEL_ID, channelId);
                }});
                RtcManager.instance().joinChannel(new HashMap<String, String>() {{
                    put(SdkManager.TOKEN, me.rtcToken);
                    put(SdkManager.CHANNEL_ID, channelId);
                    put(SdkManager.USER_ID, me.getUidStr());
                    put(SdkManager.USER_EXTRA, BuildConfig.EXTRA);
                }});
            }

            @Override
            public void onFailure(Throwable throwable) {
                ToastManager.showShort(throwable.toString());
            }
        });
    }

    public void modifyRoomInfo(@NonNull RoomReq req) {
        service.room(appId, meetingVM.getRoomId(), req)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void modifyMemberInfo(@NonNull String userId, @NonNull MemberReq req) {
        service.roomUser(appId, meetingVM.getRoomId(), userId, req)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void invite(@NonNull String userId, @NonNull InviteReq req) {
        service.invite(appId, meetingVM.getRoomId(), userId, req)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void apply(@NonNull String userId, @NonNull ApplyReq req) {
        service.apply(appId, meetingVM.getRoomId(), userId, req)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void screen(@NonNull String userId, @NonNull ScreenReq req) {
        service.screen(appId, meetingVM.getRoomId(), userId, req)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void board(@NonNull String userId, @NonNull BoardReq req) {
        service.board(appId, meetingVM.getRoomId(), userId, req)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void setHost(@NonNull String userId) {
        service.host(appId, meetingVM.getRoomId(), userId)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }

    public void sendMessage(@NonNull String content) {
        service.roomChat(appId, meetingVM.getRoomId(), new ChatReq() {{
            message = content;
            type = ChatType.TEXT;
        }}).enqueue(new BaseCallback<>(data -> {

        }));
    }

    public void exitRoom(@NonNull String userId) {
        service.roomExit(appId, meetingVM.getRoomId(), userId)
                .enqueue(new BaseCallback<>(data -> {

                }));
    }
}
