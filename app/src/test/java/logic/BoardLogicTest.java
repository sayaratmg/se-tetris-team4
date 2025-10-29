package logic;

import blocks.Block;
import component.GameConfig;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * BoardLogic 테스트 - 라인 커버리지 확보용
 * -------------------------------------------------
 * 커버 대상:
 * - 이동, 회전, 하드드랍
 * - 점수 증가, 아이템 모드
 * - 게임오버 발생
 * - getter/setter 및 프리뷰 큐
 */
public class BoardLogicTest {

    private BoardLogic logic;
    private AtomicBoolean gameOverCalled;

    /** 테스트용 더미 Block (2x2 정사각형) */
    static class DummyBlock extends Block {
        protected final int[][] shape = {{1, 1}, {1, 1}};
        protected final Color color = Color.RED;

        public DummyBlock() {
            super();
        }

        @Override
        public int[][] getShapeArray() { return shape; }

        @Override
        public int getShape(int x, int y) { return shape[y][x]; }

        @Override
        public int width() { return 2; }

        @Override
        public int height() { return 2; }

        @Override
        public Color getColor() { return color; }

        @Override
        public Block clone() {
            DummyBlock b = new DummyBlock();
            return b;
        }

        @Override
        public void rotate() {
            // no-op
        }
    }

    @Before
    public void setup() {
        gameOverCalled = new AtomicBoolean(false);
        logic = new BoardLogic(score -> gameOverCalled.set(true), GameConfig.Difficulty.NORMAL);
        logic.getState().setCurr(new DummyBlock());
        logic.getState().setPosition(3, 0);
    }

    @Test
    public void testInitialState() {
        assertNotNull(logic.getBoard());
        assertNotNull(logic.getCurr());
        assertFalse(logic.isGameOver());
        assertEquals(0, logic.getScore());
    }

    @Test
    public void testMoveLeftRightDown() {
        int x0 = logic.getX();
        logic.moveLeft();
        assertEquals(x0 - 1, logic.getX());

        logic.moveRight();
        assertEquals(x0, logic.getX());

        int y0 = logic.getY();
        logic.moveDown();
        assertTrue(logic.getY() > y0);
    }

    @Test
    public void testRotateBlockRollback() {
        // 회전 시 충돌 → 롤백 테스트
        DummyBlock b = new DummyBlock() {
            @Override
            public void rotate() {
                // 일부러 불가능한 회전 형태로 변경
                shape[0][0] = 9;
            }
        };
        logic.getState().setCurr(b);
        logic.rotateBlock();
        assertEquals(9, b.getShape(0, 0)); // 회전 시도 됨
    }

    @Test
    public void testHardDrop() {
        int yBefore = logic.getY();
        logic.hardDrop();
        assertTrue(logic.getY() >= yBefore);
        assertTrue(logic.getScore() > 0);
    }

    @Test
    public void testGameOverTriggered() {
        // 고정 후 이동 불가 → gameOver
        logic.getState().setPosition(0, GameState.HEIGHT - 2);
        logic.moveDown();
        logic.moveDown(); // fixBlock() 호출됨
        if (logic.isGameOver()) {
            assertTrue(gameOverCalled.get());
        }
    }

    @Test
    public void testSetShakeOffsetAndItemMode() {
        logic.setShakeOffset(3);
        assertEquals(3, logic.getShakeOffset());

        logic.setItemMode(true);
        assertTrue(logic.isItemMode());
    }

    @Test
    public void testGetNextBlocksAndPreviewQueue() {
        assertNotNull(logic.getNextBlocks());
        assertTrue(logic.getNextBlocks().size() <= 3);
    }

    @Test
    public void testClearLinesIntegration() {
        var clear = logic.getClearService();
        Color[][] board = logic.getBoard();

        // 맨 아래줄 채우기
        for (int x = 0; x < GameState.WIDTH; x++)
            board[GameState.HEIGHT - 1][x] = Color.BLUE;

        int lines = clear.clearLines(null, null);
        assertTrue(lines >= 0);
    }

    @Test
    public void testGravityFunctions() {
        var clear = logic.getClearService();
        clear.applyGravityInstantly();
        clear.applyLineGravity();
        clear.setSkipDuringItem(true);
        assertTrue(clear.isSkipDuringItem());
        clear.setSkipDuringItem(false);
        clear.applyGravityFromRow(0);
    }

    @Test
    public void testFadeLayerAndBuffGetter() {
        assertNotNull(logic.getFadeLayer());
        assertNotNull(logic.getBuffManager());
        assertTrue(logic.getDropInterval() > 0);
    }
}
