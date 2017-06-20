package com.doublechess.core;

import com.doublechess.core.exception.FENFormatException;
import com.doublechess.core.exception.IllegalMoveException;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class BoardTest {
    @Test
    public void createBoard() throws FENFormatException {
        new Board();
    }

    @Test
    public void startFEN() throws FENFormatException {
        assertEquals("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", new Board().getFEN());
    }

    @Test
    public void kingPawnPossibleMoves() throws FENFormatException, IllegalMoveException {
        // Create board and play king pawn opening
        Board board = new Board();
        board.move("e2", "e4");
        board.move("e7", "e5");

        // Pawns single moves
        assertNotNull(board.getPossibleMove("a2", "a3"));
        assertNotNull(board.getPossibleMove("b2", "b3"));
        assertNotNull(board.getPossibleMove("c2", "c3"));
        assertNotNull(board.getPossibleMove("d2", "d3"));
        assertNotNull(board.getPossibleMove("f2", "f3"));
        assertNotNull(board.getPossibleMove("g2", "g3"));
        assertNotNull(board.getPossibleMove("h2", "h3"));
        // Pawns double moves
        assertNotNull(board.getPossibleMove("a2", "a4"));
        assertNotNull(board.getPossibleMove("b2", "b4"));
        assertNotNull(board.getPossibleMove("c2", "c4"));
        assertNotNull(board.getPossibleMove("d2", "d4"));
        assertNotNull(board.getPossibleMove("f2", "f4"));
        assertNotNull(board.getPossibleMove("g2", "g4"));
        assertNotNull(board.getPossibleMove("h2", "h4"));
        // Knight moves
        assertNotNull(board.getPossibleMove("b1", "a3"));
        assertNotNull(board.getPossibleMove("b1", "c3"));
        assertNotNull(board.getPossibleMove("g1", "f3"));
        assertNotNull(board.getPossibleMove("g1", "h3"));
        assertNotNull(board.getPossibleMove("g1", "e2"));
        // Queen moves
        assertNotNull(board.getPossibleMove("d1", "e2"));
        assertNotNull(board.getPossibleMove("d1", "f3"));
        assertNotNull(board.getPossibleMove("d1", "g4"));
        assertNotNull(board.getPossibleMove("d1", "h5"));
        // Bishop moves
        assertNotNull(board.getPossibleMove("f1", "e2"));
        assertNotNull(board.getPossibleMove("f1", "d3"));
        assertNotNull(board.getPossibleMove("f1", "c4"));
        assertNotNull(board.getPossibleMove("f1", "b5"));
        assertNotNull(board.getPossibleMove("f1", "a6"));
        // King move
        assertNotNull(board.getPossibleMove("e1", "e2"));
    }

    @Test
    public void ryuLopezFEN() throws IllegalMoveException, FENFormatException {
        Board board = ryuLopez();
        String[] expected = new String[]{
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
                "rnbqkbnr/pppp1ppp/8/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 1 2",
                "r1bqkbnr/pppp1ppp/2n5/4p3/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 2 3",
                "r1bqkbnr/pppp1ppp/2n5/1B2p3/4P3/5N2/PPPP1PPP/RNBQK2R b KQkq - 3 3"
        };
        assertArrayEquals(expected, board.getFENHistory().toArray());
    }

    @Test
    public void ryuLopezMovesNotation() throws IllegalMoveException, FENFormatException {
        Board board = ryuLopez();
        validateMovesHistory(new String[]{"e4", "e5", "Nf3", "Nc6", "Bb5"}, board);
    }

    @Test
    public void simpleCheckmates() throws FENFormatException, IllegalMoveException {
        Board board = new Board("4k3/8/4K3/2Q3R1/8/8/8/8 w - - 0 1");
        board.move("g5", "g8");
        assertEquals("Rg8#", board.getLastMove().getAlgebraic());

        board = new Board("4k3/8/4K3/2Q3R1/8/8/8/8 w - - 0 1");
        board.move("c5", "e7");
        assertEquals("Qe7#", board.getLastMove().getAlgebraic());
    }

    @Test
    public void discoveredCheckmate() throws FENFormatException, IllegalMoveException {
        Board board = new Board("4k3/8/8/8/8/8/3RKR2/4Q3 w - - 0 1");
        board.move("e2", "d1");
        assertEquals("Kd1#", board.getLastMove().getAlgebraic());
    }

    @Test
    public void smotheredCheckmate() throws FENFormatException, IllegalMoveException {
        Board board = new Board("r6k/6pp/8/4N3/2Q5/8/8/4K3 w - - 0 1");
        board.move("e5", "f7");
        board.move("h8", "g8");
        board.move("f7", "h6");
        board.move("g8", "h8");
        board.move("c4", "g8");
        board.move("a8", "g8");
        board.move("h6", "f7");
        validateMovesHistory(new String[]{"Nf7+", "Kg8", "Nh6+", "Kh8", "Qg8+", "Rxg8", "Nf7#"}, board);
    }

    @Test
    public void pinnedCheckmate() throws FENFormatException, IllegalMoveException {
        Board board = new Board("3qknR1/3p1pp1/8/4B3/4Q3/8/8/R3K3 w - - 0 1");
        board.move("e5", "g7");
        board.move("d8", "e7");
        board.move("a1", "a8");
        validateMovesHistory(new String[]{"Bxg7+", "Qe7", "Ra8#"}, board);
    }

    @Test
    public void castlingCheckmate() throws FENFormatException, IllegalMoveException {
        Board board = new Board("rn3r2/pbppq1p1/1p2pN2/8/3P2NP/6P1/PPP1BP1R/R3K1k1 w Q - 0 1");
        board.move("e1", "c1");
        assertEquals("O-O-O#", board.getLastMove().getAlgebraic());
    }

    @Test
    public void castling() throws FENFormatException, IllegalMoveException {
        Board board = new Board("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        board.move("e1", "g1");
        board.move("e8", "c8");
        validateMovesHistory(new String[]{"O-O", "O-O-O"}, board);
    }

    @Test
    public void longerMovesNotation() throws FENFormatException, IllegalMoveException {
        Board board = new Board("4k3/8/r7/8/8/r7/2N3N1/4K3 w - - 0 1");
        board.move("c2", "e3");
        board.move("a6", "a5");
        board.move("e1", "f2");
        board.move("a3", "a2");
        validateMovesHistory(new String[]{"Nce3", "R6a5", "Kf2", "Ra2+"}, board);

        board = new Board("4k3/8/8/8/2N5/8/2N3N1/4K3 w - - 0 1");
        board.move("c2", "e3");
        assertEquals("Nc2e3", board.getLastMove().getAlgebraic());
    }

    @Test
    public void pawnCaptures() throws FENFormatException, IllegalMoveException {
        Board board = new Board("4k3/8/8/2p1nppp/1Pp2PPP/1P6/8/4K3 w - - 0 1");
        board.move("f4", "e5");
        board.move("f5", "g4");
        board.move("b4", "c5");
        board.move("c4", "b3");
        validateMovesHistory(new String[]{"fxe5", "fxg4", "bxc5", "cxb3"}, board);
    }

    @Test
    public void enpassant() throws FENFormatException, IllegalMoveException {
        Board board = new Board("4k3/1p3prp/8/P3P1P1/4R1K1/8/8/8 b - - 0 1");
        board.move("b7", "b5");
        board.move("a5", "b6");
        board.move("f7", "f5");
        board.move("e5", "f6");
        board.move("e8", "f8");
        board.move("b6", "b7");
        board.move("h7", "h5");

        String[] expectedFEN = new String[]{
                "4k3/1p3prp/8/P3P1P1/4R1K1/8/8/8 b - - 0 1",
                "4k3/5prp/8/Pp2P1P1/4R1K1/8/8/8 w - b6 0 2",
                "4k3/5prp/1P6/4P1P1/4R1K1/8/8/8 b - - 0 2",
                "4k3/6rp/1P6/4PpP1/4R1K1/8/8/8 w - f6 0 3",
                "4k3/6rp/1P3P2/6P1/4R1K1/8/8/8 b - - 0 3",
                "5k2/6rp/1P3P2/6P1/4R1K1/8/8/8 w - - 1 4",
                "5k2/1P4rp/5P2/6P1/4R1K1/8/8/8 b - - 0 4",
                "5k2/1P4r1/5P2/6Pp/4R1K1/8/8/8 w - - 0 5"
        };

        validateMovesHistory(new String[]{"b5", "axb6", "f5+", "exf6+", "Kf8", "b7", "h5+"}, board);
        assertArrayEquals(expectedFEN, board.getFENHistory().toArray());
    }

    @Test
    public void promotion() throws FENFormatException, IllegalMoveException {
        Board board = new Board("7r/P1kPPPP1/8/8/8/8/8/K7 w - - 0 1");
        board.move("e7", "e8", PromotionPiece.KNIGHT);
        board.move("c7", "c6");
        board.move("a7", "a8", PromotionPiece.BISHOP);
        board.move("c6", "c5");
        board.move("f7", "f8", PromotionPiece.QUEEN);
        board.move("c5", "d4");
        board.move("d7", "d8", PromotionPiece.ROOK);
        board.move("d4", "c3");
        board.move("g7", "h8");

        validateMovesHistory(new String[]{"e8=N+", "Kc6", "a8=B+", "Kc5",
                "f8=Q+", "Kd4", "d8=R+", "Kc3", "gxh8=Q+"}, board);
    }

    private void validateMovesHistory(String[] expected, Board board) {
        List<Move> movesHistory = board.getMovesHistory();
        for (int i = 0; i < expected.length; i++) {
            assertEquals(expected[i], movesHistory.get(i).getAlgebraic());
        }
    }

    private Board ryuLopez() throws FENFormatException, IllegalMoveException {
        Board board = new Board();
        board.move("e2", "e4");
        board.move("e7", "e5");
        board.move("g1", "f3");
        board.move("b8", "c6");
        board.move("f1", "b5");
        return board;
    }
}