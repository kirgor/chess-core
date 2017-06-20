package com.doublechess.core.piece;

import com.doublechess.core.CoreUtils;
import com.doublechess.core.Direction;

import java.util.List;

public abstract class Piece {
    protected boolean white;
    protected int position;
    protected boolean moved;

    public Piece() {
    }

    public Piece(boolean white, int position) {
        this.white = white;
        this.position = position;
    }

    public boolean isWhite() {
        return white;
    }

    public void setWhite(boolean white) {
        this.white = white;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public boolean isMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public int getRank() {
        return CoreUtils.positionToRank(position);
    }

    public int getFile() {
        return CoreUtils.positionToFile(position);
    }

    public abstract boolean isLongRange();

    public abstract List<Direction> getAttackDirections();
}
