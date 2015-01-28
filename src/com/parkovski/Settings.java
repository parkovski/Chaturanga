package com.parkovski;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;

public class Settings {
	private static Settings settings = null;
	
	private Activity activity;
	private boolean updated;
	private SharedPreferences prefs;
	private SharedPreferences.Editor edit;
	
	public boolean touch_mode;
	public int background_color;
	public int rotation;
	public boolean contonlose;
	public int compdiff;
	public boolean usebitmap;
	
	private Settings( Activity a ) {
		activity = a;
		resetAll( );
		load( );
	}
	
	public static void initSettings( Activity a ) {
		if( settings == null )
			settings = new Settings( a );
	}
	
	public static Settings getSettings( ) {
		return settings;
	}
	
	public boolean isUpdated( ) {
		return updated;
	}
	
	public void validate( ) {
		updated = false;
	}
	
	public void resetAll( ) {
		touch_mode = true;
		background_color = Color.LTGRAY;
		rotation = 0;
		contonlose = false;
		compdiff = 1;
		
		updated = true;
	}
	
	public void load( ) {
		if( prefs == null )
			prefs = activity.getPreferences( Activity.MODE_PRIVATE );

		// supply all the defaults also, in case they don't exist
		touch_mode = prefs.getBoolean( "touchmode", true );
		background_color = prefs.getInt( "background_color", Color.LTGRAY );
		rotation = prefs.getInt( "rotation", 0 );
		contonlose = prefs.getBoolean( "continue_on_lose", false );
		compdiff = prefs.getInt( "computer_difficulty", 1 );
		usebitmap = prefs.getBoolean( "usebitmap", true );
		
		updated = true;
	}
		
	public void save( ) {
		if( prefs == null )
			prefs = activity.getPreferences( Activity.MODE_PRIVATE );
		if( edit == null )
			edit = prefs.edit( );
				
		edit.putBoolean( "touchmode", touch_mode );
		edit.putInt( "background_color", background_color );
		edit.putInt( "rotation", rotation );
		edit.putBoolean( "continue_on_lose", contonlose );
		edit.putInt( "computer_difficulty", compdiff );
		edit.putBoolean( "usebitmap", usebitmap );
		
		edit.commit( );
		
		updated = true;
	}
}
