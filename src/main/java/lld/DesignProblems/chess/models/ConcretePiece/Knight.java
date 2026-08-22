package lld.DesignProblems.chess.models.ConcretePiece;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.Board;
import lld.DesignProblems.chess.models.Piece;
import lld.DesignProblems.chess.models.Position;

public class Knight extends Piece {
    public Knight(Color color){
        super(color);
    }

    @Override
    protected char getTypeSymbol() {
        return 'N';
    }

    @Override
    public boolean canMove(Board board, Position from, Position to) {
        boolean movesStraight = from.row() == to.row() ||  from.column() == to.column();

        int rowDifference = Math.abs(from.row() - to.row());
        int columnDifference = Math.abs(from.column() - to.column());

        boolean movesDiagonal = rowDifference == columnDifference;
        boolean staysWithinTwoSquares = rowDifference <= 2 && columnDifference <= 2;
        if(movesStraight || movesDiagonal || !staysWithinTwoSquares){
            return false;
        }

        Piece destinationPiece = board.getPiece(to);
        return destinationPiece == null || destinationPiece.getColor()!=getColor();
    }
}
