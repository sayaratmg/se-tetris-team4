package component;

import component.GameConfig;

public class SpeedManager {
    private int level;
    private int dropInterval;
    private int decrement; // 레벨 업 시 속도 증가폭

    public SpeedManager() {
        setDifficulty(GameConfig.Difficulty.NORMAL);
    }

    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 난이도 설정 시 초기 속도 및 증가폭 조정
     */
    public void setDifficulty(GameConfig.Difficulty difficulty) {
        switch (difficulty) {
            case EASY -> {
                level = 1;
                dropInterval = 1000; // 느리게 시작
                decrement = 100; // 천천히 빨라짐
            }
            case NORMAL -> {
                level = 1;
                dropInterval = 800; // 보통
                decrement = 120;
            }
            case HARD -> {
                level = 1;
                dropInterval = 600; // 빠르게 시작
                decrement = 150; // 더 가파르게 빨라짐
            }
        }
    }

    public void increaseLevel() {
        if (level < 10) {
            level++;
            dropInterval = Math.max(250, (int) (dropInterval * 0.9)); // 10%씩 감소
        }
    }

    public int getDropInterval() {
        return dropInterval;
    }

    public int getLevel() {
        return level;
    }

    /**
     * 디버깅용 toString()
     */
    @Override
    public String toString() {
        return "SpeedManager{level=" + level + ", dropInterval=" + dropInterval + "}";
    }
}
