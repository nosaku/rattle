# Rattle



## Getting started

Requires Java 21+

Download JafaFX version: javafx-sdk-21.0.9

Use below VM arguments when running App.java:

--module-path "{SDK folder}\lib"
--add-modules javafx.controls,javafx.fxml
--add-exports javafx.base/com.sun.javafx.event=ALL-UNNAMED

Use below maven goals to package executable JARs:

clean package

To run the native Windows executeble you need "Microsoft Visual C++ Redistributable".
