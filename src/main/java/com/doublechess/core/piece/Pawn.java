package com.doublechess.core.piece;

import com.doublechess.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {
    private static final ArrayList<Direction> WHITE_DIRECTIONS = new ArrayList<Direction>();
    private static final ArrayList<Direction> BLACK_DIRECTIONS = new ArrayList<Direction>();

    static {
        WHITE_DIRECTIONS.add(new Direction(1, 1));
        WHITE_DIRECTIONS.add(new Direction(1, -1));
        BLACK_DIRECTIONS.add(new Direction(-1, 1));
        BLACK_DIRECTIONS.add(new Direction(-1, -1));
    }

    public Pawn() {
    }

    public Pawn(boolean white, int position) {
        super(white, position);
    }

    @Override
    public boolean isLongRange() {
        return false;
    }

    @Override
    public List<Direction> getAttackDirections() {
        return white ? WHITE_DIRECTIONS : BLACK_DIRECTIONS;
    }
}
