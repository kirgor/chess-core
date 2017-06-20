package com.doublechess.core.piece;

import com.doublechess.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    private static final ArrayList<Direction> DIRECTIONS = new ArrayList<Direction>();

    static {
        DIRECTIONS.add(new Direction(2, 1));
        DIRECTIONS.add(new Direction(2, -1));
        DIRECTIONS.add(new Direction(1, 2));
        DIRECTIONS.add(new Direction(1, -2));
        DIRECTIONS.add(new Direction(-2, 1));
        DIRECTIONS.add(new Direction(-2, -1));
        DIRECTIONS.add(new Direction(-1, 2));
        DIRECTIONS.add(new Direction(-1, -2));
    }

    public Knight() {
    }

    public Knight(boolean white, int position) {
        super(white, position);
    }

    @Override
    public boolean isLongRange() {
        return false;
    }

    @Override
    public List<Direction> getAttackDirections() {
        return DIRECTIONS;
    }
}
