package component.score;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ScoreBoardTest {

    @Rule
    public TemporaryFolder tmp = new TemporaryFolder();

    private ScoreBoard newBoard(int capacity) throws Exception {
        Path file = tmp.newFile("scores.csv").toPath();
        return new ScoreBoard(file, capacity);
    }

    @Test
    public void addScore_sortsDescendingAndTrimsToCapacity() throws Exception {
        ScoreBoard board = newBoard(3);

        int r1 = board.addScore("A", 100);
        int r2 = board.addScore("B", 300);
        int r3 = board.addScore("C", 200);
        int r4 = board.addScore("D", 50);   // 용량 초과라면 잘림 대상

        // 정렬: 300(B), 200(C), 100(A)만 남음 (내림차순)
        assertEquals(3, board.getEntries().size());
        assertEquals("B", board.getEntries().get(0).name());
        assertEquals("C", board.getEntries().get(1).name());
        assertEquals("A", board.getEntries().get(2).name());
        
        assertEquals(0, r1); // A(100)을 추가한 '그 시점'엔 0등
        assertEquals(0, r2); // B(300)은 항상 0등
        assertEquals(1, r3); // C(200)은 그 시점에 1등(두 번째)
        assertEquals(-1, r4); // D(50)은 capacity 초과로 잘려 indexOf == -1

        // capacity 넘겨서 잘린 경우 indexOf(e) == -1 가능 (현재 구현 그대로 검증)
        assertEquals(-1, r4);
    }

    @Test
    public void saveAndLoad_roundTrip() throws Exception {
        Path file = tmp.newFile("scores.csv").toPath();
        ScoreBoard board = new ScoreBoard(file, 5);

        board.addScore("Alice", 120);
        board.addScore("Bob", 300);
        board.addScore("Carol", 180);
        assertEquals(3, board.getEntries().size());

        // 새 인스턴스로 로드 → 동일한 정렬 상태 보장
        ScoreBoard loaded = new ScoreBoard(file, 5);
        assertEquals(3, loaded.getEntries().size());
        assertEquals("Bob", loaded.getEntries().get(0).name());
        assertEquals(300, loaded.getEntries().get(0).score());
    }

    @Test
    public void reset_clearsEntriesAndFile() throws Exception {
        Path file = tmp.newFile("scores.csv").toPath();
        ScoreBoard board = new ScoreBoard(file, 5);

        board.addScore("X", 10);
        assertFalse(board.getEntries().isEmpty());

        board.reset();
        assertTrue(board.getEntries().isEmpty());

        // 파일도 비어 있는지 확인
        String content = Files.readString(file, StandardCharsets.UTF_8);
        assertTrue(content.isEmpty());
    }

    @Test
    public void addScore_tieBreakersFollowScoreEntryCompareTo() throws Exception {
        ScoreBoard board = newBoard(5);

        // 동점(200)일 때, "최근 시간"이 앞으로 오도록
        // ScoreBoard.addScore는 now()를 쓰므로 순서대로 호출해서 서로 다른 at를 유도
        int a = board.addScore("A", 200);
        Thread.sleep(5);
        int b = board.addScore("B", 200);

        // 더 늦게 추가된 B가 더 "최근" → 리스트 0번이어야 함
        assertEquals("B", board.getEntries().get(0).name());
        assertEquals("A", board.getEntries().get(1).name());

        // 이름 타이브레이커 확인 (동점 + 같은 시각을 만들긴 어려우니 ScoreEntry 단위 테스트로 이미 커버됨)
    }

    @Test
    public void load_createsFileIfMissing_andKeepsCapacityLimit() throws Exception {
        Path file = tmp.getRoot().toPath().resolve("not-exists.csv");
        assertFalse(Files.exists(file));

        ScoreBoard board = new ScoreBoard(file, 2);
        assertTrue(Files.exists(file));  // load()가 파일 생성

        board.addScore("A", 100);
        board.addScore("B", 200);
        board.addScore("C", 300);

        assertEquals(2, board.getEntries().size());
        assertEquals("C", board.getEntries().get(0).name());
        assertEquals("B", board.getEntries().get(1).name());
    }
}
