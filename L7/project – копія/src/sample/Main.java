package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import sample.controllers.MainController;


/**
 * ������: �������� �������� ��� ���� ������ USB � � �������� ���� �� ������ ����.
 * ���� ����������� ����� B64.
 * ���� - MD5. ��������� ��� �� ��������� �� ������� ����� �����.
 * ������ -  DES. ������ ������� �� 2 � ����� ������� �������� DES. � ���� ������� ������������.
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
