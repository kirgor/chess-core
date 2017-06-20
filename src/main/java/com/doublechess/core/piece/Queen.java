package com.doublechess.core.piece;

import com.doublechess.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {
    private static final ArrayList<Direction> DIRECTIONS = new ArrayList<Direction>();

    static {
        DIRECTIONS.add(new Direction(0, 1));
        DIRECTIONS.add(new Direction(0, -1));
        DIRECTIONS.add(new Direction(1, 0));
        DIRECTIONS.add(new Direction(-1, 0));
        DIRECTIONS.add(new Direction(1, 1));
        DIRECTIONS.add(new Direction(1, -1));
        DIRECTIONS.add(new Direction(-1, 1));
        DIRECTIONS.add(new Direction(-1, -1));
    }

    public Queen() {
    }

    public Queen(boolean white, int position) {
        super(white, position);
    }

    @Override
    public boolean isLongRange() {
        return true;
    }

    @Override
    public List<Direction> getAttackDirections() {
        return DIRECTIONS;
    }
}
