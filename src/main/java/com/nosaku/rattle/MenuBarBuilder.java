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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;

/**
 * Builder class for creating the application menu bar
 */
public class MenuBarBuilder {
	
	private final MenuCallbacks callbacks;
	
	public MenuBarBuilder(MenuCallbacks callbacks) {
		this.callbacks = callbacks;
	}
	
	/**
	 * Builds and returns the complete menu bar
	 * 
	 * @return The configured MenuBar
	 */
	public MenuBar build() {
		MenuBar menuBar = new MenuBar();
		menuBar.getMenus().add(createFileMenu());
		menuBar.getMenus().add(createSettingsMenu());
		menuBar.getMenus().add(createHelpMenu());
		return menuBar;
	}
	
	private Menu createFileMenu() {
		Menu fileMenu = new Menu("_File");
		fileMenu.setMnemonicParsing(true);
		
		MenuItem newRequestMenuItem = new MenuItem("New Reques_t");
		newRequestMenuItem.setMnemonicParsing(true);
		newRequestMenuItem.setOnAction(e -> callbacks.onNewRequest());
		newRequestMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.T, KeyCombination.CONTROL_DOWN));
		
		MenuItem newGroupMenuItem = new MenuItem("New _Group");
		newGroupMenuItem.setMnemonicParsing(true);
		newGroupMenuItem.setOnAction(e -> callbacks.onNewGroup());
		newGroupMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.G, KeyCombination.CONTROL_DOWN));
		
		MenuItem saveMenuItem = new MenuItem("_Save");
		saveMenuItem.setMnemonicParsing(true);
		saveMenuItem.setOnAction(e -> callbacks.onSave());
		saveMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN));
		
		MenuItem saveAllMenuItem = new MenuItem("S_ave All");
		saveAllMenuItem.setMnemonicParsing(true);
		saveAllMenuItem.setOnAction(e -> callbacks.onSaveAll());
		saveAllMenuItem.setAccelerator(
				new KeyCodeCombination(KeyCode.S, KeyCombination.CONTROL_DOWN, KeyCombination.SHIFT_DOWN));
		
		MenuItem exitMenuItem = new MenuItem("E_xit");
		exitMenuItem.setMnemonicParsing(true);
		exitMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN));
		exitMenuItem.setOnAction(e -> Platform.exit());
		
		fileMenu.getItems().addAll(newRequestMenuItem, newGroupMenuItem, saveMenuItem, saveAllMenuItem, exitMenuItem);
		return fileMenu;
	}
	
	private Menu createSettingsMenu() {
		Menu settingsMenu = new Menu("_Settings");
		settingsMenu.setMnemonicParsing(true);
		
		MenuItem settingsMenuItem = new MenuItem("_Preferences");
		settingsMenuItem.setMnemonicParsing(true);
		settingsMenuItem.setOnAction(e -> callbacks.onProxySettings());
		settingsMenuItem.setAccelerator(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.CONTROL_DOWN));
		
		settingsMenu.getItems().add(settingsMenuItem);
		return settingsMenu;
	}
	
	private Menu createHelpMenu() {
		Menu helpMenu = new Menu("_Help");
		helpMenu.setMnemonicParsing(true);
		
		MenuItem aboutMenuItem = new MenuItem("_About");
		aboutMenuItem.setMnemonicParsing(true);
		aboutMenuItem.setOnAction(e -> callbacks.onAbout());
		
		helpMenu.getItems().add(aboutMenuItem);
		return helpMenu;
	}
	
	/**
	 * Interface for menu action callbacks
	 */
	public interface MenuCallbacks {
		void onNewRequest();
		void onSave();
		void onSaveAll();
		void onNewGroup();
		void onProxySettings();
		void onAbout();
	}
}
