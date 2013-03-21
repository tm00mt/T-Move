package pl.tmsj.tetrismove;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	TetrisView gV;
	GenericMenu gM;
	public static MediaPlayer bgMusic = null;
	public static String pathToPreferences = "GameOptions";
	public static String MusicId = "backgroundMusic";
	public static String SoundId = "deletedLineSound";
	SharedPreferences sharedPreferences;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gV = new TetrisView(this);
        setContentView(gV);
    }
    
    private void playBGMusic(int mId) {
    	switch (mId) {
		case 1:
			bgMusic = MediaPlayer.create(MainActivity.this, R.raw.tetris_theme);
			bgMusic.setLooping(true);
			bgMusic.start();
			break;
		case 2:
			bgMusic = MediaPlayer.create(MainActivity.this, R.raw.tetris_party_mix);
			bgMusic.setLooping(true);
			bgMusic.start();
	        break;
		}
	}

	@Override
    public void onWindowFocusChanged(boolean hasFocus) {
    	gV.setGameFocus(hasFocus);
    	super.onWindowFocusChanged(hasFocus);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	gM = new GenericMenu(menu);
    	gM.populate();
    	return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
    	if(item.getTitle().equals("Restart"))
    		gV.restartGame();
    	if(item.getTitle().equals("Music")) {
    		Intent i = new Intent(MainActivity.this, SelectMusic.class);
			startActivity(i);
    	}
    	if(item.getTitle().equals("Quit"))
    		finish();//just close
    	return super.onMenuItemSelected(featureId, item);
    }
    
    @Override
	protected void onPause() {
		super.onPause();
		if (bgMusic != null && bgMusic.isPlaying())
			//bgMusic.release();
			bgMusic.pause();
		//finish();
	}
    
    @Override
    protected void onResume() {
    	super.onResume();
    	
    	sharedPreferences = getSharedPreferences(pathToPreferences, 0);
    	if (bgMusic == null || !bgMusic.isPlaying()) {
    		int musicId = sharedPreferences.getInt(MusicId, 1);
        	playBGMusic(musicId);
    	}
    	
		int soundId = sharedPreferences.getInt(MainActivity.SoundId, 1);
		switch (soundId) {
		case 0:
			TetrisView.lineDeletedSoundId = 0;
			break;
		case 1:
			TetrisView.lineDeletedSoundId = 1;
			break;
		default:
			TetrisView.lineDeletedSoundId = 0;
		}
    }

}
