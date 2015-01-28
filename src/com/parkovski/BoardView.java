package com.parkovski;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {
	
	final float mScale = getContext().getResources().getDisplayMetrics().density;
	
	protected Bitmap[] pawns, ships, horses, elephants, rajas, rajapawns;
	protected Bitmap[][] pieces_list;
	protected Bitmap background;
	protected Bitmap retrievemessage, rajathrone, youwin, youlose;
	private boolean retrieving;
	private int retrievex, retrievey, retrieveflags;
	private boolean won;
	public boolean capture1, capture2, capture3, capture4;
	
	public static final int CONTROL_HUMAN = 0;
	public static final int CONTROL_COMPUTER = 1;
	public static final int CONTROL_BLUETOOTH = 2;
	private int[] teamcontrol;
	private ComputerPlayer comp;
	
	protected int board[][];
	private int dispbit1, dispbit2; // for touch mode
	private int rotation;
	
	private int src_x = -1, src_y, dest_x = -1, dest_y;
	private int mXDraw, mYDraw;
	private int mPadX, mPadY;
	
	private boolean animating;
	private boolean anim_on_finish_new_turn;
	private boolean anim_on_finish_comp_move;
	private int anim_comp1, anim_comp2;
	private int anim_percent;
	private int anim_x0, anim_y0, anim_x1, anim_y1;
	private Handler anim_handler;
	private Runnable anim_task;
	private static final int ANIM_DELAY = 30;
	
	private static final int UNDO_STACK_SIZE = 8;
	private int undo_stackptr;
	private int[] undo_oldpiece;
	private int[] undo_oldx, undo_oldy;
	private int[] undo_newpiece;
	private int[] undo_newx, undo_newy;
	private int[] undo_turn;
	private int[] undo_dice;
	
	protected int mSize;
	protected int mImgSize;
	
	protected Bitmap bkgnd_image;
	protected int bkgnd_x, bkgnd_y; // offsets
	
	private boolean touch_mode;
	// if this is true, we touch space A and then B
	// instead of dragging from A to B.
	private int bkgnd_color;
	private boolean cont_on_lose;
	private boolean bkgnd_usebitmap;
	
	interface OnUpdateListener {
		public void onUpdate( BoardView board, int playertype );
		public void onRecolor( BoardView board, int color );
		public void onRetrieve( BoardView board, Bitmap[] list );
		public void onWin( BoardView board, Bitmap[] list );
		public void onAutoSelectDie( int die, int mask );
	}
	
	OnUpdateListener listener;
	
	class Dice {
		private Random rnd = new Random( System.currentTimeMillis( ) );
		private int die1, die2;
		private boolean valid1, valid2;
		
		public Dice( ) {
			roll( );
		}
		
		public void roll( ) {
			die1 = Math.abs( rnd.nextInt( ) ) % 4 + 2; // a die cannot be a pawn.
			die2 = Math.abs( rnd.nextInt( ) ) % 4 + 2;
			valid1 = valid2 = true;
		}
		
		public int hasEx( int roll ) {
			if( valid1 && die1 == roll ) return roll;
			if( valid2 && die2 == roll ) return roll;
			// special case: rolled a raja, but can move a pawn.
			if( valid1 && die1 == 5 && roll == 1 ) return 5;
			if( valid2 && die2 == 5 && roll == 1 ) return 5;
			// pawns have a special case, if we are out of some piece.
			if( roll == 1 ) return 1;
			return 0;
		}
		
		public boolean has( int roll ) {
			return hasEx(roll) > 1;
		}
		
		public boolean pawncheck( boolean hasShip, boolean hasHorse, boolean hasElephant ) {
			if( ( ( die1 == 2 && valid1 ) || ( die2 == 2 && valid2 ) ) && hasShip ) return true;
			if( ( ( die1 == 3 && valid1 ) || ( die2 == 3 && valid2 ) ) && hasHorse ) return true;
			if( ( ( die1 == 4 && valid1 ) || ( die2 == 4 && valid2 ) ) && hasElephant ) return true;
			return false;
		}
		
		public void use( int roll ) {
			if( valid1 && die1 == roll ) valid1 = false;
			else if( valid2 && die2 == roll ) valid2 = false;
		}
		
		public boolean isValid( ) {
			return valid1 | valid2;
		}
		
		public int getDie( int n ) {
			if( n == 1 && valid1 ) return die1;
			if( n == 2 && valid2 ) return die2;
			return 0;
		}
		
		public int save( ) {
			int saved = (die1<<4)|die2;
			if( valid1 ) saved |= 256;
			if( valid2 ) saved |= 512;
			return saved;
		}
		
		public void restore( int saved ) {
			die1 = (saved>>4)&0x7;
			die2 = saved&0x7;
			valid1 = (saved & 256) == 256;
			valid2 = (saved & 512) == 512;
		}
	}
	
	private int turn;
	public Dice dice;
	public int selected_die;

	public BoardView( Context context, AttributeSet attrs ) {
		super(context, attrs);
		
		dice = new Dice( );
		
		loadImages( );
		mImgSize = pawns[0].getWidth( );
		mSize = mImgSize * 8;
		
		board = new int[8][];
		for( int i = 0; i < 8; i++ )
			board[i] = new int[8];
		
		anim_handler = new Handler( );
		anim_task = new Runnable( ) {
			public void run( ) {
				anim_percent+=5;
				if( anim_percent == 100 ) {
					animating = false;
					anim_percent = 0;
					board[anim_x1][anim_y1] = board[anim_x0][anim_y0];
					board[anim_x0][anim_y0] = 0;
					
					anim_handler.removeCallbacks( this );
					if( !didwin( ) ) {
						if( teamcontrol[turn-1] == CONTROL_COMPUTER )
							comp.checkRetrieve( anim_x1, anim_y1 );
						else
							checkRetrieve( anim_x1, anim_y1 );
						
						if( anim_on_finish_comp_move ) {
							anim_on_finish_comp_move = false;
							boolean compmove = comp.move( anim_comp1, anim_comp2 );
							if( compmove ) {
								anim_on_finish_new_turn = true;
								return;
							} else {
								nextTurn( );
							}
						}
						else if( anim_on_finish_new_turn ) {
							anim_on_finish_new_turn = false;
							invalidate( );
							nextTurn( );
							return;
						}
					}
					anim_on_finish_new_turn = false;
				} else {
					anim_handler.postDelayed( this, ANIM_DELAY );
				}
				invalidate( );
			}
		};
		
		teamcontrol = new int[]{0,1,1,1};
		
		comp = new ComputerPlayer( this );
		
		initBoard( );
	}
	
	private void undo_init( ) {
		undo_stackptr = -1;
		if( undo_newx == null ) {
			undo_newx = new int[UNDO_STACK_SIZE*2];
			undo_newy = new int[UNDO_STACK_SIZE*2];
			undo_newpiece = new int[UNDO_STACK_SIZE*2];
			undo_oldx = new int[UNDO_STACK_SIZE*2];
			undo_oldy = new int[UNDO_STACK_SIZE*2];
			undo_oldpiece = new int[UNDO_STACK_SIZE*2];
			undo_turn = new int[UNDO_STACK_SIZE*2];
			undo_dice = new int[UNDO_STACK_SIZE*2];
		}
	}
	
	private void undo_push( int nx, int ny, int np, int ox, int oy, int op ) {
		if( undo_stackptr == UNDO_STACK_SIZE * 2 - 1 ) {
			System.arraycopy( undo_newx, UNDO_STACK_SIZE, undo_newx, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_newy, UNDO_STACK_SIZE, undo_newy, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_newpiece, UNDO_STACK_SIZE, undo_newpiece, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_oldx, UNDO_STACK_SIZE, undo_oldx, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_oldy, UNDO_STACK_SIZE, undo_oldy, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_oldpiece, UNDO_STACK_SIZE, undo_oldpiece, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_turn, UNDO_STACK_SIZE, undo_turn, 0, UNDO_STACK_SIZE );
			System.arraycopy( undo_dice, UNDO_STACK_SIZE, undo_dice, 0, UNDO_STACK_SIZE );
			undo_stackptr = UNDO_STACK_SIZE - 1;
		}
		
		undo_stackptr++;
		
		undo_newx[undo_stackptr] = nx;
		undo_newy[undo_stackptr] = ny;
		undo_newpiece[undo_stackptr] = np;
		undo_oldx[undo_stackptr] = ox;
		undo_oldy[undo_stackptr] = oy;
		undo_oldpiece[undo_stackptr] = op;
		undo_turn[undo_stackptr] = turn;
		undo_dice[undo_stackptr] = dice.save( );
	}
	
	private boolean undo_pop( ) {
		if( undo_stackptr < 0 )
			return false;
		
		board[undo_newx[undo_stackptr]][undo_newy[undo_stackptr]] = undo_newpiece[undo_stackptr];
		board[undo_oldx[undo_stackptr]][undo_oldy[undo_stackptr]] = undo_oldpiece[undo_stackptr];
		turn = undo_turn[undo_stackptr];
		dice.restore( undo_dice[undo_stackptr] );
		
		undo_stackptr--;
		
		return undo_stackptr >= 0;
	}
	
	private void undo_move( int x0, int y0, int x1, int y1 ) {
		undo_push( x0, y0, board[x0][y0], x1, y1, board[x1][y1] );
		board[x1][y1] = board[x0][y0];
		board[x0][y0] = 0;
	}
	
	public void undo( ) {
		if( animating ) {
			animating = false;
			anim_handler.removeCallbacks( anim_task );
		}
		
		// make sure there's a human player here to undo
		int sp = undo_stackptr;
		while( sp >= 0 ) {
			if( teamcontrol[undo_turn[sp]-1] == CONTROL_HUMAN )
				break;
			sp--;
		}
		if( sp < 0 )
			return;
		
		// undo all the computer moves first
		//int team = undo_turn[undo_stackptr]-1;
		while( undo_stackptr >= 0 ) {
			if( teamcontrol[undo_turn[undo_stackptr]-1] != CONTROL_COMPUTER )
				break;
			undo_pop( );
		}
		undo_pop( );
		
		int rajas = 0;
		for( int[] row : board ) {
			for( int piece : row ) {
				if( ( piece & 0x7 ) == 5 )
					rajas++;
			}
		}
		won = rajas <= 1;
		invalidate( );
	}
	
	protected void loadImages( ) {
		Resources res = getResources( );
		
		background = BitmapFactory.decodeResource( res, R.drawable.bkgnd_large );
		
		retrievemessage = BitmapFactory.decodeResource( res, R.drawable.retrievepieces );
		rajathrone = BitmapFactory.decodeResource( res, R.drawable.awcomehere );
		youwin = BitmapFactory.decodeResource( res, R.drawable.youwin );
		youlose = BitmapFactory.decodeResource( res, R.drawable.youlose );
		
		pawns = new Bitmap[4];
		pawns[0] = BitmapFactory.decodeResource( res, R.drawable.lpblack );
		pawns[1] = BitmapFactory.decodeResource( res, R.drawable.lpblue );
		pawns[2] = BitmapFactory.decodeResource( res, R.drawable.lpred );
		pawns[3] = BitmapFactory.decodeResource( res, R.drawable.lpgreen );
		
		ships = new Bitmap[4];
		ships[0] = BitmapFactory.decodeResource( res, R.drawable.lsblack );
		ships[1] = BitmapFactory.decodeResource( res, R.drawable.lsblue );
		ships[2] = BitmapFactory.decodeResource( res, R.drawable.lsred );
		ships[3] = BitmapFactory.decodeResource( res, R.drawable.lsgreen );
		
		horses = new Bitmap[4];
		horses[0] = BitmapFactory.decodeResource( res, R.drawable.lhblack );
		horses[1] = BitmapFactory.decodeResource( res, R.drawable.lhblue );
		horses[2] = BitmapFactory.decodeResource( res, R.drawable.lhred );
		horses[3] = BitmapFactory.decodeResource( res, R.drawable.lhgreen );
		
		elephants = new Bitmap[4];
		elephants[0] = BitmapFactory.decodeResource( res, R.drawable.leblack );
		elephants[1] = BitmapFactory.decodeResource( res, R.drawable.leblue );
		elephants[2] = BitmapFactory.decodeResource( res, R.drawable.lered );
		elephants[3] = BitmapFactory.decodeResource( res, R.drawable.legreen );
		
		rajas = new Bitmap[4];
		rajas[0] = BitmapFactory.decodeResource( res, R.drawable.lrblack );
		rajas[1] = BitmapFactory.decodeResource( res, R.drawable.lrblue );
		rajas[2] = BitmapFactory.decodeResource( res, R.drawable.lrred );
		rajas[3] = BitmapFactory.decodeResource( res, R.drawable.lrgreen );
		
		rajapawns = new Bitmap[4];
		rajapawns[0] = BitmapFactory.decodeResource( res, R.drawable.lrpblack );
		rajapawns[1] = BitmapFactory.decodeResource( res, R.drawable.lrpblue );
		rajapawns[2] = BitmapFactory.decodeResource( res, R.drawable.lrpred );
		rajapawns[3] = BitmapFactory.decodeResource( res, R.drawable.lrpgreen );
		
		pieces_list = new Bitmap[6][];
		pieces_list[0] = pawns;
		pieces_list[1] = ships;
		pieces_list[2] = horses;
		pieces_list[3] = elephants;
		pieces_list[4] = rajas;
		pieces_list[5] = rajapawns;
	}
	
	public int getPlayerMask( ) {
		int mask = 0;
		for( int i = 0; i < 4; i++ ) {
			mask |= teamcontrol[i]<<i<<i;
		}
		
		return mask;
	}
	
	public void checkSettings( ) {
		Settings settings = Settings.getSettings( );
		if( settings == null ) {
			return;
		}
		
		if( settings.isUpdated( ) ) {
			settings.validate( );
			
			// load everything here
			touch_mode = settings.touch_mode;
			bkgnd_color = settings.background_color;
			rotation = settings.rotation;
			cont_on_lose = settings.contonlose;
			comp.difficulty = settings.compdiff;
			bkgnd_usebitmap = settings.usebitmap;
			
			if( listener != null )
				listener.onRecolor( this, bkgnd_color );
			invalidate( );
		}
	}
	
	public void save( Bundle bundle ) {
		if( animating && anim_x0 >= 0 && anim_x0 < 8 && anim_y0 >= 0 && anim_y0 < 8 ) {
			// cut the animation and just move the pieces.
			anim_handler.removeCallbacks( anim_task );
			undo_move( anim_x0, anim_y0, anim_x1, anim_y1 );
		}
		for( int i = 0; i < 8; i++ )
			bundle.putIntArray( "row"+i, board[i] );
		bundle.putBoolean( "won", won );
		bundle.putBoolean( "retrieving", retrieving );
		bundle.putInt( "retrievex", retrievex );
		bundle.putInt( "retrievey", retrievey );
		bundle.putInt( "retrieveflags", retrieveflags );
		bundle.putBoolean( "capture1", capture1 );
		bundle.putBoolean( "capture2", capture2 );
		bundle.putBoolean( "capture3", capture3 );
		bundle.putBoolean( "capture4", capture4 );
		bundle.putInt( "rotation", rotation );
		bundle.putInt( "turn", turn );
		bundle.putIntArray( "teamcontrol", teamcontrol );
		bundle.putBoolean( "finishcomputerturn", animating && anim_on_finish_comp_move );
		bundle.putBoolean( "nextturn", animating && anim_on_finish_new_turn );
		animating = false;
		anim_percent = 0;
		bundle.putInt( "team2", anim_comp2 );
		bundle.putInt( "dice", dice.save( ) );
	}
	
	public void restore( Bundle bundle ) {
		for( int i = 0; i < 8; i++ )
			board[i] = bundle.getIntArray( "row"+i );
		won = bundle.getBoolean( "won" );
		retrieving = bundle.getBoolean( "retrieving" );
		retrievex = bundle.getInt( "retrievex" );
		retrievey = bundle.getInt( "retrievey" );
		retrieveflags = bundle.getInt( "retrieveflags" );
		capture1 = bundle.getBoolean( "capture1" );
		capture2 = bundle.getBoolean( "capture2" );
		capture3 = bundle.getBoolean( "capture3" );
		capture4 = bundle.getBoolean( "capture4" );
		rotation = bundle.getInt( "rotation" );
		turn = bundle.getInt( "turn" );
		teamcontrol = bundle.getIntArray( "teamcontrol" );
		dice.restore( bundle.getInt( "dice" ) );
		if( bundle.getBoolean( "nextturn" ) ) {
			nextTurn( );
		} else if( bundle.getBoolean( "finishcomputerturn" ) ) {
			if( comp.move( turn, bundle.getInt( "team2" ) ) )
				anim_on_finish_new_turn = true;
			else
				nextTurn( );
		}
	}
	
	public Bitmap[][] getBitmapList( ) {
		return pieces_list;
	}
	
	public int[][] getBoard( ) {
		return board;
	}
		
	public void setOnUpdateListener( OnUpdateListener listener ) {
		this.listener = listener;
	}
	
	public boolean pieceExists( int piece ) {
		int type = piece&0x7;
		int team = piece>>4;
		for( int i = 0; i < 8; i++ ) {
			for( int j = 0; j < 8; j++ ) {
				if( board[i][j] == piece ) return true;
				else if( (team==1||team==3) && capture1 && board[i][j]==(type|(3<<4)) ) return true;
				else if( (team==2||team==4) && capture2 && board[i][j]==(type|(4<<4)) ) return true;
				else if( (team==3||team==1) && capture3 && board[i][j]==(type|(1<<4)) ) return true;
				else if( (team==4||team==2) && capture4 && board[i][j]==(type|(2<<4)) ) return true;
			}
		}
		return false;
	}

	private boolean didwin( ) {
		int rajacount = 0;
		boolean human_raja = false;
		for( int[] arr : board ) {
			for( int square : arr ) {
				if( ( square & 0x7 ) == 5 ) {
					rajacount++;
					if( teamcontrol[( square >> 4 ) - 1] != CONTROL_COMPUTER )
						human_raja = true;
				}
			}
		}
		
		if( !( cont_on_lose || human_raja ) || rajacount <= 1 ) {
			won = true;
			if( listener != null ) listener.onWin( this, getWinList( ) );
			src_x = dest_x = -1;
			invalidate( );
			return true;
		}
		return false;
	}
			
	public void nextTurn( ) {
		if( retrieving || won ) return;
		if( animating ) {
			anim_on_finish_new_turn = true;
			return;
		}
		
		src_x = dest_x = -1;
		do {
			if( turn == 4 ) turn = 1; else turn++;
		} while( !pieceExists( (turn<<4)|5 ) );
		dice.roll( );
		
		if( listener != null ) listener.onUpdate( this, teamcontrol[turn-1] );
		
		if( teamcontrol[turn-1] == CONTROL_COMPUTER ) {
			int team2 = -1;
			if( turn == 1 && capture1 ) team2 = 3;
			else if( turn == 2 && capture2 ) team2 = 4;
			else if( turn == 3 && capture3 ) team2 = 1;
			else if( turn == 4 && capture4 ) team2 = 2;
			
			boolean compmove = comp.move( turn, team2 );
			if( compmove ) {
				anim_on_finish_comp_move = true;
				anim_comp1 = turn;
				anim_comp2 = team2;
				return;
			} else {
				nextTurn( );
				return;
			}
		}
	}
	
	public void updateDiceView( DiceView dv ) {
		dv.setTeam( turn );
		dv.setGo( teamcontrol[turn-1] == 0 );
		dv.setDisplay( dice.getDie( 1 ), dice.getDie( 2 ) );
	}
	
	private boolean movesLeft( ) {
		return (dice.getDie( 1 ) != 0 || dice.getDie( 2 ) != 0);
	}
	
	public void setPlayers( int p1, int p2, int p3, int p4 ) {
		teamcontrol[0] = p1;
		teamcontrol[1] = p2;
		teamcontrol[2] = p3;
		teamcontrol[3] = p4;
	}
	
	// Note: tile = (team << 4) | piece
	// black = 1, blue = 2, red = 3, green = 4
	// pawn = 1, ship = 2, horse = 3, elephant = 4, raja = 5
	public void initBoard( ) {
		for( int i = 0; i < 8; i++ ) {
			Arrays.fill( board[i], 0 );
		}
		
		turn = 4;
		won = false;
		retrieving = false;
		rotation = 0;
		capture1 = capture2 = capture3 = capture4 = false;
		anim_on_finish_new_turn = false;
		anim_on_finish_comp_move = false;
		animating = false;
		checkSettings( );
		
		board[0][6] = board[1][6] = board[2][6] = board[3][6] = (1<<4)|1;
		board[0][7] = (1<<4)|2;
		board[1][7] = (1<<4)|3;
		board[2][7] = (1<<4)|4;
		board[3][7] = (1<<4)|5;
		
		board[1][0] = board[1][1] = board[1][2] = board[1][3] = (2<<4)|1;
		board[0][0] = (2<<4)|2;
		board[0][1] = (2<<4)|3;
		board[0][2] = (2<<4)|4;
		board[0][3] = (2<<4)|5;
		
		board[7][1] = board[6][1] = board[5][1] = board[4][1] = (3<<4)|1;
		board[7][0] = (3<<4)|2;
		board[6][0] = (3<<4)|3;
		board[5][0] = (3<<4)|4;
		board[4][0] = (3<<4)|5;
		
		board[6][7] = board[6][6] = board[6][5] = board[6][4] = (4<<4)|1;
		board[7][7] = (4<<4)|2;
		board[7][6] = (4<<4)|3;
		board[7][5] = (4<<4)|4;
		board[7][4] = (4<<4)|5;
		
		undo_init( );
		
		nextTurn( );
	}
	
	public void replacePiece( int choice ) {
		retrieving = false;
		if( choice == 0 ) {
			invalidate( );
			return;
		}
		
		int piece = 0;
		if( choice == 3 ) piece = 4;
		else if( choice == 2 ) {
			// either ship or horse -> elephant, if both, then it's a horse.
			if( (retrieveflags & 0x3)<0x3 ) piece = 4; else piece = 3;
		} else {
			// choice = 1
			if( (retrieveflags & 0x1)==0x1 ) piece = 2;
			else if( (retrieveflags & 0x2)==0x2 ) piece = 3;
			else piece = 4;
		}
		board[retrievex][retrievey] &= ~0xF;
		board[retrievex][retrievey] |= piece;
		if( !movesLeft( ) )
			nextTurn( );
		invalidate( );
	}
	
	public int getReplacementPiece( ) {
		if( dice.has( 2 ) && !pieceExists( (turn<<4)|2 ) ) return 2;
		if( dice.has( 3 ) && !pieceExists( (turn<<4)|3 ) ) return 3;
		if( dice.has( 4 ) && !pieceExists( (turn<<4)|4 ) ) return 4;
		return 0;
	}
	
	public boolean isLegalDiceMove( int piece ) {
		// if it's a pawn, and we are missing a horse/ship/elephant that got rolled
		// then it's a legal move
		if( (piece&0x7) == 1 ) {
			if( getReplacementPiece( ) != 0 )
				return true;
		}
		return dice.has( piece&0x7 );
	}
	
	public boolean canMoveTo( int piece, int x1, int y1, int x2, int y2 ) {
		if( x1 < 0 || y1 < 0 || x2 < 0 || y2 < 0 ||
				x1 >= 8 || y1 >= 8 || x2 >= 8 || y2 >= 8 ) return false;
		int team1 = piece>>4;
		int team2 = board[x2][y2]>>4;
		
		if( capture1 && team1 == 3 ) team1 = 1;
		else if( capture2 && team1 == 4 ) team1 = 2;
		else if( capture3 && team1 == 1 ) team1 = 3;
		else if( capture4 && team1 == 2 ) team1 = 4;
		if( capture1 && team2 == 3 ) team2 = 1;
		else if( capture2 && team2 == 4 ) team2 = 2;
		else if( capture3 && team2 == 1 ) team2 = 3;
		else if( capture4 && team2 == 2 ) team2 = 4;
		
		if( team1 == team2 ) return false;
		if( team1 != turn ) return false;
		if( !isLegalDiceMove( piece ) ) return false;
		
		// determine which direction is forward for this team (for pawns)
		int xforward = 0, yforward = 0;
		switch( piece>>4 ) {
		case 1:
			yforward = -1;
			break;
		case 2:
			xforward = 1;
			break;
		case 3:
			yforward = 1;
			break;
		case 4:
			xforward = -1;
			break;
		}
		
		int dx = x2 - x1, dy = y2 - y1;
		switch( piece&0x7 ) {
		case 1:
			// pawn. can only go forward, and must take diagonally.
			if( ( x2 == x1+xforward ) && ( y2 == y1+yforward ) && board[x2][y2] == 0 )
				return true;
			if( x1 != x2 && y1 != y2 && ( (
					( x2 == x1+xforward && ( y2 == y1 + 1 || y2 == y1 - 1 ) ) ||
					( y2 == y1+yforward && ( x2 == x1 + 1 || x2 == x1 - 1 ) )
					) && board[x2][y2] != 0
					) )
				return true;
			break;
		case 2:
			// ship. can only move diagonally by 2 spaces and can't take rajas.
			if( Math.abs( dx ) == 2 && Math.abs( dy ) == 2 && (board[x2][y2]&0x7) != 5 )
				return true;
			break;
		case 3:
			// horse. move in a triangle of lengths (2,1), therefore |dx|+|dy| = 3
			// and neither of them must be zero.
			if( dx == 0 || dy == 0 ) return false;
			if( Math.abs( dx ) + Math.abs( dy ) == 3 )
				return true;
			break;
		case 4:
			// elephant. moves in a straight line and can't jump anything.
			if( dx == 0 ) {
				// moving along the y axis
				for( int i = Math.min( y1, y2 ); i <= Math.max( y1, y2 ); i++ )
					if( board[x1][i] != 0 && i != y1 && i != y2 ) return false;
				return true;
			}
			else if( dy == 0 ) {
				// moving along the x axis
				for( int i = Math.min( x1, x2 ); i <= Math.max( x1, x2 ); i++ )
					if( board[i][y1] != 0 && i != x1 && i != x2 ) return false;
				return true;
			}
			break;
		case 5:
			// raja. can only move within one square
			if( Math.abs( dx ) <= 1 && Math.abs( dy ) <= 1 ) return true;
			break;
		}
		
		return false;
	}
	
	public boolean canMoveTo( int x1, int y1, int x2, int y2 ) {
		return canMoveTo( board[x1][y1], x1, y1, x2, y2 );
	}
	
	public Bitmap[] getWinList( ) {
		Bitmap[] list = new Bitmap[5];
		for( int i = 0; i < 5; i++ )
			list[i] = pieces_list[i][turn-1];
		return list;
	}
	
	public Bitmap[] getRetrieveList( ) {
		boolean ship = false, horse = false, elephant = false;
		int count = 3;
		int rteam = board[retrievex][retrievey]>>4;
		for( int[] row : board ) {
			for( int pc : row ) {
				if( pc == ((rteam<<4)|2) ) { ship = true; count--; }
				else if( pc == ((rteam<<4)|3) ) { horse = true; count--; }
				else if( pc == ((rteam<<4)|4) ) { elephant = true; count--; }
			}
		}
		retrieveflags = 0;
		Bitmap[] rtrv_list = new Bitmap[count+1];
		
		count = 0;
		if( !ship ) { rtrv_list[1] = pieces_list[1][rteam-1]; count++; retrieveflags = 1; }
		if( !horse ) { rtrv_list[count+1] = pieces_list[2][rteam-1]; count++; retrieveflags |= 2; }
		if( !elephant ) { rtrv_list[count+1] = pieces_list[3][rteam-1]; retrieveflags |= 4; }
		
		return rtrv_list;
	}
	
	private void checkRetrieve( int cx, int cy ) {
		int newpiece = board[cx][cy];
		if( ( newpiece&0x7 ) == 1 ) {
			int rteam = newpiece>>4;
			boolean canretrieve = false;
			switch( rteam ) {
			case 1:
				if( cy == 0 ) canretrieve = true;
				break;
			case 2:
				if( cx == 7 ) canretrieve = true;
				break;
			case 3:
				if( cy == 7 ) canretrieve = true;
				break;
			case 4:
				if( cx == 0 ) canretrieve = true;
				break;
			}
			
			if( canretrieve ) {
				retrievex = cx;
				retrievey = cy;
				Bitmap[] list = getRetrieveList( );
				if( list.length > 1 ) {
					if( listener != null ) {
						retrieving = true;
						listener.onRetrieve( this, list );
					}
				}
			}
		}
		
		// check takeovers also
		if( (newpiece&0x7) == 5 ) {
			if( cx == 4 && cy == 0 && (newpiece>>4)==1 && !pieceExists( (3<<4)|5 ) )
				capture1 = true;
			else if( cx == 7 && cy == 4 && (newpiece>>4)==2 && !pieceExists( (4<<4)|5 ) )
				capture2 = true;
			else if( cx == 3 && cy == 7 && (newpiece>>4)==3 && !pieceExists( (1<<4)|5 ) )
				capture3 = true;
			else if( cx == 0 && cy == 3 && (newpiece>>4)==4 && !pieceExists( (2<<4)|5 ) )
				capture4 = true;
		}
	}
	
	public void animate( int x0, int y0, int x1, int y1 ) {
		animating = true;
		anim_x0 = x0;
		anim_y0 = y0;
		anim_x1 = x1;
		anim_y1 = y1;
		
		undo_push( x0, y0, board[x0][y0], x1, y1, board[x1][y1] );
		
		anim_handler.postDelayed( anim_task, ANIM_DELAY );
	}
	
	private void findmoves( ) {
		// find moves for (src_x,src_y) and set dispbit
		ArrayList<ComputerPlayer.Move> moves = new ArrayList<ComputerPlayer.Move>( );
		switch( board[src_x][src_y]&0x7 ) {
		case 1:
			comp.getPawnMoves( moves, src_x, src_y, false );
			break;
		case 2:
			comp.getShipMoves( moves, src_x, src_y, false );
			break;
		case 3:
			comp.getHorseMoves( moves, src_x, src_y, false );
			break;
		case 4:
			comp.getElephantMoves( moves, src_x, src_y, false );
			break;
		case 5:
			comp.getRajaMoves( moves, src_x, src_y, false );
			break;
		}
		
		dispbit1 = dispbit2 = 0;
		for( ComputerPlayer.Move m : moves ) {
			int bit = m.x_to*8+m.y_to;
			if( bit >= 32 ) {
				dispbit2 |= 1<<(bit&~32);
				// !!! note that math is ok because bit will never be 64 or greater.
			} else {
				dispbit1 |= 1<<bit;
			}
		}
	}
	
	private void diceselect( int piece ) {
		boolean ship = false, horse = false, elephant = false;
		int mask = (1 << piece) >>> 1;
		
		if( piece == 1 ) {
			for( int[] row : board ) {
				for( int pc : row ) {
					if( (pc>>4) == turn ) {
						switch( pc&0x7 ) {
						case 2:
							ship = true;
							break;
						case 3:
							horse = true;
							break;
						case 4:
							elephant = true;
							break;
						}
					}
				}
			}
			mask = 0x10;
			piece = 5;
			if( !elephant && dice.has( 4 ) ) { mask |= 0x8; piece = 4; }
			if( !horse && dice.has( 3 ) ) { mask |= 0x4; piece = 3; }
			if( !ship && dice.has( 2 ) ) { mask |= 0x2; piece = 2; }
		}
		
		selected_die = piece;
		if( listener != null )
			listener.onAutoSelectDie( piece, mask );
	}
	
	@Override
	public boolean onTouchEvent( MotionEvent event ) {
		int action = event.getAction( );
		int x = (int)event.getX( ), y = (int)event.getY( );
		int padx = mPadX, pady = mPadY;
		
		int size = mSize;
		int imgsize = mImgSize;
		
		if( animating ) {
			if( src_x >= 0 ) {
				src_x = dest_x = -1;
				invalidate( );
			}
			return true;
		}
		
		if( x < padx || y < pady || x >= (padx+size) || y >= (pady+size) ) {
			// only check/reset x's to -1
			if( src_x >= 0 ) {
				src_x = dest_x = -1;
				invalidate( );
			}
			return true;
		}
		
		int cx = (x-padx)/imgsize, cy = (y-pady)/imgsize;
		
		switch( rotation ) {
		case 0:
			break;
		case 1:
			cx ^= cy;
			cy ^= cx;
			cx ^= cy;
			cx = 7-cx;
			break;
		case 2:
			cx = 7-cx;
			cy = 7-cy;
			break;
		case 3:
			cx ^= cy;
			cy ^= cx;
			cx ^= cy;
			cy = 7-cy;
			break;
		}
		
		switch( action ) {
		case MotionEvent.ACTION_DOWN:
			if( touch_mode && (board[cx][cy] != 0 || src_x != -1 )) {
				dest_x = cx;
				dest_y = cy;
			} else if( !touch_mode && board[cx][cy] != 0 ) {
				src_x = cx;
				src_y = cy;
				mXDraw = x;
				mYDraw = y;
				diceselect( board[cx][cy] & 0x7 );
				invalidate( );
			}
			return true;
		case MotionEvent.ACTION_UP:
			if( src_x < 0 ){
				if( touch_mode && board[cx][cy] != 0 && !animating ) {
					src_x = cx;
					src_y = cy;
					findmoves( );
					if( dispbit1 == 0 && dispbit2 == 0 ) {
						// no moves
						src_x = -1;
						return true;
					}
					diceselect( board[cx][cy] & 0x7 );
					invalidate( );
				}
				
				return true;
			}
			
			if( teamcontrol[turn-1] != CONTROL_HUMAN ) {
				src_x = dest_x = -1;
				invalidate( );
				if( listener != null )
					listener.onAutoSelectDie( 0, 0 );
				return true;
			}
			
			if( canMoveTo( src_x, src_y, cx, cy ) ) {
				if( touch_mode ) {
					animate( src_x, src_y, cx, cy );
				} else {
					undo_move( src_x, src_y, cx, cy );
				}
				
				dice.use( selected_die );
				
				if( didwin( ) )
					return true;
				
				// was it a pawn? if so, do we get a piece back?
				if( !touch_mode )
					checkRetrieve( cx, cy );
				
				if( !movesLeft( ) )
					nextTurn( );
				else if( listener != null )
					listener.onUpdate( this, teamcontrol[turn-1] );
			}
			if( listener != null )
				listener.onAutoSelectDie( 0, 0 );
			src_x = dest_x = -1;
			invalidate( );
			return true;
		case MotionEvent.ACTION_MOVE:
			if( src_x < 0 ) return true;
			if( touch_mode ) {
				// if we move off the square, reset everything
				if( cx != dest_x || cy != dest_y ) {
					src_x = -1;
					invalidate( );
				}
				return true;
			}
			mXDraw = x;
			mYDraw = y;
			if( teamcontrol[turn-1] == CONTROL_HUMAN && canMoveTo( src_x, src_y, cx, cy ) ) {
				dest_x = cx;
				dest_y = cy;
			}
			else
				dest_x = -1;
			invalidate( );
			return true;
		}
		
		return false;
	}
	
	private void drawRectWithRot( Canvas canvas, int padx, int pady, int x, int y, int w, int h, Paint paint ) {
		Rect r = null;
		int twosixtyeight = mSize - mImgSize + 2;
		
		switch( rotation ) {
		case 0:
			r = new Rect( padx+x, pady+y, padx+x+w, pady+y+h );
			break;
		case 1:
			r = new Rect( padx+y, pady+(twosixtyeight-x), padx+y+w, pady+(twosixtyeight-x)+h );
			break;
		case 2:
			r = new Rect( padx+(twosixtyeight-x), pady+(twosixtyeight-y), padx+(twosixtyeight-x)+w, pady+(twosixtyeight-y)+h );
			break;
		case 3:
			r = new Rect( padx+(twosixtyeight-y), pady+x, padx+(twosixtyeight-y)+w, pady+x+h );
			break;
		}
		
		canvas.drawRect( r, paint );
	}
	
	private void drawBitmapWithRot( Canvas canvas, Bitmap bmp, int padx, int pady, int x, int y, Paint paint ) {
		int twosixtysix = mSize - mImgSize;
		
		switch( rotation ) {
		case 0:
			break;
		case 1:
			x ^= y;
			y ^= x;
			x ^= y;
			y = twosixtysix - y;
			break;
		case 2:
			x = twosixtysix - x;
			y = twosixtysix - y;
			break;
		case 3:
			x ^= y;
			y ^= x;
			x ^= y;
			x = twosixtysix - x;
			break;
		}
		
		canvas.drawBitmap( bmp, padx+x, pady+y, paint );
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// draw the background
		Paint paint = new Paint( );
		paint.setColor( bkgnd_color );
		int size = mSize;
		int imgsize = mImgSize;
		//canvas.setMatrix( null );
		
		// pad the gameboard, so it stays the same size always.
		int width = getWidth( ), height = getHeight( );
		int fillDimension = size;//(int)(size * mScale);
		int padx = (width - fillDimension) / 2;
		int pady = (height - fillDimension) / 2;
		mPadX = padx;
		mPadY = pady;
		
		if( bkgnd_usebitmap )
			canvas.drawBitmap( background, padx, pady, paint );
		else
			canvas.drawRect( new Rect( padx, pady, fillDimension + padx, fillDimension + pady ), paint );
		
		// draw the grid lines
		paint.setColor( 0x55000000 );
		for( int i = 0; i <= 8; i++ ) {
			canvas.drawLine( padx+i*imgsize, pady, padx+i*imgsize, pady+size, paint );
			canvas.drawLine( padx, pady+i*imgsize, padx+size, pady+i*imgsize, paint );
		}
				
		if( src_x >= 0 ) {
			// a piece is moving: change it's square's color
			paint.setColor( 0x550000BB );
			drawRectWithRot( canvas, padx, pady, src_x*imgsize+1, src_y*imgsize+1, imgsize-1, imgsize-1, paint );
			
			paint.setColor( 0xAA33CC44 );
			if( touch_mode ) {
				for( int i = 0; i < 32; i++ ) {
					if( (dispbit1 & (1<<i)) != 0 )
						drawRectWithRot( canvas, padx, pady, (i/8)*imgsize+1, (i%8)*imgsize+1, imgsize-1, imgsize-1, paint );
				}
				for( int i = 32; i < 64; i++ ) {
					if( (dispbit2 & (1<<(i&~32))) != 0 )
						drawRectWithRot( canvas, padx, pady, (i/8)*imgsize+1, (i%8)*imgsize+1, imgsize-1, imgsize-1, paint );
				}
			} else if( dest_x >= 0 ) {
				drawRectWithRot( canvas, padx, pady, dest_x*imgsize+1, dest_y*imgsize+1, imgsize-1, imgsize-1, paint );
			}
		}
		paint.setAlpha( 0xFF );
				
		drawBitmapWithRot( canvas, rajathrone, padx, pady, imgsize*3, imgsize*7, paint );
		drawBitmapWithRot( canvas, rajathrone, padx, pady, 0, imgsize*3, paint );
		drawBitmapWithRot( canvas, rajathrone, padx, pady, imgsize*4, 0, paint );
		drawBitmapWithRot( canvas, rajathrone, padx, pady, imgsize*7, imgsize*4, paint );
		
		if( board == null ) return;
		for( int i = 0; i < 8; i++ ) {
			for( int j = 0; j < 8; j++ ) {
				if( board[i][j] != 0 ) {
					if( !( animating && anim_x0 == i && anim_y0 == j ) )
						drawBitmapWithRot( canvas,
								pieces_list[(board[i][j]&0x7)-1][(board[i][j]>>4)-1],
								padx, pady, i*imgsize, j*imgsize, paint );
				}
			}
		}
		
		if( animating && anim_x0 >= 0 && anim_x0 < 8 && anim_y0 >= 0 && anim_y0 < 8) {
			int xpos, ypos;
			// v(t)=-(t-50)^2/50+50
			xpos = anim_percent*anim_percent;
			xpos -= xpos*anim_percent/150;
			ypos = xpos;
			xpos *= (anim_x1 - anim_x0) * imgsize;
			ypos *= (anim_y1 - anim_y0) * imgsize;
			xpos /= 3333;
			ypos /= 3333;
			xpos += anim_x0 * imgsize;
			ypos += anim_y0 * imgsize;
			drawBitmapWithRot( canvas,
				pieces_list[(board[anim_x0][anim_y0]&0x7)-1][(board[anim_x0][anim_y0]>>4)-1],
				padx, pady, xpos, ypos, paint );
		}
		else if( src_x >= 0 && !touch_mode )
			canvas.drawBitmap( pieces_list[(board[src_x][src_y]&0x7)-1][(board[src_x][src_y]>>4)-1],
					mXDraw-imgsize/2, mYDraw-imgsize/2, paint );
		// note -38/2 to center the image where it is being dragged.
		
		if( retrieving )
			canvas.drawBitmap( retrievemessage, padx, pady, paint );
		else if( won ) {
			if( teamcontrol[turn-1] == CONTROL_HUMAN )
				canvas.drawBitmap( youwin, padx, pady, paint );
			else
				canvas.drawBitmap( youlose, padx, pady, paint );
		}
	}
}
