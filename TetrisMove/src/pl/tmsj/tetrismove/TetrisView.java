package pl.tmsj.tetrismove;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.AudioManager;
import android.media.SoundPool;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class TetrisView extends View implements ITetrisConstants {
	//members
	private boolean mHasFocus; 	//gameView is the focus and can be updated with setter below
	public  void setGameFocus( boolean hasFocus ) {	mHasFocus = hasFocus;	}
	private long mNextUpdate; 	//time stamp when next update can be called
	private long mLastGravity;	//allow updates of shape independently from gravity by checking this
	private int mTicks;			//number of ticks that have been calculated
	private int mSeconds;		//number of seconds of game play
    private Paint mPaint;		//paint object to use in draws.
    private Activity mActivityHandle; //save reference to activity to be able to quit from here
    private ScoreManager scoreManager;

    public static SoundPool shortSounds = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
    int[] soundsId = new int[]{0,0};
	SharedPreferences sharedPreferences;
    public static int lineDeletedSoundId = 0;
    
    //tabelę można wykorzystać do przechowywania ilości punktów, które trzeba zdobyć
    //aby przejść do kolejnego levelu, poosiągnięciu ostatniego levelu trzeba dodać
    //kolejne pole z ilością punktów
    //levelBoundaries[currentLevel] - przechowuje punkty do levelu currentLevel + 1
    //private int[] levelBoundaries = new int[]{20, 50, 100};
    private int levelBoundary = 5;
    private int currentGravityRate = STARTING_GRAVITY_RATE;
    float touchDownX, touchDownY, touchUpX, touchUpY;
    
    //game specific
    private TetrisGrid grid; 			//game play field/grid
    private TetrisShape currentShape;	//current shape controllable by the user
    private int currentAction;			//current game action fired by player
    
	public TetrisView(Activity context) {
		//init view obj
		super(context);
		
		//pobieram do tablicy kolejne dźwięki
	    soundsId[1] = shortSounds.load(context, R.raw.bomb, 1);
		
		mActivityHandle = context;
		setBackgroundColor(Color.BLACK);
		setFocusable(true);
		setFocusableInTouchMode(true);
		requestFocus();
		
		//inst objs
		grid = new TetrisGrid();
		currentShape = new TetrisShape(grid);
		mPaint = new Paint();
		scoreManager = new ScoreManager(context);
		
		//initialize
		init();
	}
	
	private void init() {

		//initialize members
		currentShape.isInited = false;
		currentAction = ACTION_NONE;
		mNextUpdate = 0;
		mTicks = 0;
		mSeconds = 0;
		mLastGravity = 1;

		grid.init();
		
		scoreManager.currentScore = 0;
		scoreManager.scoreWasSaved = false;
        touchDownX = touchDownY = touchUpX = touchUpY = 0;
	}

	public void restartGame() {
		init();
		currentShape.isGameOver = false;
	}

	
	public void quitGame() {
		mActivityHandle.finish();
	}
		
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		grid.setBackGroundDimentions(w, h);
		super.onSizeChanged(w, h, oldw, oldh);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		//if DISREGARD_MULTIPLE_KEYPRESSED then check if its the first press of this key
		if(DISREGARD_MULTIPLE_KEYPRESSED && event.getRepeatCount() < 1)
		{
			switch(keyCode)
			{
				case KeyEvent.KEYCODE_DPAD_LEFT:
				case KeyEvent.KEYCODE_4:
				{
					currentAction = ACTION_STRAFE_LEFT;
					break;
				}
				case KeyEvent.KEYCODE_DPAD_RIGHT:
				case KeyEvent.KEYCODE_6:
				{
					currentAction = ACTION_STRAFE_RIGHT;
					break;
				}
				case KeyEvent.KEYCODE_DPAD_UP:
				case KeyEvent.KEYCODE_2:
				{
					currentAction = ACTION_ROTATE_L;
					break;
				}
				case KeyEvent.KEYCODE_DPAD_DOWN:
				case KeyEvent.KEYCODE_8:
				{
//					currentAction = ACTION_ROTATE_R;
					currentAction = ACTION_STRAFE_DOWN;
					break;
				}
				case KeyEvent.KEYCODE_5:
				case KeyEvent.KEYCODE_ENTER:
				case KeyEvent.KEYCODE_SPACE:
				case KeyEvent.KEYCODE_DPAD_CENTER:
				{
					currentAction = ACTION_MAKE_FALL;
					break;
				}
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void update() {
		long time = System.currentTimeMillis();
		
		if( mHasFocus )
		{
			//manage gameOver
			if(currentShape.isGameOver)
			{
				if(!AlertManager.IsAlertActive())
				{
					int alertType = (scoreManager.isTopScore() && !scoreManager.scoreWasSaved)? AlertManager.TYPE_TOP_SCORE:AlertManager.TYPE_GAME_OVER;
					AlertManager.PushAlert(this, alertType);
				}
			}
			//normal state
			else if( time > mNextUpdate )
			{
				
				mNextUpdate = time + 1000 / FRAME_RATE;
				mTicks++;
				currentShape.update(currentAction);
				currentAction = ACTION_NONE;
				if(time - mLastGravity > currentGravityRate || currentShape.IsFalling())
				{
					mLastGravity = time;
					boolean shapeIsLocked = currentShape.addGravity();
					
					if(shapeIsLocked)
					{
						int points = grid.update();
						if(points != 0) {
							//odtwarzam krótki dźwięk po skazowaniu linii
							if (lineDeletedSoundId != 0)
								shortSounds.play(lineDeletedSoundId, 1, 1, 0, 0, 1);
							//zliczam ilość skasowanych wierszy
							scoreManager.linesDeleted += points;
							//premia za jednoczesne skasowanie trzech lub czterech linii
							if (points > 2)
								points *= 2;
							scoreManager.currentScore += points * scoreManager.currentLevel;
						}
					}
					
					if (scoreManager.currentScore >= levelBoundary) {
						scoreManager.currentLevel++;
						levelBoundary *= 2;
						//przyśpieszam spadanie klocków o 100 ms
						currentGravityRate -= 100;
					}
					
				}
				if(mTicks/FRAME_RATE > mSeconds)
				{
					mSeconds = mTicks/FRAME_RATE;
				}
			}
			
		}
		else
		{
			//if paused you don't want to rush into a loop when exiting pause
			mNextUpdate = time + (1000 / OUT_OF_PAUSE_DELAY);
		}
		return;
		
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		
		//so ugly to update in draw but i'm to lazy to implement a real game loop
		update(); //TODO Separate update from draw, and call both inside a clean game loop
		
		super.onDraw(canvas);
		
		//paint elements
		grid.paint(canvas, mPaint);
		
		//paint HUD
		TetrisHud.paintRightHud(canvas, mPaint, getRight(), getTop(), scoreManager, currentShape.getNextType());
		
		//make sure draw will be recalled.
		invalidate();
	}

	public void manageScoreSave(boolean saveToDB, String player) {
		scoreManager.scoreWasSaved = true;
		if(saveToDB && player != null )
				scoreManager.saveScoreIfTopScore(player);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// Ustawianie FPS przez co aplikacja będzie się wolniej odświeżała
		// (metoda onTouchEvent będzie się dłużej/rzadziej wykonywała)
		// przez co może nieco płynniej działać, bo procesor będzie mniej obciążony
		try {
			// Zatrzymanie wątku na 50 ms co daje
			// 1000 ms / 50 ms = 20 FPS
			Thread.sleep(50);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			touchDownX = event.getX();
			touchDownY = event.getY();
			break;
		case MotionEvent.ACTION_UP:
			touchUpX = event.getX();
			touchUpY = event.getY();
			
			//sprawdzam, czy ruch palca był dłuższy w pionie czy w poziomie
			if (Math.abs(touchDownX-touchUpX) > Math.abs(touchDownY-touchUpY)) {
				//gdy ruch był "bardziej poziomy" to przesuwam klocek w lewo lub w prawo
				if (touchDownX > touchUpX)
					currentAction = ACTION_STRAFE_LEFT;
				else
					currentAction = ACTION_STRAFE_RIGHT;
			} else {
				//gdy ruch był "bardziej pionowy" to obracam klocek lub przesuwam w dół
				if (touchDownY > touchUpY)
					currentAction = ACTION_ROTATE_L;
				else
					currentAction = ACTION_STRAFE_DOWN;
			}
			break;
		}
		return true;
	}
	
}
