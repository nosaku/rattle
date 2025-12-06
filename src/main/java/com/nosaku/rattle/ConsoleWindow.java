/*
 * Copyright (c) 2025 nosaku
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.nosaku.rattle;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Console window component for displaying HTTP request/response details
 */
public class ConsoleWindow {
	private Stage consoleStage;
	private TextArea consoleArea;
	
	/**
	 * Shows the console window. Creates it if it doesn't exist yet.
	 * 
	 * @param owner The owner window (usually the main application window)
	 */
	public void show(Window owner) {
		if (consoleStage == null) {
			createConsoleStage(owner);
		}

		if (!consoleStage.isShowing()) {
			consoleStage.show();
			// Position with offset from main window
			if (owner != null) {
				consoleStage.setX(owner.getX() + 50);
				consoleStage.setY(owner.getY() + 50);
			}
		} else {
			consoleStage.toFront();
		}
	}
	
	/**
	 * Checks if the console window is currently showing
	 * 
	 * @return true if the window is showing, false otherwise
	 */
	public boolean isShowing() {
		return consoleStage != null && consoleStage.isShowing();
	}
	
	/**
	 * Closes the console window
	 */
	public void close() {
		if (consoleStage != null) {
			consoleStage.close();
		}
	}
	
	/**
	 * Logs a message to the console
	 * 
	 * @param message The message to log
	 */
	public void log(String message) {
		if (consoleArea != null) {
			Platform.runLater(() -> {
				consoleArea.appendText(message + "\n");
			});
		}
	}
	
	/**
	 * Clears all console content
	 */
	public void clear() {
		if (consoleArea != null) {
			Platform.runLater(() -> {
				consoleArea.clear();
			});
		}
	}
	
	private void createConsoleStage(Window owner) {
		consoleStage = new Stage();
		consoleStage.setTitle("Console - HTTP Request/Response Details");
		consoleStage.setResizable(true);
		consoleStage.setMaximized(false);

		// Set icon for console window
		try {
			Image icon = new Image("rattlesnake.png");
			consoleStage.getIcons().add(icon);
		} catch (Exception e) {
			// Icon not found, continue without it
		}

		consoleArea = new TextArea();
		consoleArea.setEditable(false);
		consoleArea.setWrapText(false);
		consoleArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

		Button clearButton = new Button("Clear Console");
		clearButton.setOnAction(e -> consoleArea.clear());

		HBox buttonBar = new HBox(10, clearButton);
		buttonBar.setPadding(new Insets(5));
		buttonBar.setStyle("-fx-background-color: #f0f0f0;");

		VBox consoleLayout = new VBox(buttonBar, consoleArea);
		VBox.setVgrow(consoleArea, Priority.ALWAYS);

		double consoleWidth = 800;
		double consoleHeight = 600;
		if (owner != null) {
			consoleWidth = owner.getWidth() / 2;
			consoleHeight = owner.getHeight() / 2;
		}

		Scene consoleScene = new Scene(consoleLayout, consoleWidth, consoleHeight);
		consoleStage.setScene(consoleScene);

		consoleStage.setOnCloseRequest(e -> consoleStage = null);
	}
}
