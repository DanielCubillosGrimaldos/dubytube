module org.dubytube.dubytube {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires com.google.gson;

    opens org.dubytube.dubytube.viewController to javafx.fxml;
    opens org.dubytube.dubytube.domain to javafx.base, com.google.gson;
    opens org.dubytube.dubytube.repo to com.google.gson;

    exports org.dubytube.dubytube;
    exports org.dubytube.dubytube.domain;
    exports org.dubytube.dubytube.repo;
    exports org.dubytube.dubytube.viewController;
    exports org.dubytube.dubytube.ds;
    exports org.dubytube.dubytube.services;
}
