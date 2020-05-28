package io.agora.meeting.fragment.nav;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import io.agora.meeting.BuildConfig;
import io.agora.meeting.R;
import io.agora.meeting.base.BaseFragment;
import io.agora.meeting.databinding.FragmentAboutBinding;
import io.agora.meeting.viewmodel.CommonViewModel;

public class AboutFragment extends BaseFragment<FragmentAboutBinding> {
    private CommonViewModel commonVM;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        commonVM = new ViewModelProvider(requireActivity()).get(CommonViewModel.class);
    }

    @Override
    protected FragmentAboutBinding createBinding(@NonNull LayoutInflater inflater, @Nullable ViewGroup container) {
        return FragmentAboutBinding.inflate(inflater, container, false);
    }

    @Override
    protected void init() {
        binding.tvTips.setText(getString(R.string.version_tips, BuildConfig.VERSION_NAME));
        binding.tvAgreement.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.tvPolicy.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG);
        binding.setClickListener(v -> {
            switch (v.getId()) {
                case R.id.btn_update:
                    commonVM.checkVersion(false);
                    break;
                case R.id.tv_agreement:
                    break;
                case R.id.tv_policy:
                    break;
            }
        });
    }
}
