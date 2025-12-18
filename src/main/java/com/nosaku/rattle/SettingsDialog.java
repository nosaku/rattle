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

import com.nosaku.rattle.vo.ProxySettingsVo;
import com.nosaku.rattle.vo.ProxySettingsVo.ProxyMode;
import com.nosaku.rattle.vo.SettingsVo;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class SettingsDialog extends Dialog<SettingsVo> {

	public SettingsDialog(ProxySettingsVo currentProxySettings, String currentTheme) {
		super();
		setTitle("Settings");
		setHeaderText("Application Settings");
		
		// Set icon to dialog
		Image iconImage = null;
		try {
			iconImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/rattlesnake.png")));
		} catch (Exception e) {
			// Icon not found, proceed without it
		}
		if (iconImage != null) {
			Stage dialogStage = (Stage) this.getDialogPane().getScene().getWindow();
			dialogStage.getIcons().add(iconImage);
		}

		// Main container
		VBox mainContainer = new VBox(20);
		mainContainer.setPadding(new Insets(20));

		// === THEME SECTION ===
		VBox themeSection = new VBox(10);
		Label themeSectionLabel = new Label("Theme");
		themeSectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		
		Label themeLabel = new Label("Appearance:");
		ToggleGroup themeToggleGroup = new ToggleGroup();
		
		RadioButton lightRadioButton = new RadioButton("Light");
		lightRadioButton.setToggleGroup(themeToggleGroup);
		lightRadioButton.setUserData("light");
		
		RadioButton darkRadioButton = new RadioButton("Dark");
		darkRadioButton.setToggleGroup(themeToggleGroup);
		darkRadioButton.setUserData("dark");
		
		RadioButton systemRadioButton = new RadioButton("System");
		systemRadioButton.setToggleGroup(themeToggleGroup);
		systemRadioButton.setUserData("system");
		
		// Set initial theme
		switch (currentTheme != null ? currentTheme : "light") {
			case "dark":
				darkRadioButton.setSelected(true);
				break;
			case "system":
				systemRadioButton.setSelected(true);
				break;
			default:
				lightRadioButton.setSelected(true);
		}
		
		HBox themeBox = new HBox(15);
		themeBox.getChildren().addAll(lightRadioButton, darkRadioButton, systemRadioButton);
		
		themeSection.getChildren().addAll(themeSectionLabel, themeLabel, themeBox);
		
		// Separator
		Separator separator = new Separator();
		
		// === PROXY SECTION ===
		VBox proxySection = new VBox(10);
		Label proxySectionLabel = new Label("Proxy");
		proxySectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
		
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);

		// Proxy Mode
		Label modeLabel = new Label("Mode:");
		ToggleGroup modeToggleGroup = new ToggleGroup();
		
		RadioButton offRadioButton = new RadioButton("Off");
		offRadioButton.setToggleGroup(modeToggleGroup);
		offRadioButton.setUserData(ProxyMode.OFF);
		
		RadioButton onRadioButton = new RadioButton("On");
		onRadioButton.setToggleGroup(modeToggleGroup);
		onRadioButton.setUserData(ProxyMode.ON);
		
		RadioButton systemProxyRadioButton = new RadioButton("System Proxy");
		systemProxyRadioButton.setToggleGroup(modeToggleGroup);
		systemProxyRadioButton.setUserData(ProxyMode.SYSTEM_PROXY);
		
		// Set initial mode
		ProxyMode initialMode = currentProxySettings != null ? currentProxySettings.getProxyMode() : ProxyMode.OFF;
		if (initialMode == ProxyMode.ON) {
			onRadioButton.setSelected(true);
		} else if (initialMode == ProxyMode.SYSTEM_PROXY) {
			systemProxyRadioButton.setSelected(true);
		} else {
			offRadioButton.setSelected(true);
		}
		
		HBox modeBox = new HBox(15);
		modeBox.getChildren().addAll(offRadioButton, onRadioButton, systemProxyRadioButton);
		grid.add(modeLabel, 0, 0);
		grid.add(modeBox, 1, 0);

		// HTTP Proxy field
		Label httpProxyLabel = new Label("HTTP Proxy:");
		TextField httpProxyField = new TextField();
		httpProxyField.setPromptText("e.g., proxy.example.com:8080");
		if (currentProxySettings != null && currentProxySettings.getHttpProxy() != null) {
			httpProxyField.setText(currentProxySettings.getHttpProxy());
		}
		grid.add(httpProxyLabel, 0, 1);
		grid.add(httpProxyField, 1, 1);

		// HTTPS Proxy field
		Label httpsProxyLabel = new Label("HTTPS Proxy:");
		TextField httpsProxyField = new TextField();
		httpsProxyField.setPromptText("e.g., proxy.example.com:8080");
		if (currentProxySettings != null && currentProxySettings.getHttpsProxy() != null) {
			httpsProxyField.setText(currentProxySettings.getHttpsProxy());
		}
		grid.add(httpsProxyLabel, 0, 2);
		grid.add(httpsProxyField, 1, 2);

		// Username field
		Label usernameLabel = new Label("Username:");
		TextField usernameField = new TextField();
		usernameField.setPromptText("Optional");
		if (currentProxySettings != null && currentProxySettings.getUsername() != null) {
			usernameField.setText(currentProxySettings.getUsername());
		}
		grid.add(usernameLabel, 0, 3);
		grid.add(usernameField, 1, 3);

		// Password field
		Label passwordLabel = new Label("Password:");
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Optional");
		if (currentProxySettings != null && currentProxySettings.getPassword() != null) {
			passwordField.setText(currentProxySettings.getPassword());
		}
		grid.add(passwordLabel, 0, 4);
		grid.add(passwordField, 1, 4);

		// SSL/TLS Section
		Label sslLabel = new Label("SSL/TLS Settings");
		sslLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
		grid.add(sslLabel, 0, 5, 2, 1);

		// SSL Certificate Verification checkbox
		Label verifySslLabel = new Label("Verify SSL/TLS Certificates:");
		javafx.scene.control.CheckBox verifySslCheckBox = new javafx.scene.control.CheckBox();
		verifySslCheckBox.setSelected(currentProxySettings != null ? currentProxySettings.isVerifySslCertificate() : true);
		grid.add(verifySslLabel, 0, 6);
		grid.add(verifySslCheckBox, 1, 6);

		proxySection.getChildren().addAll(proxySectionLabel, grid);

		// Add all sections to main container
		mainContainer.getChildren().addAll(themeSection, separator, proxySection);

		// Add buttons
		ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		getDialogPane().getButtonTypes().addAll(saveButtonType, cancelButtonType);

		getDialogPane().setContent(mainContainer);

		// Convert result
		setResultConverter(dialogButton -> {
			if (dialogButton == saveButtonType) {
				// Theme result
				String selectedTheme = (String) themeToggleGroup.getSelectedToggle().getUserData();
				
				// Proxy result
				ProxySettingsVo result = new ProxySettingsVo();
				result.setProxyMode((ProxyMode) modeToggleGroup.getSelectedToggle().getUserData());
				result.setHttpProxy(httpProxyField.getText().trim().isEmpty() ? null : httpProxyField.getText().trim());
				result.setHttpsProxy(httpsProxyField.getText().trim().isEmpty() ? null : httpsProxyField.getText().trim());
				result.setUsername(usernameField.getText().trim().isEmpty() ? null : usernameField.getText().trim());
				result.setPassword(passwordField.getText().trim().isEmpty() ? null : passwordField.getText().trim());
				result.setVerifySslCertificate(verifySslCheckBox.isSelected());
				
				return new SettingsVo(selectedTheme, result);
			}
			return null;
		});
	}
}
