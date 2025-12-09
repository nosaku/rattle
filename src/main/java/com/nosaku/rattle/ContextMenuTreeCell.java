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

import com.nosaku.rattle.util.CommonConstants;
import com.nosaku.rattle.vo.ApiModelVo;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

public class ContextMenuTreeCell extends TextFieldTreeCell<ApiModelVo> {
	private ContextMenu childMenu = new ContextMenu();
	private ContextMenu authChildMenu = new ContextMenu();
	private ContextMenu authParentMenu = new ContextMenu();
	private ContextMenu groupMenu = new ContextMenu();
	private App app;

	public ContextMenuTreeCell(App app) {
		super(createConverter());
		this.app = app;

		// Child menu items (for API requests)
		MenuItem renameItem = new MenuItem("Rename");
		childMenu.getItems().add(renameItem);
		renameItem.setOnAction(event -> {
			getTreeView().setEditable(true);
			startEdit();
		});
		MenuItem cloneItem = new MenuItem("Clone");
		childMenu.getItems().add(cloneItem);
		cloneItem.setOnAction(event -> {
			app.cloneTreeItem(getTreeItem());
		});
		MenuItem deleteItem = new MenuItem("Delete");
		childMenu.getItems().add(deleteItem);
		deleteItem.setOnAction(event -> {
			app.deleteTreeItem(getTreeItem());
		});

		// Auth config child menu items
		MenuItem authRenameItem = new MenuItem("Rename");
		authChildMenu.getItems().add(authRenameItem);
		authRenameItem.setOnAction(event -> {
			getTreeView().setEditable(true);
			startEdit();
		});
		MenuItem authCloneItem = new MenuItem("Clone");
		authChildMenu.getItems().add(authCloneItem);
		authCloneItem.setOnAction(event -> {
			app.cloneTreeItem(getTreeItem());
		});
		MenuItem clearTokenItem = new MenuItem("Clear Token");
		authChildMenu.getItems().add(clearTokenItem);
		clearTokenItem.setOnAction(event -> {
			if (getItem() != null && getItem().getId() != null) {
				app.clearAuthToken(getItem().getId());
			}
		});
		MenuItem authDeleteItem = new MenuItem("Delete");
		authChildMenu.getItems().add(authDeleteItem);
		authDeleteItem.setOnAction(event -> {
			app.deleteTreeItem(getTreeItem());
		});

		// Auth parent menu items
		MenuItem newAuthConfigItem = new MenuItem("New Auth Configuration");
		authParentMenu.getItems().add(newAuthConfigItem);
		newAuthConfigItem.setOnAction(event -> {
			app.addNewAuthConfig();
		});
		MenuItem clearAllTokensItem = new MenuItem("Clear All Tokens");
		authParentMenu.getItems().add(clearAllTokensItem);
		clearAllTokensItem.setOnAction(event -> {
			app.clearAllAuthTokens();
		});

		// Group menu items (for regular groups, not Auth Configurations)
		MenuItem addGroupItem = new MenuItem("Add Group");
		groupMenu.getItems().add(addGroupItem);
		addGroupItem.setOnAction(event -> {
			if (getTreeItem() != null) {
				app.addSubGroup(getTreeItem());
			}
		});
		MenuItem renameGroupItem = new MenuItem("Rename");
		groupMenu.getItems().add(renameGroupItem);
		renameGroupItem.setOnAction(event -> {
			getTreeView().setEditable(true);
			startEdit();
		});

		setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
					if (getItem() != null && getItem().getId() != null) {
						app.openTab(getItem().getId());
					}
				}
			}
		});
	}

	@Override
	public void updateItem(ApiModelVo item, boolean empty) {
		super.updateItem(item, empty);

		if (!isEditing()) {
			if (getTreeItem() != null && !empty && item != null) {
				// Check if this is a group (parent) item by checking if it's in the app's treeItemMap
				boolean isGroup = app.isGroupTreeItem(getTreeItem());
				
				// Apply bold styling to group items
				if (isGroup) {
					setStyle("-fx-font-weight: bold;");
				} else {
					setStyle("");
				}
				
				// Check if this is the auth parent item
				if (CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS.equals(item.getName())
						&& getTreeItem().getParent() != null && getTreeItem().getParent().getValue() != null
						&& "".equals(getTreeItem().getParent().getValue().getName())) {
					setContextMenu(authParentMenu);
				}
				// Check if this is an auth config child
				else if (item.isAuthConfig() && getTreeItem().getParent() != null
						&& getTreeItem().getParent().getValue() != null
						&& CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS
								.equals(getTreeItem().getParent().getValue().getName())) {
					setContextMenu(authChildMenu);
				}
				// Check if this is a regular group (not Auth Configurations)
				else if (isGroup && !CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS.equals(item.getName())) {
					setContextMenu(groupMenu);
				}
				// Show child menu for regular API request child items
				else if (getTreeItem().getParent() != null && getTreeItem().getParent().getValue() != null
						&& !"".equals(getTreeItem().getParent().getValue().getName())
						&& !CommonConstants.GROUP_NAME_AUTH_CONFIGURATIONS
								.equals(getTreeItem().getParent().getValue().getName())) {
					setContextMenu(childMenu);
				} else {
					setContextMenu(null);
				}
			} else {
				setContextMenu(null);
				setStyle("");
			}
			getTreeView().setEditable(false);
		}
	}

	@Override
	public void startEdit() {
		if (getTreeItem() != null && !isEmpty()) {
			super.startEdit();
		}
	}

	private static StringConverter<ApiModelVo> createConverter() {
		return new StringConverter<ApiModelVo>() {
			@Override
			public String toString(ApiModelVo object) {
				return object.getName();
			}

			@Override
			public ApiModelVo fromString(String string) {
				ApiModelVo apiModelVo = new ApiModelVo();
				apiModelVo.setName(string);
				return apiModelVo;
			}
		};
	}
}
