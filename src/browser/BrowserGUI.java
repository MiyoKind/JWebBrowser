package browser;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.web.WebView;

/**
 * Класс BrowserGUI определяет основные элементы графического интерфейса приложения
 * Поля:
 * 1). urlTextField - адресная строка браузера
 * 2). buttonBack - кнопка возвращения на предыдущую загруженную страницу
 * 3). buttonForward - кнопка перехода на следующую страницу (если есть)
 * 4). buttonRefresh - кнопка обновления страницы
 * 5). buttonHome - кнопка для перехода на домашнюю страницу
 * 6). goButton - кнопка для перехода по адресу, написанному в адресной строке
 * 7). progressBar - ползунок, идентифицирующий прогресс загрузки страницы
 * 8). webView - отображение страницы
 */

public class BrowserGUI extends Node {

    private final Button buttonBack;

    private final Button buttonForward;

    private final Button buttonRefresh;

    private final Button buttonHome;

    private final Button goButton;

    private final ProgressBar progressBar;

    private WebView webView;

    /**
     * Конструктор класса графического интерфейса приложения
     */
    public BrowserGUI() {

        this.buttonBack = new Button();
        this.buttonForward = new Button();
        this.buttonRefresh = new Button();
        this.buttonHome = new Button();
        this.goButton = new Button();
        this.webView = new WebView();
        this.progressBar = new ProgressBar();

    }

    /**
     * Геттеры
     */

    public Button getButtonBack() {
        return buttonBack;
    }

    public Button getButtonForward() {
        return buttonForward;
    }

    public Button getButtonRefresh() {
        return buttonRefresh;
    }

    public Button getButtonHome() {
        return buttonHome;
    }

    public Button getGoButton() {
        return goButton;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public WebView getWebView() {
        return webView;
    }

}
