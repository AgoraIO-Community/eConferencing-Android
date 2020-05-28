package io.agora.meeting.fragment.nav;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceFragmentCompat;

import io.agora.meeting.R;
import io.agora.meeting.annotaion.room.GlobalModuleState;
import io.agora.meeting.base.AppBarDelegate;
import io.agora.meeting.base.BaseFragment;
import io.agora.meeting.viewmodel.MeetingViewModel;

public class MemberSettingFragment extends PreferenceFragmentCompat implements AppBarDelegate {
    private MeetingViewModel meetingVM;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        meetingVM = new ViewModelProvider(requireActivity()).get(MeetingViewModel.class);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setDivider(null);
        getToolbar().setTitle(R.string.member_setting);
        BaseFragment.setupActionBar(this);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        meetingVM.muteAllAudio.observe(getViewLifecycleOwner(), muteAllAudio -> setPreferencesFromResource(R.xml.member_setting_preferences, null));
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        String key_mute_all = getString(R.string.key_mute_all);
        String key_allow_self_unmute = getString(R.string.key_allow_self_unmute);
        getPreferenceManager().setPreferenceDataStore(new PreferenceDataStore() {
            @Override
            public void putBoolean(String key, boolean value) {
                if (meetingVM == null) return;

                if (TextUtils.equals(key, key_mute_all)) {
                    meetingVM.muteAllAudio(value ? GlobalModuleState.DISABLE : GlobalModuleState.ENABLE);
                } else if (TextUtils.equals(key, key_allow_self_unmute)) {
                    meetingVM.muteAllAudio(value ? GlobalModuleState.CLOSE : GlobalModuleState.DISABLE);
                }
            }

            @Override
            public boolean getBoolean(String key, boolean defValue) {
                if (meetingVM == null) return defValue;

                Integer muteAllAudio = meetingVM.muteAllAudio.getValue();
                if (muteAllAudio == null) return defValue;

                if (TextUtils.equals(key, key_mute_all)) {
                    return muteAllAudio != GlobalModuleState.ENABLE;
                } else if (TextUtils.equals(key, key_allow_self_unmute)) {
                    return muteAllAudio == GlobalModuleState.CLOSE;
                }

                return defValue;
            }
        });
    }

    @Override
    public Toolbar getToolbar() {
        return requireView().findViewById(R.id.toolbar);
    }

    @Override
    public boolean lightMode() {
        return false;
    }
}