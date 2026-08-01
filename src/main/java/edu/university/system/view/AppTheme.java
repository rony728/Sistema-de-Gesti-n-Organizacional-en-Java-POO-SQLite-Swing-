package edu.university.system.view;

import javax.swing.UIManager;
import javax.swing.plaf.ColorUIResource;
import java.awt.Color;

/**
 * Paleta visual institucional inspirada en las imagenes de inicio del sistema.
 * Centraliza colores para que las pantallas Swing mantengan una identidad
 * consistente sin duplicar constantes en cada formulario.
 */
public final class AppTheme {

    public static final Color NAVY = new Color(5, 30, 82);
    public static final Color BLUE = new Color(0, 77, 177);
    public static final Color BLUE_DARK = new Color(0, 53, 128);
    public static final Color BLUE_LIGHT = new Color(232, 242, 252);
    public static final Color BLUE_PALE = new Color(247, 251, 255);
    public static final Color ORANGE = new Color(247, 134, 0);
    public static final Color GREEN = new Color(0, 73, 35);
    public static final Color TEXT = new Color(20, 38, 70);
    public static final Color BORDER = new Color(188, 207, 230);
    public static final Color WHITE = Color.WHITE;

    private AppTheme() {
    }

    /**
     * Configura valores globales de Swing/FlatLaf antes de crear ventanas.
     */
    public static void install() {
        UIManager.put("Component.arc", 6);
        UIManager.put("Button.arc", 6);
        UIManager.put("TextComponent.arc", 6);
        UIManager.put("Component.focusColor", resource(ORANGE));
        UIManager.put("Component.borderColor", resource(BORDER));
        UIManager.put("Component.focusedBorderColor", resource(ORANGE));
        UIManager.put("Component.innerFocusWidth", 1);

        UIManager.put("Panel.background", resource(BLUE_PALE));
        UIManager.put("Label.foreground", resource(TEXT));
        UIManager.put("TitledBorder.titleColor", resource(NAVY));

        UIManager.put("Button.background", resource(BLUE));
        UIManager.put("Button.foreground", resource(WHITE));
        UIManager.put("Button.hoverBackground", resource(BLUE_DARK));
        UIManager.put("Button.pressedBackground", resource(NAVY));
        UIManager.put("Button.disabledBackground", resource(new Color(207, 218, 232)));
        UIManager.put("Button.disabledText", resource(new Color(112, 126, 148)));

        UIManager.put("TextField.background", resource(WHITE));
        UIManager.put("PasswordField.background", resource(WHITE));
        UIManager.put("ComboBox.background", resource(WHITE));
        UIManager.put("ComboBox.buttonBackground", resource(WHITE));
        UIManager.put("ComboBox.selectionBackground", resource(BLUE));
        UIManager.put("ComboBox.selectionForeground", resource(WHITE));

        UIManager.put("Table.background", resource(WHITE));
        UIManager.put("Table.foreground", resource(TEXT));
        UIManager.put("Table.selectionBackground", resource(BLUE));
        UIManager.put("Table.selectionForeground", resource(WHITE));
        UIManager.put("Table.gridColor", resource(new Color(219, 230, 242)));
        UIManager.put("TableHeader.background", resource(NAVY));
        UIManager.put("TableHeader.foreground", resource(WHITE));

        UIManager.put("MenuBar.background", resource(NAVY));
        UIManager.put("Menu.background", resource(NAVY));
        UIManager.put("Menu.foreground", resource(WHITE));
        UIManager.put("Menu.selectionBackground", resource(BLUE));
        UIManager.put("Menu.selectionForeground", resource(WHITE));
        UIManager.put("MenuItem.selectionBackground", resource(BLUE_LIGHT));
        UIManager.put("MenuItem.selectionForeground", resource(NAVY));

        UIManager.put("InternalFrame.activeTitleBackground", resource(NAVY));
        UIManager.put("InternalFrame.activeTitleForeground", resource(WHITE));
        UIManager.put("InternalFrame.inactiveTitleBackground", resource(new Color(214, 226, 241)));
        UIManager.put("InternalFrame.inactiveTitleForeground", resource(TEXT));

        UIManager.put("ProgressBar.foreground", resource(ORANGE));
        UIManager.put("ProgressBar.background", resource(WHITE));
    }

    private static ColorUIResource resource(Color color) {
        return new ColorUIResource(color);
    }
}
