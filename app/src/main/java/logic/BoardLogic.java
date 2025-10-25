package logic;

import java.awt.Color;
import java.util.*;
import java.util.function.Consumer;

import blocks.Block;
import component.BlockBag;
import component.GameConfig;
import component.SpeedManager;
import component.items.ItemBlock;

public class BoardLogic {
    public static final int WIDTH = GameState.WIDTH;
    public static final int HEIGHT = GameState.HEIGHT;

    private final GameState state = new GameState();
    private final BlockBag bag = new BlockBag();
    private final SpeedManager speedManager = new SpeedManager();
    private final MovementService move = new MovementService(state);
    private final ClearService clear = new ClearService(state);
    private final ItemManager item = new ItemManager(bag);
    private final BuffManager buff = new BuffManager();

    private final Consumer<Integer> onGameOver;
    private Runnable onFrameUpdate;

    private boolean gameOver = false;
    private int score = 0;
    private int clearedLines = 0;
    private int deletedLinesTotal = 0;
    private boolean nextIsItem = false;
    private boolean itemMode = false;

    // 현재 + 다음 블록 큐
    private final LinkedList<Block> previewQueue = new LinkedList<>();

    public BoardLogic(Consumer<Integer> onGameOver) {
        this(onGameOver, GameConfig.Difficulty.NORMAL);
    }

    public BoardLogic(Consumer<Integer> onGameOver, GameConfig.Difficulty diff) {
        this.onGameOver = onGameOver;
        speedManager.setDifficulty(diff);

        // 초기 난이도 반영
        switch (diff) {
            case EASY -> speedManager.setLevel(1);
            case NORMAL -> speedManager.setLevel(3);
            case HARD -> speedManager.setLevel(5);
        }

        // 최초 4개 미리 준비
        refillPreview();
        state.setCurr(previewQueue.removeFirst()); // 현재 블록 설정
    }

    private void refillPreview() {
        while (previewQueue.size() < 4) {
            previewQueue.add(bag.next());
        }
    }

    public void setItemMode(boolean enabled) {
        this.itemMode = enabled;
    }

    // === 점수 관리 ===
    public void addScore(int delta) {
        if (buff.isDoubleScoreActive())
            score += delta * 2;
        else
            score += delta;
    }

    // === 이동 / 중력 ===
    public void moveDown() {
        if (move.canMove(state.getCurr(), state.getX(), state.getY() + 1)) {
            move.moveDown();
            score++;
        } else {
            fixBlock();
        }
    }

    private void fixBlock() {
        var b = state.getCurr();
        var board = state.getBoard();

        for (int j = 0; j < b.height(); j++) {
            for (int i = 0; i < b.width(); i++) {
                if (b.getShape(i, j) == 1) {
                    int bx = state.getX() + i;
                    int by = state.getY() + j;
                    if (bx >= 0 && bx < WIDTH && by >= 0 && by < HEIGHT)
                        board[by][bx] = b.getColor();
                }
            }
        }

        // 아이템 활성화
        if (itemMode && b instanceof ItemBlock ib) {
            ib.activate(this, this::spawnNext);
        } else {
            clearLines();
            spawnNext();
        }
    }

    private void clearLines() {
        int lines = clear.clearLines(onFrameUpdate, null);
        clearedLines += lines;
        deletedLinesTotal += lines;

        if (clearedLines % 10 == 0)
            speedManager.increaseLevel();

        if (itemMode && deletedLinesTotal > 0 && deletedLinesTotal % 2 == 0)
            nextIsItem = true;
    }

    private void spawnNext() {
        refillPreview();

        Block next;
        if (itemMode && nextIsItem) {
            next = item.generateItemBlock();
        } else {
            next = bag.next();
        }
        nextIsItem = false;

        // 큐 보정
        refillPreview();

        state.setCurr(next);
        state.setPosition(3, 0);

        if (!move.canMove(next, 3, 0)) {
            gameOver = true;
            onGameOver.accept(score);
        }
    }

    // === 이동 입력 ===
    public void moveLeft() {
        if (move.canMove(state.getCurr(), state.getX() - 1, state.getY()))
            move.moveLeft();
    }

    public void moveRight() {
        if (move.canMove(state.getCurr(), state.getX() + 1, state.getY()))
            move.moveRight();
    }

    public void rotateBlock() {
        Block backup = state.getCurr().clone();
        state.getCurr().rotate();
        if (!move.canMove(state.getCurr(), state.getX(), state.getY()))
            state.setCurr(backup);
    }

    public void hardDrop() {
        while (move.canMove(state.getCurr(), state.getX(), state.getY() + 1)) {
            move.moveDown();
            score += 2;
        }
        moveDown();
    }

    // === Getter ===
    public Color[][] getBoard() {
        return state.getBoard();
    }

    public Block getCurr() {
        return state.getCurr();
    }

    public int getX() {
        return state.getX();
    }

    public int getY() {
        return state.getY();
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return speedManager.getLevel();
    }

    public int getLinesCleared() {
        return clearedLines;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public void setOnFrameUpdate(Runnable r) {
        this.onFrameUpdate = r;
    }

    public Runnable getOnFrameUpdate() {
        return onFrameUpdate;
    }

    public int getDropInterval() {
        return buff.isSlowed()
                ? (int) (speedManager.getDropInterval() * 1.5)
                : speedManager.getDropInterval();
    }

    public BuffManager getBuffManager() {
        return buff;
    }

    public ClearService getClearService() {
        return clear;
    }

    public boolean isItemMode() {
        return itemMode;
    }

    // HUD용 NEXT 블록 미리보기 (현재 제외, 다음 3개)
    public List<Block> getNextBlocks() {
        return previewQueue.size() > 1
                ? new ArrayList<>(previewQueue.subList(0, Math.min(3, previewQueue.size())))
                : List.of();
    }
}
