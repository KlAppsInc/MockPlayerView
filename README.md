# MockPlayerView
Simple player of video using ExoPlayer

Define title, subtitle, calback etc.. after set video url.
```
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
```

Livecycle for play and pause video for pause and resume aplication.
```
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
    
```
