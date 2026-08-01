package edu.university.system.view;

import edu.university.system.config.AppConfig;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JWindow;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

public final class SplashScreenView extends JWindow {

    private static final int WIDTH = 460;
    private static final int HEIGHT = 260;

    public SplashScreenView(AppConfig appConfig) {
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setContentPane(createContent(appConfig));
    }

    private JComponent createContent(AppConfig appConfig) {
        JPanel rootPanel = new JPanel(new BorderLayout(12, 12));
        rootPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(38, 85, 115)),
                BorderFactory.createEmptyBorder(28, 34, 28, 34)
        ));
        rootPanel.setBackground(new Color(247, 249, 250));

        JLabel titleLabel = new JLabel(appConfig.getApplicationName(), SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(new Color(23, 45, 58));

        JLabel subtitleLabel = new JLabel(appConfig.getCompanyName(), SwingConstants.CENTER);
        subtitleLabel.setFont(subtitleLabel.getFont().deriveFont(Font.PLAIN, 15f));
        subtitleLabel.setForeground(new Color(77, 92, 103));

        JPanel centerPanel = new JPanel(new BorderLayout(8, 8));
        centerPanel.setOpaque(false);
        centerPanel.add(titleLabel, BorderLayout.CENTER);
        centerPanel.add(subtitleLabel, BorderLayout.SOUTH);

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(WIDTH - 90, 12));

        JLabel versionLabel = new JLabel("Version " + appConfig.getApplicationVersion(), SwingConstants.RIGHT);
        versionLabel.setForeground(new Color(77, 92, 103));

        JPanel bottomPanel = new JPanel(new BorderLayout(8, 8));
        bottomPanel.setOpaque(false);
        bottomPanel.add(progressBar, BorderLayout.CENTER);
        bottomPanel.add(versionLabel, BorderLayout.SOUTH);

        rootPanel.add(centerPanel, BorderLayout.CENTER);
        rootPanel.add(bottomPanel, BorderLayout.SOUTH);
        return rootPanel;
    }
}
