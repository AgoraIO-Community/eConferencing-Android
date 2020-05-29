package io.agora.meeting.viewmodel;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

import io.agora.base.Callback;
import io.agora.meeting.R;
import io.agora.meeting.annotaion.member.ModuleState;
import io.agora.meeting.annotaion.message.AdminAction;
import io.agora.meeting.annotaion.message.NormalAction;
import io.agora.meeting.annotaion.room.GlobalModuleState;
import io.agora.meeting.annotaion.room.MeetingState;
import io.agora.meeting.annotaion.room.Module;
import io.agora.meeting.base.BaseCallback;
import io.agora.meeting.data.BroadcastMsg;
import io.agora.meeting.data.Me;
import io.agora.meeting.data.Member;
import io.agora.meeting.data.PeerMsg;
import io.agora.meeting.data.Room;
import io.agora.meeting.data.RoomState;
import io.agora.meeting.data.ShareBoard;
import io.agora.meeting.data.ShareScreen;
import io.agora.meeting.service.body.req.ApplyReq;
import io.agora.meeting.service.body.req.BoardReq;
import io.agora.meeting.service.body.req.InviteReq;
import io.agora.meeting.service.body.req.MemberReq;
import io.agora.meeting.service.body.req.RoomEntryReq;
import io.agora.meeting.service.body.req.RoomReq;
import io.agora.meeting.service.body.res.RoomBoardRes;
import io.agora.meeting.util.Events;

public class MeetingViewModel extends ViewModel {
    public final MutableLiveData<Room> room = new MutableLiveData<>();
    public final MutableLiveData<Integer> muteAllChat = new MutableLiveData<>();
    public final MutableLiveData<Integer> muteAllAudio = new MutableLiveData<>();
    public final MutableLiveData<Integer> meetingState = new MutableLiveData<>();

    public final MutableLiveData<Me> me = new MutableLiveData<>();
    public final MutableLiveData<List<Member>> hosts = new MutableLiveData<>();
    public final MutableLiveData<List<Member>> audiences = new MutableLiveData<>();
    public final MediatorLiveData<List<Member>> members = new MediatorLiveData<>();
    public final MediatorLiveData<List<Member>> renders = new MediatorLiveData<>();

    public final MutableLiveData<ShareBoard> shareBoard = new MutableLiveData<>();
    public final MutableLiveData<ShareScreen> shareScreen = new MutableLiveData<>();

    public final MutableLiveData<List<BroadcastMsg.Chat>> chatMsgs = new MutableLiveData<>();
    public final LiveData<List<BroadcastMsg.Chat>> unReadChatMsgs = Transformations.map(chatMsgs, input -> {
        List<BroadcastMsg.Chat> messages = new ArrayList<>();
        for (BroadcastMsg.Chat chat : input) {
            if (!chat.data.isRead) {
                messages.add(chat);
            }
        }
        return messages;
    });

    public final MutableLiveData<List<PeerMsg.Admin>> adminMsgs = new MutableLiveData<>();
    public final MutableLiveData<List<PeerMsg.Normal>> normalMsgs = new MutableLiveData<>();

    private MeetingServiceHelper helper = new MeetingServiceHelper(this);

    public MeetingViewModel() {
        members.addSource(me, me -> initMembers(me, getHostsValue(), getAudiencesValue()));
        members.addSource(hosts, hosts -> initMembers(getMeValue(), hosts, getAudiencesValue()));
        members.addSource(audiences, audiences -> initMembers(getMeValue(), getHostsValue(), audiences));

        renders.addSource(shareBoard, shareBoard -> initRenders(shareBoard, shareScreen.getValue(), getHostsValue(), getAudiencesValue()));
        renders.addSource(shareScreen, shareScreen -> initRenders(shareBoard.getValue(), shareScreen, getHostsValue(), getAudiencesValue()));
        renders.addSource(hosts, hosts -> initRenders(shareBoard.getValue(), shareScreen.getValue(), hosts, getAudiencesValue()));
        renders.addSource(audiences, audiences -> initRenders(shareBoard.getValue(), shareScreen.getValue(), getHostsValue(), audiences));
    }

    private void initMembers(@Nullable Me me, @NonNull List<Member> hosts, @NonNull List<Member> audiences) {
        List<Member> members = new ArrayList<>();
        members.add(me);
        members.addAll(hosts);
        members.addAll(audiences);
        this.members.setValue(members);
    }

    private void initRenders(@Nullable ShareBoard shareBoard, @Nullable ShareScreen shareScreen, @NonNull List<Member> hosts, @NonNull List<Member> audiences) {
        List<Member> renders = new ArrayList<>();
        if (shareBoard != null && shareBoard.isShareBoard()) {
            renders.add(shareBoard.shareBoardUsers.get(0));
        }
        if (shareScreen != null && shareScreen.isShareScreen()) {
            renders.addAll(shareScreen.shareScreenUsers);
        }
        renders.addAll(hosts);
        renders.addAll(audiences);
        this.renders.setValue(renders);
    }

    @Nullable
    public String getRoomId() {
        Room room = this.room.getValue();
        if (room == null) return null;
        return room.roomId;
    }

    @GlobalModuleState
    public int getMuteAllChat() {
        Integer muteAllChat = this.muteAllChat.getValue();
        if (muteAllChat == null) return GlobalModuleState.ENABLE;
        return muteAllChat;
    }

    @GlobalModuleState
    public int getMuteAllAudio() {
        Integer muteAllAudio = this.muteAllAudio.getValue();
        if (muteAllAudio == null) return GlobalModuleState.ENABLE;
        return muteAllAudio;
    }

    @Nullable
    public Me getMeValue() {
        return this.me.getValue();
    }

    @Nullable
    public String getMyUserId() {
        Me me = getMeValue();
        if (me == null) return null;
        return me.userId;
    }

    public boolean isMe(@NonNull String userId) {
        return TextUtils.equals(getMyUserId(), userId);
    }

    public boolean isMe(@Nullable Member member) {
        if (member == null) return false;
        return isMe(member.userId);
    }

    public boolean isHost(@Nullable Member member) {
        if (member == null) return false;
        return member.isHost();
    }

    public boolean isBoardHost(@Nullable Member member) {
        if (member == null) return false;
        return TextUtils.equals(member.userId, getBoardHostId());
    }

    public boolean isBoardSharing() {
        ShareBoard shareBoard = this.shareBoard.getValue();
        if (shareBoard == null) return false;
        return shareBoard.isShareBoard();
    }

    public boolean isGrantBoard(@Nullable Member member) {
        if (member == null) return false;
        if (member.isHost()) return true;
        ShareBoard shareBoard = this.shareBoard.getValue();
        if (shareBoard == null) return false;
        return shareBoard.shareBoardUsers.contains(member);
    }

    public boolean isScreenSharing() {
        ShareScreen shareScreen = this.shareScreen.getValue();
        if (shareScreen == null) return false;
        return shareScreen.isShareScreen();
    }

    public boolean isGrantScreen(@Nullable Member member) {
        if (member == null) return false;
        ShareScreen shareScreen = this.shareScreen.getValue();
        if (shareScreen == null) return false;
        return shareScreen.shareScreenUsers.contains(member);
    }

    @NonNull
    public List<Member> getHostsValue() {
        List<Member> hosts = this.hosts.getValue();
        if (hosts == null) {
            hosts = new ArrayList<>();
        }
        return new ArrayList<>(hosts);
    }

    @NonNull
    public List<Member> getAudiencesValue() {
        List<Member> audiences = this.audiences.getValue();
        if (audiences == null) {
            audiences = new ArrayList<>();
        }
        return new ArrayList<>(audiences);
    }

    @Nullable
    public String getFirstHostId() {
        List<Member> hosts = getHostsValue();
        if (hosts.size() > 0) {
            return hosts.get(0).userId;
        }
        return null;
    }

    @Nullable
    public String getBoardHostId() {
        ShareBoard shareBoard = this.shareBoard.getValue();
        if (shareBoard == null || shareBoard.shareBoard == ModuleState.DISABLE) return null;
        return shareBoard.createBoardUserId;
    }

    @NonNull
    public List<BroadcastMsg.Chat> getChatMsgsValue() {
        List<BroadcastMsg.Chat> messages = this.chatMsgs.getValue();
        if (messages == null) {
            messages = new ArrayList<>();
        }
        return new ArrayList<>(messages);
    }

    @NonNull
    public List<PeerMsg.Admin> getAdminMsgsValue() {
        List<PeerMsg.Admin> adminMsgs = this.adminMsgs.getValue();
        if (adminMsgs == null) {
            adminMsgs = new ArrayList<>();
        }
        return new ArrayList<>(adminMsgs);
    }

    @NonNull
    public List<PeerMsg.Normal> getNormalMsgsValue() {
        List<PeerMsg.Normal> normalMsgs = this.normalMsgs.getValue();
        if (normalMsgs == null) {
            normalMsgs = new ArrayList<>();
        }
        return new ArrayList<>(normalMsgs);
    }

    public void updateRoom(Room room) {
        this.room.postValue(room);
    }

    public void updateRoomState(@NonNull RoomState roomState) {
        Integer muteAllChat = this.muteAllChat.getValue();
        if (!Objects.equals(muteAllChat, roomState.muteAllChat)) {
            if (roomState.muteAllChat != GlobalModuleState.ENABLE) {
                List<Member> audiences = new ArrayList<>();
                for (Member member : getAudiencesValue()) {
                    audiences.add(new Member(member) {{
                        enableChat = ModuleState.DISABLE;
                    }});
                }
                updateAudiences(audiences);
            }
            this.muteAllChat.postValue(roomState.muteAllChat);
        }
        Integer muteAllAudio = this.muteAllAudio.getValue();
        if (!Objects.equals(muteAllAudio, roomState.muteAllAudio)) {
            if (roomState.muteAllAudio != GlobalModuleState.ENABLE) {
                List<Member> audiences = new ArrayList<>();
                for (Member member : getAudiencesValue()) {
                    audiences.add(new Member(member) {{
                        enableAudio = ModuleState.DISABLE;
                    }});
                }
                updateAudiences(audiences);
            }
            this.muteAllAudio.postValue(roomState.muteAllAudio);
            if (!isHost(getMeValue())) { // skip if I'm the host
                Events.AlertEvent.setEvent(roomState.muteAllAudio);
            }
        }
        if (!Objects.equals(roomState.state, meetingState.getValue())) {
            meetingState.postValue(roomState.state);
        }
    }

    public void updateMe(Me me) {
        this.me.postValue(me);
    }

    public void updateMe(Member member) {
        Me me = getMeValue();
        if (me == null) return;
        updateMe(new Me(me, member));
    }

    public void updateHosts(@NonNull List<Member> hosts) {
        hosts.remove(getMeValue());
        this.hosts.postValue(hosts);
    }

    public void updateAudiences(@NonNull List<Member> audiences) {
        audiences.remove(getMeValue());
        this.audiences.postValue(audiences);
    }

    public void updateShareBoard(@NonNull ShareBoard shareBoard) {
        this.shareBoard.postValue(shareBoard);
    }

    public void updateShareScreen(@NonNull ShareScreen shareScreen) {
        this.shareScreen.postValue(shareScreen);
    }

    public void updateChatMsgs(@NonNull List<BroadcastMsg.Chat> messages) {
        this.chatMsgs.postValue(messages);
    }

    public void readChatMsgs() {
        List<BroadcastMsg.Chat> messages = getChatMsgsValue();
        for (BroadcastMsg.Chat message : messages) {
            message.data.isRead = true;
        }
        updateChatMsgs(messages);
    }

    public void updateAdminMsgs(@NonNull List<PeerMsg.Admin> adminMsgs) {
        this.adminMsgs.postValue(adminMsgs);
    }

    public void updateNormalMsgs(@NonNull List<PeerMsg.Normal> normalMsgs) {
        this.normalMsgs.postValue(normalMsgs);
    }

    public void entryRoom(@NonNull RoomEntryReq req, @NonNull Callback<String> callback) {
        helper.entryRoom(req, callback);
    }

    public void roomBoard(String roomId, @NonNull BaseCallback<RoomBoardRes> callback) {
        helper.roomBoard(roomId, callback);
    }

    public void getRoomInfo(@NonNull String roomId) {
        helper.getRoomInfo(roomId);
    }

    public void switchMuteAllAudio(Context context) {
        if (getMuteAllAudio() != GlobalModuleState.ENABLE) {
            muteAllAudio(GlobalModuleState.ENABLE);
            return;
        }
        AtomicBoolean flag = new AtomicBoolean();
        new AlertDialog.Builder(context).setTitle(R.string.mute_all_include_new)
                .setMultiChoiceItems(R.array.mute_all, new boolean[]{false}, (dialog, which, isChecked) -> flag.set(isChecked))
                .setPositiveButton(R.string._continue, (dialog, which) -> {
                    if (flag.get()) {
                        muteAllAudio(GlobalModuleState.CLOSE);
                    } else {
                        muteAllAudio(GlobalModuleState.DISABLE);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    public void muteAllAudio(@GlobalModuleState int moduleState) {
        helper.modifyRoomInfo(new RoomReq() {{
            muteAllAudio = moduleState;
        }});
    }

    public void closeRoom() {
        helper.modifyRoomInfo(new RoomReq() {{
            state = MeetingState.END;
        }});
    }

    public void switchAudioState(Member target, Context context) {
        if (target == null) return;

        boolean targetIsMe = isMe(target);
        if (isHost(getMeValue())) { // I'm host
            if (target.isAudioEnable()) {
                modifyModuleState(target.userId, Module.AUDIO, ModuleState.DISABLE);
            } else {
                if (targetIsMe) {
                    modifyModuleState(target.userId, Module.AUDIO, ModuleState.ENABLE);
                } else {
                    hostInvite(target.userId, Module.AUDIO);
                }
            }
        } else {
            if (targetIsMe) {
                if (target.isAudioEnable()) {
                    modifyModuleState(target.userId, Module.AUDIO, ModuleState.DISABLE);
                } else {
                    if (getMuteAllAudio() == GlobalModuleState.DISABLE) { // must apply to enable audio
                        new AlertDialog.Builder(context)
                                .setMessage(R.string.apply_audio)
                                .setPositiveButton(R.string.yes, (dialog, which) -> audienceApply(getFirstHostId(), Module.AUDIO))
                                .setNegativeButton(R.string.no, null)
                                .show();
                        return;
                    }
                    modifyModuleState(target.userId, Module.AUDIO, ModuleState.ENABLE);
                }
            }
        }
    }

    public void switchVideoState(Member target) {
        if (target == null) return;

        boolean targetIsMe = isMe(target);
        if (isHost(getMeValue())) { // I'm host
            if (target.isVideoEnable()) {
                modifyModuleState(target.userId, Module.VIDEO, ModuleState.DISABLE);
            } else {
                if (targetIsMe) {
                    modifyModuleState(target.userId, Module.VIDEO, ModuleState.ENABLE);
                } else {
                    hostInvite(target.userId, Module.VIDEO);
                }
            }
        } else {
            if (targetIsMe) {
                modifyModuleState(target.userId, Module.VIDEO, target.isVideoEnable() ? ModuleState.DISABLE : ModuleState.ENABLE);
            }
        }
    }

    public void switchBoardState(Member target) {
        if (target == null) return;

        boolean targetIsMe = isMe(target);
        if (isBoardHost(getMeValue())) { // I'm board host
            modifyModuleState(target.userId, Module.BOARD, isGrantBoard(target) ? ModuleState.DISABLE : ModuleState.ENABLE);
        } else {
            if (targetIsMe) {
                if (isBoardSharing()) {
                    if (isGrantBoard(target)) {
                        modifyModuleState(target.userId, Module.BOARD, ModuleState.DISABLE);
                    }
                } else {
                    String boardHostId = getBoardHostId();
                    if (TextUtils.isEmpty(boardHostId)) {
                        modifyModuleState(target.userId, Module.BOARD, ModuleState.ENABLE);
                    } else { // must apply to control whiteboard
                        audienceApply(boardHostId, Module.BOARD);
                    }
                }
            }
        }
    }

    @SuppressLint("SwitchIntDef")
    private void modifyModuleState(String userId, @Module int module, @ModuleState int moduleState) {
        if (module == Module.BOARD) {
            helper.board(userId, new BoardReq() {{
                state = moduleState;
            }});
        } else {
            helper.modifyMemberInfo(userId, new MemberReq() {{
                switch (module) {
                    case Module.AUDIO:
                        enableAudio = moduleState;
                        break;
                    case Module.VIDEO:
                        enableVideo = moduleState;
                        break;
                    case Module.CHAT:
                        enableChat = moduleState;
                        break;
                }
            }});
        }
    }

    private void hostInvite(String userId, @Module int inviteModule) {
        helper.invite(userId, new InviteReq() {{
            module = inviteModule;
            action = AdminAction.INVITE;
        }});
    }

    public void acceptApply(PeerMsg.Normal apply) {
        modifyModuleState(apply.data.userId, apply.data.module, ModuleState.ENABLE);
    }

    public void rejectApply(PeerMsg.Normal apply) {
        helper.invite(apply.data.userId, new InviteReq() {{
            module = apply.data.module;
            action = AdminAction.REJECT_APPLY;
        }});
    }

    private void audienceApply(@Nullable String userId, @Module int applyModule) {
        if (TextUtils.isEmpty(userId)) return;

        helper.apply(userId, new ApplyReq() {{
            module = applyModule;
            action = NormalAction.APPLY;
        }});
    }

    public void acceptInvite(@NonNull PeerMsg.Admin invite) {
        modifyModuleState(getMyUserId(), invite.data.module, ModuleState.ENABLE);
    }

    public void rejectInvite(@NonNull PeerMsg.Admin invite) {
        helper.apply(invite.data.userId, new ApplyReq() {{
            module = invite.data.module;
            action = NormalAction.REJECT_INVITE;
        }});
    }

    public void setHost(@Nullable Member member) {
        if (member == null) return;

        if (isHost(getMeValue())) {
            helper.setHost(member.userId);
        }
    }

    public void sendMessage(@NonNull String content) {
        helper.sendMessage(content);
    }

    public void exitRoom(@Nullable Member member) {
        if (member == null) return;

        helper.exitRoom(member.userId);
    }
}
