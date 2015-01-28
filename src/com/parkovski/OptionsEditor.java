package com.parkovski;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class OptionsEditor extends Activity {
	
	private Spinner sp_touchmode;
	private Spinner sp_rotation;
	private Spinner sp_continue;
	private Spinner sp_compdiff;
	
	private Button resetall;
	
	private Settings settings;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.options );
		settings = Settings.getSettings( );
		
		sp_touchmode = (Spinner)findViewById( R.id.sp_touchmode );
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.touchmode, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource( android.R.layout.simple_dropdown_item_1line );
		sp_touchmode.setAdapter( adapter );
		sp_touchmode.setOnItemSelectedListener( new Spinner.OnItemSelectedListener( ) {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				settings.touch_mode = arg0.getSelectedItemPosition( ) == 0;
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		} );
		
		sp_rotation = (Spinner)findViewById( R.id.sp_rotation );
		adapter = ArrayAdapter.createFromResource(
				this, R.array.rotation, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource( android.R.layout.simple_dropdown_item_1line );
		sp_rotation.setAdapter( adapter );
		sp_rotation.setOnItemSelectedListener( new Spinner.OnItemSelectedListener( ) {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				settings.rotation = arg0.getSelectedItemPosition( );
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		} );
		
		sp_continue = (Spinner)findViewById( R.id.sp_continue );
		adapter = ArrayAdapter.createFromResource(
				this, R.array.yesno, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource( android.R.layout.simple_dropdown_item_1line );
		sp_continue.setAdapter( adapter );
		sp_continue.setOnItemSelectedListener( new Spinner.OnItemSelectedListener( ) {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				settings.contonlose = arg0.getSelectedItemPosition( ) == 0;
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		} );
		
		sp_compdiff = (Spinner)findViewById( R.id.sp_compdifficulty );
		adapter = ArrayAdapter.createFromResource(
				this, R.array.difficulty, android.R.layout.simple_spinner_item );
		adapter.setDropDownViewResource( android.R.layout.simple_dropdown_item_1line );
		sp_compdiff.setAdapter( adapter );
		sp_compdiff.setOnItemSelectedListener( new Spinner.OnItemSelectedListener( ) {
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				settings.compdiff = arg0.getSelectedItemPosition( );
			}
			public void onNothingSelected(AdapterView<?> arg0) {}
		} );
				
		resetall = (Button)findViewById( R.id.resetoptions );
		resetall.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				settings.resetAll( );
				refreshFromSettings( );
			}
		} );
		
		refreshFromSettings( );
	}
	
	private void refreshFromSettings( ) {
		sp_touchmode.setSelection( settings.touch_mode ? 0 : 1 );
		sp_rotation.setSelection( settings.rotation );
		sp_continue.setSelection( settings.contonlose ? 0 : 1 );
		sp_compdiff.setSelection( settings.compdiff );
	}
	
	@Override
	protected void onPause( ) {
		super.onPause( );
		Settings.getSettings( ).save( );
	}

}
