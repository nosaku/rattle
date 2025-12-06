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

import com.nosaku.rattle.vo.ApiModelVo;

import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.StringConverter;

public class ContextMenuTreeCell extends TextFieldTreeCell<ApiModelVo> {
	private ContextMenu menu = new ContextMenu();

	public ContextMenuTreeCell(App app) {
		super(createConverter());

		MenuItem renameItem = new MenuItem("Rename");
		menu.getItems().add(renameItem);
		renameItem.setOnAction(event -> {
			getTreeView().setEditable(true);
			startEdit();
		});
		MenuItem cloneItem = new MenuItem("Clone");
		menu.getItems().add(cloneItem);
		cloneItem.setOnAction(event -> {
			app.cloneTreeItem(getTreeItem());
		});
		MenuItem deleteItem = new MenuItem("Delete");
		menu.getItems().add(deleteItem);
		deleteItem.setOnAction(event -> {
			app.deleteTreeItem(getTreeItem());
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
			// Only show context menu for child items, not for the root/parent item
			if (getTreeItem() != null && getTreeItem().getParent() != null && !empty) {
				setContextMenu(menu);
			} else {
				setContextMenu(null);
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
