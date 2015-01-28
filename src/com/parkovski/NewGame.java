package com.parkovski;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

public class NewGame extends Activity {
	Button mSinglePlayer;//, mHostBluetooth, mJoinBluetooth;
	CheckBox mPlayer1, mPlayer2, mPlayer3, mPlayer4;
	
	public static final int RESULT_SINGLEPLAYER = RESULT_FIRST_USER;
	//public static final int RESULT_HOSTBLUETOOTH = RESULT_FIRST_USER+1;
	//public static final int RESULT_JOINBLUETOOTH = RESULT_FIRST_USER+2;
	
	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.newgame );
		mSinglePlayer = (Button)findViewById( R.id.singleplayer );
		mPlayer1 = (CheckBox)findViewById( R.id.player1 );
		mPlayer2 = (CheckBox)findViewById( R.id.player2 );
		mPlayer3 = (CheckBox)findViewById( R.id.player3 );
		mPlayer4 = (CheckBox)findViewById( R.id.player4 );
		
		mSinglePlayer.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				Intent intent = new Intent( );
				int playermask = getIntent( ).getExtras( ).getInt( "players" );
				playermask &= ~0x55; // preserve bluetooth but not computer player option
				if( mPlayer1.isChecked( ) ) playermask = (playermask | 0x1) & ~0x2;
				if( mPlayer2.isChecked( ) ) playermask = (playermask | 0x4) & ~0x8;
				if( mPlayer3.isChecked( ) ) playermask = (playermask | 0x10) & ~0x20;
				if( mPlayer4.isChecked( ) ) playermask = (playermask | 0x40) & ~0x80;
				intent.putExtra( "playermask", playermask );
				setResult( RESULT_SINGLEPLAYER, intent );
				finish( );
			}
		} );
		/*
		mHostBluetooth.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick( View arg0 ) {
				Intent intent = new Intent( );
				int playermask = 0;
				
				// host plays as black team (player 1)
				// for everyone else, either computer player or get from bluetooth
				if( mPlayer2.isChecked( ) )
					playermask |= 0x4;
				else
					playermask |= 0x8;
				
				if( mPlayer3.isChecked( ) )
					playermask |= 0x10;
				else
					playermask |= 0x20;
				
				if( mPlayer4.isChecked( ) )
					playermask |= 0x40;
				else
					playermask |= 0x80;
				
				intent.putExtra( "playermask", playermask );
				setResult( RESULT_HOSTBLUETOOTH, intent );
				finish( );
			}
		} );
		
		mJoinBluetooth.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick( View arg0 ) {
				Intent intent = new Intent( );
				intent.putExtra( "playermask", 0xAA ); // get all players from a bluetooth server
				setResult( RESULT_JOINBLUETOOTH, intent );
				finish( );
			}
		} );
		*/
	}
	
	@Override
	public void onResume( ) {
		super.onResume( );
		int players = getIntent( ).getExtras( ).getInt( "players" );
		mPlayer1.setChecked( ( players & 0x1 ) == 0x1 );
		mPlayer2.setChecked( ( players & 0x4 ) == 0x4 );
		mPlayer3.setChecked( ( players & 0x10 ) == 0x10 );
		mPlayer4.setChecked( ( players & 0x40 ) == 0x40 );
	}
}
