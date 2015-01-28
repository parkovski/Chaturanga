package com.parkovski;

import java.util.ArrayList;

public class ComputerPlayer {
	public int difficulty;
	
	BoardView mBoard;
	Move doublesmove;
	
	class Move {
		public int pointvalue;
		public int pointvalueadv; // potential future moves
		public int x_from, y_from, x_int, y_int, x_to, y_to;
		/*
		 * Most moves are only 1 step, but when doubles are rolled
		 * we can have a 2 step move, using the intermediate variables.
		 * This move is carried out, and the benefits are calculated
		 * but not the drawbacks, since the piece will not get left
		 * in that intermediate spot.
		 */
		
		public Move( int x1, int y1, int x2, int y2 ) {
			x_from = x1;
			y_from = y1;
			x_to = x2;
			y_to = y2;
			x_int = -1;
		}
	}
	
	public ComputerPlayer( BoardView board ) {
		mBoard = board;
	}
	
	int safeGetPieceAt( int x, int y ) {
		if( x < 0 || y < 0 || x > 7 || y > 7 )
			return -1;
		return mBoard.board[x][y];
	}
	
	public int assessAt( int piece, int x, int y ) {
		int points = 0;
		
		// look at who i could possibly be taken by...
		int[] rajaspaces = new int[]{	x-1,y-1,	x,y-1,	x+1,y-1,
										x-1,y,				x+1,y,
										x-1,y+1,	x,y+1,	x+1,y+1 };
		
		int[] shipspaces = new int[]{	x-2,y-2,	x+2,y-2,
										x-2,y+2,	x+2,y+2 };
		
		int[] horsespaces = new int[]{	x-2,y-1,	x-1,y-2,	x-2,y+1,	x-1,y+2,
										x+2,y-1,	x+1,y-2,	x+2,y+1,	x+1,y+2 };
		
		// easier just to scan...
		//int[] elephantspaces
		
		int[] pawnspaces = new int[]{	x-1,y-1,	x+1,y-1,
										x+1,y+1,	x-1,y+1 };
		
		int can_get_taken_count = 0;
		int myteam = piece>>4;
		int pc = 0;
		boolean rajas[] = new boolean[4];
		
		rajas[0] = mBoard.pieceExists( 0x15 );
		rajas[1] = mBoard.pieceExists( 0x25 );
		rajas[2] = mBoard.pieceExists( 0x35 );
		rajas[3] = mBoard.pieceExists( 0x45 );
		
		if( mBoard.capture1 && myteam != 1 ) rajas[2] = true;
		if( mBoard.capture2 && myteam != 2 ) rajas[3] = true;
		if( mBoard.capture3 && myteam != 3 ) rajas[0] = true;
		if( mBoard.capture4 && myteam != 4 ) rajas[1] = true;
		
		for( int i = 0; i < rajaspaces.length; i+=2 ) {
			pc = safeGetPieceAt( rajaspaces[i], rajaspaces[i+1] );
			if( (pc>>4) != myteam && (pc&0x7) == 5 && rajas[(pc>>4)-1] )
				can_get_taken_count++;
		}
		
		for( int i = 0; i < shipspaces.length; i+=2 ) {
			pc = safeGetPieceAt( shipspaces[i], shipspaces[i+1] );
			if( (pc>>4) != myteam && (pc&0x7) == 2 && (piece&0x7) != 5 && rajas[(pc>>4)-1] )
				can_get_taken_count++;
		}
		
		for( int i = 0; i < horsespaces.length; i+=2 ) {
			pc = safeGetPieceAt( horsespaces[i], horsespaces[i+1] );
			if( (pc>>4) != myteam && (pc&0x7) == 3 && rajas[(pc>>4)-1] )
				can_get_taken_count++;
		}
		
		int delta = 1;
		while( (pc = safeGetPieceAt( x-delta, y ) ) != -1 ) {
			if( (pc>>4) != myteam && (pc&0x7) == 4 && rajas[(pc>>4)-1] ) {
				can_get_taken_count++;
				break;
			} else if( pc != 0 ) {
				break;
			} else {
				delta++;
			}
		}
		
		delta = 1;
		while( (pc = safeGetPieceAt( x+delta, y ) ) != -1 ) {
			if( (pc>>4) != myteam && (pc&0x7) == 4 && rajas[(pc>>4)-1] ) {
				can_get_taken_count++;
				break;
			} else if( pc != 0 ) {
				break;
			} else {
				delta++;
			}
		}
		
		delta = 1;
		while( (pc = safeGetPieceAt( x, y-delta ) ) != -1 ) {
			if( (pc>>4) != myteam && (pc&0x7) == 4 && rajas[(pc>>4)-1] ) {
				can_get_taken_count++;
				break;
			} else if( pc != 0 ) {
				break;
			} else {
				delta++;
			}
		}
		
		delta = 1;
		while( (pc = safeGetPieceAt( x, y+delta ) ) != -1 ) {
			if( (pc>>4) != myteam && (pc&0x7) == 4 && rajas[(pc>>4)-1] ) {
				can_get_taken_count++;
				break;
			} else if( pc != 0 ) {
				break;
			} else {
				delta++;
			}
		}
		
		// now for pawns...
		pc = safeGetPieceAt( pawnspaces[0], pawnspaces[1] );
		if( ((pc>>4) == 2 || (pc>>4) == 3) && (pc>>4) != myteam && rajas[(pc>>4)-1] )
			can_get_taken_count++;
		pc = safeGetPieceAt( pawnspaces[2], pawnspaces[3] );
		if( ((pc>>4) == 3 || (pc>>4) == 4) && (pc>>4) != myteam && rajas[(pc>>4)-1] )
			can_get_taken_count++;
		pc = safeGetPieceAt( pawnspaces[4], pawnspaces[5] );
		if( ((pc>>4) == 4 || (pc>>4) == 1) && (pc>>4) != myteam && rajas[(pc>>4)-1] )
			can_get_taken_count++;
		pc = safeGetPieceAt( pawnspaces[6], pawnspaces[7] );
		if( ((pc>>4) == 1 || (pc>>4) == 2) && (pc>>4) != myteam && rajas[(pc>>4)-1] )
			can_get_taken_count++;
		
		if( can_get_taken_count > 0 ) {
			if( difficulty < 1 ) {
				switch( piece&0x7 ) {
				case 5:
					points = -20;
					break;
				default:
					points = -10;
					break;
				}
			} else {
				switch( piece&0x7 ) {
				case 1:
					points = -3 - 2 * can_get_taken_count;
					break;
				case 2:
					points = -1 - can_get_taken_count;
					break;
				case 3:
					points = -35 - 10 * can_get_taken_count;
					break;
				case 4:
					points = -40 - 10 * can_get_taken_count;
					break;
				case 5:
					points = -180 - 20 * can_get_taken_count;
					break;
				default:
					break;
				}
			}
		}
		
		return points;
	}
	
	public int assessFuture( Move move ) {
		
		// which piece is it?
		int piecewithteam = mBoard.board[move.x_from][move.y_from];
		int piece = piecewithteam&0x7;
		
		ArrayList<Move> moves = new ArrayList<Move>( 8 );
		switch( piece ) {
		case 1:
			getPawnMoves( moves, piecewithteam, move.x_to, move.y_to, false );
			break;
		case 2:
			getShipMoves( moves, piecewithteam, move.x_to, move.y_to, false );
			break;
		case 3:
			getHorseMoves( moves, piecewithteam, move.x_to, move.y_to, false );
			break;
		case 4:
			getElephantMoves( moves, piecewithteam, move.x_to, move.y_to, false );
			break;
		case 5:
			getRajaMoves( moves, piecewithteam, move.x_to, move.y_to, false );
			break;
		}
		
		// find the max possible value for going here.
		int maxpts = 0;
		for( Move m : moves ) {
			int points = assess( m );
			if( points > maxpts )
				maxpts = points;
		}
		
		return maxpts;
	}
	
	public int benefits( int ov, int nv, int newx, int newy ) {
		int total = 0;
		
		// taking a piece? which one?
		if( ov != 0 && mBoard.pieceExists( (ov&0xF0)|5 ) ) {
			if( difficulty < 1 ) {
				switch( ov&0x7 ) {
				case 5:
					total += 25;
					break;
				default:
					total += 5;
				}
			} else {
				switch( ov&0x7 ) {
				case 1:
					total += 10;
					break;
				case 2:
					total += 5;
					break;
				case 3:
					total += 25;
					break;
				case 4:
					total += 30;
					break;
				case 5:
					total += 50;
					break;
				default:
					break;
				}
			}
		}
		
		if( difficulty > 0 ) {
			// am i a pawn & we're missing a piece?
			if( (nv&0x7) == 1 ) {
				if( !mBoard.pieceExists( (nv&0xF0)|2 ) ||
						!mBoard.pieceExists( (nv&0xF0)|3 ) ||
						!mBoard.pieceExists( (nv&0xF0)|4 ) )
					total += 5;
			}
			
			// am i a raja? am i taking over the other team?
			if( (nv&0x7) == 5 ) {
				if(
						( (nv>>4) == 1 && newx == 4 && newy == 0 && !mBoard.pieceExists( 0x35 ) )
					||	( (nv>>4) == 2 && newx == 7 && newy == 4 && !mBoard.pieceExists( 0x45 ) )
					||	( (nv>>4) == 3 && newx == 3 && newy == 7 && !mBoard.pieceExists( 0x15 ) )
					||	( (nv>>4) == 4 && newx == 0 && newy == 3 && !mBoard.pieceExists( 0x25 ) )
					)
					total += 15;
			}
		}
		
		return total;
	}
	
	// find out the point value of the move
	public int assess( Move move, int nv ) {
		int ov; // old value of new spot
		ov = mBoard.board[move.x_to][move.y_to];
		
		int pts_start = assessAt( nv, move.x_from, move.y_from );
		int pts_finish = assessAt( nv, move.x_to, move.y_to );
		//int pts_inter = 0;
		
		int total = pts_finish - pts_start;
		
		// find the pluses of moving to the new spot
		total += benefits( ov, nv, move.x_to, move.y_to );
		
		// for doubles
		if( move.x_int != -1 )
			total += 1 + benefits( ov, nv, move.x_int, move.y_int );
		// note the 1 is added here just to encourage moving doubles
		// instead of moving out and then back in.
		
		return total;
	}
	
	public int assess( Move m ) {
		return assess( m, mBoard.board[m.x_from][m.y_from] );
	}
	
	// In the future, add code for looking at moves when doubles rolled.
	private void doublify( Move move, int x0, int y0 ) {
		move.x_int = move.x_from;
		move.y_int = move.y_from;
		move.x_from = x0;
		move.y_from = y0;
	}
	
	public void getPawnMoves( ArrayList<Move> moves, int piece, int x, int y, boolean dbl ) {
		Move move;
		ArrayList<Move> mymoves = new ArrayList<Move>( 2 );
		ArrayList<Move> dblmoves = null;
		if( dbl ) dblmoves = new ArrayList<Move>( 2 );
		
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
		
		// First check if we can go straight forward
		if( mBoard.canMoveTo( piece, x, y, x+xforward, y+yforward ) ) {
			move = new Move( x, y, x+xforward, y+yforward );
			mymoves.add( move );
		}
		
		// Now check if we can take a piece
		if( xforward == 0 ) {
			if( mBoard.canMoveTo( piece, x, y, x+1, y+yforward ) )
				mymoves.add( new Move( x, y, x+1, y+yforward ) );
			if( mBoard.canMoveTo( piece, x, y, x-1, y+yforward ) )
				mymoves.add( new Move( x, y, x-1, y+yforward ) );
		} else {
			if( mBoard.canMoveTo( piece, x, y, x+xforward, y+1 ) )
				mymoves.add( new Move( x, y, x+xforward, y+1 ) );
			if( mBoard.canMoveTo( piece, x, y, x+xforward, y-1 ) )
				mymoves.add( new Move( x, y, x+xforward, y-1 ) );
		}
		
		if( dbl ) {
			for( Move m : mymoves ) {
				getPawnMoves( dblmoves, piece, m.x_to, m.y_to, false );
				for( Move m2 : dblmoves ) {
					doublify( m2, x, y );
					moves.add( m2 );
				}
				dblmoves.clear( );
			}
		}
		moves.addAll( mymoves );
	}
	
	public void getPawnMoves( ArrayList<Move> moves, int x, int y, boolean dbl ) {
		getPawnMoves( moves, mBoard.board[x][y], x, y, dbl );
	}
	
	public void getShipMoves( ArrayList<Move> moves, int piece, int x, int y, boolean dbl ) {
		ArrayList<Move> mymoves = new ArrayList<Move>( 2 );
		ArrayList<Move> dblmoves = null;
		if( dbl ) dblmoves = new ArrayList<Move>( 2 );
		
		if( mBoard.canMoveTo( piece, x, y, x-2, y-2 ) )
			mymoves.add( new Move( x, y, x-2, y-2 ) );
		if( mBoard.canMoveTo( piece, x, y, x-2, y+2 ) )
			mymoves.add( new Move( x, y, x-2, y+2 ) );
		if( mBoard.canMoveTo( piece, x, y, x+2, y-2 ) )
			mymoves.add( new Move( x, y, x+2, y-2 ) );
		if( mBoard.canMoveTo( piece, x, y, x+2, y+2 ) )
			mymoves.add( new Move( x, y, x+2, y+2 ) );
		
		if( dbl ) {
			for( Move m : mymoves ) {
				getShipMoves( dblmoves, piece, m.x_to, m.y_to, false );
				for( Move m2 : dblmoves ) {
					doublify( m2, x, y );
					moves.add( m2 );
				}
				dblmoves.clear( );
			}
		}
		moves.addAll( mymoves );
	}
	
	public void getShipMoves( ArrayList<Move> moves, int x, int y, boolean dbl ) {
		getShipMoves( moves, mBoard.board[x][y], x, y, dbl );
	}
	
	public void getHorseMoves( ArrayList<Move> moves, int piece, int x, int y, boolean dbl ) {
		ArrayList<Move> mymoves = new ArrayList<Move>( 5 );
		ArrayList<Move> dblmoves = null;
		if( dbl ) dblmoves = new ArrayList<Move>( 5 );
		
		if( mBoard.canMoveTo( piece, x, y, x-1, y-2 ) )
			mymoves.add( new Move( x, y, x-1, y-2 ) );
		if( mBoard.canMoveTo( piece, x, y, x-1, y+2 ) )
			mymoves.add( new Move( x, y, x-1, y+2 ) );
		if( mBoard.canMoveTo( piece, x, y, x+1, y-2 ) )
			mymoves.add( new Move( x, y, x+1, y-2 ) );
		if( mBoard.canMoveTo( piece, x, y, x+1, y+2 ) )
			mymoves.add( new Move( x, y, x+1, y+2 ) );
		if( mBoard.canMoveTo( piece, x, y, x-2, y-1 ) )
			mymoves.add( new Move( x, y, x-2, y-1 ) );
		if( mBoard.canMoveTo( piece, x, y, x-2, y+1 ) )
			mymoves.add( new Move( x, y, x-2, y+1 ) );
		if( mBoard.canMoveTo( piece, x, y, x+2, y-1 ) )
			mymoves.add( new Move( x, y, x+2, y-1 ) );
		if( mBoard.canMoveTo( piece, x, y, x+2, y+1 ) )
			mymoves.add( new Move( x, y, x+2, y+1 ) );
		
		if( dbl ) {
			for( Move m : mymoves ) {
				getHorseMoves( dblmoves, piece, m.x_to, m.y_to, false );
				for( Move m2 : dblmoves ) {
					doublify( m2, x, y );
					moves.add( m2 );
				}
				dblmoves.clear( );
			}
		}
		moves.addAll( mymoves );
	}
	
	public void getHorseMoves( ArrayList<Move> moves, int x, int y, boolean dbl ) {
		getHorseMoves( moves, mBoard.board[x][y], x, y, dbl );
	}
	
	public void getElephantMoves( ArrayList<Move> moves, int piece, int x, int y, boolean dbl ) {
		ArrayList<Move> mymoves = new ArrayList<Move>( 8 );
		ArrayList<Move> dblmoves = null;
		if( dbl ) dblmoves = new ArrayList<Move>( 8 );
		
		int c2; // second coordinate
		
		c2 = x-1;
		while( mBoard.canMoveTo( piece, x, y, c2, y ) ) {
			mymoves.add( new Move( x, y, c2, y ) );
			c2--;
		}
		
		c2 = x+1;
		while( mBoard.canMoveTo( piece, x, y, c2, y ) ) {
			mymoves.add( new Move( x, y, c2, y ) );
			c2++;
		}
		
		c2 = y-1;
		while( mBoard.canMoveTo( piece, x, y, x, c2 ) ) {
			mymoves.add( new Move( x, y, x, c2 ) );
			c2--;
		}
		
		c2 = y+1;
		while( mBoard.canMoveTo( piece, x, y, x, c2 ) ) {
			mymoves.add( new Move( x, y, x, c2 ) );
			c2++;
		}
		
		if( dbl ) {
			for( Move m : mymoves ) {
				getElephantMoves( dblmoves, piece, m.x_to, m.y_to, false );
				for( Move m2 : dblmoves ) {
					doublify( m2, x, y );
					moves.add( m2 );
				}
				dblmoves.clear( );
			}
		}
		moves.addAll( mymoves );
	}
	
	public void getElephantMoves( ArrayList<Move> moves, int x, int y, boolean dbl ) {
		getElephantMoves( moves, mBoard.board[x][y], x, y, dbl );
	}

	public void getRajaMoves( ArrayList<Move> moves, int piece, int x, int y, boolean dbl ) {
		ArrayList<Move> mymoves = new ArrayList<Move>( 5 );
		ArrayList<Move> dblmoves = null;
		if( dbl ) dblmoves = new ArrayList<Move>( 5 );
		
		for( int i = -1; i <= 1; i++ ) {
			for( int j = -1; j <= 1; j++ ) {
				if( (i|j) != 0 && mBoard.canMoveTo( piece, x, y, x+i, y+j ) )
					mymoves.add( new Move( x, y, x+i, y+j ) );
			}
		}
		
		if( dbl ) {
			for( Move m : mymoves ) {
				getRajaMoves( dblmoves, piece, m.x_to, m.y_to, false );
				for( Move m2 : dblmoves ) {
					doublify( m2, x, y );
					moves.add( m2 );
				}
				dblmoves.clear( );
			}
		}
		moves.addAll( mymoves );
	}
	
	public void getRajaMoves( ArrayList<Move> moves, int x, int y, boolean dbl ) {
		getRajaMoves( moves, mBoard.board[x][y], x, y, dbl );
	}
	
	public void checkRetrieve( int x, int y ) {
		int rteam;
		boolean canretrieve;
		if( ( mBoard.board[x][y]&0x7 ) == 1 ) {
			rteam = mBoard.board[x][y]>>4;
			canretrieve = false;
			switch( rteam ) {
			case 1:
				if( y == 0 ) canretrieve = true;
				break;
			case 2:
				if( x == 7 ) canretrieve = true;
				break;
			case 3:
				if( y == 7 ) canretrieve = true;
				break;
			case 4:
				if( x == 0 ) canretrieve = true;
				break;
			}
		}
		else
			return;
		
		if( canretrieve ) {
			if( !mBoard.pieceExists( 4|(rteam<<4) ) )
				mBoard.board[x][y] = 4|(rteam<<4);
			else if( !mBoard.pieceExists( 3|(rteam<<4) ) )
				mBoard.board[x][y] = 3|(rteam<<4);
			else if( !mBoard.pieceExists( 2|(rteam<<4) ) )
				mBoard.board[x][y] = 2|(rteam<<4);
		}
	}
	
	public void execute( Move move ) {
		if( move.x_int != -1 ) {
			doublesmove = new Move( move.x_int, move.y_int, move.x_to, move.y_to );
			mBoard.animate( move.x_from, move.y_from, move.x_int, move.y_int );
		} else {
			mBoard.animate( move.x_from, move.y_from, move.x_to, move.y_to );
		}
	}
	
	public boolean move( int team, int team2 ) {
		if( doublesmove != null ) {
			execute( doublesmove );
			doublesmove = null;
			return true;
		}
		
		ArrayList<Move> moves = new ArrayList<Move>( 15 );
		int[][] board = mBoard.getBoard( );
		
		boolean ship = false, horse = false, elephant = false;
		
		firstscan:
		for( int i = 0; i < 8; i++ ) {
			for( int j = 0; j < 8; j++ ) {
				if( ( board[i][j]>>4 ) == team || ( board[i][j]>>4 ) == team2 ) {
					switch( board[i][j]&0x7 ) {
					case 2:
						ship = true;
						if( horse && elephant ) break firstscan;
						break;
					case 3:
						horse = true;
						if( ship && elephant ) break firstscan;
						break;
					case 4:
						elephant = true;
						if( ship && horse ) break firstscan;
						break;
					default:
						break;
					}
				}
			}
		}
		
		boolean valuablepieces = ship && horse && elephant;
		
		// note, doubles are very important since they change the logic of a move.
		int die1 = mBoard.dice.getDie( 1 ), die2 = mBoard.dice.getDie( 2 );
		boolean doubles = die1 == die2;
		boolean doubleship = doubles && die1 == 2;
		boolean doublehorse = doubles && die1 == 3;
		boolean doubleelephant = doubles && die1 == 4;
		boolean doubleraja = doubles && die1 == 5;
		boolean doublepawn = doubleraja;
		
		// since a pawn can be moved on so many different conditions...
		if( !doublepawn ) {
			if( doubles ) {
				doublepawn = ( doubleship && !ship ) || ( doublehorse && !horse )
							|| ( doubleelephant && !elephant );
			} else if( die1 == 5 || die2 == 5 ) {
				int otherdie = die1 == 5 ? die2 : die1;
				doublepawn = ( otherdie == 2 && !ship ) || ( otherdie == 3 && !horse )
							|| ( otherdie == 4 && !elephant );
			} else {
				doublepawn = ( ( die1 == 2 && !ship ) || ( die1 == 3 && !horse )
							|| ( die1 == 4 && !elephant ) )
							&&
							( ( die2 == 2 && !ship ) || ( die2 == 3 && !horse )
							|| ( die2 == 4 && !elephant ) );
			}
		}
		
		for( int i = 0; i < 8; i++ ) {
			for( int j = 0; j < 8; j++ ) {
				if( ( board[i][j]>>4 ) == team || ( board[i][j]>>4 ) == team2 ) {
					switch( board[i][j]&0x7 ) {
					case 1:
						if( mBoard.dice.has( 5 ) || !valuablepieces )
							getPawnMoves( moves, i, j, doublepawn );
						break;
					case 2:
						if( mBoard.dice.has( 2 ) )
							getShipMoves( moves, i, j, doubleship );
						break;
					case 3:
						if( mBoard.dice.has( 3 ) )
							getHorseMoves( moves, i, j, doublehorse );
						break;
					case 4:
						if( mBoard.dice.has( 4 ) )
							getElephantMoves( moves, i, j, doubleelephant );
						break;
					case 5:
						if( mBoard.dice.has( 5 ) )
							getRajaMoves( moves, i, j, doubleraja );
						break;
					default:
						break;
					} //switch
				} //if
			} //for
		} //for
		
		// now pick the moves with the best point value...
		
		int nrofmoves = moves.size( );
		if( nrofmoves == 0 )
			return false;
		ArrayList<Integer> possible_moves = null;
		int nr;
		
		// just so you never know, there is a very low chance we'll choose a
		// move that wouldn't be smart to take.
		// on easy mode that chance is much higher.
		final double chance = 0.15;
		if( difficulty > 0 || Math.random( ) > chance ) {
			// use good logic to find the move
			possible_moves = new ArrayList<Integer>( 5 );
			int maxpts = 0;
			for( int i = 0; i < nrofmoves; i++ ) {
				Move m = moves.get( i );
				if( difficulty > 0 )
					m.pointvalueadv = assessFuture( m );
				m.pointvalue = assess( m );
				if( m.pointvalue > 0 && m.pointvalueadv > 0 )
					m.pointvalue += m.pointvalueadv >> 3;
				if( m.pointvalue > maxpts )
					maxpts = m.pointvalue;
			}
			for( int i = 0; i < nrofmoves; i++ ) {
				if( moves.get( i ).pointvalue >= maxpts - 10 )
					possible_moves.add( i );
			}
			
			nr = possible_moves.size( );
			if( nr == 0 )
				return false;
			
			nr = (int)Math.floor( Math.random( ) * nr );
			nr = possible_moves.get( nr );
		} else {
			nr = (int)Math.floor( Math.random( ) * nrofmoves );
		}		
		
		Move move = moves.get( nr );
		
		execute( move );
		
		// update the dice
		// if we're moving a pawn and don't have this piece...
		if( (board[move.x_from][move.y_from]&0x7) == 1 ) {
			int rep = mBoard.getReplacementPiece( );
			if( rep != 0 ) mBoard.dice.use( rep ); else mBoard.dice.use( 5 );
		}
		else
			mBoard.dice.use( board[move.x_from][move.y_from]&0x7 );
		
		return true;
	}
}
