package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controllers.MainController;


/**
 * Задача: створити програму яка буде читати USB і в заданому місці має знайти файл.
 * Файл прокручений через B64.
 * Логін - MD5. Перевірити хеш на співпадіння із готовою базою даних.
 * Пароль -  DES. Пароль поділити на 2 і обидві частини програти DES. А потім зробити конкатенацію.
 */

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        MainController.mainStage = primaryStage;
        Parent root = FXMLLoader.load(getClass().getResource("views/main.fxml"));
        primaryStage.setTitle("Secure Log-In");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}
