package logic;

import java.awt.Color;
import java.util.function.Consumer;
import java.awt.Toolkit;

import blocks.Block;
import component.BlockBag;
import component.SpeedManager;
import component.items.ItemBlock;
import component.items.LineClearItem;
import component.items.WeightItem;
import component.items.DoubleScoreItem;
import component.items.RandomClearItem;
import component.items.SlowItem;

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

    private Runnable onFrameUpdate; // UI 업데이트 콜백

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

    // --- 추가: 모드/버프 상태 ---
    private boolean itemMode = false;
    private boolean doubleScore = false;
    private long doubleScoreEndTime = 0;
    private boolean slowed = false;
    private long slowEndTime = 0;

    private int deletedLinesTotal = 0; // 지금까지 삭제된 줄의 총합
    private boolean nextIsItem = false; // 다음 블록이 아이템인지 여부

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

            // // 💥 아이템 블록 착지 시 발동
            // if (itemMode && curr instanceof ItemBlock item) {
            // item.activate(this);
            // }
            // 💥 아이템 착지 시 처리
            if (itemMode && curr instanceof ItemBlock item) {
                // 콜백: 애니메이션 끝난 후 다음 블록 생성
                item.activate(this, () -> {
                    clearLines();
                    curr = nextIsItem ? generateItemBlock() : bag.next();
                    nextIsItem = false;
                    x = 3;
                    y = 0;

                    if (!canMove(curr, x, y)) {
                        gameOver = true;
                        onGameOver.accept(score);
                    }
                });
                return; // ✅ 다음 블록 생성을 여기서 멈춰
            }
            clearLines();

            // // 새 블록 생성
            // if (itemMode && clearedLines > 0 && clearedLines % 10 == 0) {
            // curr = generateItemBlock();
            // } else {
            // curr = bag.next();
            // }
            curr = generateItemBlock();

            x = 3;
            y = 0;

            if (!canMove(curr, x, y)) {
                gameOver = true;
                onGameOver.accept(score);
            }
        }
    }

    // === 좌우 이동 ===
    public void moveLeft() {
        if (canMove(curr, x - 1, y))
            x--;
    }

    public void moveRight() {
        if (canMove(curr, x + 1, y))
            x++;
    }

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
            if (canMove(curr, x - 1, y))
                x--;
            else if (canMove(curr, x + 1, y))
                x++;
            else {
                curr = backup;
                x = oldX;
                y = oldY;
            }
        }
    }

    // === 줄 클리어 ===
    private void clearLines() {
        int linesThisRound = 0;
        for (int i = 0; i < HEIGHT; i++) {
            boolean full = true;
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j] == null) {
                    full = false;
                    break;
                }
            }
            // if (full) {
            // for (int k = i; k > 0; k--)
            // board[k] = board[k - 1].clone();
            // board[0] = new Color[WIDTH];

            // score += 100;
            // }

            if (full) {
                linesThisRound++;

                // 🎬 라인 삭제 애니메이션
                animateRowClearSequential(i, () -> {
                    // 콜백 (애니메이션 끝난 후)
                    score += 100;
                    clearedLines++;
                    deletedLinesTotal++;

                    if (clearedLines % 10 == 0)
                        speedManager.increaseLevel();

                    if (onFrameUpdate != null)
                        onFrameUpdate.run();
                });
            }
        }
        // clearedLines++;
        // if (clearedLines % 10 == 0)
        // speedManager.increaseLevel();
        clearedLines += linesThisRound;
        deletedLinesTotal += linesThisRound;

        // 난이도 조정
        if (clearedLines % 10 == 0)
            speedManager.increaseLevel();

        // ✅ 10줄마다 아이템 예고
        if (deletedLinesTotal > 0 && deletedLinesTotal % 10 == 0)
            nextIsItem = true;

    }

    // === 아이템 블록 생성 ===
    private Block generateItemBlock() {
        double r = Math.random();
        Block base = bag.next();
        if (r < 0.2)
            return new LineClearItem(base);
        // else if (r < 0.4)
        else
            return new WeightItem();
        // else if (r < 0.6)
        //     return new DoubleScoreItem();
        // else if (r < 0.8)
        //     return new RandomClearItem();
        // else
        //     return new SlowItem();
    }

    /**
     * 일정 시간 동안 점수를 2배로 적용한다.
     * 
     * @param durationMs 버프 지속시간 (밀리초)
     */
    public void enableDoubleScore(long durationMs) {
        doubleScore = true;
        doubleScoreEndTime = System.currentTimeMillis() + durationMs;
    }

    // 점수 조정용
    public void addScore(int delta) {
        score += (isDoubleScoreActive() ? delta * 2 : delta);
    }

    private boolean isDoubleScoreActive() {
        if (doubleScore && System.currentTimeMillis() < doubleScoreEndTime)
            return true;
        else {
            doubleScore = false;
            return false;
        }
    }

    public void slowDownTemporarily(long ms) {
        slowed = true;
        slowEndTime = System.currentTimeMillis() + ms;
    }

    public int getDropInterval() {
        int base = speedManager.getDropInterval();
        if (slowed && System.currentTimeMillis() < slowEndTime)
            return (int) (base * 1.5);
        else {
            slowed = false;
            return base;
        }
    }

    // === 모드 관리 ===
    public void setItemMode(boolean enabled) {
        this.itemMode = enabled;
    }

    public boolean isItemMode() {
        return itemMode;
    }

    public void updateBoardAfterClear() {
        // 아래에서 위로 탐색하면서, 각 칸을 가능한 아래쪽으로 떨어뜨린다.
        for (int row = HEIGHT - 2; row >= 0; row--) { // 위에서부터
            for (int col = 0; col < WIDTH; col++) {
                if (board[row][col] != null) {
                    int dropTo = row;
                    while (dropTo + 1 < HEIGHT && board[dropTo + 1][col] == null) {
                        dropTo++;
                    }
                    if (dropTo != row) {
                        board[dropTo][col] = board[row][col];
                        board[row][col] = null;
                    }
                }
            }
        }
    }

    // // === 라인 클리어 애니메이션 ===
    // public void animateRowClear(int targetY, Runnable onComplete) {
    // new javax.swing.Timer(80, new java.awt.event.ActionListener() {
    // int count = 0;

    // @Override
    // public void actionPerformed(java.awt.event.ActionEvent e) {
    // if (count < 4) { // 4번 깜빡임
    // boolean visible = (count % 2 == 0);
    // for (int j = 0; j < WIDTH; j++) {
    // board[targetY][j] = visible ? Color.WHITE : null;
    // }
    // count++;
    // } else {
    // ((javax.swing.Timer) e.getSource()).stop();
    // // 실제 삭제
    // for (int j = 0; j < WIDTH; j++)
    // board[targetY][j] = null;
    // updateBoardAfterClear();
    // if (onComplete != null)
    // onComplete.run();
    // }
    // }
    // }).start();
    // }
    // 중력 낙하 애니메이션
    public void animateGravityFall() {
        new javax.swing.Timer(60, new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                boolean moved = false;
                for (int row = HEIGHT - 2; row >= 0; row--) {
                    for (int col = 0; col < WIDTH; col++) {
                        if (board[row][col] != null && board[row + 1][col] == null) {
                            board[row + 1][col] = board[row][col];
                            board[row][col] = null;
                            moved = true;
                        }
                    }
                }
                if (!moved)
                    ((javax.swing.Timer) e.getSource()).stop();
            }
        }).start();
    }

    /**
     * 한 줄(row)을 왼쪽→오른쪽으로 순차적으로 지우는 애니메이션.
     */
    public void animateRowClearSequential(int targetY, Runnable onComplete) {
        final int[] col = { 0 };

        javax.swing.Timer timer = new javax.swing.Timer(50, null);
        timer.addActionListener(e -> {
            if (col[0] < WIDTH) {
                // 💡 현재 칸 밝게 표시
                if (board[targetY][col[0]] != null)
                    board[targetY][col[0]] = new Color(255, 255, 180);

                if (onFrameUpdate != null)
                    onFrameUpdate.run();

                // 60ms 뒤에 실제 삭제
                int x = col[0];
                new javax.swing.Timer(60, ev -> {
                    board[targetY][x] = null;
                    if (onFrameUpdate != null)
                        onFrameUpdate.run();
                    ((javax.swing.Timer) ev.getSource()).stop();
                }).start();

                col[0]++;
            } else {
                ((javax.swing.Timer) e.getSource()).stop();

                // 💥 모든 칸을 확실하게 null 처리 (혹시 안 지워진 칸 대비)
                for (int j = 0; j < WIDTH; j++)
                    board[targetY][j] = null;

                // 💫 중력 적용 (라인 클리어 후)
                updateBoardAfterClear();

                // Toolkit.getDefaultToolkit().beep(); //효과음 가능

                if (onFrameUpdate != null)
                    onFrameUpdate.run();

                if (onComplete != null)
                    onComplete.run();
            }
        });

        timer.start();
    }

    private boolean isLineEmpty(int i) {
        for (int j = 0; j < BoardLogic.WIDTH; j++) {
            if (board[i][j] != null)
                return false;
        }
        return true;
    }

    // === Getter ===
    public Color[][] getBoard() {
        return board;
    }
    
    public Block getCurr() {
        return curr;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getScore() {
        return score;
    }

    public int getLevel() {
        return speedManager.getLevel();
    }

    // public int getDropInterval() {
    // return speedManager.getDropInterval();
    // }

    public boolean isGameOver() {
        return gameOver;
    }

    public BlockBag getBag() {
        return bag;
    }

    public void setOnFrameUpdate(Runnable r) {
        this.onFrameUpdate = r;
    }

    public Runnable getOnFrameUpdate() {
        return onFrameUpdate;
    }
    public int getLinesCleared() {
        return clearedLines;
    }

}
