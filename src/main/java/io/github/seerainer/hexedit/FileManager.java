package io.github.seerainer.hexedit;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Handles file operations for the Hex Editor, including opening, saving, and
 * tracking the current file and its data.
 */
class FileManager {
    private String currentFile;
    private byte[] fileData;

    /**
     * Gets the path of the currently opened file as a string.
     *
     * @return the current file path, or null if no file is open
     */
    String getCurrentFile() {
	return currentFile;
    }

    /**
     * Gets the Path object for the currently opened file.
     *
     * @return the Path of the current file
     */
    private Path getCurrentFilePath() {
	return Path.of(currentFile);
    }

    /**
     * Opens a file and reads its contents into memory.
     *
     * @param fileName the path to the file to open
     * @return the file data as a byte array
     * @throws IOException if the file cannot be read
     */
    byte[] openFile(final String fileName) throws IOException {
	currentFile = fileName;
	try (var ch = FileChannel.open(getCurrentFilePath(), StandardOpenOption.READ)) {
	    final var size = ch.size();
	    if (size > Integer.MAX_VALUE) {
		throw new IOException(new StringBuilder().append("File too large to fit into a single byte[]: ")
			.append(size).append(" bytes").toString());
	    }
	    final var mbb = ch.map(FileChannel.MapMode.READ_ONLY, 0, size);
	    fileData = new byte[(int) size];
	    mbb.get(fileData);
	    return fileData;
	}
    }

    /**
     * Saves the given data to the currently opened file.
     *
     * @param data the data to save
     * @throws IOException if the file cannot be written
     */
    void saveFile(final byte[] data) throws IOException {
	if (currentFile != null) {
	    Files.write(getCurrentFilePath(), data);
	}
    }

    /**
     * Saves the given data to a new file and sets it as the current file.
     *
     * @param fileName the path to save the file as
     * @param data     the data to save
     * @throws IOException if the file cannot be written
     */
    void saveFileAs(final String fileName, final byte[] data) throws IOException {
	currentFile = fileName;
	Files.write(getCurrentFilePath(), data);
    }

    /**
     * Sets the current file path.
     *
     * @param file the file path to set as current
     */
    void setCurrentFile(final String file) {
	this.currentFile = file;
    }
}