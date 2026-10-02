package com.tech.eskool;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.tech.eskool.databinding.ActivityYoutubeBinding;
import com.tech.eskool.service.SessionManager;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class YoutubeActivity extends AppCompatActivity {
    ActivityYoutubeBinding binding;
    YouTubePlayerView youTubePlayerView;
    SessionManager sessionManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityYoutubeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        youTubePlayerView = binding.youtubePlayerView;
        sessionManager = new SessionManager(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        sessionManager = new SessionManager(this);
        if(sessionManager.checkConnectivity(this)
                && sessionManager.getYoutubeUrl() != null
                && !sessionManager.getYoutubeUrl().isEmpty()) {
            getLifecycle().addObserver(youTubePlayerView);
            youTubePlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
                @Override
                public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                    String url = sessionManager.getYoutubeUrl();
                    Log.i("WebViewContent", url);
                    youTubePlayer.loadVideo(url, 0);
                }
            });
        }else {
            getAlertWithOkForActivity(StartActivity.NO_INTERNET);
        }
    }

    public void getAlertWithOkForActivity(String msg)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this, R.style.myAlertDialog);
        alert.setTitle(R.string.app_name);
        alert.setIcon(R.mipmap.ic_launcher);
        alert.setMessage(msg);
        alert.setCancelable(false);
        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        });
        AlertDialog dialog = alert.create();
        dialog.show();

    }

}