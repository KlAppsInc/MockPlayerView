package br.klapps.widget;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

import br.klapps.R;

public class MockPlayerView extends RelativeLayout {

    private ProgressBar progressLoading;
    private PlayerView playerView;
    private SimpleExoPlayer exoPlayer;
    private DefaultBandwidthMeter BANDWIDTH_METER;
    private DataSource.Factory mediaDataSourceFactory;
    private RelativeLayout backgroundView;
    private long progress = 0;
    private PlayerListener listener;
    private String title = "";
    private String subtitle = "";
    private TextView textTitle, textSubtitle;

    public MockPlayerView(@NonNull Context context) {
        super(context);
        initView(context);
    }

    public MockPlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public MockPlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    private void initView(Context context) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View rootView = inflater.inflate(R.layout.layout_mock_player_view, this, true);
        if (rootView == null) {
            throw new IllegalArgumentException("Error of instance in " + MockPlayerView.class.getSimpleName());
        }
        playerView = rootView.findViewById(R.id.exo_player_view);
        progressLoading = rootView.findViewById(R.id.progress_loading);
        backgroundView = playerView.findViewById(R.id.background_view);
        textTitle = playerView.findViewById(R.id.player_title);
        textSubtitle = playerView.findViewById(R.id.player_subtitle);

        initComponents(context);
        attachActivity(context);
        setupListeners(rootView);

    }

    private void initComponents(Context context) {
        BANDWIDTH_METER = new DefaultBandwidthMeter.Builder(context).build();
        mediaDataSourceFactory = buildDataSourceFactory(context, true);
        exoPlayer = new SimpleExoPlayer.Builder(context).build();
        GradientDrawable gradientDrawable = new GradientDrawable();
        int[] colors = {Color.BLACK, Color.TRANSPARENT, Color.TRANSPARENT, Color.BLACK};
        gradientDrawable.setColors(colors);
        gradientDrawable.setGradientType(GradientDrawable.LINEAR_GRADIENT);
        gradientDrawable.setOrientation(GradientDrawable.Orientation.TOP_BOTTOM);
        backgroundView.setBackground(gradientDrawable);
    }

    private void attachActivity(Context context) {
        try {
            if (context instanceof Activity) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private void setupListeners(View view) {
        view.findViewById(R.id.player_back).setOnClickListener(v -> {
            if (listener != null) listener.onBackPressed();
        });
    }

    private void initExoPlayer(String url) {

        playerView.setKeepScreenOn(true);
        playerView.setPlayer(exoPlayer);
        playerView.setUseController(true);
        playerView.requestFocus();
        MediaSource mediaSource = buildMediaSource(getContext(), Uri.parse(url));
        exoPlayer.prepare(mediaSource);
        exoPlayer.setPlayWhenReady(true);
        exoPlayer.seekTo(progress);

        exoPlayer.addListener(new Player.EventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    progressLoading.setVisibility(VISIBLE);
                } else if (playbackState == Player.STATE_READY) {
                    progressLoading.setVisibility(GONE);
                }
            }

            @Override
            public void onPlayerError(@NonNull ExoPlaybackException error) {
                if (listener != null) listener.onPlayerError(error.getMessage());
            }
        });
        //exoPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);

        if (!TextUtils.isEmpty(title)) {
            textTitle.setText(title);
            textTitle.setVisibility(VISIBLE);
        }
        if (!TextUtils.isEmpty(subtitle)) {
            textSubtitle.setText(subtitle);
            textSubtitle.setVisibility(VISIBLE);
        }

    }

    private MediaSource buildMediaSource(Context context, Uri uri) {
        int type = Util.inferContentType(uri);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource.Factory(new DefaultSsChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(context, false)).createMediaSource(uri);
            case C.TYPE_DASH:
                return new DashMediaSource.Factory(new DefaultDashChunkSource.Factory(mediaDataSourceFactory), buildDataSourceFactory(context, false)).createMediaSource(uri);
            case C.TYPE_HLS:
                return new HlsMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            case C.TYPE_OTHER:
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            default: {
                return new ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(uri);
            }
        }
    }

    private DataSource.Factory buildDataSourceFactory(Context context, boolean useBandwidthMeter) {
        return buildDataSourceFactory(context, useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    private DataSource.Factory buildDataSourceFactory(Context context, DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(context, bandwidthMeter,
                buildHttpDataSourceFactory(context, bandwidthMeter));
    }

    private HttpDataSource.Factory buildHttpDataSourceFactory(Context context, DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultHttpDataSourceFactory(Util.getUserAgent(context, MockPlayerView.class.getSimpleName()), bandwidthMeter);
    }

    public void onPause() {
        if (exoPlayer != null) exoPlayer.setPlayWhenReady(false);
    }

    public void onResume() {
        if (exoPlayer != null) exoPlayer.setPlayWhenReady(true);
    }

    public void start(String url) {
        initExoPlayer(url);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setProgress(long progress) {
        this.progress = progress;
    }

    public long getProgress() {
        if (exoPlayer != null)
            return exoPlayer.getCurrentPosition();
        return 0L;
    }

    public void setOnPlayerListener(PlayerListener listener) {
        this.listener = listener;
    }

    public interface PlayerListener {

        void onBackPressed();

        void onPlayerError(String messageError);
    }
}
