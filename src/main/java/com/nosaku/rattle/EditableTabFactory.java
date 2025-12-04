package com.nosaku.rattle;

import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.Tab;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * A factory class to create JavaFX Tabs with in-place name editing capabilities.
 */
public class EditableTabFactory {

    /**
     * Creates a new Tab with the given title that can be edited in-place
     * via a double-click event on the tab header.
     *
     * @param title The initial title for the tab.
     * @return The created editable Tab.
     */
    public static Tab createEditableTab(String title) {
        Tab tab = new Tab(title);

        Label label = new Label(title);
        TextField textField = new TextField(title);
        HBox tabHeader = new HBox(label, textField);

        // label.setTextOverrun(OverrunStyle.ELLIPSIS);
        // HBox.setHgrow(label, Priority.ALWAYS);
        // label.setMaxWidth(Double.POSITIVE_INFINITY);

        // Configure visibility: Start with Label visible, TextField hidden
        label.setVisible(true);
        textField.setVisible(false);
        textField.setManaged(false); // Does not take up space when invisible

        // Set the HBox as the sole graphic of the tab
        tab.setGraphic(tabHeader);
        tab.setText(null); // Clear the default text so only the graphic appears

        // Switch to TextField for editing on double click
        label.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                switchMode(true, label, textField);
                textField.requestFocus();
                textField.selectAll();
            }
        });

        // Handle focus loss (commit edit)
        textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                commitEdit(tab, label, textField);
            }
        });

        // Handle Enter and Escape key presses
        textField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                commitEdit(tab, label, textField);
            } else if (event.getCode() == KeyCode.ESCAPE) {
                // Revert and switch back to Label
                switchMode(false, label, textField);
                textField.setText(label.getText()); // Restore original text
            }
        });

        return tab;
    }

    /**
     * Switches between visible Label and visible TextField.
     */
    private static void switchMode(boolean isEditing, Label label, TextField textField) {
        label.setVisible(!isEditing);
        label.setManaged(!isEditing);
        textField.setVisible(isEditing);
        textField.setManaged(isEditing);
    }

    /**
     * Commits the edit, updates the label text, and switches back to the label.
     */
    private static void commitEdit(Tab tab, Label label, TextField textField) {
        String newText = textField.getText().trim();
        if (!newText.isEmpty()) {
            label.setText(newText);
            // Optional: update the tab's underlying text property too
            // tab.setText(newText);
        }
        switchMode(false, label, textField);
    }
}
