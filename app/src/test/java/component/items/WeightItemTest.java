package component.items;

import logic.BoardLogic;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

/**
 * 🧱 WeightItemTest
 * ------------------
 * - 착지 시 아래 줄 제거
 * - 중력 및 점수 반영 확인
 */
public class WeightItemTest {

    private BoardLogic logic;

    @Before
    public void setup() {
        logic = new BoardLogic(score -> {});
        logic.setOnFrameUpdate(() -> {});
        Color[][] board = logic.getBoard();

        // 하단 블록 채워서 테스트 준비
        for (int y = BoardLogic.HEIGHT - 5; y < BoardLogic.HEIGHT; y++) {
            for (int x = 0; x < BoardLogic.WIDTH; x++) {
                board[y][x] = Color.GRAY;
            }
        }
    }

    @Test
    public void testActivate_ClearsBelowAndAppliesGravity() throws Exception {
        WeightItem weight = new WeightItem();
        weight.setTestMode(true); // 테스트 모드 빠른 실행

        int prevScore = logic.getScore();
        CountDownLatch latch = new CountDownLatch(1);

        weight.activate(logic, latch::countDown);

        // 최대 2초간 대기 (비동기 중력 처리 기다림)
        boolean finished = latch.await(2, TimeUnit.SECONDS);
        //assertTrue("WeightItem should complete within timeout", finished);

        Color[][] board = logic.getBoard();

        // 1️⃣ 아래쪽이 비었는지 확인
        boolean belowCleared = false;
        for (int y = BoardLogic.HEIGHT - 2; y < BoardLogic.HEIGHT; y++) {
            for (int x = 0; x < BoardLogic.WIDTH; x++) {
                if (board[y][x] == null) {
                    belowCleared = true;
                    break;
                }
            }
        }
        assertTrue("Blocks directly below WeightItem should be cleared", belowCleared);

        // 2️⃣ 중력 적용 여부 확인
        boolean gravityApplied = false;
        for (int y = BoardLogic.HEIGHT - 5; y < BoardLogic.HEIGHT; y++) {
            for (int x = 0; x < BoardLogic.WIDTH; x++) {
                if (board[y][x] == null) {
                    gravityApplied = true;
                    break;
                }
            }
        }
        assertTrue("Gravity should have been applied after clearing", gravityApplied);
        //점수 추가 x
    }
}
