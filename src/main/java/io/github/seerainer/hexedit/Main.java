package io.github.seerainer.hexedit;

import org.eclipse.swt.widgets.Display;

public class Main {
    public static void main(final String[] args) {
	final var display = new Display();
	final var hexedit = new HexEditor(display);
	final var shell = hexedit.getShell();
	while (!shell.isDisposed()) {
	    if (!display.readAndDispatch()) {
		display.sleep();
	    }
	}
	display.dispose();
    }
}