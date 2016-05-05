package sample.controllers;

import com.codeminders.hidapi.ClassPathLibraryLoader;
import com.codeminders.hidapi.HIDDeviceInfo;
import com.codeminders.hidapi.HIDManager;
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
import sample.Main;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable{

    // TODO hide file on USB (Hidden flag works bad)

    private Alert alertBox;

    // values (login/password - admin) just for example, not usable in application

    // login = 'admin'
    private String login = "21232f297a57a5a743894a0e4a801fc3";

    // password = 'admin'
    private String password = "9rYl92uo/kg=";

    // Example: 2016-04-20 12:05:16.338
    private String timestamp;

    private Properties localProperties;

    public static Stage mainStage;

    private static File[] initListOfDevices = File.listRoots();

    @FXML
    private Label helloLabel;

    @FXML
    private Button passwordButton;


    public static File findFileOnUSB(){
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


    public void showNewPasswordForm(){
        javafx.application.Platform.runLater(() -> {
            Parent root = null;
            Stage stage = new Stage();
            try {
                root = FXMLLoader.load(getClass().getResource("../views/newPasswordForm.fxml"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            Scene scene = new Scene(root, 400, 200);
            stage.setScene(scene);
            stage.setResizable(false);
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(mainStage.getScene().getWindow());
            NewPasswordController.currentStage = stage;
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

    private void waitForNewDevice(){

        switch (getOperatingSystemName()){
            case "WINdOWS":
                windowsHandler();
                break;

            case "Mac OS X":
                macHandler();
                break;
        }

    }

    private void createAndWriteTimestamp(){
        try {
            Properties properties = getPropertiesByFile(findFileOnUSB());
            Calendar calendar = Calendar.getInstance();
            Date now = calendar.getTime();
            Timestamp timestamp = new Timestamp(now.getTime());
            properties.setProperty("timestamp", timestamp.toString());

            // Save data on USB
            OutputStream outputStream = new FileOutputStream(findFileOnUSB());
            properties.store(outputStream, "");

            // Save data on Local file
            File file = new File(new File("").getAbsoluteFile() + "\\src\\sample\\credentials.properties");
            outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getOperatingSystemName(){
        return System.getProperty("os.name");
    }

    private void windowsHandler(){
        javafx.application.Platform.runLater(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if ( File.listRoots().length > initListOfDevices.length ) {

                    if ( findFileOnUSB() != null ) {
                        alertBox = new Alert(Alert.AlertType.INFORMATION);
                        alertBox.setHeaderText("File found!");
                        alertBox.setContentText("");
                        alertBox.initOwner(mainStage.getScene().getWindow());
                        alertBox.showAndWait();

                        try {
                            Properties properties = getPropertiesByFile(findFileOnUSB());
                            setLogin(properties.getProperty("login"));
                            setPassword(properties.getProperty("password"));
                            setTimestamp(properties.getProperty("timestamp"));

                            if ( properties.get("login") != null )

                                if ( properties.get("login").equals(login) &&
                                        properties.get("password").equals(password)
                                        /*properties.get("timestamp").equals(timestamp)*/ ) {

                                    helloLabel.setText("Welcome user!");
                                    passwordButton.setVisible(true);
                                    createAndWriteTimestamp();

                                } else {
                                    alertBox.setAlertType(Alert.AlertType.ERROR);
                                    alertBox.setHeaderText("Not valid security file");
                                    alertBox.setContentText("");
                                    alertBox.initOwner(mainStage.getScene().getWindow());
                                    alertBox.showAndWait();
                                }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        break;
                    }

                } else if ( File.listRoots().length < initListOfDevices.length ) {
                    initListOfDevices = File.listRoots();
                }

            }
        });


    }

    private void macHandler(){


        javafx.application.Platform.runLater(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                findDeviceOnMac();

            }
        });
    }

    private void findDeviceOnMac(){

        try {
            ClassPathLibraryLoader.loadNativeHIDLibrary();
            HIDManager hidManager = HIDManager.getInstance();
            HIDDeviceInfo[] infos = hidManager.listDevices();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
