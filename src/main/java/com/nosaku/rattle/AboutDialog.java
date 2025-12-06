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

import java.util.Objects;

import com.nosaku.rattle.util.CommonConstants;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * About dialog for displaying application information
 */
public class AboutDialog {
	
	/**
	 * Shows the About dialog
	 * 
	 * @param owner The owner window for the dialog
	 */
	public static void show(Window owner) {
		Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
		aboutAlert.setTitle("About Rattle");
		aboutAlert.setHeaderText("About Rattle");
		
		if (owner != null) {
			aboutAlert.initOwner(owner);
		}

		Image iconImage = loadIcon();
		if (iconImage != null) {
			Stage dialogStage = (Stage) aboutAlert.getDialogPane().getScene().getWindow();
			dialogStage.getIcons().add(iconImage);
		}

		HBox content = createContent(iconImage);
		aboutAlert.getDialogPane().setContent(content);
		aboutAlert.showAndWait();
	}
	
	private static Image loadIcon() {
		try {
			return new Image(Objects.requireNonNull(
					AboutDialog.class.getResourceAsStream("/rattlesnake.png")));
		} catch (Exception e) {
			return null;
		}
	}
	
	private static HBox createContent(Image iconImage) {
		HBox content = new HBox(20);
		content.setAlignment(Pos.CENTER_LEFT);
		content.setPadding(new Insets(20));

		ImageView imageView = new ImageView();
		if (iconImage != null) {
			imageView.setImage(iconImage);
			imageView.setFitWidth(120);
			imageView.setPreserveRatio(true);
		}

		VBox rightBox = new VBox(10);
		rightBox.setAlignment(Pos.CENTER_LEFT);

		Label appNameLabel = new Label("Rattle API Client");
		appNameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

		Label descriptionLabel = new Label(
				"Rattle is an API client for testing and managing RESTful APIs.\n\n"
				+ "Features:\n"
				+ "- Tabbed requests\n"
				+ "- JSON highlighting\n"
				+ "- Request history\n"
				+ "- Easy parameter and header editing\n\n"
				+ "Enjoy productivity and simplicity for your API workflow!");
		descriptionLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #333;");
		descriptionLabel.setWrapText(true);

		Label copyrightLabel = new Label(CommonConstants.COPYRIGHT_LABEL_TEXT);
		copyrightLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #555;");

		rightBox.getChildren().addAll(appNameLabel, descriptionLabel, copyrightLabel);
		content.getChildren().addAll(imageView, rightBox);
		
		return content;
	}
}
