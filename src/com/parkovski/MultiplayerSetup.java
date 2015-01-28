package com.parkovski;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MultiplayerSetup extends Activity {

	private Button mBtHost, mBtJoin, mBtStop;
	
	public static final int RESULT_BTHOST = RESULT_FIRST_USER;
	public static final int RESULT_BTJOIN = RESULT_FIRST_USER + 1;
	public static final int RESULT_BTSTOP = RESULT_FIRST_USER + 2;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView( R.layout.multiplayer );
		
		mBtHost = (Button)findViewById( R.id.bthost );
		mBtJoin = (Button)findViewById( R.id.btjoin );
		mBtStop = (Button)findViewById( R.id.btstop );
		
		mBtHost.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				setResult( RESULT_BTHOST );
				finish( );
			}
		} );
		
		mBtJoin.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				setResult( RESULT_BTJOIN );
				finish( );
			}
		} );
		
		mBtStop.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				setResult( RESULT_BTSTOP );
				finish( );
			}
		} );
	}

}
