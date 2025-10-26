package component.score;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ScoreBoard {

    private final List<ScoreEntry> entries = new ArrayList<>();
    private final int capacity;
    private final Path storagePath;

    public ScoreBoard(Path storagePath, int capacity) {
        this.storagePath = storagePath;
        this.capacity = Math.max(1, capacity);
        load();
    }

    public static ScoreBoard createDefault() {
        Path p = Paths.get(System.getProperty("user.home"), ".tetris", "scores.csv");
        return new ScoreBoard(p, 999);
    }

    public ScoreBoard(Path storagePath) {
        this(storagePath,10);
    }

    /** 현재 스코어 목록 (읽기 전용) 반환 */
    public synchronized List<ScoreEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * 점수 추가 → 정렬 → 용량 초과 시 잘라내기 → 파일 저장
     * @return 추가된 점수의 인덱스(등수)
     */
    public synchronized int addScore(String name, int score) {
        ScoreEntry e = new ScoreEntry(name, score, LocalDateTime.now());
        entries.add(e);
        entries.sort(ScoreEntry::compareTo);
        if (entries.size() > capacity) {
            entries.subList(capacity, entries.size()).clear();
        }
        save();
        return entries.indexOf(e);
    }

    /** 모든 점수 초기화 */
    public synchronized void reset() {
        entries.clear();
        save();
    }

    /** 파일에서 점수 목록을 불러오기 */
    public synchronized void load() {
        entries.clear();
        try {
            if (Files.notExists(storagePath)) {
                if (storagePath.getParent() != null) {
                    Files.createDirectories(storagePath.getParent());
                }
                Files.createFile(storagePath);
                return;
            }
            List<String> lines = Files.readAllLines(storagePath, StandardCharsets.UTF_8);
            lines.stream()
                 .map(ScoreEntry::fromCsv)
                 .filter(Optional::isPresent)
                 .map(Optional::get)
                 .sorted()
                 .limit(capacity)
                 .forEach(entries::add);
        } catch (IOException e) {
                System.out.println("log test");  
        }
    }

    /**
     * 현재 점수 목록을 CSV 형태로 파일에 저장
     */
    public synchronized void save() {
       try {
           List<String> lines = entries.stream()
                                        .map(ScoreEntry::toCSV)
                                        .collect(Collectors.toList());
        if (storagePath.getParent() != null) {
            Files.createDirectories(storagePath.getParent());
        }
        Files.write(storagePath, lines, StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
       } catch (IOException e) {
                System.out.println("log test"); 
       } 
    }
}
