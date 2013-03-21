package pl.tmsj.tetrismove;

import android.app.Activity;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class SelectMusic extends Activity {

	RadioGroup rgMusic;
	RadioButton rbMusicOff;
	RadioButton rbTetrisTheme;
	RadioButton rbTetrisMix;
	RadioGroup rgSound;
	RadioButton rbSoundOff;
	RadioButton rbExplosion;
	SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pick_music);
		
		rgMusic = (RadioGroup) findViewById(R.id.rgMusic);
		rgSound = (RadioGroup) findViewById(R.id.rgSound);
		
		rbMusicOff = (RadioButton) findViewById(R.id.rbMusicOff);
		rbTetrisTheme = (RadioButton) findViewById(R.id.rbTetrisTheme);
		rbTetrisMix = (RadioButton) findViewById(R.id.rbTetrisMix);
		rbSoundOff = (RadioButton) findViewById(R.id.rbSoundOff);
		rbExplosion = (RadioButton) findViewById(R.id.rbExplosion);
		
		sharedPreferences = getSharedPreferences(MainActivity.pathToPreferences, 0);
		
		int musicId = sharedPreferences.getInt(MainActivity.MusicId, 1);
		switch (musicId) {
			case 0: 
				rgMusic.check(R.id.rbMusicOff); break;
			case 1: 
				rgMusic.check(R.id.rbTetrisTheme); break;
			case 2: 
				rgMusic.check(R.id.rbTetrisMix); break;
			default:
				rgMusic.check(R.id.rbMusicOff);
		}
		
		int soundId = sharedPreferences.getInt(MainActivity.SoundId, 1);
		switch (soundId) {
			case 0: 
				rgSound.check(R.id.rbSoundOff); break;
			case 1: 
				rgSound.check(R.id.rbExplosion); break;
			default:
				rgSound.check(R.id.rbSoundOff);
		}
		
		rgMusic.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() 
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
	        	if (MainActivity.bgMusic != null)
	        		MainActivity.bgMusic.stop();
	        	
	        	SharedPreferences.Editor editor = sharedPreferences.edit();
	        	switch (checkedId) {
	        		case R.id.rbMusicOff:
	        			editor.putInt(MainActivity.MusicId, 0);
	        			break;
		        	case R.id.rbTetrisTheme:
		        		MainActivity.bgMusic = MediaPlayer.create(SelectMusic.this, R.raw.tetris_theme);
		        		MainActivity.bgMusic.setLooping(true);
		        		MainActivity.bgMusic.start();
		        		editor.putInt(MainActivity.MusicId, 1);
			        	break;
		        	case R.id.rbTetrisMix:
		        		MainActivity.bgMusic = MediaPlayer.create(SelectMusic.this, R.raw.tetris_party_mix);
		        		MainActivity.bgMusic.setLooping(true);
		        		MainActivity.bgMusic.start();
		        		editor.putInt(MainActivity.MusicId, 2);
			        	break;
	        	}
    			editor.commit();
	        }
	    });
		
		rgSound.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() 
	    {
	        public void onCheckedChanged(RadioGroup group, int checkedId) {
    			SharedPreferences.Editor editor = sharedPreferences.edit();
	        	switch (checkedId) {
		        	case R.id.rbSoundOff:
	        			editor.putInt(MainActivity.SoundId, 0);
	        			TetrisView.lineDeletedSoundId = 0;
						break;
		        	case R.id.rbExplosion:
		        		editor.putInt(MainActivity.SoundId, 1);
		        		TetrisView.lineDeletedSoundId = 1;
		        		TetrisView.shortSounds.play(TetrisView.lineDeletedSoundId, 1, 1, 0, 0, 1);
			        	break;
	        	}
    			editor.commit();
	        }
	    });
	}
}
