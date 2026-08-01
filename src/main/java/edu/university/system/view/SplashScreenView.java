package edu.university.system.view;

import edu.university.system.config.AppConfig;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;

/**
 * Pantalla de presentación mostrada durante el inicio del sistema.
 *
 * La imagen se carga desde los recursos internos del proyecto Maven para
 * asegurar que funcione desde NetBeans, Maven y el archivo JAR ejecutable.
 */
public final class SplashScreenView extends JWindow {

    private static final int WIDTH = 724;
    private static final int HEIGHT = 543;

    private static final String SPLASH_IMAGE_PATH =
            "/images/splash_inicio.png";

    /**
     * Construye y centra la pantalla de inicio.
     *
     * @param appConfig configuración general de la aplicación
     */
    public SplashScreenView(AppConfig appConfig) {
        setSize(WIDTH, HEIGHT);
        setLocationRelativeTo(null);
        setAlwaysOnTop(true);
        setContentPane(createContent(appConfig));
    }

    /**
     * Crea el contenido principal de la pantalla de inicio.
     *
     * @param appConfig configuración general de la aplicación
     * @return componente que contiene la imagen de presentación
     */
    private JComponent createContent(AppConfig appConfig) {
        URL imageUrl = SplashScreenView.class.getResource(
                SPLASH_IMAGE_PATH
        );

        if (imageUrl == null) {
            System.err.println(
                    "No se encontró la imagen de inicio: "
                    + SPLASH_IMAGE_PATH
            );

            return createFallbackContent(appConfig);
        }

        Image splashImage = new ImageIcon(imageUrl).getImage();

        SplashImagePanel imagePanel = new SplashImagePanel(
                splashImage
        );

        imagePanel.setPreferredSize(
                new Dimension(WIDTH, HEIGHT)
        );

        return imagePanel;
    }

    /**
     * Crea una presentación sencilla en caso de que la imagen no exista.
     *
     * Esto evita que la aplicación falle por un recurso ausente.
     *
     * @param appConfig configuración general de la aplicación
     * @return componente alternativo
     */
    private JComponent createFallbackContent(
            AppConfig appConfig
    ) {
        JPanel fallbackPanel = new JPanel(
                new BorderLayout()
        );

        fallbackPanel.setBackground(
                AppTheme.BLUE_PALE
        );

        JLabel messageLabel = new JLabel(
                "<html><div style='text-align:center;'>"
                + "<h1>"
                + appConfig.getApplicationName()
                + "</h1>"
                + "<p>Sistema de Gestión Organizacional</p>"
                + "<p>POO + SQLite + Swing</p>"
                + "<p>Versión "
                + appConfig.getApplicationVersion()
                + "</p>"
                + "</div></html>",
                SwingConstants.CENTER
        );

        messageLabel.setForeground(
                AppTheme.NAVY
        );

        fallbackPanel.add(
                messageLabel,
                BorderLayout.CENTER
        );

        return fallbackPanel;
    }

    /**
     * Panel encargado de dibujar la imagen adaptada al tamaño del splash.
     */
    private static final class SplashImagePanel
            extends JPanel {

        private final Image splashImage;

        private SplashImagePanel(Image splashImage) {
            this.splashImage = splashImage;
            setOpaque(true);
            setBackground(AppTheme.WHITE);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (splashImage == null) {
                return;
            }

            int panelWidth = getWidth();
            int panelHeight = getHeight();

            int imageWidth = splashImage.getWidth(this);
            int imageHeight = splashImage.getHeight(this);

            if (panelWidth <= 0
                    || panelHeight <= 0
                    || imageWidth <= 0
                    || imageHeight <= 0) {
                return;
            }

            /*
             * Usa la escala menor para mostrar la imagen completa
             * sin recortar el logo, el título ni la barra de carga.
             */
            double scale = Math.min(
                    (double) panelWidth / imageWidth,
                    (double) panelHeight / imageHeight
            );

            int scaledWidth = (int) Math.round(
                    imageWidth * scale
            );

            int scaledHeight = (int) Math.round(
                    imageHeight * scale
            );

            int x = (panelWidth - scaledWidth) / 2;
            int y = (panelHeight - scaledHeight) / 2;

            graphics.drawImage(
                    splashImage,
                    x,
                    y,
                    scaledWidth,
                    scaledHeight,
                    this
            );
        }
    }
}
