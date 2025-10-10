package logic;

import java.awt.Color;
import java.util.function.Consumer;

import blocks.Block;
import component.BlockBag;
import component.SpeedManager;

/**
 * BoardLogic
 * 
 * - 게임 핵심 로직 전담 클래스
 * - Board(UI)는 화면, 키입력, 타이머만 담당
 * - BoardLogic은 블록 이동, 점수, 라인 클리어, 충돌, 게임오버 관리
 */
public class BoardLogic {

    // === 보드 기본 설정 ===
    public static final int HEIGHT = 20;
    public static final int WIDTH = 10;

    // === 게임 상태 ===
    private final Color[][] board = new Color[HEIGHT][WIDTH];
    private final BlockBag bag = new BlockBag();
    private final SpeedManager speedManager = new SpeedManager();
    private final Consumer<Integer> onGameOver; // UI 콜백

    private Block curr;
    private int x = 3, y = 0;
    private int score = 0;
    private int clearedLines = 0;
    private boolean gameOver = false;

    // === 생성자 ===
    public BoardLogic(Consumer<Integer> onGameOver) {
        this.onGameOver = onGameOver;
        this.curr = bag.next();
    }

    // === 이동 가능 여부 확인 ===
    public boolean canMove(Block block, int newX, int newY) {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int bx = newX + i, by = newY + j;
                    if (bx < 0 || bx >= WIDTH || by < 0 || by >= HEIGHT)
                        return false;
                    if (board[by][bx] != null)
                        return false;
                }
            }
        }
        return true;
    }

    // === 블록 낙하 ===
    public void moveDown() {
        if (canMove(curr, x, y + 1)) {
            y++;
            score++;
        } else {
            // 블록 고정
            for (int j = 0; j < curr.height(); j++) {
                for (int i = 0; i < curr.width(); i++) {
                    if (curr.getShape(i, j) == 1) {
                        int bx = x + i, by = y + j;
                        if (bx >= 0 && bx < WIDTH && by >= 0 && by < HEIGHT)
                            board[by][bx] = curr.getColor();
                    }
                }
            }
            clearLines();
            // 새 블록 생성
            curr = bag.next();
            x = 3;
            y = 0;

            if (!canMove(curr, x, y)) {
                gameOver = true;
                onGameOver.accept(score);
            }
        }
    }

    // === 좌우 이동 ===
    public void moveLeft() { if (canMove(curr, x - 1, y)) x--; }
    public void moveRight() { if (canMove(curr, x + 1, y)) x++; }

    // === 하드드롭 ===
    public void hardDrop() {
        while (canMove(curr, x, y + 1)) {
            y++;
            score += 2;
        }
        moveDown();
    }

    // === 회전 (벽킥 간단 적용) ===
    public void rotateBlock() {
        Block backup = curr.clone();
        int oldX = x, oldY = y;

        curr.rotate();
        if (!canMove(curr, x, y)) {
            if (canMove(curr, x - 1, y)) x--;
            else if (canMove(curr, x + 1, y)) x++;
            else {
                curr = backup;
                x = oldX;
                y = oldY;
            }
        }
    }

    // === 줄 클리어 ===
    private void clearLines() {
        for (int i = 0; i < HEIGHT; i++) {
            boolean full = true;
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j] == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int k = i; k > 0; k--)
                    board[k] = board[k - 1].clone();
                board[0] = new Color[WIDTH];

                score += 100;
                clearedLines++;
                if (clearedLines % 10 == 0)
                    speedManager.increaseLevel();
            }
        }
    }

    // === Getter ===
    public Color[][] getBoard() { return board; }
    public Block getCurr() { return curr; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getScore() { return score; }
    public int getLevel() { return speedManager.getLevel(); }
    public int getDropInterval() { return speedManager.getDropInterval(); }
    public boolean isGameOver() { return gameOver; }
    public BlockBag getBag() { return bag; }
}
