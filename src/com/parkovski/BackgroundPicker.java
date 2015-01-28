package com.parkovski;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

public class BackgroundPicker extends Activity{// implements ColorPickerDialog.OnColorChangedListener {
	
	private RadioGroup group;
	//private Button select;

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.background );
		
		group = (RadioGroup) findViewById( R.id.bkgnd_group );
		
		group.setOnCheckedChangeListener( new RadioGroup.OnCheckedChangeListener() {
			public void onCheckedChanged(RadioGroup arg0, int arg1) {
				Settings.getSettings( ).usebitmap = arg1 == R.id.bg_image;
			}
		} );
		/*
		select = (Button) findViewById( R.id.selectbg );
		
		select.setOnClickListener( new Button.OnClickListener( ) {
			public void onClick(View arg0) {
				switch( group.getCheckedRadioButtonId( ) ) {
				case R.id.bg_image:
					break; // not implemented yet
				case R.id.bg_solidcolor:
					ColorPickerDialog dlg = new ColorPickerDialog( BackgroundPicker.this,
								BackgroundPicker.this,
								Settings.getSettings( ).background_color
								);
					
					dlg.show( );
					break;
				}
			}
		} );
		*/
		refreshFromSettings( );
	}
	
	private void refreshFromSettings( ) {
		group.check( Settings.getSettings( ).usebitmap ? R.id.bg_image : R.id.bg_solidcolor );
	}
	
	@Override
	protected void onPause( ) {
		super.onPause( );
		Settings.getSettings( ).save( );
	}

	public void colorChanged( int color ) {
		Settings.getSettings( ).background_color = color;
	}

}
