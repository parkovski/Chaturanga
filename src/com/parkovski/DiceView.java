package com.parkovski;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class DiceView extends View {
	private Bitmap[][] mList;
	private int mTeam, mDie1, mDie2;
	private int selected;
	private int allowmask;
	
	private Bitmap[] mAltModeList;
	private int[] x1;
	private int y1;
	
	private boolean go;
	private boolean canCancel;
	private Bitmap greenlight, redlight, greylight, yellowlight;
	
	private int mode;
	public static final int MODE_DICE = 1;
	public static final int MODE_GETBACKPIECE = 2;
	public static final int MODE_YOUWIN = 3;
	
	public int background_color;
	
	interface OnChooseListener {
		public void onChoose( int choice );
	}
	
	OnChooseListener listener;
	
	public DiceView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		Settings s = Settings.getSettings( );
		if( s != null )
			background_color = s.background_color;
		mode = MODE_DICE;
		
		Resources res = getResources( );
		greenlight = BitmapFactory.decodeResource( res, R.drawable.greenlight );
		redlight = BitmapFactory.decodeResource( res, R.drawable.redlight );
		greylight = BitmapFactory.decodeResource( res, R.drawable.greylight );
		yellowlight = BitmapFactory.decodeResource( res, R.drawable.yellowlight );
	}
	
	public void setOnChooseListener( OnChooseListener listener ) {
		this.listener = listener;
	}
	
	public void setBmpList( Bitmap[][] list ) {
		mList = list;
	}
	
	public void setTeam( int team ) {
		mTeam = team;
	}
	
	public void setDisplay( int d1, int d2 ) {
		if( d1 == 5 ) d1 = 6;
		if( d2 == 5 ) d2 = 6;
		mDie1 = d1;
		mDie2 = d2;
		selected = allowmask = 0;
		invalidate( );
	}
	
	public void select( int which, int mask ) {
		if( which == 5 ) which = 6;
		if( mDie1 == which ) selected = 1;
		else if( mDie2 == which ) selected = 2;
		else selected = 0;
		allowmask = mask;
		invalidate( );
	}
	
	public int getSelected( ) {
		int r = mDie1;
		if( selected == 2 )
			r = mDie2;
		if( r == 6 )
			r = 5;
		return r;
	}
	
	public void setMode( int newMode, Bitmap[] list ) {
		mode = newMode;
		mAltModeList = list;
		x1 = new int[list.length];
		invalidate( );
	}
	
	public void setMode( int newMode ) {
		mode = newMode;
		mAltModeList = null;
		x1 = null;
		invalidate( );
	}
	
	public int getMode( ) {
		return mode;
	}
	
	public void setGo( boolean go ) {
		if( this.go == go ) return;
		this.go = go;
		invalidate( );
	}
	
	public void setGo( boolean go, boolean canCancel ) {
		if( this.go == go && this.canCancel == canCancel ) return;
		this.go = go;
		this.canCancel = canCancel;
		invalidate( );
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw( canvas );
		
		switch( mode ) {
		case MODE_DICE:
			onDrawDice( canvas );
			break;
		case MODE_GETBACKPIECE:
			onDrawGetBackPiece( canvas );
			break;
		case MODE_YOUWIN:
			onDrawYouWin( canvas );
			break;
		}
	}
	
	private void onDrawDice( Canvas canvas ) {
		Paint paint = new Paint( );
		if( mList == null ) return;
		
		int imgx, imgy;
		int padx, pady;
		boolean oneDie = false;
		padx = getWidth( );
		pady = getHeight( );
		
		oneDie = (mDie1==0 || mDie2==0);
		imgx = mList[0][0].getWidth( );
		imgy = mList[0][0].getHeight( );
		if( oneDie )
			padx = ( padx - imgx ) / 2;
		else
			padx = ( padx - 2*imgx ) / 3;
		pady = ( pady - imgy ) / 2;
		
		int startx = padx;
		
		int lightx = greylight.getWidth( );
		Bitmap stoplight = canCancel ? yellowlight : redlight;
		if( padx > lightx + 10 ) {
			canvas.drawBitmap( go ? greenlight : greylight, 5, pady, paint );
			canvas.drawBitmap( go ? greylight : stoplight, getWidth( ) - lightx - 5, pady, paint );
		}
		
		if( mDie1 != 0 ) {
			paint.setColor( Color.DKGRAY );
			canvas.drawRect( new Rect( startx, pady, startx+imgx, pady+imgy ), paint );
			paint.setColor( background_color );
			Rect dr = new Rect( startx+1, pady+1, startx+imgx-1, pady+imgy-1 );
			canvas.drawRect( dr, paint );
			if( selected == 1 ) {
				paint.setColor( 0xAA33CC44 );
				canvas.drawRect( dr, paint );
				paint.setAlpha( 0xFF );
			}
			canvas.drawBitmap( mList[mDie1-1][mTeam-1], startx, pady, paint );
			startx += padx+imgx;
		}
		if( mDie2 != 0 ) {
			paint.setColor( Color.DKGRAY );
			canvas.drawRect( new Rect( startx, pady, startx+imgx, pady+imgy ), paint );
			paint.setColor( background_color );
			Rect dr = new Rect( startx+1, pady+1, startx+imgx-1, pady+imgy-1 );
			canvas.drawRect( dr, paint );
			if( selected == 2 ) {
				paint.setColor( 0xAA33CC44 );
				canvas.drawRect( dr, paint );
				paint.setAlpha( 0xFF );
			}
			canvas.drawBitmap( mList[mDie2-1][mTeam-1], startx, pady, paint );
		}
	}
	
	private void onDrawGetBackPiece( Canvas canvas ) {
		Paint paint = new Paint( );
		if( mAltModeList == null ) return;
		
		int imgx, imgy;
		int padx, pady;
		Bitmap[] arr = mAltModeList;
		padx = getWidth( );
		pady = getHeight( );
		
		// note: arr[0] is null, as the cancel space.
		imgx = arr[1].getWidth( );
		imgy = arr[1].getHeight( );
		padx = ( padx - arr.length*imgx ) / (arr.length+1);
		pady = ( pady - imgy ) / 2;
		y1 = pady;
		
		int startx = padx;
		for( int i = 0; i < arr.length; i++ ) {
			paint.setColor( Color.DKGRAY );
			x1[i]=startx;
			canvas.drawRect( new Rect( startx, pady, startx+imgx, pady+imgy ), paint );
			paint.setColor( background_color );
			canvas.drawRect( new Rect( startx+1, pady+1, startx+imgx, pady+imgy ), paint );
			if( arr[i] != null )
				canvas.drawBitmap( arr[i], startx, pady, paint );
			startx += padx+imgx;
		}
	}
	
	private void onDrawYouWin( Canvas canvas ) {
		Paint paint = new Paint( );
		if( mAltModeList == null ) return;
		
		int imgx, imgy;
		int padx, pady;
		Bitmap[] arr = mAltModeList;
		padx = getWidth( );
		pady = getHeight( );
		
		imgx = arr[0].getWidth( );
		imgy = arr[0].getHeight( );
		padx = ( padx - arr.length*imgx - ( 5 * arr.length - 5 ) ) / 2;
		pady = ( pady - imgy ) / 2;
		
		int startx = padx;
		for( int i = 0; i < arr.length; i++ ) {
			paint.setColor( Color.DKGRAY );
			x1[i]=startx;
			canvas.drawRect( new Rect( startx, pady, startx+imgx, pady+imgy ), paint );
			paint.setColor( background_color );
			canvas.drawRect( new Rect( startx+1, pady+1, startx+imgx-1, pady+imgy-1 ), paint );
			canvas.drawBitmap( arr[i], startx, pady, paint );
			startx += 5+imgx;
		}
	}

	/*@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		//super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		// 2 dice w/ 10dip padding
		// default size 30x20 (only the padding)
		int x, y = 20;
		if( mList != null ) {
			y += mList[0][0].getHeight( );
		}
		
		x = MeasureSpec.getSize( widthMeasureSpec );
		setMeasuredDimension( x, y );
	}*/

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction( );
		if( action == MotionEvent.ACTION_DOWN ) return true;
		else if( action != MotionEvent.ACTION_UP ) return false;
		
		int x = (int)event.getX( );
		int y = (int)event.getY( );
		
		if( mode == MODE_GETBACKPIECE ) {
			Bitmap[] arr = mAltModeList;
			if( arr == null ) return true;
			int imgx = arr[1].getWidth( );
			int imgy = arr[1].getHeight( );
			int y2 = y1 + imgy;
			
			for( int i = 0; i < x1.length; i++ ) {
				if( x >= x1[i] && x < (x1[i]+imgx) && y >= y1 && y < y2 && listener != null ) {
					listener.onChoose( i );
					return true;
				}
			}
		} else if( mode == MODE_DICE ) {
			int imgx, imgy;
			int padx, pady;
			padx = getWidth( );
			pady = getHeight( );
			
			boolean oneDie = (mDie1==0 || mDie2==0);
			imgx = mList[0][0].getWidth( );
			imgy = mList[0][0].getHeight( );
			if( oneDie )
				return true;
			else
				padx = ( padx - 2*imgx ) / 3;
			pady = ( pady - imgy ) / 2;
			
			if( y < pady || y >= pady + imgy )
				return true;
			
			int d1 = mDie1 == 6 ? 5 : mDie1;
			int d2 = mDie2 == 6 ? 5 : mDie2;
			
			if( x >= padx && x < padx + imgx ) {
				if( ( allowmask & ( ( 1 << d1 ) >>> 1 ) ) != 0 )
					selected = 1;
			} else if( x >= padx + padx + imgx && x < padx + padx + imgx + imgx ) {
				if( ( allowmask & ( ( 1 << d2 ) >>> 1 ) ) != 0 )
					selected = 2;
			}
			
			if( listener != null ) {
				if( selected == 1 )
					listener.onChoose( d1 );
				else if( selected == 2 )
					listener.onChoose( d2 );
			}
			
			invalidate( );
		}
		
		return false;
	}
}
