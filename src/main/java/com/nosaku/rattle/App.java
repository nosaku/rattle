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

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.iliareshetov.RichJsonFX;
import com.nosaku.rattle.util.CommonConstants;
import com.nosaku.rattle.util.CommonUtil;
import com.nosaku.rattle.util.OAuthTokenStore;
import com.nosaku.rattle.util.StringUtil;
import com.nosaku.rattle.vo.ApiModelVo;
import com.nosaku.rattle.vo.AppVo;
import com.nosaku.rattle.vo.ProxySettingsVo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.TreeView.EditEvent;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.util.Callback;

public class App extends Application {
	private double lastDividerPosition = 0.2;
	private Map<String, ApiModelVo> apiModelVoMap = new LinkedHashMap<>();
	private TabPane centerTabs;
	private TreeItem<ApiModelVo> rootTreeItem;
	private TreeItem<ApiModelVo> authConfigTreeItem;
	private TreeView<ApiModelVo> treeView;
	private ProxySettingsVo proxySettings;
	private ConsoleWindow consoleWindow;
	private TabManager tabManager;

	public App() {
		this.consoleWindow = new ConsoleWindow();
	}

	public static void main(String[] args) {
		Application.launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		Label appTitle = new Label(CommonConstants.APP_TITLE);
		appTitle.setFont(new Font("Arial", 14));

		Label copyright = new Label(CommonConstants.COPYRIGHT_LABEL_TEXT);
		HBox footer = new HBox(copyright);
		footer.setAlignment(Pos.CENTER_RIGHT);
		footer.setPadding(new Insets(5, 10, 5, 10));
		footer.setStyle("-fx-background-color: #f0f0f0;");

		ApiModelVo historyTreeItem = new ApiModelVo();
		historyTreeItem.setName("History");
		rootTreeItem = new TreeItem<>(historyTreeItem);
		rootTreeItem.setExpanded(true);
		
		ApiModelVo authConfigParent = new ApiModelVo();
		authConfigParent.setName("Auth configurations");
		authConfigTreeItem = new TreeItem<>(authConfigParent);
		authConfigTreeItem.setExpanded(true);
		
		// Create a virtual root to hold both trees
		ApiModelVo virtualRoot = new ApiModelVo();
		virtualRoot.setName("");
		TreeItem<ApiModelVo> virtualRootItem = new TreeItem<>(virtualRoot);
		virtualRootItem.setExpanded(true);
		virtualRootItem.getChildren().addAll(rootTreeItem, authConfigTreeItem);
		
		centerTabs = new TabPane();
		treeView = new TreeView<>(virtualRootItem);
		treeView.setShowRoot(false); // Hide the virtual root
		
		tabManager = new TabManager(centerTabs, rootTreeItem, authConfigTreeItem, treeView, apiModelVoMap,
				tabId -> createApiTabContent(tabId), () -> saveApiModelVoMapAsJson());
		
		// treeView.setEditable(true);
		treeView.setCellFactory(new Callback<TreeView<ApiModelVo>, TreeCell<ApiModelVo>>() {
			@Override
			public TreeCell<ApiModelVo> call(TreeView<ApiModelVo> p) {
				return new ContextMenuTreeCell(App.this);
			}
		});
		treeView.setOnEditCommit(event -> {
			renameTreeItem(event, centerTabs);
		});
		treeView.setOnKeyPressed(event -> {
			TreeItem<ApiModelVo> selectedItem = treeView.getSelectionModel().getSelectedItem();
			if (selectedItem != null && selectedItem.getValue() != null) {
				ApiModelVo value = selectedItem.getValue();

				if (event.getCode() == KeyCode.DELETE) {
					// Prevent deleting root
					if (value.getId() != null && !"History".equalsIgnoreCase(value.getName())) {
						deleteTreeItem(selectedItem);
					}
				} else if (event.getCode() == KeyCode.F2) {
					// TODO
				} else if (event.getCode() == KeyCode.ENTER) {
					if (value != null && value.getId() != null) {
						openTab(value.getId());
						event.consume();
					}
				}
			}
		});

		Button addButton = new Button("New request");
		addButton.setMaxWidth(Double.MAX_VALUE);
		VBox.setMargin(addButton, new Insets(5, 5, 0, 5));

		VBox leftPanel = new VBox(5);
		VBox.setVgrow(treeView, Priority.ALWAYS);
		leftPanel.getChildren().addAll(addButton, treeView);
		leftPanel.setMinWidth(150);
		addButton.setOnAction(event -> {
			tabManager.addNewTab(null, true, false);
		});

		SplitPane splitPane = new SplitPane();
		splitPane.getItems().addAll(leftPanel, centerTabs);
		splitPane.setDividerPositions(lastDividerPosition);

		splitPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
			Platform.runLater(() -> {
				splitPane.setDividerPosition(0, lastDividerPosition);
			});
		});

		BorderPane root = new BorderPane();

		MenuBar menuBar = new MenuBarBuilder(new MenuBarBuilder.MenuCallbacks() {
			@Override
			public void onNewRequest() {
				tabManager.addNewTab(null, true, false);
			}

			@Override
			public void onSave() {
				tabManager.saveCurrentTab();
			}

			@Override
			public void onSaveAll() {
				tabManager.saveAllTabs();
			}

			@Override
			public void onViewConsole() {
				consoleWindow.show(centerTabs.getScene().getWindow());
			}

			@Override
			public void onProxySettings() {
				openProxySettingsDialog();
			}

			@Override
			public void onAbout() {
				AboutDialog.show(centerTabs.getScene().getWindow());
			}
		}).build();

		VBox topContainer = new VBox(menuBar/* , header */);
		root.setTop(topContainer);
		root.setCenter(splitPane);
		root.setBottom(footer);

		Scene scene = new Scene(root, 800, 600);
		// TODO dark theme
		scene.getStylesheets()
				.add(Objects.requireNonNull(App.class.getResource("/json-light-theme.css")).toExternalForm());

		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN),
				() -> tabManager.saveCurrentTab());
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
				() -> tabManager.saveAllTabs());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
				() -> tabManager.addNewTab(null, true, false));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
				() -> tabManager.closeCurrentTab());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), () -> Platform.exit());

		stage.setTitle("Rattle");
		Image icon = new Image("rattlesnake.png");
		stage.getIcons().add(icon);
		// stage.setMaximized(true); // TODO
		stage.setScene(scene);

		stage.setOnCloseRequest(event -> {
			if (tabManager.hasUnsavedTabs()) {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "You have unsaved changes. Save before closing?",
						ButtonType.YES, ButtonType.NO);
				alert.setHeaderText("Unsaved Changes");
				alert.initOwner(stage);
				alert.showAndWait().ifPresent(response -> {
					if (response == ButtonType.YES) {
						for (Map.Entry<String, ApiModelVo> entry : apiModelVoMap.entrySet()) {
							entry.getValue().setNewTab(false);
						}
					} else {
						List<String> keysToRemove = new ArrayList<>();
						for (Map.Entry<String, ApiModelVo> entry : apiModelVoMap.entrySet()) {
							if (entry.getValue().isNewTab()) {
								keysToRemove.add(entry.getKey());
							}
						}
						keysToRemove.forEach(apiModelVoMap::remove);
					}
				});
			}
			tabManager.saveAllTabs();
			if (consoleWindow.isShowing()) {
				consoleWindow.close();
			}
		});

		initApp();
		stage.show();
	}

	private void initApp() {
		ObjectMapper mapper = new ObjectMapper();
		File dir = new File(System.getProperty("user.home"), ".rattle");
		if (!dir.exists()) {
			return;
		}
		File inFile = new File(dir, CommonConstants.FILE_NAME);
		if (!inFile.exists()) {
			return;
		}
		int lastTabIndex = 0;
		try (FileReader in = new FileReader(inFile)) {
			String json = new String(Files.readAllBytes(Paths.get(inFile.getAbsolutePath())));
			AppVo appVo = mapper.readValue(json, AppVo.class);

			if (appVo.getProxySettings() != null) {
				this.proxySettings = appVo.getProxySettings();
				ApiHelper.getInstance().setProxySettings(this.proxySettings);
			}

			List<ApiModelVo> apiModelVoList = appVo.getApiList();
			Tab currentTab = null;
			int lastAuthConfigIndex = 0;
			
			for (ApiModelVo apiModelVo : apiModelVoList) {
				apiModelVoMap.put(apiModelVo.getId(), apiModelVo);
				
				if (apiModelVo.isAuthConfig()) {
					// Handle auth configuration
					if (apiModelVo.isTabOpen()) {
						Tab tab = tabManager.addNewAuthConfigTab(apiModelVo.getId(), true, false);
						if (tab != null && apiModelVo.isTabOpen() && apiModelVo.isCurrentTab()) {
							currentTab = tab;
						}
					} else {
						TreeItem<ApiModelVo> newTreeItem = new TreeItem<>(apiModelVo);
						authConfigTreeItem.getChildren().add(newTreeItem);
					}
					if (apiModelVo.getTabNbr() > lastAuthConfigIndex) {
						lastAuthConfigIndex = apiModelVo.getTabNbr();
					}
				} else {
					// Handle API request
					if (apiModelVo.isTabOpen()) {
						Tab tab = tabManager.addNewTab(apiModelVo.getId(), true, false);
						if (tab != null && apiModelVo.isTabOpen() && apiModelVo.isCurrentTab()) {
							currentTab = tab;
						}
					} else {
						TreeItem<ApiModelVo> newTreeItem = new TreeItem<>(apiModelVo);
						rootTreeItem.getChildren().add(newTreeItem);
					}
					if (apiModelVo.getTabNbr() > lastTabIndex) {
						lastTabIndex = apiModelVo.getTabNbr();
					}
				}
			}
			tabManager.setTabIndex(lastTabIndex);
			tabManager.setAuthConfigIndex(lastAuthConfigIndex);
			if (currentTab != null) {
				centerTabs.getSelectionModel().select(currentTab);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private VBox createApiTabContent(String tabId) {
		ApiModelVo apiModelVo = apiModelVoMap.get(tabId);
		
		ComboBox<String> methodComboBox = new ComboBox<>(
				FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD"));
		if (StringUtil.nonEmptyStr(apiModelVo.getMethod())) {
			methodComboBox.setValue(apiModelVo.getMethod());
		} else {
			// Default to POST for auth configs
			methodComboBox.setValue(apiModelVo.isAuthConfig() ? "POST" : "GET");
		}
		
		// Set default URL for auth configs
		String defaultUrl = apiModelVo.isAuthConfig() ? "" : "https://jsonplaceholder.typicode.com/todos";
		TextField urlTextField = new TextField(StringUtil.nonEmptyStr(apiModelVo.getUrl()) ? apiModelVo.getUrl() : defaultUrl);
		HBox.setHgrow(urlTextField, Priority.ALWAYS);
		Button sendButton = new Button("Send");

		Tab currentTab = null;
		for (Tab tab : centerTabs.getTabs()) {
			if (tab.getId().equals(tabId)) {
				currentTab = tab;
				break;
			}
		}

		HBox requestBar = new HBox(10);
		requestBar.setPadding(new Insets(10));
		requestBar.getChildren().addAll(methodComboBox, urlTextField, sendButton);
		requestBar.setAlignment(Pos.CENTER_LEFT);
		requestBar.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ddd; -fx-border-width: 0 0 1 0;");

		Tab finalCurrentTab = currentTab;
		urlTextField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (finalCurrentTab != null && !newVal.equals(oldVal)) {
				tabManager.markTabAsModified(finalCurrentTab);
			}
		});

		methodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (finalCurrentTab != null && !newVal.equals(oldVal)) {
				tabManager.markTabAsModified(finalCurrentTab);
			}
		});

		VBox paramsContainer = new VBox(5);
		paramsContainer.setPadding(new Insets(10));
		Button addParamButton = new Button("+ Add Parameter");
		addParamButton.setOnAction(e -> {
			HBox newRow = createParamRow(paramsContainer, false, null, null);
			paramsContainer.getChildren().add(paramsContainer.getChildren().size() - 1, newRow);
		});

		if (apiModelVo.getParams() != null && !apiModelVo.getParams().isEmpty()) {
			for (Map.Entry<String, String> entry : apiModelVo.getParams().entrySet()) {
				HBox newRow = createParamRow(paramsContainer, false, entry.getKey(), entry.getValue());
				paramsContainer.getChildren().add(newRow);
			}
		} else {
			// For auth configs, prefill with OAuth2 params
			if (apiModelVo.isAuthConfig()) {
				paramsContainer.getChildren().add(createParamRow(paramsContainer, false, "grant_type", "client_credentials"));
				paramsContainer.getChildren().add(createParamRow(paramsContainer, false, "client_id", ""));
				paramsContainer.getChildren().add(createParamRow(paramsContainer, false, "client_secret", ""));
				paramsContainer.getChildren().add(createParamRow(paramsContainer, false, "scope", ""));
			} else {
				paramsContainer.getChildren().add(createParamRow(paramsContainer, false, null, null));
			}
		}
		paramsContainer.getChildren().add(addParamButton);
		ScrollPane paramsScrollPane = new ScrollPane(paramsContainer);
		paramsScrollPane.setFitToWidth(true);

		VBox headersContainer = new VBox(5);
		headersContainer.setPadding(new Insets(10));
		Button addHeaderButton = new Button("+ Add Header");
		addHeaderButton.setOnAction(e -> {
			HBox newRow = createParamRow(headersContainer, true, null, null);
			headersContainer.getChildren().add(headersContainer.getChildren().size() - 1, newRow);
		});

		if (apiModelVo.getHeaders() != null && !apiModelVo.getHeaders().isEmpty()) {
			for (Map.Entry<String, String> entry : apiModelVo.getHeaders().entrySet()) {
				HBox newRow = createParamRow(headersContainer, true, entry.getKey(), entry.getValue());
				headersContainer.getChildren().add(newRow);
			}
		} else {
			headersContainer.getChildren().add(createParamRow(headersContainer, true, null, null));
		}
		headersContainer.getChildren().add(addHeaderButton);
		ScrollPane headersScrollPane = new ScrollPane(headersContainer);
		headersScrollPane.setFitToWidth(true);

		// Content Area (SplitPane for Parameters/Body and Response)
		// Top Content: Parameters, Headers, Body selector tabs
		TabPane topTabs = new TabPane();
		
		// Add Auth tab (different content for auth configs vs regular requests)
		if (apiModelVo.isAuthConfig()) {
			VBox authTabContent = createAuthConfigContent(apiModelVo, finalCurrentTab);
			topTabs.getTabs().add(new Tab("Auth", authTabContent));
		} else {
			VBox authSelectionContent = createAuthSelectionContent(apiModelVo, finalCurrentTab);
			topTabs.getTabs().add(new Tab("Auth", authSelectionContent));
		}
		
		topTabs.getTabs().add(new Tab("Params", paramsScrollPane));
		topTabs.getTabs().add(new Tab("Headers", headersScrollPane));

		TextArea bodyTextArea = new TextArea(apiModelVo.getBody());
		bodyTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
			if (finalCurrentTab != null && !newVal.equals(oldVal)) {
				tabManager.markTabAsModified(finalCurrentTab);
			}
		});
		topTabs.getTabs().add(new Tab("Body", bodyTextArea));
		topTabs.getTabs().forEach(tab -> tab.setClosable(false));

		// Bottom Content: Response area
		VBox responseContainer = new VBox(5);
		responseContainer.setPadding(new Insets(10));
		Label responseLabel = new Label("Status Code");
		HBox responseLabelRow = new HBox(responseLabel);
		responseLabelRow.setAlignment(Pos.CENTER_RIGHT);
		responseLabel.setPadding(new Insets(0, 10, 0, 0));
		CodeArea responseArea = new CodeArea();
		responseArea.setWrapText(true);
		responseArea.setStyle("-fx-word-wrap: break-word;");
		VirtualizedScrollPane<CodeArea> responseAreaScrollPane = new VirtualizedScrollPane<>(responseArea);
		
		ProgressIndicator loadingSpinner = new ProgressIndicator();
		loadingSpinner.setMaxSize(50, 50);
		loadingSpinner.setVisible(false);
		
		StackPane responseStackPane = new StackPane();
		responseStackPane.getChildren().addAll(responseAreaScrollPane, loadingSpinner);
		VBox.setVgrow(responseStackPane, Priority.ALWAYS);
		
		responseContainer.getChildren().addAll(responseLabelRow, responseStackPane);

		SplitPane mainContentSplit = new SplitPane();
		mainContentSplit.setOrientation(Orientation.VERTICAL);
		mainContentSplit.getItems().addAll(topTabs, responseContainer);
		mainContentSplit.setDividerPositions(0.4);

		VBox mainLayout = new VBox();
		mainLayout.getChildren().addAll(requestBar, mainContentSplit);
		VBox.setVgrow(mainContentSplit, Priority.ALWAYS);

		Runnable sendAction = () -> {
			invokeApi(tabId, methodComboBox.getValue(), urlTextField.getText(), getParams(paramsContainer),
					getParams(headersContainer), bodyTextArea.getText(), responseArea, responseLabel, loadingSpinner);
		};

		sendButton.setOnAction(e -> sendAction.run());

		mainLayout.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.D && e.isControlDown()) {
				sendAction.run();
				e.consume();
			}
		});

		return mainLayout;
	}

	private HBox createParamRow(VBox parentContainer, boolean isHeaderRow, String key, String value) {
		CheckBox enableCheckBox = new CheckBox();
		enableCheckBox.setSelected(true);

		TextField keyField = new TextField();
		keyField.setId("key");
		keyField.setPromptText("Key");
		if (StringUtil.nonEmptyStr(key)) {
			keyField.setText(key);
		}
		if (isHeaderRow) {
			TextFields.bindAutoCompletion(keyField, CommonConstants.HTTP_HEADERS);
		}
		TextField valueField = new TextField();
		valueField.setId("value");
		valueField.setPromptText("Value");
		if (StringUtil.nonEmptyStr(value)) {
			valueField.setText(value);
		}
		Button deleteButton = new Button("✕");
		deleteButton.setStyle("-fx-font-size: 10px; -fx-padding: 2 5 2 5;");

		HBox row = new HBox(5);
		row.setAlignment(Pos.CENTER_LEFT);
		row.setPadding(new Insets(2, 0, 2, 0));
		row.getChildren().addAll(keyField, valueField, enableCheckBox, deleteButton);
		HBox.setHgrow(keyField, Priority.ALWAYS);
		HBox.setHgrow(valueField, Priority.ALWAYS);

		deleteButton.setOnAction(e -> parentContainer.getChildren().remove(row));

		keyField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.equals(oldVal)) {
				tabManager.markParentTabAsModified(parentContainer);
			}
		});

		valueField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.equals(oldVal)) {
				tabManager.markParentTabAsModified(parentContainer);
			}
		});

		enableCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.equals(oldVal)) {
				tabManager.markParentTabAsModified(parentContainer);
			}
		});

		return row;
	}

	private void renameTreeItem(EditEvent<ApiModelVo> event, TabPane centerTabs) {
		ApiModelVo existingModel = event.getTreeItem().getValue();
		String newName = event.getNewValue().getName();
		existingModel.setName(newName);
		// event.getTreeItem().setValue(existingModel);
		treeView.refresh();

		for (Tab tab : centerTabs.getTabs()) {
			if (tab.getId().equals(existingModel.getId())) {
				if (tab.getText().lastIndexOf(" *") != -1) {
					tab.setText(tabManager.truncateTabTitle(newName) + " *");
				} else {
					tab.setText(tabManager.truncateTabTitle(newName));
					tabManager.saveTab(tab);
				}
				break;
			}
		}
	}

	public void cloneTreeItem(TreeItem<ApiModelVo> treeItem) {
		tabManager.addNewTab(treeItem.getValue().getId(), true, true);
	}

	public void deleteTreeItem(TreeItem<ApiModelVo> treeItem) {
		if (treeItem == null || treeItem.getValue() == null) {
			return;
		}
		ApiModelVo value = treeItem.getValue();
		if (value.getId() == null || "History".equalsIgnoreCase(value.getName())) {
			return;
		}

		Tab toClose = null;
		for (Tab t : centerTabs.getTabs()) {
			if (t.getId().equals(value.getId())) {
				toClose = t;
				break;
			}
		}
		if (toClose != null) {
			centerTabs.getTabs().remove(toClose);
		}

		TreeItem<ApiModelVo> parent = treeItem.getParent();
		if (parent != null) {
			parent.getChildren().remove(treeItem);
		}

		apiModelVoMap.remove(value.getId());
		saveApiModelVoMapAsJson();
	}

	private void invokeApi(String tabId, String method, String url, Map<String, String> params,
			Map<String, String> headers, String body, CodeArea responseArea, Label responseLabel,
			ProgressIndicator loadingSpinner) {
		ApiModelVo currentApiModel = apiModelVoMap.get(tabId);
		
		ApiModelVo apiModelVo = new ApiModelVo();
		apiModelVo.setMethod(method);
		apiModelVo.setUrl(url);
		apiModelVo.setParams(params);
		apiModelVo.setHeaders(headers != null ? new LinkedHashMap<>(headers) : new LinkedHashMap<>());
		apiModelVo.setBody(body);

		responseArea.clear();
		loadingSpinner.setVisible(true);

		javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
					// Check if auth config is set for this request
					if (currentApiModel != null && StringUtil.nonEmptyStr(currentApiModel.getAuthConfigId())) {
						String authConfigId = currentApiModel.getAuthConfigId();
						ApiModelVo authConfig = apiModelVoMap.get(authConfigId);
						
						if (authConfig != null && authConfig.isAuthConfig()) {
							if (OAuthTokenStore.getInstance().isTokenExpired(authConfigId)) {
								String tokenJsonStr = fetchAuthToken(authConfig);
								OAuthTokenStore.getInstance().storeToken(authConfigId, tokenJsonStr);
							}
							String bearerToken = OAuthTokenStore.getInstance().getBearerToken(authConfigId);
							if (bearerToken != null) {
								apiModelVo.getHeaders().put("Authorization", bearerToken);
							}
						}
					}
					
					ApiHelper.getInstance().invokeApi(apiModelVo);
				} catch (Exception e) {
					apiModelVo.setResponse("Error: " + e.getClass().getName() + "\n" + "Message: " + e.getMessage()
							+ "\n\n" + "Stack Trace:\n" + CommonUtil.getStackTraceAsString(e));
					throw e;
				}
				return null;
			}

			@Override
			protected void succeeded() {
				loadingSpinner.setVisible(false);
				
				responseLabel.setText("Status Code: " + apiModelVo.getStatusCode());
				responseArea.appendText(apiModelVo.getResponse());
				responseArea.scrollYToPixel(0);
				RichJsonFX highlighter = new RichJsonFX();
				try {
					responseArea.setStyleSpans(0, highlighter.highlight(apiModelVo.getResponse()));
				} catch (Exception exp) {
					exp.printStackTrace();
				}

				if (apiModelVo.getConsoleLog() != null) {
					consoleWindow.log(apiModelVo.getConsoleLog());
				}
			}

			@Override
			protected void failed() {
				loadingSpinner.setVisible(false);
				
				responseLabel.setText("Status: Error");
				if (apiModelVo.getResponse() != null && !apiModelVo.getResponse().isEmpty()) {
					responseArea.appendText(apiModelVo.getResponse());
					if (apiModelVo.getConsoleLog() != null) {
						consoleWindow.log(apiModelVo.getConsoleLog());
					}
				} else {
					Throwable exception = getException();
					responseArea.appendText("Error: " + exception.getClass().getName() + "\n" + "Message: "
							+ exception.getMessage() + "\n\n" + "Stack Trace:\n" + CommonUtil.getStackTraceAsString(exception));
					if (apiModelVo.getConsoleLog() != null) {
						consoleWindow.log(apiModelVo.getConsoleLog());
					}
				}
				responseArea.scrollYToPixel(0);
			}
		};

		new Thread(task).start();
	}

	private void saveApiModelVoMapAsJson() {
		ObjectMapper mapper = new ObjectMapper();
		File dir = new File(System.getProperty("user.home"), ".rattle");
		if (!dir.exists()) {
			dir.mkdirs();
		}
		File outFile = new File(dir, CommonConstants.FILE_NAME);
		try (FileWriter out = new FileWriter(outFile)) {
			AppVo appVo = new AppVo();
			List<ApiModelVo> apiModelVoList = new ArrayList<>();
			appVo.setApiList(apiModelVoList);
			appVo.setProxySettings(this.proxySettings);
			for (Map.Entry<String, ApiModelVo> entry : apiModelVoMap.entrySet()) {
				ApiModelVo apiModelVo = entry.getValue().clone();
				apiModelVo.setResponse(null);
				apiModelVo.setConsoleLog(null);
				apiModelVoList.add(apiModelVo);
				for (Tab tab : centerTabs.getTabs()) {
					if (tab.getId().equals(apiModelVo.getId())) {
						apiModelVo.setTabOpen(true);
					}
				}
			}
			out.write(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(appVo));
			out.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openProxySettingsDialog() {
		ProxySettingsDialog dialog = new ProxySettingsDialog(this.proxySettings);
		dialog.initOwner(centerTabs.getScene() != null ? centerTabs.getScene().getWindow() : null);
		dialog.showAndWait().ifPresent(newSettings -> {
			this.proxySettings = newSettings;
			ApiHelper.getInstance().setProxySettings(newSettings);
			saveApiModelVoMapAsJson();

			Alert alert = new Alert(Alert.AlertType.INFORMATION);
			alert.setTitle("Proxy Settings");
			alert.setHeaderText("Proxy settings updated successfully");
			alert.setContentText("Proxy settings have been saved and will be used for future API requests.");
			alert.initOwner(centerTabs.getScene().getWindow());
			alert.showAndWait();
		});
	}

	private String fetchAuthToken(ApiModelVo authConfig) throws Exception {
		// Create a request to fetch the token
		ApiModelVo tokenRequest = new ApiModelVo();
		tokenRequest.setMethod(authConfig.getMethod() != null ? authConfig.getMethod() : "POST");
		tokenRequest.setUrl(authConfig.getUrl());
		tokenRequest.setParams(authConfig.getParams());
		tokenRequest.setHeaders(authConfig.getHeaders());
		tokenRequest.setBody(authConfig.getBody());
		
		// Invoke the token endpoint
		ApiHelper.getInstance().invokeApi(tokenRequest);
		
		// Return the JSON response for OAuthTokenStore to parse
		if (tokenRequest.getStatusCode() >= 200 && tokenRequest.getStatusCode() < 300) {
			return tokenRequest.getResponse();
		} else {
			throw new Exception("Auth token request failed with status " + tokenRequest.getStatusCode() + 
					": " + tokenRequest.getResponse());
		}
	}
	
	private Map<String, String> getParams(VBox paramsContainer) {
		Map<String, String> params = new LinkedHashMap<>();
		for (Node node : paramsContainer.getChildren()) {
			if (node instanceof HBox) {
				HBox row = (HBox) node;
				String key = null;
				String value = null;
				boolean isSelected = false;
				for (Node fieldNode : row.getChildren()) {
					if (fieldNode instanceof TextField) {
						TextField textField = (TextField) fieldNode;
						if ("key".equals(textField.getId())) {
							key = textField.getText();
						} else if ("value".equals(textField.getId())) {
							value = textField.getText();
						}
					} else if (fieldNode instanceof CheckBox) {
						CheckBox checkBox = (CheckBox) fieldNode;
						isSelected = checkBox.isSelected();
					}
				}
				if (isSelected && StringUtil.nonEmptyStr(key)) {
					params.put(key, value);
				}
			}
		}
		return params;
	}

	public void openTab(String id) {
		tabManager.openTab(id);
	}
	
	public void addNewAuthConfig() {
		tabManager.addNewAuthConfigTab(null, true, false);
	}
	
	public void clearAuthToken(String authConfigId) {
		OAuthTokenStore.getInstance().clearToken(authConfigId);
	}
	
	public void clearAllAuthTokens() {
		OAuthTokenStore.getInstance().clearAllTokens();
	}
	
	private VBox createAuthSelectionContent(ApiModelVo apiModelVo, Tab currentTab) {
		VBox authBox = new VBox(10);
		authBox.setPadding(new Insets(10));
		
		Label authLabel = new Label("Authentication:");
		ComboBox<String> authComboBox = new ComboBox<>();
		authComboBox.setPromptText("Select authentication");
		authComboBox.setPrefWidth(300);
		
		// Build list of auth configurations with display names
		Map<String, String> authConfigMap = new LinkedHashMap<>(); // id -> name
		authConfigMap.put("None", "None");
		
		// Get all auth configs from the tree
		for (TreeItem<ApiModelVo> item : authConfigTreeItem.getChildren()) {
			if (item.getValue() != null && item.getValue().getId() != null && item.getValue().getName() != null) {
				authConfigMap.put(item.getValue().getId(), item.getValue().getName());
			}
		}
		
		authComboBox.setItems(FXCollections.observableArrayList(authConfigMap.values()));
		
		// Set current selection - find name by ID
		String displayName = "None";
		if (StringUtil.nonEmptyStr(apiModelVo.getAuthConfigId())) {
			for (Map.Entry<String, String> entry : authConfigMap.entrySet()) {
				if (entry.getKey().equals(apiModelVo.getAuthConfigId())) {
					displayName = entry.getValue();
					break;
				}
			}
		}
		authComboBox.setValue(displayName);
		
		authComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (currentTab != null && !newVal.equals(oldVal)) {
				tabManager.markTabAsModified(currentTab);
				
				// Find ID by name
				String selectedId = null;
				for (Map.Entry<String, String> entry : authConfigMap.entrySet()) {
					if (entry.getValue().equals(newVal)) {
						selectedId = entry.getKey();
						break;
					}
				}
				apiModelVo.setAuthConfigId("None".equals(selectedId) ? null : selectedId);
			}
		});
		
		authBox.getChildren().addAll(authLabel, authComboBox);
		
		return authBox;
	}
	
	private VBox createAuthConfigContent(ApiModelVo apiModelVo, Tab currentTab) {
		VBox authBox = new VBox(10);
		authBox.setPadding(new Insets(10));
		
		// Auth Type Dropdown
		Label authTypeLabel = new Label("Authentication Type:");
		ComboBox<String> authTypeComboBox = new ComboBox<>(
			FXCollections.observableArrayList(CommonConstants.AUTHENTICATION_TYPES)
		);
		authTypeComboBox.setPromptText("Select authentication type");
		authTypeComboBox.setPrefWidth(300);
		
		if (StringUtil.nonEmptyStr(apiModelVo.getAuthType())) {
			authTypeComboBox.setValue(apiModelVo.getAuthType());
		}

		// Container for auth-type specific fields
		VBox fieldsContainer = new VBox(10);
		fieldsContainer.setPadding(new Insets(10, 0, 0, 0));
		
		VBox oAuth2Fields = createOAuth2Fields(apiModelVo, currentTab);
		if (CommonConstants.AUTHENTICATION_TYPE_OAUTH2.equals(authTypeComboBox.getValue())) {
			fieldsContainer.getChildren().add(oAuth2Fields);
		}
		
		// Listen for auth type changes
		authTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (currentTab != null && !newVal.equals(oldVal)) {
				tabManager.markTabAsModified(currentTab);
			}
			
			apiModelVo.setAuthType(newVal);
			fieldsContainer.getChildren().clear();
			
			if (CommonConstants.AUTHENTICATION_TYPE_OAUTH2.equals(newVal)) {
				fieldsContainer.getChildren().add(oAuth2Fields);
				prefillOAuth2Defaults(apiModelVo);
			} else {
				// Placeholder for other auth types
				Label comingSoonLabel = new Label("Configuration for " + newVal + " coming soon...");
				comingSoonLabel.setStyle("-fx-text-fill: #666;");
				fieldsContainer.getChildren().add(comingSoonLabel);
			}
		});
		
		authBox.getChildren().addAll(authTypeLabel, authTypeComboBox, fieldsContainer);
		
		return authBox;
	}
	
	private void prefillOAuth2Defaults(ApiModelVo apiModelVo) {
		// Params will be prefilled when the tab is created
		// This method is for any additional setup when auth type changes
	}
	
	private VBox createOAuth2Fields(ApiModelVo apiModelVo, Tab currentTab) {
		VBox oAuth2Box = new VBox(10);
		
		Label instructionsLabel = new Label(CommonConstants.AUTHENTICATION_TYPE_OAUTH2 + " Configuration:");
		instructionsLabel.setStyle("-fx-font-weight: bold;");
		
		Label infoLabel = new Label("Enter the token URL in the main URL field above.\nFill in the parameters in the Params tab (grant_type, client_id, client_secret, scope).");
		infoLabel.setStyle("-fx-text-fill: #666;");
		infoLabel.setWrapText(true);
		
		oAuth2Box.getChildren().addAll(instructionsLabel, infoLabel);
		
		return oAuth2Box;
	}
}
