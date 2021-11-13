package br.klapps.myapplication;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import br.klapps.widget.MockPlayerView;

public class MainActivity extends AppCompatActivity {

    private MockPlayerView mockPlayerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mockPlayerView = findViewById(R.id.player_view);
        mockPlayerView.setTitle("Big Buck Bunny");
        mockPlayerView.setOnPlayerListener(new MockPlayerView.PlayerListener() {

            @Override
            public void onBackPressed() {
                Toast.makeText(getApplicationContext(), "Click", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPlayerError(String messageError) {
                Toast.makeText(getApplicationContext(), messageError, Toast.LENGTH_SHORT).show();
            }
        });

        mockPlayerView.start("http://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mockPlayerView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mockPlayerView.onPause();
    }
}