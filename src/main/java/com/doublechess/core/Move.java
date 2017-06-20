package com.doublechess.core;

import com.doublechess.core.piece.*;

import static com.doublechess.core.CoreUtils.*;

public class Move {
    private int from;
    private int to;
    private Piece piece;
    private Piece capturedPiece;
    private boolean firstPieceMove;
    private boolean enpassant;
    private boolean castling;
    private Class<? extends Piece> promotionPieceClass;
    private Move[] promotionMoves;
    private boolean check;
    private boolean lastMove;
    private boolean fromFileExcess;
    private boolean fromRankExcess;
    private String algebraic;

    public Move(int from, int to, Piece piece, Piece capturedPiece,
                boolean firstPieceMove, boolean enpassant, boolean castling) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.firstPieceMove = firstPieceMove;
        this.enpassant = enpassant;
        this.castling = castling;
        algebraic = String.format("%s -> %s", positionToAlgebraic(from), positionToAlgebraic(to));

        if (piece instanceof Pawn && positionToRank(to) == (piece.isWhite() ? 7 : 0)) {
            promotionMoves = new Move[]{
                    new Move(from, to, piece, capturedPiece, Queen.class),
                    new Move(from, to, piece, capturedPiece, Knight.class),
                    new Move(from, to, piece, capturedPiece, Rook.class),
                    new Move(from, to, piece, capturedPiece, Bishop.class)
            };
        }
    }

    private Move(int from, int to, Piece piece, Piece capturedPiece, Class<? extends Piece> promotionPieceClass) {
        this.from = from;
        this.to = to;
        this.piece = piece;
        this.capturedPiece = capturedPiece;
        this.promotionPieceClass = promotionPieceClass;
    }

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCapturedPiece() {
        return capturedPiece;
    }

    public boolean isFirstPieceMove() {
        return firstPieceMove;
    }

    public boolean isEnpassant() {
        return enpassant;
    }

    public boolean isCastling() {
        return castling;
    }

    public Class<? extends Piece> getPromotionPieceClass() {
        return promotionPieceClass;
    }

    public Move[] getPromotionMoves() {
        return promotionMoves;
    }

    public boolean isCheck() {
        return check;
    }

    public void setCheck(boolean check) {
        this.check = check;
    }

    public boolean isLastMove() {
        return lastMove;
    }

    public void setLastMove(boolean lastMove) {
        this.lastMove = lastMove;
    }

    public boolean isFromFileExcess() {
        return fromFileExcess;
    }

    public void setFromFileExcess(boolean fromFileExcess) {
        this.fromFileExcess = fromFileExcess;
    }

    public boolean isFromRankExcess() {
        return fromRankExcess;
    }

    public void setFromRankExcess(boolean fromRankExcess) {
        this.fromRankExcess = fromRankExcess;
    }

    public String getAlgebraic() {
        return algebraic;
    }

    public void updateAlgebraic() {
        // Build algebraic
        StringBuilder stringBuilder = new StringBuilder();

        if (!castling) {
            // Moving piece code square
            if (!(piece instanceof Pawn)) {
                stringBuilder.append(Character.toUpperCase(pieceClassToChar(piece.getClass())));
            }

            // Departure square
            if (!(piece instanceof Pawn && capturedPiece == null)) {
                if (!fromFileExcess) {
                    stringBuilder.append(positionToFileChar(from));
                }
                if (!fromRankExcess) {
                    stringBuilder.append(positionToRank(from) + 1);
                }
            }

            // Capture
            if (capturedPiece != null) {
                stringBuilder.append('x');
            }

            // Arrival square
            stringBuilder.append(positionToFileChar(to)).append(positionToRank(to) + 1);

            // Promotion
            if (promotionPieceClass != null) {
                stringBuilder.append('=').append(Character.toUpperCase(pieceClassToChar(promotionPieceClass)));
            }
        } else {
            if (to > from) {
                stringBuilder.append("O-O");
            } else {
                stringBuilder.append("O-O-O");
            }
        }

        // Check or checkmate
        if (check) {
            stringBuilder.append(lastMove ? '#' : '+');
        }

        algebraic = stringBuilder.toString();
    }

    @Override
    public String toString() {
        return algebraic;
    }
}
