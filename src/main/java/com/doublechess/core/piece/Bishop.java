package com.doublechess.core.piece;

import com.doublechess.core.Direction;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {
    private static final ArrayList<Direction> DIRECTIONS = new ArrayList<Direction>();

    static {
        DIRECTIONS.add(new Direction(1, 1));
        DIRECTIONS.add(new Direction(1, -1));
        DIRECTIONS.add(new Direction(-1, 1));
        DIRECTIONS.add(new Direction(-1, -1));
    }

    public Bishop() {
    }

    public Bishop(boolean white, int position) {
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
