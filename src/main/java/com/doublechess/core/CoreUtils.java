package com.doublechess.core;

import com.doublechess.core.piece.*;

import java.util.HashMap;

public abstract class CoreUtils {
    private static final HashMap<Character, Class<? extends Piece>> PIECE_CLASSES_BY_CHARACTER;
    private static final HashMap<Class<? extends Piece>, Character> CHARACTERS_BY_PIECE_CLASS;

    static {
        PIECE_CLASSES_BY_CHARACTER = new HashMap<Character, Class<? extends Piece>>();
        PIECE_CLASSES_BY_CHARACTER.put('r', Rook.class);
        PIECE_CLASSES_BY_CHARACTER.put('n', Knight.class);
        PIECE_CLASSES_BY_CHARACTER.put('b', Bishop.class);
        PIECE_CLASSES_BY_CHARACTER.put('q', Queen.class);
        PIECE_CLASSES_BY_CHARACTER.put('k', King.class);
        PIECE_CLASSES_BY_CHARACTER.put('p', Pawn.class);

        CHARACTERS_BY_PIECE_CLASS = new HashMap<Class<? extends Piece>, Character>();
        CHARACTERS_BY_PIECE_CLASS.put(Rook.class, 'r');
        CHARACTERS_BY_PIECE_CLASS.put(Knight.class, 'n');
        CHARACTERS_BY_PIECE_CLASS.put(Bishop.class, 'b');
        CHARACTERS_BY_PIECE_CLASS.put(Queen.class, 'q');
        CHARACTERS_BY_PIECE_CLASS.put(King.class, 'k');
        CHARACTERS_BY_PIECE_CLASS.put(Pawn.class, 'p');
    }

    public static int positionToRank(int position) {
        return position / 8;
    }

    public static int positionToFile(int position) {
        return position % 8;
    }

    public static int charToFile(char c) {
        return (int) c - (int) 'a';
    }

    public static char fileToChar(int file) {
        return (char) ((int) 'a' + file);
    }

    public static char positionToFileChar(int position) {
        return fileToChar(positionToFile(position));
    }

    public static String positionToAlgebraic(int position) {
        return String.format("%s%d", fileToChar(positionToFile(position)), positionToRank(position) + 1);
    }

    public static int algebraicToPosition(String algebraic) {
        return charToFile(algebraic.charAt(0)) + ((int) algebraic.charAt(1) - (int) '1') * 8;
    }

    public static char pieceClassToChar(Class<? extends Piece> pieceClass) {
        return CHARACTERS_BY_PIECE_CLASS.get(pieceClass);
    }

    public static Class<? extends Piece> charToPieceClass(char c) {
        return PIECE_CLASSES_BY_CHARACTER.get(Character.toLowerCase(c));
    }
}
