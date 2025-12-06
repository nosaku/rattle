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
import java.util.UUID;

import org.controlsfx.control.textfield.TextFields;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.iliareshetov.RichJsonFX;
import com.nosaku.rattle.util.CommonConstants;
import com.nosaku.rattle.util.CommonUtil;
import com.nosaku.rattle.util.StringUtil;
import com.nosaku.rattle.vo.ApiModelVo;
import com.nosaku.rattle.vo.AppVo;
import com.nosaku.rattle.vo.ProxySettingsVo;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.event.EventHandler;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
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
	private int tabIndex;
	private Map<String, ApiModelVo> apiModelVoMap = new LinkedHashMap<>();
	private TabPane centerTabs;
	private TreeItem<ApiModelVo> rootTreeItem;
	private TreeView<ApiModelVo> treeView;
	private ProxySettingsVo proxySettings;
	private ConsoleWindow consoleWindow;

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
		centerTabs = new TabPane();
		treeView = new TreeView<>(rootTreeItem);
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
			addNewTab(centerTabs, rootTreeItem, treeView, null, true);
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
				addNewTab(centerTabs, rootTreeItem, treeView, null, true);
			}

			@Override
			public void onSave() {
				saveCurrentTab();
			}

			@Override
			public void onSaveAll() {
				saveAllTabs();
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
				() -> saveCurrentTab());
		scene.getAccelerators().put(
				new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN),
				() -> saveAllTabs());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN),
				() -> addNewTab(centerTabs, rootTreeItem, treeView, null, true));
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.W, KeyCombination.CONTROL_DOWN),
				() -> closeCurrentTab());
		scene.getAccelerators().put(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), () -> Platform.exit());

		stage.setTitle("Rattle");
		Image icon = new Image("rattlesnake.png");
		stage.getIcons().add(icon);
		// stage.setMaximized(true); // TODO
		stage.setScene(scene);

		stage.setOnCloseRequest(event -> {
			if (hasUnsavedTabs()) {
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
						for (Map.Entry<String, ApiModelVo> entry : apiModelVoMap.entrySet()) {
							if (entry.getValue().isNewTab()) {
								apiModelVoMap.remove(entry.getKey());
							}
						}
					}
				});
			}
			saveAllTabs();
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
			for (ApiModelVo apiModelVo : apiModelVoList) {
				apiModelVoMap.put(apiModelVo.getId(), apiModelVo);
				if (apiModelVo.isTabOpen()) {
					Tab tab = addNewTab(centerTabs, rootTreeItem, treeView, apiModelVo.getId(), true);
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
			this.tabIndex = lastTabIndex;
			if (currentTab != null) {
				centerTabs.getSelectionModel().select(currentTab);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Tab addNewTab(TabPane tabPane, TreeItem<ApiModelVo> parentTreeItem, TreeView<ApiModelVo> treeView,
			String tabId, boolean isAddTreeItem) {
		Tab tab = new Tab();
		ApiModelVo apiModelVo = null;
		if (tabId == null) {
			String title = "Request " + (++tabIndex);
			apiModelVo = new ApiModelVo();
			apiModelVo.setId(UUID.randomUUID().toString());
			apiModelVo.setName(title);
			apiModelVo.setTabNbr(tabIndex);
			apiModelVo.setNewTab(true);
			apiModelVoMap.put(apiModelVo.getId(), apiModelVo);
			tab.setText(truncateTabTitle(title) + " *");
			tab.setId(apiModelVo.getId());
			tab.setClosable(true);
		} else {
			apiModelVo = apiModelVoMap.get(tabId);
			tab.setText(truncateTabTitle(apiModelVo.getName()));
			tab.setId(apiModelVo.getId());
			tab.setClosable(true);
			if (isAddTreeItem) {
				// TODO url etc.
			}
		}
		tabPane.getTabs().add(tab);

		VBox contentContainer = createApiTabContent(tab.getId());
		contentContainer.setFocusTraversable(true);
		tab.setContent(contentContainer);

		if (tabId == null || isAddTreeItem) {
			TreeItem<ApiModelVo> newTreeItem = new TreeItem<>(apiModelVo);
			parentTreeItem.getChildren().add(newTreeItem);
			tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					treeView.getSelectionModel().select(newTreeItem);
				}
			});
		}
		tab.setOnCloseRequest(event -> {
			ApiModelVo closeTabApiModelVo = apiModelVoMap.get(tab.getId());
			boolean isSave = (closeTabApiModelVo != null && closeTabApiModelVo.isModified())
					|| tab.getText().endsWith(" *");
			if (isSave) {
				Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
						"This tab has unsaved changes. Save before closing?", ButtonType.YES, ButtonType.NO,
						ButtonType.CANCEL);
				alert.setHeaderText("Unsaved changes");
				if (tabPane.getScene() != null && tabPane.getScene().getWindow() != null) {
					alert.initOwner(tabPane.getScene().getWindow());
				}
				alert.showAndWait().ifPresent(response -> {
					if (response == ButtonType.YES) {
						closeTabApiModelVo.setTabOpen(false);
						saveTab(tab, true);
					} else if (response == ButtonType.NO) {
						if (tab.getText().endsWith(" *")) {
							tab.setText(tab.getText().substring(0, tab.getText().lastIndexOf(" *")));
						}
						if (closeTabApiModelVo != null) {
							closeTabApiModelVo.setModified(false);
						}
					} else {
						event.consume();
					}
				});
			} else {
				closeTabApiModelVo.setTabOpen(false);
				saveTab(tab, true);
			}
		});

		tabPane.getSelectionModel().select(tab);
		Platform.runLater(() -> {
			if (tab.getContent() != null) {
				tab.getContent().requestFocus();
			}
		});
		return tab;
	}

	private VBox createApiTabContent(String tabId) {
		ApiModelVo apiModelVo = apiModelVoMap.get(tabId);

		ComboBox<String> methodComboBox = new ComboBox<>(
				FXCollections.observableArrayList("GET", "POST", "PUT", "DELETE", "PATCH", "HEAD"));
		if (StringUtil.nonEmptyStr(apiModelVo.getMethod())) {
			methodComboBox.setValue(apiModelVo.getMethod());
		} else {
			methodComboBox.setValue("GET");
		}
		TextField urlTextField = new TextField(StringUtil.nonEmptyStr(apiModelVo.getUrl()) ? apiModelVo.getUrl()
				: "https://jsonplaceholder.typicode.com/todos");
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
				markTabAsModified(finalCurrentTab);
			}
		});

		methodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
			if (finalCurrentTab != null && !newVal.equals(oldVal)) {
				markTabAsModified(finalCurrentTab);
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
			paramsContainer.getChildren().add(createParamRow(paramsContainer, false, null, null));
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
		topTabs.getTabs().add(new Tab("Params", paramsScrollPane));
		topTabs.getTabs().add(new Tab("Headers", headersScrollPane));

		TextArea bodyTextArea = new TextArea(apiModelVo.getBody());
		bodyTextArea.textProperty().addListener((obs, oldVal, newVal) -> {
			if (finalCurrentTab != null && !newVal.equals(oldVal)) {
				markTabAsModified(finalCurrentTab);
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
				markParentTabAsModified(parentContainer);
			}
		});

		valueField.textProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.equals(oldVal)) {
				markParentTabAsModified(parentContainer);
			}
		});

		enableCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
			if (!newVal.equals(oldVal)) {
				markParentTabAsModified(parentContainer);
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
					tab.setText(truncateTabTitle(newName) + " *");
				} else {
					tab.setText(truncateTabTitle(newName));
					saveTab(tab);
				}
				break;
			}
		}
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

	private String truncateTabTitle(String title) {
		if (title == null) {
			return "";
		}
		String cleanTitle = title.endsWith(" *") ? title.substring(0, title.length() - 2) : title;
		if (cleanTitle.length() > 15) {
			return cleanTitle.substring(0, 12) + "...";
		}
		return cleanTitle;
	}

	private void markTabAsModified(Tab tab) {
		String currentText = tab.getText();
		if (!currentText.endsWith(" *")) {
			tab.setText(currentText + " *");
			ApiModelVo apiModelVo = apiModelVoMap.get(tab.getId());
			if (apiModelVo != null) {
				apiModelVo.setModified(true);
			}
		}
	}

	private void markParentTabAsModified(VBox container) {
		for (Tab tab : centerTabs.getTabs()) {
			if (tab.getContent() == container || isDescendantOf(container, tab.getContent())) {
				markTabAsModified(tab);
				break;
			}
		}
	}

	private boolean isDescendantOf(Node child, Node parent) {
		Node current = child.getParent();
		while (current != null) {
			if (current == parent) {
				return true;
			}
			current = current.getParent();
		}
		return false;
	}

	private void saveCurrentTab() {
		Tab selectedTab = centerTabs.getSelectionModel().getSelectedItem();
		if (selectedTab != null) {
			saveTab(selectedTab);
		}
	}

	private void saveAllTabs() {
		for (Tab tab : new ArrayList<>(centerTabs.getTabs())) {
			saveTab(tab, false);
		}
		saveApiModelVoMapAsJson();
	}

	private boolean hasUnsavedTabs() {
		for (Tab tab : centerTabs.getTabs()) {
			if (tab.getText().endsWith(" *")) {
				return true;
			}
			ApiModelVo apiModelVo = apiModelVoMap.get(tab.getId());
			if (apiModelVo != null && apiModelVo.isModified()) {
				return true;
			}
		}
		return false;
	}

	private void closeCurrentTab() {
		Tab selectedTab = centerTabs.getSelectionModel().getSelectedItem();
		if (selectedTab == null) {
			return;
		}

		EventHandler<javafx.event.Event> handler = selectedTab.getOnCloseRequest();
		if (handler != null) {
			Event closeEvent = new Event(Event.ANY);
			handler.handle(closeEvent);
			if (closeEvent.isConsumed()) {
				return;
			}
		}

		TabPane tabPane = selectedTab.getTabPane();
		if (tabPane != null) {
			tabPane.getTabs().remove(selectedTab);
		}
	}

	private void saveTab(Tab tab) {
		this.saveTab(tab, true);
	}

	private void saveTab(Tab tab, boolean isWriteToFile) {
		if (tab != null) {
			String tabText = tab.getText();
			if (tabText.endsWith(" *")) {
				tabText = tabText.substring(0, tabText.length() - 2);
				tab.setText(tabText);
			}
			ApiModelVo apiModelVo = apiModelVoMap.get(tab.getId());
			if (apiModelVo != null) {
				apiModelVo.setCurrentTab(tab.isSelected());
				VBox mainLayout = (VBox) tab.getContent();
				if (mainLayout != null && !mainLayout.getChildren().isEmpty()) {
					HBox requestBar = (HBox) mainLayout.getChildren().get(0);
					if (requestBar != null && requestBar.getChildren().size() >= 2) {
						ComboBox<String> methodComboBox = (ComboBox<String>) requestBar.getChildren().get(0);
						apiModelVo.setMethod(methodComboBox.getValue());
						TextField urlTextField = (TextField) requestBar.getChildren().get(1);
						apiModelVo.setUrl(urlTextField.getText());
					}

					if (mainLayout.getChildren().size() > 1) {
						SplitPane mainContentSplit = (SplitPane) mainLayout.getChildren().get(1);
						if (mainContentSplit != null && mainContentSplit.getItems().size() > 0) {
							TabPane topTabs = (TabPane) mainContentSplit.getItems().get(0);

							Tab paramsTab = topTabs.getTabs().get(0);
							ScrollPane paramsScrollPane = (ScrollPane) paramsTab.getContent();
							VBox paramsContainer = (VBox) paramsScrollPane.getContent();
							Map<String, String> params = extractParamsFromContainer(paramsContainer);
							apiModelVo.setParams(params);

							Tab headersTab = topTabs.getTabs().get(1);
							ScrollPane headersScrollPane = (ScrollPane) headersTab.getContent();
							VBox headersContainer = (VBox) headersScrollPane.getContent();
							Map<String, String> headers = extractParamsFromContainer(headersContainer);
							apiModelVo.setHeaders(headers);

							Tab bodyTab = topTabs.getTabs().get(2);
							TextArea bodyTextArea = (TextArea) bodyTab.getContent();
							apiModelVo.setBody(bodyTextArea.getText());
						}
					}
				}
				apiModelVo.setModified(false);
			}
			if (isWriteToFile) {
				saveApiModelVoMapAsJson();
			}
		}
	}

	private Map<String, String> extractParamsFromContainer(VBox container) {
		Map<String, String> result = new LinkedHashMap<>();
		for (Node node : container.getChildren()) {
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
					result.put(key, value != null ? value : "");
				}
			}
		}
		return result;
	}

	private void invokeApi(String tabId, String method, String url, Map<String, String> params,
			Map<String, String> headers, String body, CodeArea responseArea, Label responseLabel,
			ProgressIndicator loadingSpinner) {
		ApiModelVo apiModelVo = new ApiModelVo();
		apiModelVo.setMethod(method);
		apiModelVo.setUrl(url);
		apiModelVo.setParams(params);
		apiModelVo.setHeaders(headers);
		apiModelVo.setBody(body);

		responseArea.clear();
		loadingSpinner.setVisible(true);

		javafx.concurrent.Task<Void> task = new javafx.concurrent.Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {
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
		boolean isOpenTabFound = false;
		for (Tab tab : centerTabs.getTabs()) {
			if (tab.getId().equals(id)) {
				centerTabs.getSelectionModel().select(tab);
				isOpenTabFound = true;
				break;
			}
		}
		if (!isOpenTabFound) {
			addNewTab(centerTabs, rootTreeItem, treeView, id, false);
		}
	}
}
