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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.nosaku.rattle.util.CommonConstants;
import com.nosaku.rattle.util.CommonUtil;
import com.nosaku.rattle.util.StringUtil;
import com.nosaku.rattle.vo.ApiGroupVo;
import com.nosaku.rattle.vo.ApiModelVo;

import javafx.application.Platform;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Manages tab operations including creation, saving, and closing
 */
public class TabManager {
	
	private final TabPane tabPane;
	private final TreeView<ApiModelVo> treeView;
	private final Map<String, ApiModelVo> apiModelVoMap;
	private final Map<String, ApiGroupVo> apiGroupVoMap;
	private final TabContentFactory contentFactory;
	private final Runnable onSaveCallback;
	Map<String, TreeItem<ApiModelVo>> treeItemMap;
	
	private int tabIndex;
	private int authConfigIndex;
	
	public TabManager(TabPane tabPane, Map<String, TreeItem<ApiModelVo>> treeItemMap,
			TreeView<ApiModelVo> treeView, Map<String, ApiModelVo> apiModelVoMap,
			Map<String, ApiGroupVo> apiGroupVoMap,
			TabContentFactory contentFactory, Runnable onSaveCallback) {
		this.tabPane = tabPane;
		this.treeItemMap = treeItemMap;
		this.treeView = treeView;
		this.apiModelVoMap = apiModelVoMap;
		this.apiGroupVoMap = apiGroupVoMap;
		this.contentFactory = contentFactory;
		this.onSaveCallback = onSaveCallback;
		this.tabIndex = 0;
		this.authConfigIndex = 0;
	}
	
	public void setTabIndex(int tabIndex) {
		this.tabIndex = tabIndex;
	}
	
	public int getTabIndex() {
		return tabIndex;
	}
	
	public void setAuthConfigIndex(int authConfigIndex) {
		this.authConfigIndex = authConfigIndex;
	}
	
	public int getAuthConfigIndex() {
		return authConfigIndex;
	}
	
	/**
	 * Adds a new auth configuration tab to the tab pane
	 */
	public Tab addNewAuthConfigTab(String tabId, boolean isAddTreeItem, boolean isCloneItem) {
		return addNewTab(tabId, isAddTreeItem, isCloneItem, null, true);
	}
	
	/**
	 * Adds a new auth configuration tab to the tab pane with specific parent group
	 */
	public Tab addNewAuthConfigTab(String tabId, boolean isAddTreeItem, boolean isCloneItem, String parentGroupId) {
		return addNewTab(tabId, isAddTreeItem, isCloneItem, null, true);
	}
	
	/**
	 * Adds a new tab to the tab pane
	 */
	public Tab addNewTab(String tabId, boolean isAddTreeItem, boolean isCloneItem) {
		return addNewTab(tabId, isAddTreeItem, isCloneItem, null, false);
	}
	
	/**
	 * Unified method to add a new tab (regular or auth config) to the tab pane
	 */
	private Tab addNewTab(String tabId, boolean isAddTreeItem, boolean isCloneItem, String parentGroupId, boolean isAuthConfig) {
		Tab tab = new Tab();
		tab.setClosable(true);				

		ApiModelVo apiModelVo = null;
		
		if (tabId == null) {
			int index = isAuthConfig ? ++authConfigIndex : ++tabIndex;
			String title = isAuthConfig ? "Auth config " + index : "Request " + index;
			apiModelVo = new ApiModelVo();
			apiModelVo.setId(UUID.randomUUID().toString());
			apiModelVo.setName(title);
			apiModelVo.setTabNbr(index);
			apiModelVo.setNewTab(true);
			apiModelVo.setAuthConfig(isAuthConfig);
			apiModelVoMap.put(apiModelVo.getId(), apiModelVo);
			tab.setText(truncateTabTitle(title) + " *");
			tab.setId(apiModelVo.getId());
		} else {
			apiModelVo = apiModelVoMap.get(tabId);
			if (isCloneItem) {
				int index = isAuthConfig ? ++authConfigIndex : ++tabIndex;
				ApiModelVo clonedModel = apiModelVo.clone();
				clonedModel.setId(UUID.randomUUID().toString());
				clonedModel.setName("(Copy) " + apiModelVo.getName());
				clonedModel.setTabNbr(index);
				clonedModel.setNewTab(true);
				clonedModel.setModified(true);
				clonedModel.setTabOpen(true);
				clonedModel.setCurrentTab(true);
				clonedModel.setAuthConfig(isAuthConfig);
				apiModelVoMap.put(clonedModel.getId(), clonedModel);
				tab.setText(truncateTabTitle(clonedModel.getName()) + " *");
				tab.setId(clonedModel.getId());
				apiModelVo = clonedModel;
			} else {
				tab.setText(truncateTabTitle(apiModelVo.getName()));
				tab.setId(apiModelVo.getId());
			}
		}
		
		tabPane.getTabs().add(tab);
		
		VBox contentContainer = contentFactory.createTabContent(tab.getId());
		contentContainer.setFocusTraversable(true);
		tab.setContent(contentContainer);
		
		if (tabId == null || isAddTreeItem) {
			TreeItem<ApiModelVo> newTreeItem = new TreeItem<>(apiModelVo);
			
			// Determine the parent tree item
			TreeItem<ApiModelVo> rootTreeItem = null;
			TreeItem<ApiModelVo> selectedItem = treeView.getSelectionModel().getSelectedItem();
			String defaultGroupName = isAuthConfig ? CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS : CommonConstants.GROUP_NAME_HISTORY;
			
			if (parentGroupId != null && treeItemMap.containsKey(parentGroupId)) {
				// Explicit parent group provided (from context menu)
				rootTreeItem = treeItemMap.get(parentGroupId);
				apiModelVo.setGroupId(parentGroupId);
			} else if (isCloneItem && tabId != null) {
				// If cloning, insert after the source item in the same parent
				TreeItem<ApiModelVo> sourceTreeItem = findTreeItemById(tabId);
				if (sourceTreeItem != null && sourceTreeItem.getParent() != null) {
					rootTreeItem = sourceTreeItem.getParent();
					int sourceIndex = rootTreeItem.getChildren().indexOf(sourceTreeItem);
					rootTreeItem.getChildren().add(sourceIndex + 1, newTreeItem);
					// Set the groupId for the cloned item
					if (rootTreeItem.getValue() != null) {
						apiModelVo.setGroupId(rootTreeItem.getValue().getId());
					}
				} else {
					// Source item not in tree (might be in an open tab), use the source's groupId if available
					ApiModelVo sourceModel = apiModelVoMap.get(tabId);
					if (sourceModel != null && sourceModel.getGroupId() != null) {
						rootTreeItem = treeItemMap.get(sourceModel.getGroupId());
						if (rootTreeItem != null) {
							rootTreeItem.getChildren().add(newTreeItem);
							apiModelVo.setGroupId(sourceModel.getGroupId());
						}
					}
					// Fallback to default group if source groupId not found
					if (rootTreeItem == null) {
						rootTreeItem = treeItemMap.get(CommonUtil.getGroupId(defaultGroupName, apiGroupVoMap));
						rootTreeItem.getChildren().add(newTreeItem);
					}
				}
			} else if (tabId != null && apiModelVo.getGroupId() != null) {
				// If loading existing item with groupId (from initApp), use its saved group
				rootTreeItem = treeItemMap.get(apiModelVo.getGroupId());
				if (rootTreeItem == null) {
					// Fallback to default group if saved group doesn't exist
					rootTreeItem = treeItemMap.get(CommonUtil.getGroupId(defaultGroupName, apiGroupVoMap));
				}
				rootTreeItem.getChildren().add(newTreeItem);
			} else {
				// For new items, add to selected parent or default group if nothing selected
				if (selectedItem != null) {
					boolean isGroup = treeItemMap.containsValue(selectedItem);
					if (isGroup && selectedItem.getValue() != null) {
						// Check if selection matches the item type (auth config vs regular request)
						if (isAuthConfig) {
							// For auth configs, check if this group or ancestor is Auth Configurations
							TreeItem<ApiModelVo> current = selectedItem;
							while (current != null && current.getValue() != null) {
								if (CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS.equals(current.getValue().getName())) {
									rootTreeItem = selectedItem;
									break;
								}
								current = current.getParent();
							}
						} else {
							// For regular requests, exclude Auth Configurations hierarchy
							if (!CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS.equals(selectedItem.getValue().getName())) {
								rootTreeItem = selectedItem;
							}
						}
					} else if (selectedItem.getParent() != null) {
						TreeItem<ApiModelVo> parentItem = selectedItem.getParent();
						if (parentItem.getValue() != null) {
							if (isAuthConfig) {
								// Check if parent or ancestor is Auth Configurations
								TreeItem<ApiModelVo> current = parentItem;
								while (current != null && current.getValue() != null) {
									if (CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS.equals(current.getValue().getName())) {
										rootTreeItem = parentItem;
										break;
									}
									current = current.getParent();
								}
							} else {
								// For regular requests, exclude Auth Configurations hierarchy
								if (!CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS.equals(parentItem.getValue().getName())) {
									rootTreeItem = parentItem;
								}
							}
						}
					}
				}
				
				// Default to appropriate root group if no valid parent found
				if (rootTreeItem == null) {
					rootTreeItem = treeItemMap.get(CommonUtil.getGroupId(defaultGroupName, apiGroupVoMap));
				}
				
				rootTreeItem.getChildren().add(newTreeItem);
				
				// Set the groupId for the new item
				if (rootTreeItem.getValue() != null) {
					apiModelVo.setGroupId(rootTreeItem.getValue().getId());
				}
			}
			
			// Ensure parent tree item is expanded so the new item is visible
			if (rootTreeItem != null) {
				rootTreeItem.setExpanded(true);
			}
			
			treeView.getSelectionModel().select(newTreeItem);
			tab.selectedProperty().addListener((observable, oldValue, newValue) -> {
				if (newValue) {
					treeView.getSelectionModel().select(newTreeItem);
				}
			});
		}
		
		tab.setOnCloseRequest(event -> handleTabClose(tab, event));
		
		tabPane.getSelectionModel().select(tab);
		Platform.runLater(() -> {
			if (tab.getContent() != null) {
				tab.getContent().requestFocus();
			}
		});
		
		return tab;
	}
	
	private void handleTabClose(Tab tab, Event event) {
		ApiModelVo closeTabApiModelVo = apiModelVoMap.get(tab.getId());
		boolean isSave = (closeTabApiModelVo != null && closeTabApiModelVo.isModified())
				|| tab.getText().endsWith(" *");
		
		if (isSave) {
			Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
					"This tab has unsaved changes. Save before closing?", 
					ButtonType.YES, ButtonType.NO, ButtonType.CANCEL);
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
	}
	
	/**
	 * Opens a tab by ID, selecting it if already open or creating it if not
	 */
	public void openTab(String id) {
		boolean isOpenTabFound = false;
		for (Tab tab : tabPane.getTabs()) {
			if (tab.getId().equals(id)) {
				tabPane.getSelectionModel().select(tab);
				isOpenTabFound = true;
				break;
			}
		}
		if (!isOpenTabFound) {
			// Check if it's an auth config or API request
			ApiModelVo apiModelVo = apiModelVoMap.get(id);
			if (apiModelVo != null && apiModelVo.isAuthConfig()) {
				addNewAuthConfigTab(id, false, false);
			} else {
				addNewTab(id, false, false);
			}
		}
	}
	
	/**
	 * Marks a tab as modified (adds asterisk)
	 */
	public void markTabAsModified(Tab tab) {
		String currentText = tab.getText();
		if (!currentText.endsWith(" *")) {
			tab.setText(currentText + " *");
			ApiModelVo apiModelVo = apiModelVoMap.get(tab.getId());
			if (apiModelVo != null) {
				apiModelVo.setModified(true);
			}
		}
	}
	
	/**
	 * Marks the parent tab of a container as modified
	 */
	public void markParentTabAsModified(VBox container) {
		for (Tab tab : tabPane.getTabs()) {
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
	
	/**
	 * Saves the currently selected tab
	 */
	public void saveCurrentTab() {
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
		if (selectedTab != null) {
			saveTab(selectedTab);
		}
	}
	
	/**
	 * Saves all tabs
	 */
	public void saveAllTabs() {
		for (Tab tab : new ArrayList<>(tabPane.getTabs())) {
			saveTab(tab, false);
		}
		if (onSaveCallback != null) {
			onSaveCallback.run();
		}
	}
	
	/**
	 * Checks if any tabs have unsaved changes
	 */
	public boolean hasUnsavedTabs() {
		for (Tab tab : tabPane.getTabs()) {
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
	
	/**
	 * Closes the currently selected tab
	 */
	public void closeCurrentTab() {
		Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
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
	
	public void saveTab(Tab tab) {
		saveTab(tab, true);
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
					extractTabData(mainLayout, apiModelVo);
				}
				apiModelVo.setModified(false);
			}
			
			if (isWriteToFile && onSaveCallback != null) {
				onSaveCallback.run();
			}
		}
	}
	
//	@SuppressWarnings("unchecked")
//	private void extractAuthConfigTabData(VBox mainLayout, ApiModelVo apiModelVo) {
//		// Auth config data is already saved in real-time via listeners
//		// This method is here for consistency and future enhancements
//	}
	
	@SuppressWarnings("unchecked")
	private void extractTabData(VBox mainLayout, ApiModelVo apiModelVo) {
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

//				Tab authTab = topTabs.getTabs().get(0);
//				VBox authTabContainer = (VBox) authTab.getContent();
//				for (Node node : authTabContainer.getChildren()) {
//					if (node instanceof ComboBox) {
//						ComboBox<String> authComboBox = (ComboBox<String>) node;
//						System.out.println("node " + authComboBox.getValue());
//					}
//				}
				
				Tab paramsTab = topTabs.getTabs().get(1);
				ScrollPane paramsScrollPane = (ScrollPane) paramsTab.getContent();
				VBox paramsContainer = (VBox) paramsScrollPane.getContent();
				Map<String, String> params = extractParamsFromContainer(paramsContainer);
				apiModelVo.setParams(params);
				
				Tab headersTab = topTabs.getTabs().get(2);
				ScrollPane headersScrollPane = (ScrollPane) headersTab.getContent();
				VBox headersContainer = (VBox) headersScrollPane.getContent();
				Map<String, String> headers = extractParamsFromContainer(headersContainer);
				apiModelVo.setHeaders(headers);
				
				Tab bodyTab = topTabs.getTabs().get(3);
				TextArea bodyTextArea = (TextArea) bodyTab.getContent();
				apiModelVo.setBody(bodyTextArea.getText());
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
	
	public String truncateTabTitle(String title) {
		if (title == null) {
			return "";
		}
		String cleanTitle = title.endsWith(" *") ? title.substring(0, title.length() - 2) : title;
		if (cleanTitle.length() > 15) {
			return cleanTitle.substring(0, 12) + "...";
		}
		return cleanTitle;
	}
	
	/**
	 * Finds a tree item by its ApiModelVo ID
	 */
	private TreeItem<ApiModelVo> findTreeItemById(String id) {
		if (id == null) {
			return null;
		}
		
		for (TreeItem<ApiModelVo> item : treeItemMap.values()) {
			if (item.getValue() != null && id.equals(item.getValue().getId())) {
				return item;
			}			
		}
		return null;
	}
	
	/**
	 * Finds a tree item by its ApiModelVo ID in a specific parent tree
	 */
	private TreeItem<ApiModelVo> findTreeItemById(String id, TreeItem<ApiModelVo> parentTree) {
		if (id == null) {
			return null;
		}
		for (TreeItem<ApiModelVo> item : parentTree.getChildren()) {
			if (item.getValue() != null && id.equals(item.getValue().getId())) {
				return item;
			}
		}
		return null;
	}
	
	/**
	 * Interface for creating tab content
	 */
	public interface TabContentFactory {
		VBox createTabContent(String tabId);
	}
}
