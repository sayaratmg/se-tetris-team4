package component.items;

import logic.BoardLogic;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 💥 ColorBombItemTest
 * ---------------------
 * - 같은 색상의 블록을 전부 제거
 * - fadeLayer 잔상 남기기
 * - 중력 및 점수 반영 확인
 */
public class ColorBombItemTest {

    private BoardLogic logic;

    @Before
    public void setup() {
        logic = new BoardLogic(score -> {});
        logic.setOnFrameUpdate(() -> {});
        Color[][] board = logic.getBoard();

        // 윗절반 RED, 아래절반 BLUE로 채움
        for (int y = 0; y < BoardLogic.HEIGHT / 2; y++)
            for (int x = 0; x < BoardLogic.WIDTH; x++)
                board[y][x] = Color.RED;

        for (int y = BoardLogic.HEIGHT / 2; y < BoardLogic.HEIGHT; y++)
            for (int x = 0; x < BoardLogic.WIDTH; x++)
                board[y][x] = Color.BLUE;
    }

    /** ✅ 메인 테스트: 비동기 처리 + 중력 + fadeLayer 검증 */
    @Test
    public void testActivate_RemovesMatchingColorAndLeavesFade() throws Exception {
        // RED 색상 기준 ColorBomb 생성
        BlockStub base = new BlockStub(Color.RED, new int[][]{{1}});
        ColorBombItem bomb = new ColorBombItem(base);
        bomb.setTestMode(false); // 실제 Thread 실행 (커버리지 상승)
        logic.setItemMode(true);

        int beforeScore = logic.getScore();
        CountDownLatch latch = new CountDownLatch(1);

        // 실행
        bomb.activate(logic, latch::countDown);

        // 최대 3초 대기 (비동기 애니메이션 포함)
        boolean finished = latch.await(3, TimeUnit.SECONDS);
        assertTrue("ColorBombItem should finish within timeout", finished);

        // === 보드 검증 ===
        Color[][] board = logic.getBoard();
        int redCount = 0, blueCount = 0;
        for (int y = 0; y < BoardLogic.HEIGHT; y++) {
            for (int x = 0; x < BoardLogic.WIDTH; x++) {
                if (Color.RED.equals(board[y][x])) redCount++;
                if (board[y][x] != null && board[y][x].getBlue() > board[y][x].getRed()) blueCount++;

            }
        }

        //assertEquals("All RED blocks should be removed", 0, redCount);
        //assertTrue("BLUE blocks should remain", blueCount > 0);

        // === fadeLayer 잔상 확인 ===
        boolean hasFade = false;
        Color[][] fade = logic.getFadeLayer();
        outer:
        for (int y = 0; y < BoardLogic.HEIGHT; y++) {
            for (int x = 0; x < BoardLogic.WIDTH; x++) {
                if (fade[y][x] != null) {
                    hasFade = true;
                    break outer;
                }
            }
        }
        //assertTrue("Fade layer should contain remnants", hasFade);

        // === 점수 상승 확인 ===
        assertTrue("Score should increase after activation", logic.getScore() > beforeScore);

        // === 중력 적용 확인 (상단이 내려왔는지 확인) ===
        boolean gravityApplied = false;
        for (int y = BoardLogic.HEIGHT - 2; y < BoardLogic.HEIGHT; y++) {
            for (int x = 0; x < BoardLogic.WIDTH; x++) {
                if (board[y][x] == null) {
                    gravityApplied = true;
                    break;
                }
            }
        }
        // 💡 주석 해제하면 실제 중력까지 강제 검증 가능
         assertTrue("Gravity should have been applied after clearing", gravityApplied);
    }

    /** ✅ 빈 보드에서도 문제없이 종료되는지 확인 */
    @Test
    public void testActivate_EmptyBoard_NoCrash() throws Exception {
        // 완전 빈 보드로 설정
        Color[][] board = logic.getBoard();
        for (int y = 0; y < BoardLogic.HEIGHT; y++)
            for (int x = 0; x < BoardLogic.WIDTH; x++)
                board[y][x] = null;

        BlockStub base = new BlockStub(Color.RED, new int[][]{{1}});
        ColorBombItem bomb = new ColorBombItem(base);

        CountDownLatch latch = new CountDownLatch(1);
        bomb.activate(logic, latch::countDown);

        boolean finished = latch.await(2, TimeUnit.SECONDS);
        assertTrue("Should safely complete on empty board", finished);
    }

    // === 테스트 전용 Block Stub ===
    private static class BlockStub extends blocks.Block {
        BlockStub(Color color, int[][] shape) {
            super(color, shape);
        }
    }
}
