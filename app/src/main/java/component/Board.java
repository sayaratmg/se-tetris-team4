package component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.util.Random;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.text.*;

import blocks.*;

public class Board extends JFrame {

    private static final long serialVersionUID = 2434035659171694595L;

    public static final int HEIGHT = 20;
    public static final int WIDTH = 10;
    public static final char BORDER_CHAR = 'X';

    private JTextPane pane;
    private Color[][] board;   // 고정된 블럭만 저장
    private Timer timer;
    private Block curr;
    private int x = 3, y = 0;
    private int score = 0;

    private static final int initInterval = 1000;

    public Board() {
        super("SeoulTech SE Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 보드 출력용 패널
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(Color.BLACK);
        CompoundBorder border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 10),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
        pane.setBorder(border);
        this.getContentPane().add(pane, BorderLayout.CENTER);

        // 게임 루프 타이머
        timer = new Timer(initInterval, e -> moveDown());

        // 보드 초기화
        board = new Color[HEIGHT][WIDTH];
        curr = getRandomBlock();

        // 키 입력 리스너
        pane.addKeyListener(new PlayerKeyListener());
        pane.setFocusable(true);

        drawBoard();
        timer.start();

        // 창 크기 & 표시
        setSize(300, 600);
        setVisible(true);
        pane.requestFocusInWindow();
    }

    private Block getRandomBlock() {
        Random rnd = new Random(System.currentTimeMillis());
        int block = rnd.nextInt(7);
        switch (block) {
            case 0: return new IBlock();
            case 1: return new JBlock();
            case 2: return new LBlock();
            case 3: return new ZBlock();
            case 4: return new SBlock();
            case 5: return new TBlock();
            case 6: return new OBlock();
        }
        return new LBlock();
    }

    private boolean canMove(Block block, int newX, int newY) {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int bx = newX + i;
                    int by = newY + j;
                    if (bx < 0 || bx >= WIDTH || by < 0 || by >= HEIGHT) return false;
                    if (board[by][bx] != null) return false;  //색깔 있으면 충돌
                }
            }
        }
        return true;
    }

    protected void moveDown() {
        if (canMove(curr, x, y + 1)) {
            y++;
            score++;
        } else {
            // 고정
            for (int j = 0; j < curr.height(); j++) {
                for (int i = 0; i < curr.width(); i++) {
                    if (curr.getShape(i, j) == 1) {
                        board[y + j][x + i] = curr.getColor();
                    }
                }
            }
            clearLines();
            curr = getRandomBlock();
            x = 3; y = 0;
            if (!canMove(curr, x, y)) {
                gameOver();
            }
        }
        drawBoard();
    }

    private void clearLines() {
        for (int i = 0; i < HEIGHT; i++) {
            boolean full = true;
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j] == null) {
                    full = false;
                    break;
                }
            }
            if (full) {
                for (int k = i; k > 0; k--) {
                    board[k] = board[k - 1].clone();
                }
                board[0] = new Color[WIDTH];
                score += 100;
            }
        }
    }

    private void gameOver() {
        timer.stop();
        System.out.println("GAME OVER! Score: " + score);
    }

    protected void moveRight() {
        if (canMove(curr, x + 1, y)) x++;
        drawBoard();
    }

    protected void moveLeft() {
        if (canMove(curr, x - 1, y)) x--;
        drawBoard();
    }

    protected void hardDrop() {
        while (canMove(curr, x, y + 1)) {
            y++;
            score += 2;
        }
        moveDown();
    }

    // 보드 출력 (색상 + 고정폭 적용)
    public void drawBoard() {
        StringBuilder sb = new StringBuilder();

        // 상단 테두리
        for (int t = 0; t < WIDTH + 2; t++) sb.append(BORDER_CHAR);
        sb.append("\n");

        for (int i = 0; i < HEIGHT; i++) {
            sb.append(BORDER_CHAR);
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j] !=null || isCurrBlockAt(j, i)) {
                    sb.append("O"); // 블럭
                } else {
                    sb.append(" ");
                }
            }
            sb.append(BORDER_CHAR);
            sb.append("\n");
        }

        // 하단 테두리
        for (int t = 0; t < WIDTH + 2; t++) sb.append(BORDER_CHAR);

        sb.append("\nSCORE: ").append(score);

        // 출력 적용
        pane.setText(sb.toString());
        StyledDocument doc = pane.getStyledDocument();

        // 고정폭 폰트 지정
        SimpleAttributeSet baseStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(baseStyle, "Courier New");
        StyleConstants.setFontSize(baseStyle, 18);
        StyleConstants.setForeground(baseStyle, Color.WHITE);
        doc.setParagraphAttributes(0, doc.getLength(), baseStyle, false);

        // 블럭 색칠하기
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                Color c = board[i][j];
                if (isCurrBlockAt(j, i)) c = curr.getColor();
                if (c != null) {
                    SimpleAttributeSet blockStyle = new SimpleAttributeSet();
                    StyleConstants.setFontFamily(blockStyle, "Courier New");
                    StyleConstants.setFontSize(blockStyle, 18);
                    StyleConstants.setForeground(blockStyle, c);

                    int pos = (i + 1) * (WIDTH + 3) + (j + 1); // 위치 계산
                    doc.setCharacterAttributes(pos, 1, blockStyle, true);
                }
            }
        }
    }

    private boolean isCurrBlockAt(int j, int i) {
        for (int dy = 0; dy < curr.height(); dy++) {
            for (int dx = 0; dx < curr.width(); dx++) {
                if (curr.getShape(dx, dy) == 1) {
                    if (i == y + dy && j == x + dx) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public class PlayerKeyListener implements KeyListener {
        @Override public void keyTyped(KeyEvent e) {}
        @Override
        public void keyPressed(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_DOWN: moveDown(); break;
                case KeyEvent.VK_RIGHT: moveRight(); break;
                case KeyEvent.VK_LEFT: moveLeft(); break;
                case KeyEvent.VK_UP: curr.rotate(); break;
                case KeyEvent.VK_SPACE: hardDrop(); break;
            }
            drawBoard();
        }
        @Override public void keyReleased(KeyEvent e) {}
    }
}
