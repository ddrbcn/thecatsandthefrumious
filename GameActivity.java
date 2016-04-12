package es.ddrbcn.thecatsandthefrumious;

import java.util.ArrayList;
import java.util.List;

import es.ddrbcn.thecatsandthefrumious.R;

import android.R.layout;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.v4.widget.DrawerLayout.LayoutParams;
import android.util.LayoutDirection;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;


/**
 * A class representing the main activity of a Cats and Mouse game. Contains the board, 
 * the game, as well as the origin and destination locations.
 * 
 * 
 * 
 * @author ddrbcn
 * 
 */
public class GameActivity extends Activity {

	RelativeLayout main;
	FrameLayout mBoardCells[][] = new FrameLayout[8][8];
	CatsGame mCatsGame = new CatsGame();
	Location mOriginLoc = new Location();
	Location mDestinationLoc = new Location();
	Button newGameButton;
    
	
	/* Lets computer play */
	public class ComputerPlayTask extends AsyncTask<Void, Void, Integer> {
		
		protected Integer doInBackground(Void... params) {
			
         if(!mCatsGame.getGameHasWinner()){
			State nextState = new State(mCatsGame.currentState);
			nextState = mCatsGame.move(mCatsGame.currentState,
					                   mCatsGame.currentState.getIsMouseturn());
			/* Mouse computer player case*/
			if (mCatsGame.currentState.getIsMouseturn()) {
				mOriginLoc.setX(mCatsGame.currentState.getMouseLocation()
						.getX());
				mOriginLoc.setY(mCatsGame.currentState.getMouseLocation()
						.getY());
				mDestinationLoc.setX(nextState.getMouseLocation().getX());
				mDestinationLoc.setY(nextState.getMouseLocation().getY());
				
		    /* Cats computer player case*/
			}else{
				for (int i = 0; i < 4; i++) {

					if (!mCatsGame.currentState.getCatsLocations().get(i)
							.equals(nextState.getCatsLocations().get(i))) {
						mOriginLoc.setX(mCatsGame.currentState
								.getCatsLocations().get(i).getX());
						mOriginLoc.setY(mCatsGame.currentState
								.getCatsLocations().get(i).getY());
						mDestinationLoc.setX(nextState.getCatsLocations()
								.get(i).getX());
						mDestinationLoc.setY(nextState.getCatsLocations()
								.get(i).getY());
						break;
					}
				}
			}
         }
		 return 1;

		}

		/*
		 * protected void onProgressUpdate(Integer... progress) {
		 * setProgressPercent(progress[0]); }
		 */

		protected void onPostExecute(Integer result) {
	     if(!mCatsGame.getGameHasWinner()){
			updateBoard();
			updateGame();
			checkWinner();
	     }
		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		InitialBoard();

		// setContentView(R.layout.activity_game);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		/* Inflate the menu; this adds items to the action bar if it is present.*/
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub

		/* Handle item selection */
		switch (item.getItemId()) {
		
        /*Opponent menu*/
		case R.id.action_cats:
			mCatsGame.playerCats = false;
			mCatsGame.playerMouse = true;
			if (!mCatsGame.getGameHasWinner()
					&& (!mCatsGame.getPlayerMouse() && mCatsGame.currentState
							.getIsMouseturn())
					|| (!mCatsGame.getPlayerCats() && !mCatsGame.currentState
							.getIsMouseturn()))
				new ComputerPlayTask().execute();

			return true;

		case R.id.action_mouse:
			mCatsGame.playerCats = true;
			mCatsGame.playerMouse = false;
			if (!mCatsGame.getGameHasWinner()
					&& (!mCatsGame.getPlayerMouse() && mCatsGame.currentState
							.getIsMouseturn())
					|| (!mCatsGame.getPlayerCats() && !mCatsGame.currentState
							.getIsMouseturn()))
				new ComputerPlayTask().execute();
			return true;

		case R.id.action_human:
			mCatsGame.playerCats = true;
			mCatsGame.playerMouse = true;
			return true;
        
		/*New game menu*/
		case R.id.action_newgame:

			newGame();

			return true;
		
		/*Level menu*/
		case R.id.action_easy:
			mCatsGame.setplyDepth(3);
			return true;
		case R.id.action_normal:
			mCatsGame.setplyDepth(5);
			return true;	
		case R.id.action_hard:
			mCatsGame.setplyDepth(7);
			return true;
		default:

			return super.onOptionsItemSelected(item);
		}
	}

	private void newGame() {

		mCatsGame.gameHasWinner = false;
		mCatsGame.currentState = new State();

		mOriginLoc = new Location();
		mDestinationLoc = new Location();
		InitialBoard();
		if (!mCatsGame.getPlayerMouse())
			new ComputerPlayTask().execute();
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();

		
	}
@Override
protected void onResume() {
	// TODO Auto-generated method stub
	super.onResume();
	if (!mCatsGame.getGameHasWinner()
			&& (!mCatsGame.getPlayerMouse() && mCatsGame.currentState
					.getIsMouseturn())
			|| (!mCatsGame.getPlayerCats() && !mCatsGame.currentState
					.getIsMouseturn()))
		new ComputerPlayTask().execute();
	
	
}
	@SuppressLint("NewApi")
	private void InitialBoard() {
		
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();

		int width;
		int height;

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			display.getSize(size);
			width = size.x;
			height = size.y;
		} else {
			width = display.getWidth();
			height = display.getHeight();
		}
		
		int cellWidth = (int)(width / 8);


		// TableRow.LayoutParams params = new TableRow.LayoutParams();// (85,
		// 85);

		RelativeLayout.LayoutParams relLayParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

		main = new RelativeLayout(this);
		main.setLayoutParams(relLayParams);
		main.setId(1);
		main.setBackgroundColor(Color.BLACK);
		
		
		TableRow.LayoutParams rowParams = new TableRow.LayoutParams(
				width,cellWidth);
		RelativeLayout.LayoutParams tableParams = new RelativeLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT);

		newGameButton = new Button(this);
		newGameButton.setText("New game...");
		newGameButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				newGame();

			}
		});

		SquaredTableLayout table = new SquaredTableLayout(this);
		table.setId(2);
		table.setLayoutParams(tableParams);
		tableParams.addRule(RelativeLayout.CENTER_IN_PARENT);

		table.setShrinkAllColumns(true);
		table.setStretchAllColumns(true);
		

		int k = 1;
		for (int i = 0; i < 8; i++) {

			if ((i + 1) % 2 == 0) {
				k = 0;
			} else {
				k = 1;
			}
			TableRow row = new TableRow(this);
			row.setLayoutParams(rowParams);
			for (int j = 0; j < 8; j++) {
				mBoardCells[j][i] = new FrameLayout(this);
				
				// testing layout configuration
				/*FrameLayout.LayoutParams cellParams = new FrameLayout.LayoutParams(
						cellWidth, cellWidth);
				mBoardCells[j][i].setLayoutParams(cellParams);*/

				if (k == 0) {
					mBoardCells[j][i]
							.setBackgroundResource(R.drawable.square_light);
				} else {

					CellImageButton temp = new CellImageButton(this, j, i);
					temp.setOnClickListener(new View.OnClickListener() {
						public void onClick(View v) {
							boolean isValidClick = false;

							if ((mCatsGame.getPlayerMouse() && mCatsGame.currentState
									.getIsMouseturn())
									|| (mCatsGame.getPlayerCats() && !mCatsGame.currentState
											.getIsMouseturn())) {

								isValidClick = validateClick(v);
								if (isValidClick
										&& !mCatsGame.getGameHasWinner()
										&& (!mCatsGame.getPlayerMouse() && mCatsGame.currentState
												.getIsMouseturn())
										|| (!mCatsGame.getPlayerCats() && !mCatsGame.currentState
												.getIsMouseturn()))
									new ComputerPlayTask().execute();

							}

						}
					});

					temp.setBackgroundResource(R.drawable.square_dark);
					temp.setBackgroundID(R.drawable.square_dark);

					if (i == 0) {

						temp.setBackgroundResource(R.drawable.square_redpiece);
						temp.setBackgroundID(R.drawable.square_redpiece);
					}
					if (i == mCatsGame.getState().getMouseLocation().getY()
							&& j == mCatsGame.getState().getMouseLocation()
									.getX()) {

						temp.setBackgroundResource(R.drawable.square_whitepiece);
						temp.setBackgroundID(R.drawable.square_whitepiece);
					}
					this.mBoardCells[j][i].addView(temp);

				}

				row.addView(mBoardCells[j][i]);
				if (k == 0) {
					k++;
				} else {
					if (k == 1)
						k = 0;
				}
			}

			table.addView(row);
		}
		/*
		 * table.setColumnStretchable(7, true); table.setColumnStretchable(6,
		 * true); table.setColumnStretchable(5, true);
		 * table.setColumnStretchable(4, true); table.setColumnStretchable(3,
		 * true); table.setColumnStretchable(2, true);
		 * table.setColumnStretchable(1, true); table.setColumnStretchable(0,
		 * true);
		 */

		RelativeLayout.LayoutParams paramNewGameButton = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.WRAP_CONTENT,
				RelativeLayout.LayoutParams.WRAP_CONTENT);
		paramNewGameButton.addRule(RelativeLayout.CENTER_IN_PARENT);

		/*
		 * paramNewGameButton.addRule(RelativeLayout.BELOW, table.getId());
		 * paramNewGameButton.addRule(RelativeLayout.CENTER_HORIZONTAL,
		 * table.getId());
		 */

		main.addView(table);

		main.addView(newGameButton, paramNewGameButton);
		setContentView(main);
		newGameButton.setVisibility(View.INVISIBLE);
		// addContentView(table, tableParams);
	}

	private boolean validateClick(View v) {
		CellImageButton temp = (CellImageButton) v;
		/*
		 * Log.d("GameActivity", "Location x y : " + temp.buttonLocation.getX()
		 * + temp.buttonLocation.getY()); Log.d("GameActivity",
		 * "Location x y : " + GameActivity.this.mCatsGame.currentState
		 * .getMouseLocation().getX() + " " +
		 * GameActivity.this.mCatsGame.currentState
		 * .getCatsLocations().get(0).getX());
		 */

		if (mOriginLoc.getX() < 0 || mCatsGame.getGameHasWinner()) {

			mOriginLoc.setX(temp.buttonLocation.getX());
			mOriginLoc.setY(temp.buttonLocation.getY());
			return false;

		} else {
			mDestinationLoc.setX(temp.buttonLocation.getX());
			mDestinationLoc.setY(temp.buttonLocation.getY());
			boolean isvalid = mCatsGame.currentState.isvalidMove(mOriginLoc,
					mDestinationLoc);
			if (isvalid) {
				updateBoard();

				updateGame();

				checkWinner();

			} else {
				mOriginLoc.setX(-1);
				mOriginLoc.setY(-1);
				if (mCatsGame.currentState.getCatsLocations().contains(
						mDestinationLoc)
						|| mCatsGame.currentState.getMouseLocation().equals(
								mDestinationLoc)) {
					mOriginLoc.setX(mDestinationLoc.getX());
					mOriginLoc.setY(mDestinationLoc.getY());

				}
				/*
				 * Toast.makeText(getApplicationContext(), ("Ilegal move"),
				 * Toast.LENGTH_SHORT).show();
				 */
			}

			return isvalid;
		}
	}

	private void checkWinner() {
		
	
		if ((mCatsGame.currentState.getMouseLocation().getY() == 0)
				|| (mCatsGame.currentState.getChilds().isEmpty() && !mCatsGame.currentState
						.getIsMouseturn())) {
			Toast.makeText(getApplicationContext(), ("Mouse wins!"),
					Toast.LENGTH_SHORT).show();
			mCatsGame.setGameHasWinner(true);
			newGameButton.setVisibility(View.VISIBLE);
		}
		if ((mCatsGame.currentState.getChilds().isEmpty())
				&& mCatsGame.currentState.getIsMouseturn()) {
			Toast.makeText(getApplicationContext(), ("Cats win!"),
					Toast.LENGTH_SHORT).show();
			mCatsGame.setGameHasWinner(true);
			newGameButton.setVisibility(View.VISIBLE);
		}
		mOriginLoc.setX(-1);
		mOriginLoc.setY(-1);
	}

	private void updateBoard() {
		int tempID = ((CellImageButton) (mBoardCells[mDestinationLoc.getX()][mDestinationLoc
				.getY()].getChildAt(0))).getBackgroundID();

		mBoardCells[mDestinationLoc.getX()][mDestinationLoc.getY()].getChildAt(
				0).setBackgroundResource(
				((CellImageButton) (mBoardCells[mOriginLoc.getX()][mOriginLoc
						.getY()].getChildAt(0))).getBackgroundID());

		((CellImageButton) mBoardCells[mDestinationLoc.getX()][mDestinationLoc
				.getY()].getChildAt(0))
				.setBackgroundID(((CellImageButton) (mBoardCells[mOriginLoc
						.getX()][mOriginLoc.getY()].getChildAt(0)))
						.getBackgroundID());

		mBoardCells[mOriginLoc.getX()][mOriginLoc.getY()].getChildAt(0)
				.setBackgroundResource(tempID);
		((CellImageButton) mBoardCells[mOriginLoc.getX()][mOriginLoc.getY()]
				.getChildAt(0)).setBackgroundID(tempID);
	}

	private void updateGame() {

		if (mCatsGame.currentState.getIsMouseturn()) {
			this.mCatsGame.currentState.setMouseLocation(mDestinationLoc);

		} else {

			for (int i = 0; i < mCatsGame.currentState.getCatsLocations()
					.size(); i++) {
				if (mCatsGame.currentState.getCatsLocations().get(i)
						.equals(mOriginLoc)) {
					mCatsGame.currentState.setCatsLocations(mDestinationLoc, i);
				}
			}

		}

		mCatsGame.currentState.setIsMouseturn(!mCatsGame.currentState
				.getIsMouseturn());

	}

	private void letsComputerPlay(boolean isMouseMove) {

		State nextState = new State(mCatsGame.currentState);
		nextState = mCatsGame.move(mCatsGame.currentState, isMouseMove);

		mOriginLoc.setX(mCatsGame.currentState.getMouseLocation().getX());
		mOriginLoc.setY(mCatsGame.currentState.getMouseLocation().getY());
		mDestinationLoc.setX(nextState.getMouseLocation().getX());
		mDestinationLoc.setY(nextState.getMouseLocation().getY());

	}

}
