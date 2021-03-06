package com.doublechess.core;

public enum GameResult {
    NOT_FINISHED,
    DRAW_AGREED,
    WHITE_WON_BY_RESIGNATION,
    BLACK_WON_BY_RESIGNATION,
    WHITE_WON_BY_CHECKMATE,
    WHITE_WON_ON_TIME,
    BLACK_WON_BY_CHECKMATE,
    BLACK_WON_ON_TIME,
    DRAW_BY_STALEMATE,
    DRAW_BY_UNSUFFICIENT_MATERIAL,
    DRAW_AFTER_REPETITION,
    DRAW_AFTER_50_MOVES
}