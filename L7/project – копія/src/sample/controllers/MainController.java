package sample.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import sample.Main;

import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable{

    private Alert alertBox;

    // login = 'admin'
    private String login = "21232f297a57a5a743894a0e4a801fc3";

    // password = 'admin'
    // Mode: electronic codebook
    // Output stream encode: Base64
    private String password = "9rYl92uo/kg=";

    private Properties localProperties;

    public static Stage mainStage;

    private static File[] initListOfDevices = File.listRoots();

    @FXML
    private Label helloLabel;

    @FXML
    private Button passwordButton;


    public static File findFile(){
        File[] paths;
        FileSystemView fileSystemView = FileSystemView.getFileSystemView();

        // Returns pathnames for files and directory
        paths = File.listRoots();

        for (File currentDrive : paths){
            if ( currentDrive.listFiles() != null )
                for ( File currentFile : currentDrive.listFiles() )
                    if ( currentFile.getName().equals("security.properties") ){
                        return currentFile;
                    }

        }
        return null;
    }

    private Properties getPropertiesByFile(File file) throws IOException {
        Properties properties = new Properties();
        InputStream inputStream = new FileInputStream(file);

        properties.load(inputStream);

        return properties;
    }

    private void setPropertiesFile() throws IOException {
        String filename = "credentials.properties";
        Properties properties = new Properties();

        InputStream inputStream = Main.class.getResourceAsStream(filename);

        properties.load(inputStream);
    }

    private String  MD5Encrypt(){
        return "";
    }

    private String DESEncrypt(){
        return "";
    }

    private void writeDataToPropertiesFile(){

    }


    public void setNewPassword(){
        javafx.application.Platform.runLater(()->{
            Parent root = null;
            Stage stage = new Stage();
            try{
                root = FXMLLoader.load(getClass().getResource("../views/newPasswordForm.fxml"));
            } catch (IOException e){
                e.printStackTrace();
            }

            Scene scene = new Scene(root, 400, 200);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainStage.getScene().getWindow());
            stage.showAndWait();
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainStage.setOnShown(event -> waitForNewDevice());

        try {
            setPropertiesFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void waitForNewDevice(){
        javafx.application.Platform.runLater(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if ( File.listRoots().length > initListOfDevices.length ) {

                    if ( findFile() != null ){
                        alertBox = new Alert(Alert.AlertType.INFORMATION);
                        alertBox.setHeaderText("File found!");
                        alertBox.setContentText("");
                        alertBox.initOwner(mainStage.getScene().getWindow());
                        alertBox.showAndWait();

                        try {
                            Properties properties = getPropertiesByFile(findFile());
                            setLogin(properties.getProperty("login"));

                            if ( properties.get("login").equals(login) /*&& properties.get("password").equals(password)*/ ){
                                helloLabel.setText("Welcome user!");
                                passwordButton.setVisible(true);
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    }

                } else if (File.listRoots().length < initListOfDevices.length) {
                    initListOfDevices = File.listRoots();
                }

            }
        });
    }

    public void setLogin(String login) {
        this.login = login;
    }
}
