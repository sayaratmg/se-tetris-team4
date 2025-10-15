// package component;

// import logic.BoardLogic;

// import javax.swing.*;
// import javax.swing.border.CompoundBorder;
// import javax.swing.text.*;
// import java.awt.*;
// import java.awt.event.ActionEvent;

// public class Board extends JFrame {

//     private static final long serialVersionUID = 1L;

//     public static final char BORDER_CHAR = 'X';

//     // === UI 구성 요소 ===
//     private final JTextPane pane;
//     private final JLabel scoreLabel;
//     private final JLabel statusLabel;
//     private final JPanel rootPanel;
//     private final javax.swing.Timer timer;

//     private boolean isPaused = false;
//     private final BoardLogic logic;

//     // === 생성자 ===
//     public Board() {
//         super("SeoulTech SE Tetris");
//         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

//         // ===== 메인 보드 패널 =====
//         pane = new JTextPane();
//         pane.setEditable(false);
//         pane.setFocusable(false);
//         pane.setBackground(Color.BLACK);
//         pane.setFont(new Font("Courier New", Font.PLAIN, 18));
//         CompoundBorder border = BorderFactory.createCompoundBorder(
//                 BorderFactory.createLineBorder(Color.GRAY, 10),
//                 BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
//         pane.setBorder(border);

//         rootPanel = new JPanel(new BorderLayout());
//         rootPanel.add(pane, BorderLayout.CENTER);

//         // ===== 사이드 패널 =====
//         JPanel side = new JPanel();
//         side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
//         side.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

//         scoreLabel = new JLabel("Score: 0");
//         statusLabel = new JLabel("Ready");
//         scoreLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
//         statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

//         side.add(scoreLabel);
//         side.add(Box.createVerticalStrut(8));
//         side.add(statusLabel);
//         rootPanel.add(side, BorderLayout.EAST);

//         setContentPane(rootPanel);

//         // ===== 로직 초기화 =====
//         logic = new BoardLogic(score -> setStatus("GAME OVER! Score: " + score));
//         logic.setOnFrameUpdate(this::drawBoard);

//         // ===== 타이머 설정 =====
//         timer = new javax.swing.Timer(logic.getDropInterval(), e -> {
//             if (!isPaused && !logic.isGameOver()) {
//                 logic.moveDown();
//                 drawBoard();
//             }
//         });

//         setupKeyBindings();

//         drawBoard();
//         timer.start();

//         setSize(500, 700);
//         setVisible(true);
//         rootPanel.requestFocusInWindow();
//     }

//     // === 키 바인딩 ===
//     private void setupKeyBindings() {
//         InputMap im = rootPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
//         ActionMap am = rootPanel.getActionMap();
//         rootPanel.setFocusTraversalKeysEnabled(false);

//         im.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
//         im.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
//         im.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
//         im.put(KeyStroke.getKeyStroke("UP"), "rotate");
//         im.put(KeyStroke.getKeyStroke("SPACE"), "hardDrop");
//         im.put(KeyStroke.getKeyStroke("P"), "pause");
//         im.put(KeyStroke.getKeyStroke("ESCAPE"), "exit");

//         am.put("moveRight", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 logic.moveRight();
//                 drawBoard();
//             }
//         });
//         am.put("moveLeft", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 logic.moveLeft();
//                 drawBoard();
//             }
//         });
//         am.put("moveDown", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 logic.moveDown();
//                 drawBoard();
//             }
//         });
//         am.put("rotate", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 logic.rotateBlock();
//                 drawBoard();
//             }
//         });
//         am.put("hardDrop", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 logic.hardDrop();
//                 drawBoard();
//             }
//         });
//         am.put("pause", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 togglePause();
//             }
//         });
//         am.put("exit", new AbstractAction() {
//             public void actionPerformed(ActionEvent e) {
//                 exitGame();
//             }
//         });
//     }

//     // === UI 갱신 ===
//     public void drawBoard() {
//         StringBuilder sb = new StringBuilder();

//         // 윗 테두리
//         for (int t = 0; t < BoardLogic.WIDTH + 2; t++)
//             sb.append(BORDER_CHAR);
//         sb.append("\n");

//         // 내부
//         for (int i = 0; i < BoardLogic.HEIGHT; i++) {
//             sb.append(BORDER_CHAR);
//             for (int j = 0; j < BoardLogic.WIDTH; j++) {
//                 boolean filled = logic.getBoard()[i][j] != null;
//                 boolean currHere = isCurrBlockAt(j, i);

//                 // 기본 문자
//                 String cellChar = " ";

//                 // 1️⃣ 현재 움직이는 블록 그리기
//                 if (currHere) {
//                     if (logic.getCurr() instanceof component.items.LineClearItem lItem) {
//                         int localX = j - logic.getX();
//                         int localY = i - logic.getY();
//                         if (localX == lItem.getLX() && localY == lItem.getLY())
//                             cellChar = "L";
//                         else
//                             cellChar = "O";
//                     } else if (logic.getCurr() instanceof component.items.WeightItem) {
//                         cellChar = "W";
//                     } else {
//                         cellChar = "O";
//                     }
//                 }
//                 // 2️⃣ 고정된 블록 그리기
//                 else if (filled) {
//                     cellChar = "O";
//                 }

//                 sb.append(cellChar);
//             }
//             sb.append(BORDER_CHAR).append("\n");
//         }

//         // 아랫 테두리
//         for (int t = 0; t < BoardLogic.WIDTH + 2; t++)
//             sb.append(BORDER_CHAR);

//         // 정보 표시
//         sb.append("\nSCORE: ").append(logic.getScore());
//         sb.append("\nLEVEL: ").append(logic.getLevel());
//         sb.append("\nNEXT: ").append(logic.getBag().peekNext(1).get(0).getClass().getSimpleName());
//         if (isPaused)
//             sb.append("\n[일시정지]");

//         // ===== 텍스트 반영 =====
//         pane.setText(sb.toString());
//         StyledDocument doc = pane.getStyledDocument();

//         // 색칠 (보드)
//         for (int i = 0; i < BoardLogic.HEIGHT; i++) {
//             for (int j = 0; j < BoardLogic.WIDTH; j++) {
//                 Color c = logic.getBoard()[i][j];
//                 if (isCurrBlockAt(j, i))
//                     c = logic.getCurr().getColor();
//                 if (c != null) {
//                     SimpleAttributeSet style = new SimpleAttributeSet();
//                     StyleConstants.setForeground(style, c);
//                     int pos = (i + 1) * (BoardLogic.WIDTH + 3) + (j + 1);
//                     doc.setCharacterAttributes(pos, 1, style, true);
//                 }
//             }
//         }

//         // 테두리 색칠
//         Color borderColor = Color.LIGHT_GRAY;
//         SimpleAttributeSet borderStyle = new SimpleAttributeSet();
//         StyleConstants.setForeground(borderStyle, borderColor);

//         String text = pane.getText();
//         for (int i = 0; i < text.length(); i++) {
//             if (text.charAt(i) == BORDER_CHAR)
//                 doc.setCharacterAttributes(i, 1, borderStyle, true);
//         }

//         scoreLabel.setText("Score: " + logic.getScore());
//     }

//     // ======= 💡 추가: 회색(페이드용) 식별 헬퍼 =======
//     private boolean isFadeColor(Color c) {
//         int r = c.getRed(), g = c.getGreen(), b = c.getBlue();
//         // RGB가 거의 같고, 밝기가 150~230 사이면 페이드 중으로 간주
//         return Math.abs(r - g) < 10 && Math.abs(g - b) < 10 && r >= 150 && r <= 230;
//     }

//     // === 현재 블록 위치 확인 ===
//     private boolean isCurrBlockAt(int j, int i) {
//         var curr = logic.getCurr();
//         int x = logic.getX(), y = logic.getY();

//         for (int dy = 0; dy < curr.height(); dy++) {
//             for (int dx = 0; dx < curr.width(); dx++) {
//                 if (curr.getShape(dx, dy) == 1) {
//                     if (i == y + dy && j == x + dx)
//                         return true;
//                 }
//             }
//         }
//         return false;
//     }

//     private void togglePause() {
//         isPaused = !isPaused;
//         setStatus(isPaused ? "Paused" : "Playing");
//     }

//     private void exitGame() {
//         timer.stop();
//         System.exit(0);
//     }

//     public void setStatus(String text) {
//         if (statusLabel != null)
//             statusLabel.setText(text);
//     }

//     public BoardLogic getLogic() {
//         return logic;
//     }
// }
package component;

import logic.BoardLogic;
import blocks.Block;
import component.items.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

public class Board extends JFrame {

    private static final long serialVersionUID = 1L;

    // === 시각용 상수 ===
    private static final int CELL_SIZE = 35;
    private static final int CELL_GAP = 2;
    private static final int ARC = 8;

    private static final Color BG_DARK = new Color(20, 25, 35);
    private static final Color BG_PANEL = new Color(30, 35, 50);
    private static final Color BG_GAME = new Color(25, 30, 42);
    private static final Color ACCENT = new Color(100, 255, 218);
    private static final Color TEXT_PRIMARY = new Color(230, 237, 243);
    private static final Color TEXT_SECONDARY = new Color(136, 146, 176);
    private static final Color GRID_LINE = new Color(40, 45, 60);

    // === 로직 참조 ===
    private final BoardLogic logic;

    // === HUD ===
    private final JLabel scoreLabel = new JLabel("0");
    private final JLabel levelLabel = new JLabel("1");
    private final JLabel linesLabel = new JLabel("0");
    private final JPanel nextPanel = new JPanel();

    private boolean isFullScreen = false;
    private Rectangle normalBounds;
    private GraphicsDevice graphicsDevice;

    private final GamePanel gamePanel;
    private final javax.swing.Timer timer;

    public Board() {
        super("SeoulTech SE Tetris");

        // === 로직 초기화 ===
        logic = new BoardLogic(score -> showGameOver(score));
        logic.setOnFrameUpdate(this::drawBoard);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBackground(BG_DARK);

        graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getDefaultScreenDevice();

        JPanel root = new JPanel(new BorderLayout(20, 0));
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 중앙 게임 화면
        gamePanel = new GamePanel();
        root.add(gamePanel, BorderLayout.CENTER);

        // 오른쪽 HUD
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setBackground(BG_DARK);
        rightPanel.setBorder(new EmptyBorder(0, 10, 0, 0));

        JLabel titleLabel = new JLabel("TETRIS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 32));
        titleLabel.setForeground(ACCENT);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(titleLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        rightPanel.add(createStatPanel("SCORE", scoreLabel));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(createStatPanel("LEVEL", levelLabel));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        rightPanel.add(createStatPanel("LINES", linesLabel));
        rightPanel.add(Box.createRigidArea(new Dimension(0, 30)));

        JLabel nextLabel = new JLabel("NEXT");
        nextLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nextLabel.setForeground(TEXT_PRIMARY);
        nextLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(nextLabel);
        rightPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        nextPanel.setBackground(BG_DARK);
        nextPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        rightPanel.add(nextPanel);

        rightPanel.add(Box.createVerticalGlue());
        rightPanel.add(createControlsPanel());
        root.add(rightPanel, BorderLayout.EAST);

        add(root);
        setupKeys(gamePanel);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);

        // === 드롭 타이머 ===
        timer = new javax.swing.Timer(logic.getDropInterval(), e -> {
            if (!logic.isGameOver()) {
                logic.moveDown();
                drawBoard();
            }
        });
        timer.start();
    }

    private JPanel createStatPanel(String label, JLabel valueLabel) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 20, 15, 20));
        panel.setMaximumSize(new Dimension(200, 80));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Arial", Font.BOLD, 12));
        labelComp.setForeground(TEXT_SECONDARY);
        labelComp.setAlignmentX(Component.CENTER_ALIGNMENT);

        valueLabel.setFont(new Font("Arial", Font.BOLD, 28));
        valueLabel.setForeground(TEXT_PRIMARY);
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        panel.add(labelComp);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(valueLabel);
        return panel;
    }

    private JPanel createControlsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(BG_PANEL);
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setMaximumSize(new Dimension(200, 230));

        JLabel titleLabel = new JLabel("CONTROLS");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 12));
        titleLabel.setForeground(TEXT_SECONDARY);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));

        String[] controls = {
            "← → Move", "↑ Rotate", "↓ Soft Drop",
            "SPACE Hard Drop", "P Pause",
            "F11 Full Screen", "ESC Exit"
        };
        for (String control : controls) {
            JLabel label = new JLabel(control);
            label.setFont(new Font("Arial", Font.PLAIN, 11));
            label.setForeground(TEXT_SECONDARY);
            label.setAlignmentX(Component.CENTER_ALIGNMENT);
            panel.add(label);
            panel.add(Box.createRigidArea(new Dimension(0, 5)));
        }
        return panel;
    }

    private void setupKeys(JComponent comp) {
        InputMap im = comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = comp.getActionMap();

        im.put(KeyStroke.getKeyStroke("LEFT"), "left");
        im.put(KeyStroke.getKeyStroke("RIGHT"), "right");
        im.put(KeyStroke.getKeyStroke("DOWN"), "down");
        im.put(KeyStroke.getKeyStroke("UP"), "rotate");
        im.put(KeyStroke.getKeyStroke("SPACE"), "drop");
        im.put(KeyStroke.getKeyStroke("P"), "pause");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0), "fullscreen");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "exit");

        am.put("left", new AbstractAction() { public void actionPerformed(ActionEvent e) { logic.moveLeft(); drawBoard(); }}); 
        am.put("right", new AbstractAction(){ public void actionPerformed(ActionEvent e) { logic.moveRight(); drawBoard(); }}); 
        am.put("down", new AbstractAction() { public void actionPerformed(ActionEvent e) { logic.moveDown(); drawBoard(); }}); 
        am.put("rotate", new AbstractAction(){ public void actionPerformed(ActionEvent e) { logic.rotateBlock(); drawBoard(); }}); 
        am.put("drop", new AbstractAction()   { public void actionPerformed(ActionEvent e) { logic.hardDrop(); drawBoard(); }}); 
        am.put("pause", new AbstractAction()  { public void actionPerformed(ActionEvent e) { /* TODO: toggle pause */ }}); 
        am.put("fullscreen", new AbstractAction(){ public void actionPerformed(ActionEvent e) { toggleFullScreen(); }}); 
        am.put("exit", new AbstractAction(){ public void actionPerformed(ActionEvent e) { System.exit(0); }}); 
    }

    // === 화면 갱신 ===
    public void drawBoard() {
        scoreLabel.setText(String.valueOf(logic.getScore()));
        levelLabel.setText(String.valueOf(logic.getLevel()));
        linesLabel.setText(String.valueOf(logic.getLinesCleared()));
        updateNextHUD(logic.getBag().peekNext(3));
        gamePanel.repaint();
    }

    private void updateNextHUD(List<Block> nextBlocks) {
        nextPanel.removeAll();
        nextPanel.setLayout(new GridLayout(nextBlocks.size(), 1, 0, 10));

        for (Block b : nextBlocks) {
            JPanel container = new JPanel(new BorderLayout());
            container.setBackground(BG_PANEL);
            container.setPreferredSize(new Dimension(120, 80));

            JPanel blockPanel = new JPanel() {
                @Override protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    int blockSize = 15;
                    int offsetX = (getWidth() - b.width() * blockSize) / 2;
                    int offsetY = (getHeight() - b.height() * blockSize) / 2;
                    for (int j = 0; j < b.height(); j++)
                        for (int i = 0; i < b.width(); i++)
                            if (b.getShape(i, j) == 1) {
                                g2.setColor(b.getColor());
                                g2.fillRoundRect(offsetX + i * blockSize, offsetY + j * blockSize,
                                        blockSize - 2, blockSize - 2, 4, 4);
                            }
                }
            };
            blockPanel.setBackground(BG_PANEL);
            container.add(blockPanel, BorderLayout.CENTER);
            nextPanel.add(container);
        }
        nextPanel.revalidate();
        nextPanel.repaint();
    }

    private void showGameOver(int score) {
        JOptionPane.showMessageDialog(this,
                "게임 종료!\n최종 점수: " + score,
                "Game Over", JOptionPane.INFORMATION_MESSAGE);
        timer.stop();
        dispose();
    }

    private void toggleFullScreen() {
        if (!isFullScreen) {
            normalBounds = getBounds();
            dispose();
            setUndecorated(true);
            if (graphicsDevice.isFullScreenSupported())
                graphicsDevice.setFullScreenWindow(this);
            else setExtendedState(JFrame.MAXIMIZED_BOTH);
            setVisible(true);
            isFullScreen = true;
        } else {
            if (graphicsDevice.isFullScreenSupported())
                graphicsDevice.setFullScreenWindow(null);
            dispose();
            setUndecorated(false);
            if (normalBounds != null) setBounds(normalBounds);
            setVisible(true);
            isFullScreen = false;
        }
    }

    // === 내부 패널: 게임판 렌더링 ===
    private class GamePanel extends JPanel {
        GamePanel() {
            setPreferredSize(new Dimension(BoardLogic.WIDTH * CELL_SIZE, BoardLogic.HEIGHT * CELL_SIZE));
            setBackground(BG_GAME);
            setBorder(BorderFactory.createLineBorder(GRID_LINE, 3));
        }

        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color[][] grid = logic.getBoard();

            // 그리드
            g2.setColor(GRID_LINE);
            for (int r = 0; r <= BoardLogic.HEIGHT; r++)
                g2.drawLine(0, r * CELL_SIZE, BoardLogic.WIDTH * CELL_SIZE, r * CELL_SIZE);
            for (int c = 0; c <= BoardLogic.WIDTH; c++)
                g2.drawLine(c * CELL_SIZE, 0, c * CELL_SIZE, BoardLogic.HEIGHT * CELL_SIZE);

            // 고정 블록
            for (int r = 0; r < BoardLogic.HEIGHT; r++)
                for (int c = 0; c < BoardLogic.WIDTH; c++)
                    if (grid[r][c] != null)
                        drawCell(g2, c, r, grid[r][c], null);

            // 현재 블록
            Block curr = logic.getCurr();
            if (curr != null) {
                int bx = logic.getX();
                int by = logic.getY();
                for (int j = 0; j < curr.height(); j++) {
                    for (int i = 0; i < curr.width(); i++) {
                        if (curr.getShape(i, j) == 1) {
                            drawCell(g2, bx + i, by + j, curr.getColor(), curr);
                        }
                    }
                }
            }

            g2.dispose();
        }

        /** 셀 하나를 그리는 함수 */
        private void drawCell(Graphics2D g2, int col, int row, Color color, Block block) {
            int px = col * CELL_SIZE + CELL_GAP;
            int py = row * CELL_SIZE + CELL_GAP;
            int size = CELL_SIZE - CELL_GAP * 2;
            g2.setColor(color);
            g2.fillRoundRect(px, py, size, size, ARC, ARC);
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRoundRect(px, py, size, size / 3, ARC, ARC);
            g2.setColor(new Color(0, 0, 0, 40));
            g2.fillRoundRect(px, py + size * 2 / 3, size, size / 3, ARC, ARC);

            // ✅ 아이템 블록 문자 오버레이
            if (block instanceof ItemBlock item) {
                String symbol = getItemSymbol(item);
                if (symbol != null) {
                    g2.setColor(Color.BLACK);
                    g2.setFont(new Font("Arial", Font.BOLD, 18));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = px + (size - fm.stringWidth(symbol)) / 2;
                    int ty = py + (size + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(symbol, tx, ty);
                }
            }
        }

        /** 아이템 타입별 문자 */
        private String getItemSymbol(ItemBlock item) {
            if (item instanceof LineClearItem) return "L";
            if (item instanceof WeightItem) return "W";
            if (item instanceof DoubleScoreItem) return "D";
            if (item instanceof RandomClearItem) return "R";
            if (item instanceof SlowItem) return "S";
            return null;
        }
    }

    public BoardLogic getLogic() { return logic; }
}
