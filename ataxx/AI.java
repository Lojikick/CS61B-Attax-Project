/* Skeleton code copyright (C) 2008, 2022 Paul N. Hilfinger and the
 * Regents of the University of California.  Do not distribute this or any
 * derivative work without permission. */

package ataxx;

import java.util.ArrayList;
import java.util.Random;

import static ataxx.PieceColor.*;

/** A Player that computes its own moves.
 *  @author Avik Samanta
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 4;
    /** A position magnitude indicating a win (for red if positive, blue
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. SEED is used to initialize
     *  a random-number generator for use in move computations.  Identical
     *  seeds produce identical behaviour. */
    AI(Game game, PieceColor myColor, long seed) {
        super(game, myColor);
        _random = new Random(seed);
    }

    @Override
    boolean isAuto() {
        return true;
    }

    @Override
    String getMove() {
        if (!getBoard().canMove(myColor())) {
            game().reportMove(Move.pass(), myColor());
            return "-";
        }
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        game().reportMove(move, myColor());
        return move.toString();
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(getBoard());
        _lastFoundMove = null;
        if (myColor() == RED) {
            ogSense = 1;
            minMax(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            ogSense = -1;
            minMax(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to the findMove method
     *  above. */
    private Move _lastFoundMove;
    /** The original Sense. */
    private int ogSense;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _foundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _foundMove. If the game is over
     *  on BOARD, does not set _foundMove. */
    private int minMax(Board board, int depth, boolean saveMove,
                       int sense, int alpha, int beta) {
        if (depth == 0 || board.getWinner() != null) {
            return staticScore(board, WINNING_VALUE + depth);
        }
        ArrayList<String> moves;
        if (sense == ogSense) {
            moves = moveList(board, myColor());
        } else {
            moves = moveList(board, myColor().opposite());
        }
        Move best = null;
        int bestScore = -INFTY;
        int bdex = 0;
        int a = alpha;
        int b = beta;
        int player = sense;

        while (a < b && bdex < moves.size()) {
            Board clone = makeMove(board, moves, bdex);
            if (clone.getWinner() == myColor() && depth == MAX_DEPTH) {
                bestScore = INFTY;
                if (saveMove) {
                    best = getMove(board, moves, bdex);
                    _lastFoundMove = best;
                }
                return bestScore;
            }
            int temp = minMax(clone, depth - 1, false, player * (-1), a, b);
            if (player == ogSense) {
                if (temp > a) {
                    a = temp;
                }
                if (temp > bestScore) {
                    bestScore = temp;
                    if (saveMove) {
                        best = getMove(board, moves, bdex);
                    }
                }
            } else {
                if (temp < b) {
                    b = temp;
                }
                if (sense * temp > bestScore) {
                    bestScore = temp;
                    if (saveMove) {
                        best = getMove(board, moves, bdex);
                    }
                }
            }
            bdex += 1;
        }

        if (saveMove) {
            _lastFoundMove = best;
        }
        return bestScore;
    }

    /** Return a heuristic value for BOARD.  This value is +- WINNINGVALUE in
     *  won positions, and 0 for ties. */
    private int staticScore(Board board, int winningValue) {
        PieceColor winner = board.getWinner();
        if (winner != null) {
            return switch (winner) {
            case RED -> winningValue;
            case BLUE -> -winningValue;
            default -> 0;
            };
        }
        PieceColor opponent;
        if (myColor() == RED) {
            opponent = BLUE;
        } else {
            opponent = RED;
        }
        int heuristic = board.numPieces(myColor()) - board.numPieces(opponent);
        return heuristic;
    }

    /** Pseudo-random number generator for move computation. */
    private Random _random = new Random();

    private ArrayList<String> moveList(Board board, PieceColor color) {
        ArrayList<String> coords = new ArrayList<String>();
        ArrayList<String> moves = new ArrayList<String>();
        for (char i = 'a'; i < 'h'; i++) {
            for (int j = 1; j < 8; j++) {
                char a = i;
                char b = (char) (j + '0');
                String curr = "";
                curr = curr + a + b;
                if (board.get(a, b) == color && !coords.contains(curr)) {
                    coords.add(curr);
                }
            }
        }
        int brk = 1;
        for (int z = 0; z < coords.size(); z++) {
            String curr = coords.get(z);
            char a = curr.charAt(0);
            char b = curr.charAt(1);
            for (int i = -2; i < 3; i++) {
                for (int j = -2; j < 3; j++) {
                    char x = (char) (a + i);
                    char y = (char) (b + j);
                    if (board.legalMove(a, b, x, y)) {
                        String move = "";
                        move = move + a + b + x + y;
                        moves.add(move);
                    }
                }
            }
        }
        return moves;
    }
    private Board makeMove(Board board, ArrayList<String> moves, int i) {
        String move = moves.get(i);
        Board clone = new Board(board);
        char a = move.charAt(0);
        char b = move.charAt(1);
        char c = move.charAt(2);
        char d = move.charAt(3);
        clone.makeMove(a, b, c, d);
        return clone;
    }
    private Move getMove(Board board, ArrayList<String> moves, int i) {
        String move = moves.get(i);
        Move best;
        char a = move.charAt(0);
        char b = move.charAt(1);
        char c = move.charAt(2);
        char d = move.charAt(3);
        best = Move.move(a, b, c, d);
        return best;
    }
}
