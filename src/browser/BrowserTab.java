package browser;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebHistory;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import net.sf.image4j.codec.ico.ICODecoder;
import org.apache.commons.validator.routines.UrlValidator;

import java.awt.image.BufferedImage;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class BrowserTab extends DraggableTab {

    /**
     * Схемы возможных протоколов для проверки URL адреса
     */
    private String[] schemes = {"http", "https", "ftp"};

    /**
     * Конструктор вкладки браузера
     */
    public BrowserTab(String text) {
        super(text);
    }

    /**
     * Метод для создания новой вкладки
     */
    public static BrowserTab createNewTab() {
        BrowserTab tab = new BrowserTab("New Tab");
        tab.setClosable(true);
        tab.setDetachable(true);

        BrowserGUI gui = new BrowserGUI();

        WebView webView = gui.getWebView();
        webView.setContextMenuEnabled(false);
        createContextMenu(webView);
        WebEngine webEngine = webView.getEngine();
        /* Установка имени клиента для подключения к вебсайтам */
        webEngine.setUserAgent("JFXWeb Browser by Miyo - AppleWebKit/555.99");

        Button btnGo = gui.getGoButton();
        ImageView goImg = new ImageView(new Image("rsc/img/btnGo.png"));
        goImg.setFitHeight(20);
        goImg.setFitWidth(20);
        btnGo.setGraphic(goImg);

        URLTextField urlTextField = new URLTextField();
        urlTextField.setUrlTextField(new TextField());

        //Установка URL адреса в дресную строку браузера при переходе на сайт
        webEngine.locationProperty().addListener((observableValue, oldValue, newValue) -> urlTextField.getUrlTextField().setText(newValue));

        //Обработка ошибки, если не получилось загрузить страницу
        webEngine.getLoadWorker().exceptionProperty().addListener((observableValue, oldValue, nextValue) ->
        {
            if (webEngine.getLoadWorker().getState() == Worker.State.FAILED) {
                System.err.println("Some errors!");
            }
        });

        /**
         * Анимация всплывания окна отображения веб-страницы:
         */

        webEngine.getLoadWorker().stateProperty().addListener((ov, old, next) -> {
            if (next == Worker.State.SCHEDULED) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(1_000), webView);
                fadeOut.setToValue(0.0);
                fadeOut.play();
            } else if (next == Worker.State.SUCCEEDED) {
                FadeTransition fadeIn = new FadeTransition(Duration.millis(1_000), webView);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        });

        /**
         * Привязывание фавиконок (иконок вебсайтов) на каждую создаваемую вкладку:
         */

        final ImageView favIconImageView = new ImageView();
        favIconImageView.setFitWidth(15);
        favIconImageView.setFitHeight(15);
        favIconImageView.setSmooth(true);
        webEngine.getLoadWorker().stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                try {
                    if ("about:blank".equals(webEngine.getLocation()))
                        return;

                    //Распознавание полного URL адреса сайта для нахождения фавиконки
                    String favIconFullURL = getHostName(webEngine.getLocation()) + "favicon.ico";

                    //Установка HTTP соединения
                    HttpURLConnection http = (HttpURLConnection) new URL(favIconFullURL).openConnection();
                    http.addRequestProperty("User-Agent", "Mozilla/5.0");
                    List<BufferedImage> img = ICODecoder.read(http.getInputStream());

                    //Установка фавиконки
                    favIconImageView.setImage(SwingFXUtils.toFXImage(img.get(0), null));

                } catch (Exception exception) {
                    exception.printStackTrace();
                    favIconImageView.setImage(null);
                }
            }
        });

        //Смена названия вкладки, исходя из названия страницы
        webEngine.titleProperty().addListener((observable2, oldValue, newValue) -> {
            tab.setLabelText(newValue);
            HBox tabGraphic = new HBox();
            Label tabName = new Label(tab.getLabel().getText());
            HBox.setHgrow(tabName, Priority.ALWAYS); //must test
            tabGraphic.getChildren().addAll(favIconImageView, tabName);
            tab.setGraphic(tabGraphic);
        });

        /**
         * Загрузка страниц с помощью webEngine.load(url)
         */

        EventHandler<ActionEvent> goAction = event -> Platform.runLater(() -> {

            String url = urlTextField.getUrlTextField().getText();
            String tmp = toURL(url);
            //UrlValidator - объект для проверки URL адреса на корректность
            UrlValidator urlValidator = new UrlValidator(tab.schemes);

            //Проверка если адрес введён без указания протокола (полного адреса)
            if (tmp == null) {
                tmp = toURL("http://" + url);
            }

            //Проверка если введённое значение не является URL адресом. В таком случае совершаем поиск в Google
            if (urlValidator.isValid(tmp)) {
                webEngine.load(tmp);
            } else {
                webEngine.load("https://google.com/search?q=" + url);
            }
        });

        urlTextField.getUrlTextField().setOnAction(goAction);
        btnGo.setOnAction(goAction);

        /* Реализация загрузки файла с сайта */

        webEngine.locationProperty().addListener((observableValue, oldLoc, newLoc) -> {

            String downloadableExtension = null;  // todo I wonder how to find out from WebView which documents it could not process so that I could trigger a save as for them?
            //Возможные расширения файлов для скачивания
            String[] downloadableExtensions = {".doc", ".xls", ".zip", ".tgz", ".jar", ".mp3", ".txt", ".mp4", ".exe",
                    ".msi", ".pdf", ".docx", ".css", ".js", ".psd", ".svg", ".jpg", ".png", ".bmp", ".gif",
                    ".dmg", ".bat", ".dll", ".xml", ".xlsx", ".rar", ".7z", ".htm", ".avi", ".torrent", ".bin", ".iso",
                    ".ini", ".midi", ".ppt", ".pptx", ".wav", ".sai"};
            for (String ext : downloadableExtensions) {
                /*Если адрес заканчивается на одно из вышеперечисленных расширений,
                то присваеваем его к объекту downloadableExtension */
                if (newLoc.endsWith(ext)) {
                    downloadableExtension = ext;
                    break;
                }
            }
            if (downloadableExtension != null) {
                // Создлание всплывающешго меню для сохранения файла на компьютер
                FileChooser chooser = new FileChooser();
                chooser.setTitle("Save " + newLoc);
                chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Downloadable File",
                        downloadableExtension));
                int filenameIdx = newLoc.lastIndexOf("/") + 1;
                if (filenameIdx != 0) {
                    File saveFile = chooser.showSaveDialog(webView.getScene().getWindow());
                    //Считываем файл с помощью потока байт из указанного URL адреса
                    if (saveFile != null) {
                        try (BufferedInputStream is = new BufferedInputStream(new URL(newLoc).openStream());
                             BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(saveFile))) {
                            int b = is.read();
                            while (b != -1) {
                                os.write(b);
                                b = is.read();
                            }
                            //saveFile.renameTo(new File(saveFile.getName() + downloadableExtension));
                        } catch (IOException e) {
                            System.out.println("Unable to save file: " + e);
                        }
                    }
                }
            }
        });

        ProgressBar progressBar = gui.getProgressBar();
        progressBar.setStyle("-fx-accent: red;");
        progressBar.progressProperty().bind(webEngine.getLoadWorker().progressProperty());
        progressBar.visibleProperty().bind(
                Bindings.when(progressBar.progressProperty().lessThan(0).or(
                                progressBar.progressProperty().isEqualTo(1)))
                        .then(false)
                        .otherwise(true));
        progressBar.managedProperty().bind(progressBar.visibleProperty());
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setMinHeight(Double.MAX_VALUE);

        Button btnHome = gui.getButtonHome();
        btnHome.setPrefSize(10, 10);
        ImageView homeImg = new ImageView(new Image("rsc/img/homepage.png"));
        homeImg.setFitHeight(20);
        homeImg.setFitWidth(20);
        btnHome.setGraphic(homeImg);
        btnHome.setOnAction(event -> webEngine.load("https://mirea.ru"));

        Button btnBack = gui.getButtonBack();
        btnBack.setPrefSize(10, 10);
        ImageView backImg = new ImageView(new Image("rsc/img/backward.png"));
        backImg.setFitHeight(20);
        backImg.setFitWidth(20);
        btnBack.setGraphic(backImg);
        btnBack.setOnAction(event -> goBack(webEngine.getHistory()));
        btnBack.disableProperty().bind(webEngine.getHistory().currentIndexProperty().isEqualTo(0));

        Button btnForward = gui.getButtonForward();
        btnForward.setPrefSize(10, 10);
        ImageView forwardImg = new ImageView(new Image("rsc/img/forward.png"));
        forwardImg.setFitHeight(20);
        forwardImg.setFitWidth(20);
        btnForward.setGraphic(forwardImg);
        btnForward.setOnAction(event -> goForward(webEngine.getHistory()));
        btnForward.disableProperty().bind(webEngine.getHistory().currentIndexProperty().greaterThanOrEqualTo(
                Bindings.size(webEngine.getHistory().getEntries()).subtract(1)));

        Button btnRefresh = gui.getButtonRefresh();
        btnRefresh.setPrefSize(10, 10);
        ImageView refreshImg = new ImageView(new Image("rsc/img/refresh.png"));
        refreshImg.setFitHeight(20);
        refreshImg.setFitWidth(20);
        btnRefresh.setGraphic(refreshImg);
        btnRefresh.setOnAction(event -> {
            if (!(webEngine.getHistory().getEntries().isEmpty()))
                webEngine.reload();
        });

        ScrollPane browserPane = new ScrollPane();
        browserPane.setFitToWidth(true);
        browserPane.setFitToHeight(true);

        VBox searchBar = new VBox();
        HBox hBox = new HBox(5);
        hBox.getChildren().setAll(btnHome, btnBack, btnForward, btnRefresh, urlTextField.getUrlTextField(), btnGo);
        HBox.setHgrow(urlTextField.getUrlTextField(), Priority.ALWAYS);
        searchBar.getChildren().setAll(hBox, progressBar);

        final VBox vBox = new VBox(5);
        browserPane.setContent(webView);
        vBox.getChildren().setAll(searchBar, browserPane);
        VBox.setVgrow(browserPane, Priority.ALWAYS);

        tab.setContent(vBox);

        tab.setOnCloseRequest(event -> webEngine.load(""));

        return tab;

    }

    private static void goBack(WebHistory history) {
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(() ->
                history.go(entryList.size() > 1
                        && currentIndex > 0
                        ? -1
                        : 0));
    }

    private static void goForward(WebHistory history) {
        ObservableList<WebHistory.Entry> entryList = history.getEntries();
        int currentIndex = history.getCurrentIndex();

        Platform.runLater(() ->
                history.go(entryList.size() > 1
                        && currentIndex < entryList.size() - 1
                        ? 1
                        : 0));
    }

    private static String getHostName(String urlInput) {
        try {
            URL url = new URL(urlInput);
            return url.getProtocol() + "://" + url.getHost() + "/";
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String toURL(String str) {
        try {
            return new URL(str).toExternalForm();
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    private static void createContextMenu(WebView webView) {
        ContextMenu contextMenu = new ContextMenu();
        MenuItem zoomIn = new MenuItem("Zoom In");
        zoomIn.setOnAction(event -> webView.setZoom(webView.getZoom() * 1.1));
        MenuItem zoomOut = new MenuItem("Zoom Out");
        zoomOut.setOnAction(event -> webView.setZoom(webView.getZoom() / 1.1));
        MenuItem stopLoading = new MenuItem("Stop Loading");
        stopLoading.setOnAction(event -> webView.getEngine().getLoadWorker().cancel());

        contextMenu.getItems().addAll(stopLoading, zoomIn, zoomOut);

        webView.setOnMousePressed(event -> {
            if (event.getButton() == MouseButton.SECONDARY)
                contextMenu.show(webView, event.getSceneX(), event.getSceneY());
            else
                contextMenu.hide();
        });

    }

    public static void CreateWelcomeTab(TabPane tb) {
        BrowserTab firstTab = new BrowserTab(" Welcome ");
        firstTab.setClosable(true);
        firstTab.setDetachable(true);
        WebView view = new WebView();
        WebEngine engine = view.getEngine();
        File f = new File("/Users/miyo/IdeaProjects/MyWeb Updated/src/rsc/WelcomePage/index.html");
        engine.load(f.toURI().toString());
        firstTab.setContent(view);
        tb.getTabs().add(firstTab);
        tb.getSelectionModel().select(firstTab);
    }

}