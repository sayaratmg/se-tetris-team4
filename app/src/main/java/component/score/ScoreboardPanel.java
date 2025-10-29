package component.score;

import component.GameConfig;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class ScoreboardPanel extends JPanel {

    private final ScoreBoard scoreBoard;
    private final Runnable onBack; // 메뉴로 돌아가기 콜백

    private final JComboBox<GameConfig.Mode> cbMode =
            new JComboBox<>(GameConfig.Mode.values());
    private final JComboBox<GameConfig.Difficulty> cbDiff =
            new JComboBox<>(GameConfig.Difficulty.values());

    private final DefaultTableModel model;
    private final JTable table;

    private static final DateTimeFormatter F =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ScoreboardPanel(ScoreBoard scoreBoard, Runnable onBack) {
        this.scoreBoard = scoreBoard;
        this.onBack = onBack;

        setLayout(new BorderLayout(12, 12));
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 헤더(타이틀 + 뒤로가기)
        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Scoreboard");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 22f));
        header.add(title, BorderLayout.WEST);

        JButton btnBack = new JButton("← Back");
        btnBack.addActionListener(e -> {
            if (onBack != null) onBack.run();
        });
        header.add(btnBack, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        // 상단 필터 바
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        top.add(new JLabel("Mode:"));
        top.add(cbMode);
        top.add(Box.createHorizontalStrut(10));
        top.add(new JLabel("Difficulty:"));
        top.add(cbDiff);
        top.add(Box.createHorizontalStrut(10));

        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setToolTipText("파일 재로딩 후 새로 고침");
        btnRefresh.addActionListener(e -> {
            scoreBoard.load(); // 파일에서 다시 로드
            reloadTable();
        });
        top.add(btnRefresh);

        // (선택) 리셋 버튼들
        JButton btnResetBucket = new JButton("Reset This Bucket");
        btnResetBucket.addActionListener(e -> {
            var m = (GameConfig.Mode) cbMode.getSelectedItem();
            var d = (GameConfig.Difficulty) cbDiff.getSelectedItem();
            scoreBoard.resetBucket(m, d);
            reloadTable();
        });
        top.add(btnResetBucket);

        JButton btnResetAll = new JButton("Reset All");
        btnResetAll.addActionListener(e -> {
            int ans = JOptionPane.showConfirmDialog(this,
                    "모든 모드/난이도 스코어를 초기화할까요?",
                    "Reset All", JOptionPane.YES_NO_OPTION);
            if (ans == JOptionPane.YES_OPTION) {
                scoreBoard.resetAll();
                reloadTable();
            }
        });
        top.add(btnResetAll);

        add(top, BorderLayout.SOUTH); // 헤더 아래에 붙여도 되고, 상단/하단 취향대로

        // 테이블
        String[] cols = {"순위", "이름", "점수", "기록 시각", "모드", "난이도"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        table.setRowHeight(22);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // 콤보 이벤트로 즉시 갱신
        cbMode.addActionListener(e -> reloadTable());
        cbDiff.addActionListener(e -> reloadTable());

        // 초기 선택값
        cbMode.setSelectedItem(GameConfig.Mode.CLASSIC);
        cbDiff.setSelectedItem(GameConfig.Difficulty.EASY);

        // 첫 로드
        reloadTable();

        // ESC = 뒤로가기
        registerKeyboardAction(e -> { if (onBack != null) onBack.run(); },
                KeyStroke.getKeyStroke("ESCAPE"),
                WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentShown(java.awt.event.ComponentEvent e) {
                scoreBoard.load();   // 화면 들어올 때마다 파일→메모리 동기화
                reloadTable();
            }
        });
    }

    /** 외부에서 기본 선택을 바꾸고 싶을 때 호출 */
    public void setInitialSelection(GameConfig.Mode mode, GameConfig.Difficulty diff) {
        cbMode.setSelectedItem(mode);
        cbDiff.setSelectedItem(diff);
        reloadTable();
    }

    private void reloadTable() {
        model.setRowCount(0);
        var mode = (GameConfig.Mode) cbMode.getSelectedItem();
        var diff = (GameConfig.Difficulty) cbDiff.getSelectedItem();
        var list = scoreBoard.getEntries(mode, diff);

        for (int i = 0; i < list.size(); i++) {
            var e = list.get(i);
            model.addRow(new Object[]{
                    i + 1,
                    e.name(),
                    e.score(),
                    e.at().format(F),
                    e.mode(),
                    e.difficulty()
            });
        }
    }
}
