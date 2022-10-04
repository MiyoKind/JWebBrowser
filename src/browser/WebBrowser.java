package browser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
 * @author Mikhail Shevchenko
 */
public class WebBrowser extends Application {

    @Override
    public void start(final Stage primaryStage) {

        final AnchorPane root = new AnchorPane();
        final TabPane tabPane = new TabPane();
        tabPane.setPrefSize(1325, 768);
        final Button addButton = new Button(); //adding tab button
        ImageView addImg = new ImageView(new Image("rsc/img/newTab.png"));
        addImg.setFitHeight(20);
        addImg.setFitWidth(20);
        addButton.setGraphic(addImg);
        final HBox topPanel = new HBox();
        topPanel.getChildren().addAll(tabPane, addButton);

        AnchorPane.setTopAnchor(tabPane, 40.0);
        AnchorPane.setLeftAnchor(tabPane, 5.0);
        AnchorPane.setRightAnchor(tabPane, 5.0);
        AnchorPane.setTopAnchor(addButton, 10.0);
        AnchorPane.setLeftAnchor(addButton, 1330.0);

        BrowserTab.CreateWelcomeTab(tabPane);

        addButton.setOnAction(event -> {
            BrowserTab browserTab = BrowserTab.createNewTab();
            tabPane.getTabs().add(browserTab);
            tabPane.getSelectionModel().select(browserTab);
        });

        root.getChildren().add(topPanel);

        final Scene scene = new Scene(root, 1365, 768, Color.DIMGRAY);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Web Browser");
        primaryStage.getIcons().add(new Image("rsc/img/icon.jpg"));
        primaryStage.show();

    }

    public static void main(String[] args) {
        launch(args);
    }

}