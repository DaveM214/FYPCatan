package misc.bot.moves;

public class PiecePlacement extends BotMove {
	
	private final int coord;
	private final int pieceType;

	/**
	 * Constructor for the 
	 * 
	 * @param coord
	 * @param pieceType
	 */
	public PiecePlacement(int coord, int pieceType){
		super(BotMove.PIECE_PLACEMENT); 
		this.coord = coord;
		this.pieceType = pieceType;
	}
	
	public int getPieceType(){
		return pieceType;
	}
	
	public int getCoordinate(){
		return coord;
	}
	
}
