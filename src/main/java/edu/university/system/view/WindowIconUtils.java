package edu.university.system.view;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import java.awt.Image;
import java.net.URL;

/**
 * Carga y aplica el icono institucional de la aplicacion a ventanas Swing.
 * Mantiene el recurso centralizado para que funcione igual desde Maven,
 * NetBeans y el JAR ejecutable.
 */
final class WindowIconUtils {

    private static final String ICON_RESOURCE = "/images/icono_uth.png";
    private static ImageIcon sourceIcon;
    private static ImageIcon frameIcon;

    private WindowIconUtils() {
    }

    static void applyTo(JFrame frame) {
        Image image = getIconImage();
        if (image != null) {
            frame.setIconImage(image);
        }
    }

    static void applyTo(JDialog dialog) {
        Image image = getIconImage();
        if (image != null) {
            dialog.setIconImage(image);
        }
    }

    static void applyTo(JInternalFrame internalFrame) {
        ImageIcon imageIcon = getIcon();
        if (imageIcon != null) {
            internalFrame.setFrameIcon(imageIcon);
        }
    }

    private static Image getIconImage() {
        ImageIcon imageIcon = getSourceIcon();
        return imageIcon == null ? null : imageIcon.getImage();
    }

    private static ImageIcon getIcon() {
        if (frameIcon != null) {
            return frameIcon;
        }
        ImageIcon imageIcon = getSourceIcon();
        if (imageIcon == null) {
            return null;
        }
        Image scaledImage = imageIcon.getImage().getScaledInstance(16, 16, Image.SCALE_SMOOTH);
        frameIcon = new ImageIcon(scaledImage);
        return frameIcon;
    }

    private static ImageIcon getSourceIcon() {
        if (sourceIcon != null) {
            return sourceIcon;
        }
        URL iconUrl = WindowIconUtils.class.getResource(ICON_RESOURCE);
        if (iconUrl == null) {
            System.err.println("No se encontro el icono de la aplicacion: " + ICON_RESOURCE);
            return null;
        }
        sourceIcon = new ImageIcon(iconUrl);
        return sourceIcon;
    }
}
