package lld.DesignProblems.chess.models;

import lld.DesignProblems.chess.enums.Color;
import lld.DesignProblems.chess.models.ConcretePiece.*;

public class Board {
    public static final int SIZE = 8;

    private final Piece[][] squares;

    public Board() {
        this.squares = new Piece[SIZE][SIZE];
    }

    public static Board standardBoard() {
        Board board = new Board();
        board.placeBackRank(0, Color.BLACK);
        board.placePawns(1, Color.BLACK);

        board.placePawns(6, Color.WHITE);
        board.placeBackRank(7, Color.WHITE);

        System.out.println("Initial board:");
        board.printBoard();

        return board;
    }

    public boolean isValid(Position position) {
        return position.row() >= 0
                && position.row() < SIZE
                && position.column() >= 0
                && position.column() < SIZE;
    }

    public Piece getPiece(Position position) {
        return squares[position.row()][position.column()];
    }

    public boolean isEmpty(Position position) {
        return getPiece(position) == null;
    }

    public Piece movePiece(Position from, Position to){
        Piece capturedPiece = getPiece(to);
        squares[to.row()][to.column()] = getPiece(from);
        squares[from.row()][from.column()] = null;
        return capturedPiece;
    }

    public void undoMove(Move move) {
        // Restore both the moving piece and any piece captured during simulation.
        placePiece(move.from(), move.movedPiece());
        placePiece(move.to(), move.capturedPiece());
    }

    public void placePiece(Position position, Piece piece) {
        squares[position.row()][position.column()] = piece;
    }

    public void printBoard() {
        System.out.println("  0  1  2  3  4  5  6  7");

        for (int row = 0; row < SIZE; row++) {
            StringBuilder output = new StringBuilder()
                    .append(row)
                    .append(' ');

            for (int column = 0; column < SIZE; column++) {
                Piece piece = squares[row][column];
                output.append(piece == null ? "--" : piece.getSymbol())
                        .append(' ');
            }

            System.out.println(output);
        }
    }

    private void placePawns(int row, Color color) {
        for (int column = 0; column < SIZE; column++) {
            placePiece(new Position(row, column), new Pawn(color));
        }
    }

    private void placeBackRank(int row, Color color) {
        placePiece(new Position(row, 0), new Rook(color));
        placePiece(new Position(row, 1), new Knight(color));
        placePiece(new Position(row, 2), new Bishop(color));
        placePiece(new Position(row, 3), new Queen(color));
        placePiece(new Position(row, 4), new King(color));
        placePiece(new Position(row, 5), new Bishop(color));
        placePiece(new Position(row, 6), new Knight(color));
        placePiece(new Position(row, 7), new Rook(color));
    }
}