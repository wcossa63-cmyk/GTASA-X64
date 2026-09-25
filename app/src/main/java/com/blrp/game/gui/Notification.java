package com.blrp.game.gui;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.blrp.game.R;
import com.blrp.game.gui.util.Utils;
import com.nvidia.devtech.NvEventQueueActivity;

import java.io.UnsupportedEncodingException;

public class Notification {

    private final Activity activity;

    private final ConstraintLayout notifyBg;
    private final ImageView notifyIcon;
    private final TextView notifyText;
    private final FrameLayout notifyButt1;
    private final FrameLayout notifyButt2;
    private final TextView notifyButt1Text;
    private final TextView notifyButt2Text;
    private final ProgressBar notifyProgress;

    private CountDownTimer countDownTimer;

    private int currentType = -1;
    private int duration = -1;
    private String actionForBtn = "";
    private String textBtn = "";

    public Notification(Activity activity) {
        this.activity = activity;

        this.notifyBg = activity.findViewById(R.id.noty_layout);
        this.notifyIcon = activity.findViewById(R.id.noty_image);
        this.notifyText = activity.findViewById(R.id.noty_text);
        this.notifyButt1Text = activity.findViewById(R.id.noty_btn_text_1);
        this.notifyButt2Text = activity.findViewById(R.id.noty_btn_text_2);
        this.notifyButt1 = activity.findViewById(R.id.noty_btn_1);
        this.notifyButt2 = activity.findViewById(R.id.noty_btn_2);
        this.notifyProgress = activity.findViewById(R.id.noty_progress);

        notifyBg.setOnClickListener(v -> HideNotification());

        Utils.HideLayout(notifyBg, false);
    }

    public void ShowNotification(int type, String text, int duration,
                                 String actionForBtn, String textBtn) {

        Utils.HideLayout(notifyBg, false);
        cancelTimer();

        this.currentType = type;
        this.duration = duration;
        this.actionForBtn = actionForBtn != null ? actionForBtn : "";
        this.textBtn = textBtn != null ? textBtn : "";

        notifyText.setText(Utils.transfromColors(text));

        notifyButt1.setVisibility(View.GONE);
        notifyButt2.setVisibility(View.GONE);
        notifyProgress.setVisibility(View.GONE);

        switch (type) {
            case 0:
            case 2:
                notifyIcon.setImageResource(R.drawable.noty_red_icon);
                break;

            case 1:
            case 3:
                notifyIcon.setImageResource(R.drawable.noty_green_icon);
                break;

            case 4:
                notifyIcon.setImageResource(R.drawable.noty_green_icon);
                setupButton(notifyButt1Text);
                break;

            case 5:
                notifyIcon.setImageResource(R.drawable.noty_red_icon);
                setupButton(notifyButt1Text);
                break;

            default:
                notifyIcon.setImageResource(R.drawable.notify_icon_error);
                break;
        }

        if (duration > 0) {
            notifyProgress.setVisibility(View.VISIBLE);
            notifyProgress.setMax(duration * 1000);
            notifyProgress.setProgress(duration * 1000);
        }

        Animation showAnim = AnimationUtils.loadAnimation(activity, R.anim.popup_show_notification);
        notifyBg.startAnimation(showAnim);
        Utils.ShowLayout(notifyBg, false);

        startCountdown();
    }

    private void setupButton(TextView button) {
        if (button == null || textBtn.isEmpty()) return;

        button.setVisibility(View.VISIBLE);
        button.setText(textBtn);

        button.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(activity, R.anim.button_click));

            if (!actionForBtn.isEmpty()) {
                try {
                    NvEventQueueActivity.getInstance()
                            .sendClick(actionForBtn.getBytes("windows-1251"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            HideNotification();
        });
    }

    private void startCountdown() {
        cancelTimer();

        if (duration <= 0) return;

        countDownTimer = new CountDownTimer(duration * 1000L, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                notifyProgress.setProgress((int) millisUntilFinished);
            }

            @Override
            public void onFinish() {
                HideNotification();
            }
        }.start();
    }

    public void HideNotification() {
        cancelTimer();

        Animation hideAnim = AnimationUtils.loadAnimation(activity, R.anim.popup_hide_notification);
        hideAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}
            @Override
            public void onAnimationEnd(Animation animation) {
                Utils.HideLayout(notifyBg, false);
            }
            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        notifyBg.startAnimation(hideAnim);
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    public void clear() {
        cancelTimer();
        Utils.HideLayout(notifyBg, false);
    }
}