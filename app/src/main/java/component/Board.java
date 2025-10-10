package component;

import logic.BoardLogic;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class Board extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final char BORDER_CHAR = 'X';

    // === UI 구성 요소 ===
    private final JTextPane pane;
    private final JLabel scoreLabel;
    private final JLabel statusLabel;
    private final JPanel rootPanel;
    private final javax.swing.Timer timer;

    private boolean isPaused = false;
    private final BoardLogic logic;

    // === 생성자 ===
    public Board() {
        super("SeoulTech SE Tetris");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // ===== 메인 보드 패널 =====
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setFocusable(false);
        pane.setBackground(Color.BLACK);
        pane.setFont(new Font("Courier New", Font.PLAIN, 18));
        CompoundBorder border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 10),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
        pane.setBorder(border);

        rootPanel = new JPanel(new BorderLayout());
        rootPanel.add(pane, BorderLayout.CENTER);

        // ===== 사이드 패널 =====
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        scoreLabel = new JLabel("Score: 0");
        statusLabel = new JLabel("Ready");
        scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        side.add(scoreLabel);
        side.add(Box.createVerticalStrut(8));
        side.add(statusLabel);
        rootPanel.add(side, BorderLayout.EAST);

        setContentPane(rootPanel);

        // ===== 로직 초기화 =====
        logic = new BoardLogic(score -> setStatus("GAME OVER! Score: " + score));

        // ===== 타이머 설정 =====
        timer = new javax.swing.Timer(logic.getDropInterval(), e -> {
            if (!isPaused && !logic.isGameOver()) {
                logic.moveDown();
                drawBoard();
            }
        });

        setupKeyBindings();

        drawBoard();
        timer.start();

        setSize(500, 700);
        setVisible(true);
        rootPanel.requestFocusInWindow();
    }

    // === 키 바인딩 ===
    private void setupKeyBindings() {
        InputMap im = rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = rootPanel.getActionMap();
        rootPanel.setFocusTraversalKeysEnabled(false);

        im.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        im.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        im.put(KeyStroke.getKeyStroke("UP"), "rotate");
        im.put(KeyStroke.getKeyStroke("SPACE"), "hardDrop");
        im.put(KeyStroke.getKeyStroke("P"), "pause");
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "exit");

        am.put("moveRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                logic.moveRight();
                drawBoard();
            }
        });
        am.put("moveLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                logic.moveLeft();
                drawBoard();
            }
        });
        am.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                logic.moveDown();
                drawBoard();
            }
        });
        am.put("rotate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                logic.rotateBlock();
                drawBoard();
            }
        });
        am.put("hardDrop", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                logic.hardDrop();
                drawBoard();
            }
        });
        am.put("pause", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
        am.put("exit", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                exitGame();
            }
        });
    }

    // === UI 갱신 ===
    public void drawBoard() {
        StringBuilder sb = new StringBuilder();

        // 윗 테두리
        for (int t = 0; t < BoardLogic.WIDTH + 2; t++)
            sb.append(BORDER_CHAR);
        sb.append("\n");

        // 내부
        for (int i = 0; i < BoardLogic.HEIGHT; i++) {
            sb.append(BORDER_CHAR);
            for (int j = 0; j < BoardLogic.WIDTH; j++) {
                boolean filled = false;
                if (logic.getBoard()[i][j] != null)
                    filled = true;
                else if (isCurrBlockAt(j, i))
                    filled = true;

                sb.append(filled ? "O" : " ");
            }
            sb.append(BORDER_CHAR).append("\n");
        }

        // 아랫 테두리
        for (int t = 0; t < BoardLogic.WIDTH + 2; t++)
            sb.append(BORDER_CHAR);

        // 정보 표시
        sb.append("\nSCORE: ").append(logic.getScore());
        sb.append("\nLEVEL: ").append(logic.getLevel());
        sb.append("\nNEXT: ").append(logic.getBag().peekNext(1).get(0).getClass().getSimpleName());
        if (isPaused)
            sb.append("\n[일시정지]");

        // ===== 텍스트 반영 =====
        pane.setText(sb.toString());
        StyledDocument doc = pane.getStyledDocument();

        // 색칠 (보드)
        for (int i = 0; i < BoardLogic.HEIGHT; i++) {
            for (int j = 0; j < BoardLogic.WIDTH; j++) {
                Color c = null;
                if (logic.getBoard()[i][j] != null)
                    c = logic.getBoard()[i][j];
                if (isCurrBlockAt(j, i))
                    c = logic.getCurr().getColor();

                if (c != null) {
                    SimpleAttributeSet style = new SimpleAttributeSet();
                    StyleConstants.setForeground(style, c);
                    int pos = (i + 1) * (BoardLogic.WIDTH + 3) + (j + 1);
                    doc.setCharacterAttributes(pos, 1, style, true);
                }
            }
        }

        // 테두리 색칠
        Color borderColor = Color.LIGHT_GRAY;
        SimpleAttributeSet borderStyle = new SimpleAttributeSet();
        StyleConstants.setForeground(borderStyle, borderColor);

        String text = pane.getText();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == BORDER_CHAR)
                doc.setCharacterAttributes(i, 1, borderStyle, true);
        }

        scoreLabel.setText("Score: " + logic.getScore());
    }

    // === 현재 블록 위치 확인 ===
    private boolean isCurrBlockAt(int j, int i) {
        var curr = logic.getCurr();
        int x = logic.getX(), y = logic.getY();

        for (int dy = 0; dy < curr.height(); dy++) {
            for (int dx = 0; dx < curr.width(); dx++) {
                if (curr.getShape(dx, dy) == 1) {
                    if (i == y + dy && j == x + dx)
                        return true;
                }
            }
        }
        return false;
    }

    private void togglePause() {
        isPaused = !isPaused;
        setStatus(isPaused ? "Paused" : "Playing");
    }

    private void exitGame() {
        timer.stop();
        System.exit(0);
    }

    public void setStatus(String text) {
        if (statusLabel != null)
            statusLabel.setText(text);
    }
}
