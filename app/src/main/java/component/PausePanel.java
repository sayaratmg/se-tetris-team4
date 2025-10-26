package component;

import launcher.GameLauncher;
import javax.swing.*;
import java.awt.*;

/**
 * PausePanel
 * - 일시정지 상태에서 중앙에 컬러 버튼 3개 표시 (Continue / Restart / Exit)
 * - 게임 화면 위에 반투명하게 덮이지만 배경은 그대로 보임
 */
public class PausePanel extends JPanel {

    private final Runnable onResume;
    private final Runnable onRestart;
    private final Runnable onExit;

    public PausePanel(JFrame parent, Runnable onResume, Runnable onRestart, Runnable onExit) {
        this.onResume = onResume;
        this.onRestart = onRestart;
        this.onExit = onExit;

        // 전체 투명
        setOpaque(false);
        setLayout(new GridBagLayout());
        setVisible(false);
        setBounds(0, 0, parent.getWidth(), parent.getHeight());

        // === 버튼 묶음 ===
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.Y_AXIS));
        btnPanel.setOpaque(false);

        JButton continueBtn = createStitchedButton("▶ CONTINUE", new Color(80, 200, 120));
        JButton restartBtn = createStitchedButton("🔄 RESTART", new Color(80, 160, 255));
        JButton exitBtn = createStitchedButton("❌ EXIT", new Color(240, 100, 90));

        continueBtn.addActionListener(e -> onResume.run());
        restartBtn.addActionListener(e -> onRestart.run());
        exitBtn.addActionListener(e -> onExit.run());

        btnPanel.add(continueBtn);
        btnPanel.add(Box.createVerticalStrut(20));
        btnPanel.add(restartBtn);
        btnPanel.add(Box.createVerticalStrut(20));
        btnPanel.add(exitBtn);

        add(btnPanel, new GridBagConstraints());

        // 크기 변경 대응
        parent.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                setBounds(0, 0, parent.getWidth(), parent.getHeight());
            }
        });

        parent.getLayeredPane().add(this, JLayeredPane.POPUP_LAYER);
    }

    private JButton createStitchedButton(String text, Color baseColor) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth(), h = getHeight();
                int arc = 20;

                // 그림자
                g2.setColor(new Color(0, 0, 0, 60));
                g2.fillRoundRect(4, 4, w - 4, h - 4, arc, arc);

                // 본체
                g2.setColor(getModel().isPressed() ? baseColor.darker() : baseColor);
                g2.fillRoundRect(0, 0, w - 4, h - 4, arc, arc);

                // 스티치 테두리 (점선)
                g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 1f,
                        new float[]{8f, 6f}, 0f));
                g2.setColor(new Color(255, 255, 255, 180));
                g2.drawRoundRect(3, 3, w - 10, h - 10, arc - 5, arc - 5);

                // 텍스트
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (w - fm.stringWidth(text)) / 2;
                int ty = (h + fm.getAscent() - fm.getDescent()) / 2;
                g2.setColor(Color.WHITE);
                g2.drawString(text, tx, ty);

                g2.dispose();
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 18));
        btn.setFocusPainted(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(220, 60));
        btn.setMaximumSize(new Dimension(220, 60));

        // 마우스 호버 시 색상 밝게
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(baseColor.brighter());
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(baseColor);
            }
        });

        return btn;
    }

    public void showPanel() {
        setVisible(true);
        requestFocusInWindow();
    }

    public void hidePanel() {
        setVisible(false);
    }
}
