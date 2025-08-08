package io.github.seerainer.hexedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Manages the color theme (dark or light mode) for the Hex Editor UI. Handles
 * detection of system theme and provides color resources for dark mode. Also
 * manages disposal of allocated color resources.
 */
class ThemeManager {
    private boolean isDarkMode;
    private Color darkBackground;
    private Color darkForeground;
    private Color darkOffsetBackground;
    private Color darkHexBackground;
    private Color darkAsciiBackground;
    private Color darkBorder;

    /**
     * Detects the system theme and sets up color resources for dark mode if
     * enabled. Applies additional Windows-specific settings for dark mode
     * integration.
     *
     * @param display the SWT Display instance
     */
    void detectAndSetupTheme(final Display display) {
	isDarkMode = Display.isSystemDarkTheme();
	if (!isDarkMode) {
	    return;
	}
	// Initialize dark mode color resources
	darkBackground = new Color(display, 32, 32, 32);
	darkForeground = new Color(display, 220, 220, 220);
	darkOffsetBackground = new Color(display, 45, 45, 45);
	darkHexBackground = new Color(display, 40, 40, 40);
	darkAsciiBackground = new Color(display, 38, 38, 38);
	darkBorder = new Color(display, 60, 60, 60);
	if (!"win32".equals(SWT.getPlatform())) {
	    return;
	}
	// Windows-specific dark mode integration
	display.setData("org.eclipse.swt.internal.win32.useDarkModeExplorerTheme", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.useShellTitleColoring", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.menuBarForegroundColor", darkForeground);
	display.setData("org.eclipse.swt.internal.win32.menuBarBackgroundColor", darkBackground);
	display.setData("org.eclipse.swt.internal.win32.all.use_WS_BORDER", Boolean.TRUE);
	display.setData("org.eclipse.swt.internal.win32.Text.useDarkThemeIcons", Boolean.TRUE);
    }

    /**
     * Disposes all allocated color resources for dark mode. Should be called when
     * the application is closing.
     */
    void dispose() {
	if (darkBackground != null && !darkBackground.isDisposed()) {
	    darkBackground.dispose();
	}
	if (darkForeground != null && !darkForeground.isDisposed()) {
	    darkForeground.dispose();
	}
	if (darkOffsetBackground != null && !darkOffsetBackground.isDisposed()) {
	    darkOffsetBackground.dispose();
	}
	if (darkHexBackground != null && !darkHexBackground.isDisposed()) {
	    darkHexBackground.dispose();
	}
	if (darkAsciiBackground != null && !darkAsciiBackground.isDisposed()) {
	    darkAsciiBackground.dispose();
	}
	if (darkBorder != null && !darkBorder.isDisposed()) {
	    darkBorder.dispose();
	}
    }

    /**
     * @return the background color for the ASCII area in dark mode
     */
    Color getDarkAsciiBackground() {
	return darkAsciiBackground;
    }

    /**
     * @return the main background color for dark mode
     */
    Color getDarkBackground() {
	return darkBackground;
    }

    /**
     * @return the foreground (text) color for dark mode
     */
    Color getDarkForeground() {
	return darkForeground;
    }

    /**
     * @return the background color for the hex area in dark mode
     */
    Color getDarkHexBackground() {
	return darkHexBackground;
    }

    /**
     * @return the background color for the offset area in dark mode
     */
    Color getDarkOffsetBackground() {
	return darkOffsetBackground;
    }

    /**
     * @return true if dark mode is enabled, false otherwise
     */
    boolean isDarkMode() {
	return isDarkMode;
    }
}