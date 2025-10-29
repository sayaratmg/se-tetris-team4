package component;

import component.GameConfig;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class SpeedManagerTest {

    private SpeedManager sm;

    @Before
    public void setup() {
        sm = new SpeedManager();
    }

    @Test
    public void testInitialValues() {
        assertEquals(1, sm.getLevel());
        assertEquals(1000, sm.getDropInterval());
        assertNotNull(sm.toString());
    }

    @Test
    public void testSetDifficultyEasyNormalHard() {
        sm.setDifficulty(GameConfig.Difficulty.EASY);
        sm.increaseLevel();
        int easyDrop = sm.getDropInterval();

        sm.setDifficulty(GameConfig.Difficulty.NORMAL);
        sm.increaseLevel();
        int normalDrop = sm.getDropInterval();

        sm.setDifficulty(GameConfig.Difficulty.HARD);
        sm.increaseLevel();
        int hardDrop = sm.getDropInterval();

        // HARD는 더 많이 줄어들어야 함 (빠름)
        assertTrue(hardDrop < normalDrop);
        assertTrue(easyDrop > normalDrop);
    }

    @Test
    public void testIncreaseLevelCapsAt10() {
        sm.setLevel(9);
        int before = sm.getDropInterval();
        sm.increaseLevel();
        sm.increaseLevel(); // 레벨 10 이상은 변화 없음
        assertTrue(sm.getLevel() <= 10);
        assertTrue(sm.getDropInterval() <= before);
    }

    @Test
    public void testToStringFormat() {
        String s = sm.toString();
        assertTrue(s.contains("SpeedManager"));
    }
}

                