package io.agora.meeting.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import io.agora.meeting.base.BaseFragment;
import io.agora.meeting.data.Member;
import io.agora.meeting.databinding.FragmentSimpleVideoBinding;
import io.agora.meeting.viewmodel.MeetingViewModel;

public class SimpleVideoFragment extends BaseFragment<FragmentSimpleVideoBinding> {
    private MeetingViewModel meetingVM;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        meetingVM = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);
    }

    @Override
    protected FragmentSimpleVideoBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentSimpleVideoBinding.inflate(inflater, container, false);
    }

    @Override
    protected void init() {

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        meetingVM.me.observe(getViewLifecycleOwner(), me -> {
            List<Member> renders = meetingVM.renders.getValue();
            if (renders != null && renders.size() > 0) {
                binding.setLittle(me);
            } else {
                binding.setLarge(me);
            }
        });
        meetingVM.renders.observe(getViewLifecycleOwner(), renders -> {
            if (renders.size() > 0) {
                binding.setLittle(meetingVM.getMeValue());
                binding.setLarge(renders.get(0));
            } else {
                binding.setLittle(null);
                binding.setLarge(meetingVM.getMeValue());
            }
        });
    }
}
