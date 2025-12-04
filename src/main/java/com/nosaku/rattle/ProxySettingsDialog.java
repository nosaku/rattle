package com.nosaku.rattle;

import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.image.Image;
import java.util.Objects;

import com.nosaku.rattle.vo.ProxySettingsVo;
import com.nosaku.rattle.vo.ProxySettingsVo.ProxyMode;

public class ProxySettingsDialog extends Dialog<ProxySettingsVo> {

	public ProxySettingsDialog(ProxySettingsVo currentSettings) {
		super();
		setTitle("Proxy Settings");
		setHeaderText("Configure HTTP and HTTPS Proxy Settings");
		
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

		// Create GridPane for form layout
		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setPadding(new Insets(20));

		// Proxy Mode Section
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
		ProxyMode initialMode = currentSettings != null ? currentSettings.getProxyMode() : ProxyMode.OFF;
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
		if (currentSettings != null && currentSettings.getHttpProxy() != null) {
			httpProxyField.setText(currentSettings.getHttpProxy());
		}
		grid.add(httpProxyLabel, 0, 1);
		grid.add(httpProxyField, 1, 1);

		// HTTPS Proxy field
		Label httpsProxyLabel = new Label("HTTPS Proxy:");
		TextField httpsProxyField = new TextField();
		httpsProxyField.setPromptText("e.g., proxy.example.com:8080");
		if (currentSettings != null && currentSettings.getHttpsProxy() != null) {
			httpsProxyField.setText(currentSettings.getHttpsProxy());
		}
		grid.add(httpsProxyLabel, 0, 2);
		grid.add(httpsProxyField, 1, 2);

		// Username field
		Label usernameLabel = new Label("Username:");
		TextField usernameField = new TextField();
		usernameField.setPromptText("Optional");
		if (currentSettings != null && currentSettings.getUsername() != null) {
			usernameField.setText(currentSettings.getUsername());
		}
		grid.add(usernameLabel, 0, 3);
		grid.add(usernameField, 1, 3);

		// Password field
		Label passwordLabel = new Label("Password:");
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Optional");
		if (currentSettings != null && currentSettings.getPassword() != null) {
			passwordField.setText(currentSettings.getPassword());
		}
		grid.add(passwordLabel, 0, 4);
		grid.add(passwordField, 1, 4);

		// SSL/TLS Section
		Label sslLabel = new Label("SSL/TLS Settings");
		sslLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");
		grid.add(sslLabel, 0, 5);

		// SSL Certificate Verification checkbox
		Label verifySslLabel = new Label("Verify SSL/TLS Certificates:");
		javafx.scene.control.CheckBox verifySslCheckBox = new javafx.scene.control.CheckBox();
		verifySslCheckBox.setSelected(currentSettings != null ? currentSettings.isVerifySslCertificate() : true);
		grid.add(verifySslLabel, 0, 6);
		grid.add(verifySslCheckBox, 1, 6);

		// Set the content
		this.getDialogPane().setContent(grid);

		// Add OK and Cancel buttons
		ButtonType okButtonType = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
		ButtonType cancelButtonType = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
		this.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);

		// Convert result to ProxySettingsVo when OK is clicked
		this.setResultConverter(dialogButton -> {
			if (dialogButton == okButtonType) {
				ProxyMode selectedMode = (ProxyMode) modeToggleGroup.getSelectedToggle().getUserData();
				return new ProxySettingsVo(
					selectedMode,
					httpProxyField.getText().trim().isEmpty() ? null : httpProxyField.getText().trim(),
					httpsProxyField.getText().trim().isEmpty() ? null : httpsProxyField.getText().trim(),
					usernameField.getText().trim().isEmpty() ? null : usernameField.getText().trim(),
					passwordField.getText().trim().isEmpty() ? null : passwordField.getText().trim(),
					verifySslCheckBox.isSelected()
				);
			}
			return null;
		});
	}
}
