package com.doublechess.core;

import com.doublechess.core.exception.FENFormatException;
import com.doublechess.core.exception.IllegalMoveException;
import com.doublechess.core.piece.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import static com.doublechess.core.CoreUtils.positionToFile;
import static com.doublechess.core.CoreUtils.positionToRank;

@SuppressWarnings({"WeakerAccess", "unused"})
public class Board {
    // Pieces storage
    private Piece[] pieces = new Piece[64];
    private King[] kings = new King[2];
    // Move indicator
    private boolean whiteToMove;
    // Time fields
    private long whiteTimeInitial;
    private long blackTimeInitial;
    private long whiteTime;
    private long blackTime;
    private long increment;
    private long startTime;
    private long lastMoveTime;
    private int halfMoveCounter = 0;
    private int fullMoveCounter = 1;
    // Current possible moves storage
    private Move[] possibleMoves = new Move[64 * 64];
    private ArrayList<Move> possibleMovesList = new ArrayList<>();
    private boolean[] kingsideCastlingPossible = new boolean[2];
    private boolean[] queensideCastlingPossible = new boolean[2];
    private int initEnpassantTo = -1;
    private int enpassantTo = -1;
    // Piece counters
    private boolean[] insufficient = new boolean[2];
    private int[] queenCount = new int[2];
    private int[] rookCount = new int[2];
    private int[] bishopCount = new int[2];
    private int[] knightCount = new int[2];
    private int[] pawnCount = new int[2];

    // Moves and position history
    private Stack<Move> movesHistory = new Stack<>();
    private Stack<String> fenHistory = new Stack<>();
    private GameResult gameResult;

    public Board() throws FENFormatException {
        this(0, 0, 0);
    }

    public Board(long whiteTime, long blackTime, long increment) throws FENFormatException {
        this("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", whiteTime, blackTime, increment);
    }

    public Board(String fen) throws FENFormatException {
        this(fen, 0, 0, 0);
    }

    public Board(String fen, long whiteTime, long blackTime, long increment) throws FENFormatException {
        this.whiteTimeInitial = whiteTime;
        this.blackTimeInitial = blackTime;
        this.whiteTime = whiteTime;
        this.blackTime = blackTime;
        this.increment = increment;
        this.gameResult = GameResult.NOT_FINISHED;

        parseFEN(fen);

        updatePossibleMoves();

        // Late is better than never, check if initially specified enpassant was correct
        if (initEnpassantTo != enpassantTo) {
            throw new FENFormatException("Specified enpassant capture is impossible");
        }

        fenHistory.add(buildFEN());
    }

    public Piece getPiece(int position) {
        return pieces[position];
    }

    public boolean hasPiece(int position) {
        return pieces[position] != null;
    }

    public boolean isWhiteToMove() {
        return whiteToMove;
    }

    public long getWhiteTimeInitial() {
        return whiteTimeInitial;
    }

    public long getBlackTimeInitial() {
        return blackTimeInitial;
    }

    public long getWhiteTime() {
        return whiteTime;
    }

    public long getBlackTime() {
        return blackTime;
    }

    public long getIncrement() {
        return increment;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public int getHalfMoveCounter() {
        return halfMoveCounter;
    }

    public int getFullMoveCounter() {
        return fullMoveCounter;
    }

    public Move getPossibleMove(int from, int to) {
        return possibleMoves[from * 64 + to];
    }

    public Move getPossibleMove(String from, String to) {
        return possibleMoves[CoreUtils.algebraicToPosition(from) * 64 + CoreUtils.algebraicToPosition(to)];
    }

    public List<Move> getPossibleMoves() {
        return possibleMovesList;
    }

    public String getFEN() {
        return !fenHistory.empty() ? fenHistory.peek() : null;
    }

    public Move getLastMove() {
        return !movesHistory.empty() ? movesHistory.peek() : null;
    }

    public List<String> getFENHistory() {
        return fenHistory;
    }

    public List<Move> getMovesHistory() {
        return movesHistory;
    }

    public GameResult getGameResult() {
        return gameResult;
    }

    public void move(int from, int to, PromotionPiece promotionPiece) throws IllegalMoveException {
        if (gameResult != GameResult.NOT_FINISHED) {
            throw new IllegalMoveException("The game is finished");
        }

        Move move = possibleMoves[from * 64 + to];

        if (move == null) {
            throw new IllegalMoveException();
        }

        // Choose promotion move
        if (move.getPromotionMoves() != null) {
            move = move.getPromotionMoves()[promotionPiece.ordinal()];
        }

        // Time management
        if (startTime > 0) {
            long now = Instant.now().toEpochMilli();
            long elapsed = now - (lastMoveTime > 0 ? lastMoveTime : startTime) - increment;
            if (whiteToMove) {
                whiteTime -= elapsed;
            } else {
                blackTime -= elapsed;
            }
            lastMoveTime = now;
        }

        if (move.getCapturedPiece() != null || move.getPiece() instanceof Pawn) {
            halfMoveCounter = 0;
        } else {
            halfMoveCounter++;
        }

        if (!whiteToMove) {
            fullMoveCounter++;
        }

        // Perform actual move
        moveInternal(move, false);

        // Toggle player-to-move indicator
        whiteToMove = !whiteToMove;

        // Update possible moves list and history
        movesHistory.add(move);
        updatePossibleMoves();
        fenHistory.add(buildFEN());

        // Possibly set a game result
        if (whiteToMove && blackTime < 0) {
            gameResult = insufficient[0] ? GameResult.DRAW_BY_UNSUFFICIENT_MATERIAL : GameResult.WHITE_WON_ON_TIME;
        } else if (!whiteToMove && whiteTime < 0) {
            gameResult = insufficient[1] ? GameResult.DRAW_BY_UNSUFFICIENT_MATERIAL : GameResult.BLACK_WON_ON_TIME;
        } else if (move.isLastMove()) {
            if (move.isCheck()) {
                gameResult = whiteToMove ? GameResult.BLACK_WON_BY_CHECKMATE : GameResult.WHITE_WON_BY_CHECKMATE;
            } else {
                gameResult = GameResult.DRAW_BY_STALEMATE;
            }
        } else if (insufficient[0] && insufficient[1]) {
            gameResult = GameResult.DRAW_BY_UNSUFFICIENT_MATERIAL;
        } else if (halfMoveCounter == 100) {
            gameResult = GameResult.DRAW_AFTER_50_MOVES;
        }

        // TODO: append to PGN
    }

    public void move(String from, String to, PromotionPiece promotionPiece) throws IllegalMoveException {
        move(CoreUtils.algebraicToPosition(from), CoreUtils.algebraicToPosition(to), promotionPiece);
    }

    public void move(int from, int to) throws IllegalMoveException {
        move(from, to, PromotionPiece.QUEEN);
    }

    public void move(String from, String to) throws IllegalMoveException {
        move(from, to, PromotionPiece.QUEEN);
    }

    private void moveInternal(Move move, boolean intendToUndo) {
        // Update moved flag
        move.getPiece().setMoved(true);

        if (!intendToUndo && pieces[move.getTo()] != null) {
            changePieceCounter(pieces[move.getTo()], false);
        }

        // Move the piece, works well for regular moves and captures
        setPiecePosition(move.getFrom(), move.getTo());
        pieces[move.getFrom()] = null;

        if (move.isEnpassant()) {
            // Remove taken pawn from the board
            int fromRank = positionToRank(move.getFrom());
            int toFile = CoreUtils.positionToFile(move.getTo());
            if (!intendToUndo) {
                changePieceCounter(pieces[fromRank * 8 + toFile], false);
            }
            pieces[fromRank * 8 + toFile] = null;
        }

        if (move.isCastling()) {
            int index = whiteToMove ? 0 : 1;
            if (move.getFrom() < move.getTo()) {
                // Kingside castling
                setPiecePosition(move.getFrom() + 3, move.getFrom() + 1);
                pieces[move.getFrom() + 3] = null;
                kingsideCastlingPossible[index] = false;
            } else {
                // Queenside castling
                setPiecePosition(move.getFrom() - 4, move.getFrom() - 1);
                pieces[move.getFrom() - 4] = null;
                queensideCastlingPossible[index] = false;
            }
        }

        // Promotion
        if (move.getPromotionPieceClass() != null) {
            addPiece(move.getPromotionPieceClass(), move.getPiece().isWhite(), move.getTo());
        }
    }

    private void undoMoveInternal(Move move) {
        // Update moved flag
        if (move.isFirstPieceMove()) {
            move.getPiece().setMoved(false);
        }

        // Move the piece back
        pieces[move.getFrom()] = move.getPiece();
        move.getPiece().setPosition(move.getFrom());
        pieces[move.getTo()] = null;

        // Place the captured piece back
        if (move.isEnpassant()) {
            int fromRank = positionToRank(move.getFrom());
            int toFile = CoreUtils.positionToFile(move.getTo());
            int capturedPawnPosition = fromRank * 8 + toFile;
            pieces[capturedPawnPosition] = move.getCapturedPiece();
            move.getCapturedPiece().setPosition(capturedPawnPosition);
        } else if (move.getCapturedPiece() != null) {
            pieces[move.getTo()] = move.getCapturedPiece();
            move.getCapturedPiece().setPosition(move.getTo());
        }

        if (move.isCastling()) {
            int index = whiteToMove ? 0 : 1;
            // Place the rook back
            if (move.getFrom() < move.getTo()) {
                // Kingside castling
                setPiecePosition(move.getFrom() + 1, move.getFrom() + 3);
                pieces[move.getFrom() + 1] = null;
                kingsideCastlingPossible[index] = true;
            } else {
                // Queenside castling
                setPiecePosition(move.getFrom() - 1, move.getFrom() - 4);
                pieces[move.getFrom() - 1] = null;
                queensideCastlingPossible[index] = true;
            }
        }
    }

    private void updatePossibleMoves() {
        Move lastMove = getLastMove();
        ArrayList<Move> unverifiedPossibleMoves = getUnverifiedPossibleMoves(lastMove, whiteToMove);
        enpassantTo = -1;

        for (int i = 0; i < possibleMoves.length; i++) {
            possibleMoves[i] = null;
        }
        possibleMovesList.clear();

        // Verify possible moves don't put the king in check
        King king = kings[whiteToMove ? 0 : 1];
        for (Move move : unverifiedPossibleMoves) {
            if (move.getPromotionMoves() == null) {
                verifyPossibleMove(king, move, move);
            } else {
                for (Move promotionMove : move.getPromotionMoves()) {
                    verifyPossibleMove(king, promotionMove, move);
                }
            }
        }

        // Determine if some coords are excess
        possibleMovesList.forEach(this::hideMoveExcessCoords);
    }

    @SuppressWarnings("ConstantConditions")
    private ArrayList<Move> getUnverifiedPossibleMoves(Move lastMove, boolean whiteToMove) {
        ArrayList<Move> moves = new ArrayList<>();

        for (int i = 0; i < 64; i++) {
            Piece piece = pieces[i];
            if (piece == null || piece.isWhite() != whiteToMove) {
                continue;
            }

            boolean firstPieceMove = !piece.isMoved();
            if (piece instanceof Pawn) {
                int diff = piece.isWhite() ? 8 : -8;
                int[] singleStepTo = new int[]{i + diff, i + diff - 1, i + diff + 1};
                int doubleStepTo = i + 2 * diff;

                // One-step move
                for (int to : singleStepTo) {
                    if (to >= 0 && to < 64) {
                        Piece capturedPiece;
                        if (Math.abs(CoreUtils.positionToFile(to) - CoreUtils.positionToFile(i)) == 1 && pieces[to] != null &&
                                pieces[to].isWhite() != piece.isWhite()) {
                            capturedPiece = pieces[to];
                        } else if (CoreUtils.positionToFile(to) == CoreUtils.positionToFile(i) && pieces[to] == null) {
                            capturedPiece = null;
                        } else {
                            continue;
                        }

                        moves.add(new Move(i, to, piece, capturedPiece, firstPieceMove, false, false));
                    }
                }

                // First double-step move
                if (piece.getRank() == (whiteToMove ? 1 : 6) && pieces[doubleStepTo] == null) {
                    moves.add(new Move(i, doubleStepTo, piece, null, true, false, false));
                }

                // Enpassant
                int lastMoveFrom = lastMove != null ? lastMove.getFrom() : initEnpassantTo + diff;
                int lastMoveTo = lastMove != null ? lastMove.getTo() : initEnpassantTo - diff;
                if ((lastMove != null && Math.abs(lastMoveTo - lastMoveFrom) == 16 ||
                        lastMove == null && initEnpassantTo != -1) && pieces[lastMoveTo] instanceof Pawn) {
                    int to = 0;
                    if (CoreUtils.positionToFile(i) > 0 && lastMoveTo == i - 1) {
                        to = i + diff - 1;
                    } else if (CoreUtils.positionToFile(i) < 7 && lastMoveTo == i + 1) {
                        to = i + diff + 1;
                    }

                    if (to != 0) {
                        moves.add(new Move(i, to, piece, pieces[lastMoveTo], false, true, false));
                    }
                }
            } else {
                // Fill the collection with simple attack moves
                for (Integer to : new PieceAttacksIterable(piece)) {
                    moves.add(new Move(i, to, piece, pieces[to], firstPieceMove, false, false));
                }
                // Castling
                if (piece instanceof King && firstPieceMove) {
                    int index = whiteToMove ? 0 : 1;

                    if (kingsideCastlingPossible[index] && pieces[i + 3] != null && !pieces[i + 3].isMoved() &&
                            pieces[i + 1] == null && pieces[i + 2] == null) {
                        moves.add(new Move(i, i + 2, piece, null, true, false, true));
                    }

                    if (queensideCastlingPossible[index] && pieces[i - 4] != null && !pieces[i - 4].isMoved() &&
                            pieces[i - 1] == null && pieces[i - 2] == null && pieces[i - 3] == null) {
                        moves.add(new Move(i, i - 2, piece, null, true, false, true));
                    }
                }
            }
        }

        return moves;
    }

    private void verifyPossibleMove(King king, Move move, Move parentMove) {
        // Try to perform the move to see the consequences
        moveInternal(move, true);

        // Verify that the king won't be in check
        if (!checkForCheck(king, move.isCastling() ? move.getFrom() : -1)) {
            // Move is legal
            possibleMoves[move.getFrom() * 64 + move.getTo()] = parentMove;
            possibleMovesList.add(move);

            if (move.isEnpassant()) {
                enpassantTo = move.getTo();
            }

            // Set extra data for all possible moves
            King opponentKing = kings[whiteToMove ? 1 : 0];
            // Determine if the move is check
            boolean check = checkForCheck(opponentKing, -1);
            // Check if oppent can move afterwards
            boolean opponentCanMove = false;
            ArrayList<Move> opponentMoves = getUnverifiedPossibleMoves(move, !whiteToMove);
            for (Move opponentMove : opponentMoves) {
                moveInternal(opponentMove, true);
                opponentCanMove = !checkForCheck(opponentKing, opponentMove.isCastling() ? opponentMove.getFrom() : -1);
                undoMoveInternal(opponentMove);
                if (opponentCanMove) {
                    break;
                }
            }

            move.setCheck(check);
            move.setLastMove(!opponentCanMove);
        }

        // Undo the move trial
        undoMoveInternal(move);
    }

    @SuppressWarnings("Duplicates")
    private void hideMoveExcessCoords(Move move) {
        // Default values
        boolean fromFileExcess = true;
        boolean fromRankExcess = true;

        int moveFromRank = CoreUtils.positionToRank(move.getFrom());
        int moveFromFile = CoreUtils.positionToFile(move.getFrom());

        // Pawn are handled different from other pieces
        if (move.getPiece() instanceof Pawn) {
            // If pawn captures something, departure file should be visible
            fromFileExcess = move.getCapturedPiece() == null;
        } else {
            // Departure file (or rank) should be visible if there is another piece
            // of the same type on the same rank (or file), which also can move there

            // Scan departure file
            for (int i = 0; i < 8 && fromRankExcess; i++) {
                if (i == moveFromRank) {
                    continue;
                }

                int otherMoveFrom = i * 8 + moveFromFile;
                Move otherMove = possibleMoves[otherMoveFrom * 64 + move.getTo()];
                if (otherMove != null && otherMove.getPiece().getClass() == move.getPiece().getClass()) {
                    fromRankExcess = false;
                }
            }

            // Scan departure rank
            for (int i = 0; i < 8 && fromFileExcess; i++) {
                if (i == moveFromFile) {
                    continue;
                }

                int otherMoveFrom = moveFromRank * 8 + i;
                Move otherMove = possibleMoves[otherMoveFrom * 64 + move.getTo()];
                if (otherMove != null && otherMove.getPiece().getClass() == move.getPiece().getClass()) {
                    fromFileExcess = false;
                }
            }
        }

        move.setFromFileExcess(fromFileExcess);
        move.setFromRankExcess(fromRankExcess);
        move.updateAlgebraic();
    }

    private boolean checkForCheck(King king, int castlingMoveFrom) {
        for (Piece piece : pieces) {
            if (piece != null && piece.isWhite() != king.isWhite()) {
                for (Integer attackSquare : new PieceAttacksIterable(piece)) {
                    boolean moveIsCastling = castlingMoveFrom != -1;
                    boolean kingWillBeUnderAttack = attackSquare == king.getPosition();
                    boolean kingIsUnderAttackDuringCastling = attackSquare == castlingMoveFrom;
                    boolean castlingHalfMoveUnderAttack = attackSquare == (king.getPosition() + castlingMoveFrom) / 2;
                    if (kingWillBeUnderAttack || moveIsCastling && (kingIsUnderAttackDuringCastling || castlingHalfMoveUnderAttack)) {
                        // Regular check
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void parseFEN(String fen) throws FENFormatException {
        String[] fenParts = fen.split(" ");
        if (fenParts.length != 6) {
            throw new FENFormatException("Invalid sections count (6 sections are expected)");
        }

        parseFENPieces(fenParts[0]);
        parseFENMoveOrder(fenParts[1]);
        parseFENCastling(fenParts[2]);
        parseFENEnpassant(fenParts[3]);

        try {
            halfMoveCounter = Integer.parseInt(fenParts[4]);
            fullMoveCounter = Integer.parseInt(fenParts[5]);
        } catch (NumberFormatException e) {
            throw new FENFormatException("Move counters aren't integers");
        }

        if (halfMoveCounter < 0) {
            throw new FENFormatException("Half-move counter must be 0 or greater");
        }
        if (fullMoveCounter < 1) {
            throw new FENFormatException("Full move counter must be 1 or greater");
        }
    }

    private void parseFENPieces(String fen) throws FENFormatException {
        int position = 56;
        int rankSquaresCounter = 0;
        int ranksCounter = 0;

        // Parse pieces positions
        for (int i = 0; i < fen.length(); i++) {
            char c = fen.charAt(i);
            if (rankSquaresCounter > 8) {
                throw new FENFormatException("Rank squares count of 8 was exceeded");
            }
            if (ranksCounter > 7) {
                throw new FENFormatException("Ranks count of 8 was exceeded");
            }

            if (Character.isLetter(c)) {
                // Letter means a piece
                Class<? extends Piece> pieceClass = CoreUtils.charToPieceClass(c);
                if (pieceClass != null) {
                    // Piece color is determined by letter case
                    boolean white = Character.isUpperCase(c);
                    // Create piece instance and add it to the board
                    Piece piece = addPiece(pieceClass, white, position);
                    if (pieceClass == King.class) {
                        int index = white ? 0 : 1;
                        if (kings[index] != null) {
                            throw new FENFormatException("Board can have only one king of each color");
                        }
                        kings[index] = (King) piece;
                    }
                    // Move to the next square
                    position++;
                    rankSquaresCounter++;
                } else {
                    throw new FENFormatException("Letter specified in pieces section doesn't represent a piece");
                }
            } else if (Character.isDigit(c)) {
                // Digit means blank squares
                int count = (int) c - (int) '0';
                position += count;
                rankSquaresCounter += count;
            } else if (c == '/') {
                // Slash means the next rank
                if (rankSquaresCounter < 8) {
                    throw new FENFormatException("Rank squares count of 8 wasn't reached");
                }
                rankSquaresCounter = 0;
                ranksCounter++;
                position -= 16;
            } else {
                throw new FENFormatException("Invalid character in pieces section");
            }
        }

        if (ranksCounter < 7) {
            throw new FENFormatException("Not all squares were specified in the pieces section");
        }

        if (kings[0] == null || kings[1] == null) {
            throw new FENFormatException("Both kings should be specified");
        }
    }

    private void parseFENMoveOrder(String fen) throws FENFormatException {
        switch (fen) {
            case "w":
                whiteToMove = true;
                break;
            case "b":
                whiteToMove = false;
                break;
            default:
                throw new FENFormatException("Move order is specified incorrectly ('w' or 'b' are expected");
        }
    }

    private void parseFENCastling(String fen) throws FENFormatException {
        if (fen.equals("-")) {
            return;
        }

        for (int i = 0; i < fen.length(); i++) {
            char c = fen.charAt(i);
            int index = Character.isUpperCase(c) ? 0 : 1;
            boolean white = index == 0;
            Piece initKing = pieces[4 + index * 56];
            Piece initKingsideRook = pieces[7 + index * 56];
            Piece initQueensideRook = pieces[index * 56];
            if (Character.toLowerCase(c) == 'k') {
                if (kingsideCastlingPossible[index]) {
                    throw new FENFormatException("Kingside castling possibility is specified more than once");
                }
                if (!(initKing instanceof King) || initKing.isWhite() != white ||
                        !(initKingsideRook instanceof Rook) || initKingsideRook.isWhite() != white) {
                    throw new FENFormatException("Castling impossible - king and/or kingside rook are off");
                }

                kingsideCastlingPossible[index] = true;
            } else if (Character.toLowerCase(c) == 'q') {
                if (queensideCastlingPossible[index]) {
                    throw new FENFormatException("Queenside castling possibility is specified more than once");
                }
                if (!(initKing instanceof King) || initKing.isWhite() != white ||
                        !(initQueensideRook instanceof Rook) || initQueensideRook.isWhite() != white) {
                    throw new FENFormatException("Castling impossible - king and/or queenside rook are off");
                }

                queensideCastlingPossible[index] = true;
            } else {
                throw new FENFormatException("Invalid character in the castling section");
            }
        }
    }

    private void parseFENEnpassant(String fen) throws FENFormatException {
        if (fen.equals("-")) {
            return;
        }

        if (fen.length() != 2 || fen.charAt(0) < 'a' || fen.charAt(0) > 'h' ||
                !(whiteToMove && fen.charAt(1) == '6') && !(!whiteToMove && fen.charAt(1) == '3')) {
            throw new FENFormatException("Enpassant square is incorrect");
        }

        initEnpassantTo = CoreUtils.algebraicToPosition(fen);
    }

    @SuppressWarnings("ConstantConditions")
    private String buildFEN() {
        StringBuilder stringBuilder = new StringBuilder();

        // Pieces positions
        int position = 56;
        int blankCounter = 0;
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j <= 8; j++) {
                Piece piece = j < 8 ? pieces[position] : null;

                if (j == 8 || piece != null) {
                    if (blankCounter > 0) {
                        stringBuilder.append(blankCounter);
                    }
                    blankCounter = 0;
                }

                if (j < 8) {
                    if (piece == null) {
                        blankCounter++;
                    } else {
                        char c = CoreUtils.pieceClassToChar(piece.getClass());
                        if (piece.isWhite()) {
                            c = Character.toUpperCase(c);
                        }
                        stringBuilder.append(c);
                    }

                    position++;
                } else if (i < 7) {
                    stringBuilder.append('/');
                }
            }
            position -= 16;
        }

        // Move order
        stringBuilder.append(" ").append(whiteToMove ? "w" : "b").append(" ");

        // Castling
        StringBuilder castlingStringBuilder = new StringBuilder();
        if (kingsideCastlingPossible[0]) {
            castlingStringBuilder.append("K");
        }
        if (queensideCastlingPossible[0]) {
            castlingStringBuilder.append("Q");
        }
        if (kingsideCastlingPossible[1]) {
            castlingStringBuilder.append("k");
        }
        if (queensideCastlingPossible[1]) {
            castlingStringBuilder.append("q");
        }
        stringBuilder.append(castlingStringBuilder.length() > 0 ? castlingStringBuilder : "-").append(" ");

        // Enpassant
        stringBuilder.append(enpassantTo != -1 ? CoreUtils.positionToAlgebraic(enpassantTo) : "-").append(" ");

        // Move counters
        stringBuilder.append(halfMoveCounter).append(" ").append(fullMoveCounter);

        return stringBuilder.toString();
    }


    private Piece addPiece(Class<? extends Piece> pieceClass, boolean white, int position) {
        try {
            Piece piece = pieceClass.newInstance();
            piece.setWhite(white);
            piece.setPosition(position);
            pieces[position] = piece;

            changePieceCounter(piece, true);

            return piece;
        } catch (InstantiationException | IllegalAccessException ignored) {
        }

        return null;
    }

    private void changePieceCounter(Piece piece, boolean increase) {
        int index = piece.isWhite() ? 0 : 1;
        int increment = increase ? 1 : -1;
        if (piece.getClass() == Queen.class) {
            queenCount[index] += increment;
        } else if (piece.getClass() == Rook.class) {
            rookCount[index] += increment;
        } else if (piece.getClass() == Bishop.class) {
            bishopCount[index] += increment;
        } else if (piece.getClass() == Knight.class) {
            knightCount[index] += increment;
        } else if (piece.getClass() == Pawn.class) {
            pawnCount[index] += increment;
        }

        if (!increase) {
            updateInsufficientIndicators();
        }
    }

    private void updateInsufficientIndicators() {
        for (int i = 0; i < 2; i++) {
            if (insufficient[i]) {
                // If amount of pieces is already insufficient, it can't be undone
                continue;
            }

            if (queenCount[i] == 0 && rookCount[i] == 0 && pawnCount[i] == 0) {
                // There are no queens, rooks or pawns, possibly it's insufficient material
                if (bishopCount[i] == 0 && knightCount[i] == 0) {
                    // No bishops and no knights is insufficient
                    insufficient[i] = true;
                } else if (bishopCount[i] == 0 && knightCount[i] < 3) {
                    // No bishops and less than 3 knights is insufficient
                    insufficient[i] = true;
                } else if (bishopCount[i] > 0 && knightCount[i] == 0) {
                    // No knights and some bishops
                    if (bishopCount[i] == 1) {
                        // Single bishop is surely not enough
                        insufficient[i] = true;
                    } else {
                        // Even if there are 2 or more bishops - they must be different-colored
                        boolean even = false;
                        boolean odd = false;
                        for (Piece piece : pieces) {
                            if (piece instanceof Bishop && piece.isWhite() == (i == 0)) {
                                if (piece.getPosition() % 2 == 0) {
                                    even = true;
                                } else {
                                    odd = true;
                                }
                            }
                        }

                        if (!even || !odd) {
                            insufficient[i] = true;
                        }
                    }
                }
            }
        }
    }

    private void setPiecePosition(int from, int to) {
        pieces[to] = pieces[from];
        pieces[to].setPosition(to);
    }

    public class PieceAttacksIterator implements Iterator<Integer> {
        private Piece piece;
        private Iterator<Direction> directionIterator;
        private Direction direction;
        private int rank;
        private int file;
        private boolean block;
        private int next;

        public PieceAttacksIterator(Piece piece) {
            this.piece = piece;
            directionIterator = piece.getAttackDirections().iterator();
            block = true;
            findNext();
        }

        @Override
        public boolean hasNext() {
            return next != -1;
        }

        @Override
        public Integer next() {
            int result = next;
            findNext();
            return result;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private void nextDirection() {
            direction = directionIterator.next();
            rank = piece.getRank() + direction.getRank();
            file = piece.getFile() + direction.getFile();
            block = false;
        }

        private boolean isCurrentDirectionValid() {
            return !block && rank >= 0 && rank < 8 && file >= 0 && file < 8;
        }

        private void findNext() {
            boolean found = false;

            while (!found) {
                // Switch directions until you find valid one
                while (!isCurrentDirectionValid()) {
                    if (directionIterator.hasNext()) {
                        nextDirection();
                    } else {
                        // All directions are scanned, it's all over
                        next = -1;
                        return;
                    }
                }

                // Determine position where piece can go
                int position = rank * 8 + file;

                // See if there is another piece on the way
                Piece otherPiece = pieces[position];
                if (otherPiece == null) {
                    // Piece is free to go to the empty squares
                    next = position;
                    found = true;
                } else {
                    // Allow to capture enemy pieces
                    if (otherPiece.isWhite() != piece.isWhite()) {
                        next = position;
                        found = true;
                    }
                    // Don't go further because other piece blocks
                    block = true;
                }

                if (!piece.isLongRange()) {
                    // Don't go further if piece is short range
                    block = true;
                } else {
                    // Advance to the next square on current direction
                    rank += direction.getRank();
                    file += direction.getFile();
                }
            }
        }
    }

    public class PieceAttacksIterable implements Iterable<Integer> {
        private PieceAttacksIterator iterator;

        public PieceAttacksIterable(Piece piece) {
            iterator = new PieceAttacksIterator(piece);
        }

        @Override
        public Iterator<Integer> iterator() {
            return iterator;
        }
    }
}