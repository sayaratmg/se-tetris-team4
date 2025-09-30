package component;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import javax.swing.Timer;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Queue;
import javax.swing.*;

import javax.swing.border.CompoundBorder;
import javax.swing.text.*;

public class Board extends JFrame {

	private static final long serialVersionUID = 2434035659171694595L;
	
	public static final int HEIGHT = 20;
	public static final int WIDTH = 10;
	public static final char BORDER_CHAR = 'X';
	
	private JTextPane pane;
	private JLabel scoreLabel;
	private JLabel statusLabel;
	private JPanel rootPanel;           
	private int score = 0; // UI tracking only

	private int[][] board;
	private KeyListener playerKeyListener;
	private SimpleAttributeSet styleSet;
	private Timer timer;
	private Block curr;
	int x = 3; //Default Position.
	int y = 0;
	
	private static final int initInterval = 1000;
	
	public Board() {
		super("SeoulTech SE Tetris");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//Board display setting.
		pane = new JTextPane();
		pane.setEditable(false);
		pane.setFocusable(false); 
		pane.setBackground(Color.BLACK);
		CompoundBorder border = BorderFactory.createCompoundBorder(
				BorderFactory.createLineBorder(Color.GRAY, 10),
				BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
		pane.setBorder(border);

		rootPanel = new JPanel(new BorderLayout());
		rootPanel.add(pane, BorderLayout.CENTER);

	
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

	
		styleSet = new SimpleAttributeSet();
		StyleConstants.setFontSize(styleSet, 18);
		StyleConstants.setFontFamily(styleSet, "Courier");
		StyleConstants.setBold(styleSet, true);
		StyleConstants.setForeground(styleSet, Color.WHITE);
		StyleConstants.setAlignment(styleSet, StyleConstants.ALIGN_CENTER);
		

		timer = new Timer(initInterval, new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				moveDown();
				drawBoard();
			}
		});
		
		board = new int[HEIGHT][WIDTH];
		playerKeyListener = new PlayerKeyListener();

	
		rootPanel.addKeyListener(playerKeyListener);
		rootPanel.setFocusable(true);
		rootPanel.requestFocusInWindow();

		
		curr = getRandomBlock();
		placeBlock();
		drawBoard();
		timer.start();
	}

	public JPanel getEmbeddedPanel() {
		return rootPanel;
	}

	private Block getRandomBlock() {
		Random rnd = new Random(System.currentTimeMillis());
		int block = rnd.nextInt(7); 
		switch(block) {
		case 0:
			return new IBlock();
		case 1:
			return new JBlock();
		case 2:
			return new LBlock();
		case 3:
			return new ZBlock();
		case 4:
			return new SBlock();
		case 5:
			return new TBlock();
		case 6:
			return new OBlock();			
		}
		return new LBlock();
	}
	
	private void placeBlock() {
		StyledDocument doc = pane.getStyledDocument();
		SimpleAttributeSet styles = new SimpleAttributeSet();
		StyleConstants.setForeground(styles, curr.getColor());
		for(int j=0; j<curr.height(); j++) {
			int rows = y+j == 0 ? 0 : y+j-1;
			int offset = rows * (WIDTH+3) + x + 1;
			doc.setCharacterAttributes(offset, curr.width(), styles, true);
			for(int i=0; i<curr.width(); i++) {
				board[y+j][x+i] = curr.getShape(i, j);
			}
		}
	}
	
	private void eraseCurr() {
		for(int i=x; i<x+curr.width(); i++) {
			for(int j=y; j<y+curr.height(); j++) {
				board[j][i] = 0;
			}
		}
	}

	protected void moveDown() {
		eraseCurr();
		if(y < HEIGHT - curr.height()) y++;
		else {
			placeBlock();
			curr = getRandomBlock();
			x = 3;
			y = 0;
		}
		placeBlock();
	}
	
	protected void moveRight() {
		eraseCurr();
		if(x < WIDTH - curr.width()) x++;
		placeBlock();
	}

	protected void moveLeft() {
		eraseCurr();
		if(x > 0) {
			x--;
		}
		placeBlock();
	}

	public void drawBoard() {
		StringBuffer sb = new StringBuffer();
		for(int t=0; t<WIDTH+2; t++) sb.append(BORDER_CHAR);
		sb.append("\n");
		for(int i=0; i < board.length; i++) {
			sb.append(BORDER_CHAR);
			for(int j=0; j < board[i].length; j++) {
				if(board[i][j] == 1) {
					sb.append("O");
				} else {
					sb.append(" ");
				}
			}
			sb.append(BORDER_CHAR);
			sb.append("\n");
		}
		for(int t=0; t<WIDTH+2; t++) sb.append(BORDER_CHAR);
		pane.setText(sb.toString());
		StyledDocument doc = pane.getStyledDocument();
		doc.setParagraphAttributes(0, doc.getLength(), styleSet, false);
		pane.setStyledDocument(doc);
	}
	
	public void reset() {
		this.board = new int[20][10];
	}

	public class PlayerKeyListener implements KeyListener {
		@Override
		public void keyTyped(KeyEvent e) { }

		@Override
		public void keyPressed(KeyEvent e) {
			switch(e.getKeyCode()) {
			case KeyEvent.VK_DOWN:
				moveDown();
				drawBoard();
				break;
			case KeyEvent.VK_RIGHT:
				moveRight();
				drawBoard();
				break;
			case KeyEvent.VK_LEFT:
				moveLeft();
				drawBoard();
				break;
			case KeyEvent.VK_UP:
				eraseCurr();
				curr.rotate();
				drawBoard();
				break;
			}
		}
		@Override
		public void keyReleased(KeyEvent e) { }
	}

	public void setScore(int newScore) {
		this.score = newScore;
		if (scoreLabel != null) scoreLabel.setText("Score: " + newScore);
	}

	public void setStatus(String text) {
		if (statusLabel != null) statusLabel.setText(text);
	}

import blocks.*;

public class Board extends JFrame {

    private static final long serialVersionUID = 1L;

    public static final int HEIGHT = 20;
    public static final int WIDTH = 10;
    public static final char BORDER_CHAR = 'X';

    private JTextPane pane;
    private Color[][] board;
    private javax.swing.Timer timer;

    private Block curr;
    private int x = 3, y = 0;
    private int score = 0;

    // 다음 블럭 큐
    private Queue<Block> nextBlocks = new LinkedList<>();
    private static final int NEXT_SIZE = 3;

    // 난이도 관련
    private int clearedLines = 0;
    private int speedLevel = 1;

    // 일시정지 상태
    private boolean isPaused = false;

    private static final int initInterval = 1000;

    public Board() {
        super("SeoulTech SE Tetris");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 보드 출력 패널
        pane = new JTextPane();
        pane.setEditable(false);
        pane.setBackground(Color.BLACK);
        CompoundBorder border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.GRAY, 10),
                BorderFactory.createLineBorder(Color.DARK_GRAY, 5));
        pane.setBorder(border);
        this.getContentPane().add(pane, BorderLayout.CENTER);

        // 게임 루프 타이머
        timer = new javax.swing.Timer(initInterval, e -> {
            if (!isPaused)
                moveDown();
        });

        // 보드 초기화
        board = new Color[HEIGHT][WIDTH];

        // 큐 초기화
        for (int i = 0; i < NEXT_SIZE; i++) {
            nextBlocks.add(getRandomBlock());
        }
        curr = nextBlocks.poll();

        // 키 바인딩 등록
        setupKeyBindings();

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
            case 0:
                return new IBlock();
            case 1:
                return new JBlock();
            case 2:
                return new LBlock();
            case 3:
                return new ZBlock();
            case 4:
                return new SBlock();
            case 5:
                return new TBlock();
            case 6:
                return new OBlock();
        }
        return new LBlock();
    }

    private boolean canMove(Block block, int newX, int newY) {
        for (int j = 0; j < block.height(); j++) {
            for (int i = 0; i < block.width(); i++) {
                if (block.getShape(i, j) == 1) {
                    int bx = newX + i;
                    int by = newY + j;
                    if (bx < 0 || bx >= WIDTH || by < 0 || by >= HEIGHT)
                        return false;
                    if (board[by][bx] != null)
                        return false;
                }
            }
        }
        return true;
    }



    protected void rotateBlock() {
        
        Block backup = curr.clone();
        int oldX = x, oldY = y;

        // 회전 시도
        curr.rotate();

        // 현재 위치에서 불가능하면 벽킥 시도
        if (!canMove(curr, x, y)) {
            // 왼쪽 한 칸 이동
            if (canMove(curr, x - 1, y)) {
                x -= 1;
            }
            // 오른쪽 한 칸 이동
            else if (canMove(curr, x + 1, y)) {
                x += 1;
            }
            // 그래도 안 되면 롤백
            else {
                curr = backup;
                x = oldX;
                y = oldY;
            }
        }

        drawBoard();
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

            // 다음 블럭 큐에서 가져오기
            curr = nextBlocks.poll();
            nextBlocks.add(getRandomBlock());
            x = 3;
            y = 0;

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
                clearedLines++;

                // 난이도 상승 체크
                if (clearedLines % 10 == 0) {
                    increaseSpeed();
                }
            }
        }
    }

    private void increaseSpeed() {
        int newDelay = Math.max(200, timer.getDelay() - 100);
        timer.setDelay(newDelay);
        speedLevel++;
        System.out.println("난이도 상승! 레벨: " + speedLevel + ", 딜레이: " + newDelay + "ms");
    }

    private void gameOver() {
        timer.stop();
        System.out.println("GAME OVER! Score: " + score);
    }

    protected void moveRight() {
        if (canMove(curr, x + 1, y))
            x++;
        drawBoard();
    }

    protected void moveLeft() {
        if (canMove(curr, x - 1, y))
            x--;
        drawBoard();
    }

    protected void hardDrop() {
        while (canMove(curr, x, y + 1)) {
            y++;
            score += 2;
        }
        moveDown();
    }

    // 일시정지 토글
    private void togglePause() {
        isPaused = !isPaused;
        System.out.println(isPaused ? "게임 일시정지" : "게임 재개");
    }

    // 게임 종료 처리
    private void exitGame() {
        timer.stop();
        System.out.println("게임 종료. 최종 점수: " + score);
        System.exit(0);
    }

    // 키 바인딩 설정 (즉시 반응 & 반복 입력 지원)
    private void setupKeyBindings() {
        InputMap im = pane.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap am = pane.getActionMap();

        pane.setFocusTraversalKeysEnabled(false);

        im.put(KeyStroke.getKeyStroke("RIGHT"), "moveRight");
        im.put(KeyStroke.getKeyStroke("LEFT"), "moveLeft");
        im.put(KeyStroke.getKeyStroke("DOWN"), "moveDown");
        im.put(KeyStroke.getKeyStroke("UP"), "rotate");
        im.put(KeyStroke.getKeyStroke("SPACE"), "hardDrop");
        im.put(KeyStroke.getKeyStroke("P"), "pause");
        im.put(KeyStroke.getKeyStroke("ESCAPE"), "exit");

        am.put("moveRight", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                moveRight();
                drawBoard();
            }
        });
        am.put("moveLeft", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                moveLeft();
                drawBoard();
            }
        });
        am.put("moveDown", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                moveDown();
                drawBoard();
            }
        });
        am.put("rotate", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                rotateBlock();
            }
        });
        am.put("hardDrop", new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                hardDrop();
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

    public void drawBoard() {
        StringBuilder sb = new StringBuilder();

        // 상단 테두리
        for (int t = 0; t < WIDTH + 2; t++)
            sb.append(BORDER_CHAR);
        sb.append("\n");

        for (int i = 0; i < HEIGHT; i++) {
            sb.append(BORDER_CHAR);
            for (int j = 0; j < WIDTH; j++) {
                if (board[i][j] != null || isCurrBlockAt(j, i)) {
                    sb.append("O");
                } else {
                    sb.append(" ");
                }
            }
            sb.append(BORDER_CHAR);
            sb.append("\n");
        }

        for (int t = 0; t < WIDTH + 2; t++)
            sb.append(BORDER_CHAR);
        sb.append("\nSCORE: ").append(score);
        sb.append("\nLEVEL: ").append(speedLevel);
        sb.append("\nNEXT: ").append(nextBlocks.peek().getClass().getSimpleName());
        sb.append("\n").append(isPaused ? "[일시정지]" : "");

        pane.setText(sb.toString());
        StyledDocument doc = pane.getStyledDocument();

        // 기본 폰트 스타일
        SimpleAttributeSet baseStyle = new SimpleAttributeSet();
        StyleConstants.setFontFamily(baseStyle, "Courier New");
        StyleConstants.setFontSize(baseStyle, 18);
        StyleConstants.setForeground(baseStyle, Color.WHITE);
        doc.setParagraphAttributes(0, doc.getLength(), baseStyle, false);

        // 블럭 색칠
        for (int i = 0; i < HEIGHT; i++) {
            for (int j = 0; j < WIDTH; j++) {
                Color c = board[i][j];
                if (isCurrBlockAt(j, i))
                    c = curr.getColor();
                if (c != null) {
                    SimpleAttributeSet blockStyle = new SimpleAttributeSet();
                    StyleConstants.setFontFamily(blockStyle, "Courier New");
                    StyleConstants.setFontSize(blockStyle, 18);
                    StyleConstants.setForeground(blockStyle, c);
                    int pos = (i + 1) * (WIDTH + 3) + (j + 1);
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

}
