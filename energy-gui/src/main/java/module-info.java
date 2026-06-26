module at.fhtw.energygui {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.fasterxml.jackson.databind;
    requires java.net.http;
    requires at.fhtw.energycontract;

    opens at.fhtw.energygui to javafx.fxml;
    exports at.fhtw.energygui;
}