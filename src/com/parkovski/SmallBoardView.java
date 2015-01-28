package com.parkovski;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.AttributeSet;

public class SmallBoardView extends BoardView {
	public SmallBoardView( Context context, AttributeSet attrs ) {
		super( context, attrs );
	}
	
	protected void loadImages( ) {
		Resources res = getResources( );
		
		background = BitmapFactory.decodeResource( res, R.drawable.bkgnd_small );
		
		retrievemessage = BitmapFactory.decodeResource( res, R.drawable.retrievepieces );
		rajathrone = BitmapFactory.decodeResource( res, R.drawable.sthrone );
		youwin = BitmapFactory.decodeResource( res, R.drawable.youwinsmall );
		youlose = BitmapFactory.decodeResource( res, R.drawable.youlosesmall );
		
		pawns = new Bitmap[4];
		pawns[0] = BitmapFactory.decodeResource( res, R.drawable.spblack );
		pawns[1] = BitmapFactory.decodeResource( res, R.drawable.spblue );
		pawns[2] = BitmapFactory.decodeResource( res, R.drawable.spred );
		pawns[3] = BitmapFactory.decodeResource( res, R.drawable.spgreen );
		
		ships = new Bitmap[4];
		ships[0] = BitmapFactory.decodeResource( res, R.drawable.ssblack );
		ships[1] = BitmapFactory.decodeResource( res, R.drawable.ssblue );
		ships[2] = BitmapFactory.decodeResource( res, R.drawable.ssred );
		ships[3] = BitmapFactory.decodeResource( res, R.drawable.ssgreen );
		
		horses = new Bitmap[4];
		horses[0] = BitmapFactory.decodeResource( res, R.drawable.shblack );
		horses[1] = BitmapFactory.decodeResource( res, R.drawable.shblue );
		horses[2] = BitmapFactory.decodeResource( res, R.drawable.shred );
		horses[3] = BitmapFactory.decodeResource( res, R.drawable.shgreen );
		
		elephants = new Bitmap[4];
		elephants[0] = BitmapFactory.decodeResource( res, R.drawable.seblack );
		elephants[1] = BitmapFactory.decodeResource( res, R.drawable.seblue );
		elephants[2] = BitmapFactory.decodeResource( res, R.drawable.sered );
		elephants[3] = BitmapFactory.decodeResource( res, R.drawable.segreen );
		
		rajas = new Bitmap[4];
		rajas[0] = BitmapFactory.decodeResource( res, R.drawable.srblack );
		rajas[1] = BitmapFactory.decodeResource( res, R.drawable.srblue );
		rajas[2] = BitmapFactory.decodeResource( res, R.drawable.srred );
		rajas[3] = BitmapFactory.decodeResource( res, R.drawable.srgreen );
		
		rajapawns = new Bitmap[4];
		rajapawns[0] = BitmapFactory.decodeResource( res, R.drawable.srpblack );
		rajapawns[1] = BitmapFactory.decodeResource( res, R.drawable.srpblue );
		rajapawns[2] = BitmapFactory.decodeResource( res, R.drawable.srpred );
		rajapawns[3] = BitmapFactory.decodeResource( res, R.drawable.srpgreen );
		
		pieces_list = new Bitmap[6][];
		pieces_list[0] = pawns;
		pieces_list[1] = ships;
		pieces_list[2] = horses;
		pieces_list[3] = elephants;
		pieces_list[4] = rajas;
		pieces_list[5] = rajapawns;
	}
}
