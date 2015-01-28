package com.parkovski;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class Chaturanga extends Activity
	implements BoardView.OnUpdateListener, DiceView.OnChooseListener {
	
	BoardView mBoard;
	DiceView mDice;
	Button mNextTurn;
	Button mNewGame;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Settings.initSettings( this );
        setContentView(R.layout.main);
        mDice = (DiceView)findViewById( R.id.diceview );
        mBoard = (BoardView)findViewById( R.id.board );
        mNextTurn = (Button)findViewById( R.id.nextturn );
        mNewGame = (Button)findViewById( R.id.newgame );
        
        mBoard.setOnUpdateListener( this );
        mDice.setBmpList( mBoard.getBitmapList( ) );
        mDice.setOnChooseListener( this );
        mBoard.updateDiceView( mDice );
        
        mNextTurn.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				mBoard.nextTurn( );
			}
        } );
        
        mNewGame.setOnClickListener( new Button.OnClickListener( ) {
        	public void onClick( View arg0 ) {
        		Intent i = new Intent( Chaturanga.this, NewGame.class );
        		i.putExtra( "players", mBoard.getPlayerMask( ) );
        		startActivityForResult( i, 1 );
        	}
        } );
        
        if( savedInstanceState != null ) {
        	mBoard.restore( savedInstanceState );
        	mBoard.updateDiceView( mDice );
        	int dicestate = savedInstanceState.getInt( "dicestate" );
        	switch( dicestate ) {
        	case DiceView.MODE_DICE:
        		break;
        	case DiceView.MODE_GETBACKPIECE:
        		mDice.setMode( DiceView.MODE_GETBACKPIECE, mBoard.getRetrieveList( ) );
        		break;
        	case DiceView.MODE_YOUWIN:
        		mDice.setMode( DiceView.MODE_YOUWIN, mBoard.getWinList( ) );
        		break;
        	}
        }
    }
        
    @Override
	protected void onResume( ) {
		super.onResume( );
		
		mBoard.checkSettings( );
	}
    
    @Override
    protected void onPause( ) {
    	super.onPause( );
    	
    	Settings settings = Settings.getSettings( );
    	if( settings != null )
    		settings.save( );
    }

	@Override
    protected void onSaveInstanceState( Bundle outState ) {
    	super.onSaveInstanceState( outState );
    	mBoard.save( outState );
    	outState.putInt( "dicestate", mDice.getMode( ) );
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        Bundle extras = intent == null ? null : intent.getExtras( );
        int playermask = extras == null ? 0 : extras.getInt( "playermask" );
        
        switch( requestCode ) {
        case 1:
	        switch( resultCode ) {
	        case NewGame.RESULT_SINGLEPLAYER:
	        	mDice.setMode( DiceView.MODE_DICE );
	        	mBoard.setPlayers( (playermask&0x3), (playermask&0xC)>>2,
	        			(playermask&0x30)>>4, playermask>>6 );
	        	mBoard.initBoard( );
	        	break;
	        default:
	        	break;
	        }
	        break;
        /*case 5:
        	switch( resultCode ) {
        	case MultiplayerSetup.RESULT_BTHOST:
        		//btHost( );
        		break;
        	case MultiplayerSetup.RESULT_BTJOIN:
        		//btJoin( );
        		break;
        	case MultiplayerSetup.RESULT_BTSTOP:
        		//btStop( );
        		break;
        	}
        	break;*/
        }
    }
    
    public void onUpdate( BoardView board, int playertype ) {
    	board.updateDiceView( mDice );
    }
    
    public void onRecolor( BoardView board, int color ) {
    	mDice.background_color = color;
    	mDice.invalidate( );
    }
    
    public void onRetrieve( BoardView board, Bitmap[] list ) {
    	mDice.setMode( DiceView.MODE_GETBACKPIECE, list );
    }
    
    public void onWin( BoardView board, Bitmap[] list ) {
    	mDice.setMode( DiceView.MODE_YOUWIN, list );
    }
    
    public void onAutoSelectDie( int die, int mask ) {
    	mDice.select( die, mask );
    }
    
    public void onChoose( int choice ) {
    	switch( mDice.getMode( ) ) {
    	case DiceView.MODE_GETBACKPIECE:
    		mDice.setMode( DiceView.MODE_DICE );
        	mBoard.replacePiece( choice );
        	break;
    	case DiceView.MODE_DICE:
    		mBoard.selected_die = choice;
    		break;
    	}
    }

	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater( ).inflate( R.menu.mainmenu, menu );
		return true;
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item ) {
		int id = item.getItemId( );
		Intent i = null;
		int req = 0;
		switch( id ) {
		case R.id.options:
			i = new Intent( this, OptionsEditor.class );
			req = 2;
			break;
		//case R.id.multiplayer:
		//	i = new Intent( this, MultiplayerSetup.class );
		//	req = 5;
		//	break;
		case R.id.changebkgnd:
			i = new Intent( this, BackgroundPicker.class );
			req = 3;
			break;
		case R.id.help:
			i = new Intent( this, HelpViewer.class );
			req = 4;
			break;
		case R.id.undo:
			mBoard.undo( );
			mBoard.updateDiceView( mDice );
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		
		if( i != null )
			startActivityForResult( i, req );
		
		return true;
	}
}