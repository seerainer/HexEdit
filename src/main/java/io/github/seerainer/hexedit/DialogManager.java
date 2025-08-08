package io.github.seerainer.hexedit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Manages dialog windows for find and go-to-offset operations in the Hex Editor
 * UI. Handles creation and event logic for modal dialogs.
 */
class DialogManager {
    private final HexEditor hexEditor;

    /**
     * Constructs a DialogManager for the given HexEditor instance.
     *
     * @param hexEditor the HexEditor instance
     */
    DialogManager(final HexEditor hexEditor) {
	this.hexEditor = hexEditor;
    }

    /**
     * Opens the Find dialog, allowing the user to search for a hex string in the
     * file.
     */
    void openFindDialog() {
	final var parentShell = hexEditor.getShell();
	final var findShell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	findShell.setText("Find");
	findShell.setSize(300, 120);
	findShell.setLayout(new GridLayout(2, false));
	hexEditor.applyDarkModeToControl(findShell);

	final var label = new Label(findShell, SWT.NONE);
	label.setText("Find (hex):");
	label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
	hexEditor.applyDarkModeToControl(label);

	final var findText = new Text(findShell, SWT.BORDER);
	findText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	hexEditor.applyDarkModeToControl(findText);

	final var buttonComposite = new Composite(findShell, SWT.NONE);
	buttonComposite.setLayout(new GridLayout(2, true));
	buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	hexEditor.applyDarkModeToControl(buttonComposite);

	final var findButton = new Button(buttonComposite, SWT.PUSH);
	findButton.setText("Find");
	findButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	hexEditor.applyDarkModeToControl(findButton);

	final var cancelButton = new Button(buttonComposite, SWT.PUSH);
	cancelButton.setText("Cancel");
	cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	hexEditor.applyDarkModeToControl(cancelButton);

	findButton.addListener(SWT.Selection, _ -> {
	    final var searchText = findText.getText().trim();
	    if (!searchText.isEmpty()) {
		hexEditor.findInHex(searchText);
	    }
	    findShell.close();
	});

	cancelButton.addListener(SWT.Selection, _ -> findShell.close());

	findShell.open();
    }

    /**
     * Opens the Go To Offset dialog, allowing the user to jump to a specific offset
     * in the file.
     */
    void openGotoDialog() {
	final var parentShell = hexEditor.getShell();
	final var gotoShell = new Shell(parentShell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
	gotoShell.setText("Go to Offset");
	gotoShell.setSize(250, 120);
	gotoShell.setLayout(new GridLayout(2, false));
	hexEditor.applyDarkModeToControl(gotoShell);

	final var label = new Label(gotoShell, SWT.NONE);
	label.setText("Offset (hex):");
	hexEditor.applyDarkModeToControl(label);

	final var offsetTxt = new Text(gotoShell, SWT.BORDER);
	offsetTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	hexEditor.applyDarkModeToControl(offsetTxt);

	final var buttonComposite = new Composite(gotoShell, SWT.NONE);
	buttonComposite.setLayout(new GridLayout(2, true));
	buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
	hexEditor.applyDarkModeToControl(buttonComposite);

	final var gotoButton = new Button(buttonComposite, SWT.PUSH);
	gotoButton.setText("Go");
	gotoButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	hexEditor.applyDarkModeToControl(gotoButton);

	final var cancelButton = new Button(buttonComposite, SWT.PUSH);
	cancelButton.setText("Cancel");
	cancelButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	hexEditor.applyDarkModeToControl(cancelButton);

	gotoButton.addListener(SWT.Selection, _ -> {
	    try {
		final var offsetStr = offsetTxt.getText().trim();
		final var offset = Integer.parseInt(offsetStr, 16);
		hexEditor.gotoOffset(offset);
		gotoShell.close();
	    } catch (final NumberFormatException ex) {
		hexEditor.messageBox("Invalid Offset", "Please enter a valid hexadecimal offset.", SWT.ICON_ERROR);
	    }
	});

	cancelButton.addListener(SWT.Selection, _ -> gotoShell.close());

	gotoShell.open();
    }
}