package io.agora.meeting.fragment.nav;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.agora.meeting.R;
import io.agora.meeting.adapter.VideoFragmentAdapter;
import io.agora.meeting.annotaion.room.AudioRoute;
import io.agora.meeting.annotaion.room.GlobalModuleState;
import io.agora.meeting.annotaion.room.MeetingState;
import io.agora.meeting.base.BaseFragment;
import io.agora.meeting.data.PeerMsg;
import io.agora.meeting.databinding.FragmentMeetingBinding;
import io.agora.meeting.databinding.LayoutRatingBinding;
import io.agora.meeting.fragment.ActionSheetFragment;
import io.agora.meeting.util.Events;
import io.agora.meeting.util.TimeUtil;
import io.agora.meeting.util.TipsUtil;
import io.agora.meeting.viewmodel.MeetingViewModel;
import io.agora.meeting.viewmodel.RtcViewModel;
import io.agora.meeting.viewmodel.RtmEventHandler;
import io.agora.sdk.manager.RtmManager;
import q.rorbin.badgeview.QBadgeView;

public class MeetingFragment extends BaseFragment<FragmentMeetingBinding> implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationItemView mic, video, chat;
    private QBadgeView qBadgeView;
    private RtcViewModel rtcVM;
    private MeetingViewModel meetingVM;
    private VideoFragmentAdapter adapter;

    private RtmEventHandler rtmEventHandler;
    private OnBackPressedCallback callback = new OnBackPressedCallback(true) {
        @Override
        public void handleOnBackPressed() {
            Context context = requireContext();
            AlertDialog.Builder builder = new AlertDialog.Builder(context)
                    .setTitle(R.string.exit_title)
                    .setPositiveButton(R.string.exit_meeting, (dialog, which) -> {
                        meetingVM.exitRoom(meetingVM.getMeValue());
                        LayoutRatingBinding binding = LayoutRatingBinding.inflate(LayoutInflater.from(context));
                        binding.ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
                            if (rating == 0) {
                                ratingBar.setRating(1);
                            }
                        });
                        new AlertDialog.Builder(context)
                                .setTitle(R.string.rate)
                                .setView(binding.getRoot())
                                .setPositiveButton(R.string.submit, (dialog1, which1) -> rtcVM.rate(binding.ratingBar.getProgress()))
                                .setOnDismissListener(dialog1 -> Navigation.findNavController(requireView()).navigateUp())
                                .show();
                    })
                    .setNegativeButton(R.string.cancel, null);
            if (meetingVM.isHost(meetingVM.getMeValue())) {
                builder.setMessage(R.string.exit_message)
                        .setNeutralButton(R.string.close_meeting, (dialog, which) -> meetingVM.closeRoom());
            }
            builder.show();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);
        setHasOptionsMenu(true);

        qBadgeView = new QBadgeView(requireContext());

        rtcVM = new ViewModelProvider(this).get(RtcViewModel.class);
        meetingVM = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);
        meetingVM.getRoomInfo(MeetingFragmentArgs.fromBundle(requireArguments()).getRoomId());
        subscribeOnActivity();

        rtmEventHandler = new RtmEventHandler(meetingVM);
        RtmManager.instance().registerListener(rtmEventHandler);
    }

    @Override
    protected FragmentMeetingBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentMeetingBinding.inflate(inflater, container, false);
    }

    @Override
    protected void init() {
        adapter = new VideoFragmentAdapter(this);
        binding.vpVideo.setAdapter(adapter);
        new TabLayoutMediator(binding.tab, binding.vpVideo, (tab, position) -> {
        }).attach();

        mic = binding.navView.findViewById(R.id.menu_mic);
        video = binding.navView.findViewById(R.id.menu_video);
        chat = binding.navView.findViewById(R.id.menu_chat);
        binding.navView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    public boolean lightMode() {
        return true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        subscribeOnFragment();
        binding.setViewModel(meetingVM);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        RtmManager.instance().unregisterListener(rtmEventHandler);
        requireActivity().getViewModelStore().clear();
    }

    private void subscribeOnActivity() {
        meetingVM.meetingState.observe(requireActivity(), meetingState -> {
            if (meetingState == MeetingState.END) {
                showForceExitDialog(R.string.kick_out);
            }
        });
        meetingVM.me.observe(requireActivity(), me -> {
            rtcVM.muteLocalAudioStream(!me.isAudioEnable());
            rtcVM.muteLocalVideoStream(!me.isVideoEnable());
        });
        meetingVM.adminMsgs.observe(requireActivity(), messages -> {
            if (messages != null && messages.size() > 0) {
                PeerMsg.Admin admin = messages.remove(0);
                admin.process(requireContext(), meetingVM);
            }
        });
        meetingVM.normalMsgs.observe(requireActivity(), messages -> {
            if (messages != null && messages.size() > 0) {
                PeerMsg.Normal normal = messages.remove(0);
                normal.process(requireContext(), meetingVM);
            }
        });
        Events.AlertEvent.addListener(requireActivity(), alertEvent ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(alertEvent.title)
                        .setMessage(alertEvent.message)
                        .setPositiveButton(alertEvent.positive, null)
                        .show()
        );
        Events.KickEvent.addListener(requireActivity(), kickEvent -> showForceExitDialog(R.string.kick_out));
    }

    private void subscribeOnFragment() {
        rtcVM.audioRoute.observe(getViewLifecycleOwner(), audioRoute -> {
            MenuItem item = binding.toolbar.getMenu().findItem(R.id.menu_router);
            if (item == null) return;

            switch (audioRoute) {
                case AudioRoute.HEADSET:
                    item.setIcon(R.drawable.ic_headset);
                    break;
                case AudioRoute.EARPIECE:
                    item.setIcon(R.drawable.ic_speaker_off);
                    break;
                case AudioRoute.SPEAKER:
                    item.setIcon(R.drawable.ic_speaker_on);
                    break;
            }
        });
        meetingVM.room.observe(getViewLifecycleOwner(), room -> updateSubTitle(room.startTime));
        meetingVM.me.observe(getViewLifecycleOwner(), me -> {
            mic.setActivated(me.isAudioEnable());
            video.setActivated(me.isVideoEnable());
        });
        meetingVM.renders.observe(getViewLifecycleOwner(), renders -> adapter.setItemCount(renders.size()));
        meetingVM.unReadChatMsgs.observe(getViewLifecycleOwner(), messages -> {
            qBadgeView.bindTarget(chat);
            int size = messages.size();
            if (size == 0) {
                qBadgeView.hide(false);
            } else {
                qBadgeView.setBadgeNumber(size);
            }
        });
        Events.TimeEvent.addListener(getViewLifecycleOwner(), timeEvent -> updateSubTitle(timeEvent.time));
    }

    private void updateSubTitle(long startTime) {
        long diff = new Date().getTime() - startTime;
        binding.toolbar.setSubtitle(TimeUtil.stringForTimeHMS(diff, "%02d:%02d:%02d"));
        Events.TimeEvent.setEvent(startTime);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.fragment_meeting, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                callback.handleOnBackPressed();
                return true;
            case R.id.menu_router:
                rtcVM.switchAudioRoute();
                break;
            case R.id.menu_switcher:
                rtcVM.switchCamera();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_mic:
                meetingVM.switchAudioState(meetingVM.getMeValue(), requireContext());
                break;
            case R.id.menu_video:
                meetingVM.switchVideoState(meetingVM.getMeValue());
                break;
            case R.id.menu_member:
                Navigation.findNavController(requireView()).navigate(MeetingFragmentDirections.actionMeetingFragmentToMemberListFragment());
                break;
            case R.id.menu_chat:
                Navigation.findNavController(requireView()).navigate(MeetingFragmentDirections.actionMeetingFragmentToChatFragment());
                break;
            case R.id.menu_more:
                showActionSheet();
                break;
        }
        return true;
    }

    private void showActionSheet() {
        final int boardMenuTitle = TipsUtil.getBoardMenuTitle(meetingVM, meetingVM.getMeValue());
        ActionSheetFragment actionSheet = ActionSheetFragment.getInstance(R.menu.more_action);
        actionSheet.resetMenuTitle(new HashMap<Integer, Integer>() {{
            put(R.id.menu_mute_all, meetingVM.getMuteAllAudio() == GlobalModuleState.ENABLE ? R.string.mute_all : R.string.unmute_all);
            put(R.id.menu_board, boardMenuTitle);
        }});
        actionSheet.removeMenu(new ArrayList<Integer>() {{
            // TODO not implement
            add(R.id.menu_record);

            if (!meetingVM.isHost(meetingVM.getMeValue())) {
                add(R.id.menu_mute_all);
            }
            if (boardMenuTitle == 0) {
                add(R.id.menu_board);
            }
        }});
        actionSheet.setOnItemClickListener((view, position, id) -> {
            if (id == R.id.menu_invite) {
                // TODO
            } else if (id == R.id.menu_mute_all) {
                meetingVM.switchMuteAllAudio(requireContext());
            } else if (id == R.id.menu_record) {
                // TODO not implement
            } else if (id == R.id.menu_board) {
                meetingVM.switchBoardState(meetingVM.getMeValue());
            } else if (id == R.id.menu_setting) {
                // TODO
            }
        });
        actionSheet.show(getChildFragmentManager(), null);
    }

    private void showForceExitDialog(@StringRes int titleRes) {
        meetingVM.exitRoom(meetingVM.getMeValue());
        new AlertDialog.Builder(requireContext())
                .setTitle(titleRes)
                .setPositiveButton(R.string.know, (dialog, which) ->
                        Navigation.findNavController(requireActivity(), R.id.nav_host_fragment).navigate(LoginFragmentDirections.actionGlobalLoginFragment())
                ).setCancelable(false).show();
    }
}
