module org.dubytube.dubytube {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.dubytube.dubytube.viewController to javafx.fxml;
    opens org.dubytube.dubytube.domain to javafx.base; // para PropertyValueFactory

    exports org.dubytube.dubytube;
}
