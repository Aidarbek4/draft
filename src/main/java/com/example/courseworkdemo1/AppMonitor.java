package com.example.courseworkdemo1;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinUser;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppMonitor extends Application {
    private interface User32 extends Library {
        User32 INSTANCE = Native.load("user32", User32.class);

        int EnumWindows(WinUser.WNDENUMPROC lpEnumFunc, int lParam);

        int GetWindowTextW(WinDef.HWND hWnd, char[] lpString, int nMaxCount);

        int GetWindowTextLengthW(WinDef.HWND hwnd);

        boolean IsWindowVisible(WinDef.HWND hwnd);
    }

    private ListView<String> appList;
    private Map<String, Long> appStartTimeMap;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("App Monitor");

        appList = new ListView<>();
        appStartTimeMap = new HashMap<>();
        updateAppList();
        VBox vBox = new VBox(appList);

        Scene scene = new Scene(vBox, 300, 200);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(event -> {
            // Выход из приложения
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();

        // Обновление списка приложений каждые 5 секунд
        new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Platform.runLater(this::updateAppList);
            }
        }).start();
    }

    private void updateAppList() {
        List<String> runningApps = getRunningApps();
        Platform.runLater(() -> {
            appList.getItems().setAll(runningApps);
            // Не обновляем базу данных
        });
    }

    private String getFormattedStartTime(String appName) {
        long startTime = appStartTimeMap.getOrDefault(appName, System.nanoTime());
        appStartTimeMap.put(appName, startTime); // Обновляем время при каждом обнаружении приложения

        long elapsedTime = System.nanoTime() - startTime;

        // Преобразуем время в формат HH:mm:ss
        long seconds = elapsedTime / 1_000_000_000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("(%02d:%02d:%02d)", hours % 24, minutes % 60, seconds % 60);
    }

    private List<String> getRunningApps() {
        List<String> windowTitles = new ArrayList<>();

        User32.INSTANCE.EnumWindows((hwnd, pointer) -> {
            int length = User32.INSTANCE.GetWindowTextLengthW(hwnd) + 1;
            char[] windowText = new char[length];
            User32.INSTANCE.GetWindowTextW(hwnd, windowText, length);
            String title = new String(windowText).trim();

            // Проверка видимости окна и игнорирование окон без названия или системных процессов
            if (isWindowVisible(hwnd) && !title.isEmpty() && !title.matches("\\d+")) {
                windowTitles.add(title + " " + getFormattedStartTime(title));
            }

            return true;
        }, 0);

        return windowTitles;
    }

    private boolean isWindowVisible(WinDef.HWND hwnd) {
        return User32.INSTANCE.IsWindowVisible(hwnd);
    }
}
