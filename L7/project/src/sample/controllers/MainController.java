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
import sample.Main;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.ByteArrayOutputStream;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable{


    private Alert alertBox;

    // values (login/password - admin) just for example, not usable in application

    // login = 'admin'
    private String login = "21232f297a57a5a743894a0e4a801fc3";

    // password = 'admin'
    private String password = "9rYl92uo/kg=";

    // Example: 2016-04-20 12:05:16.338
    private String timestamp;

    public static Stage mainStage;

    private static File[] initListOfWindowsDevices = File.listRoots();

    private static int initListOfMacDevices = 0;

    static String timestampGlobal;

    @FXML
    private Label helloLabel;

    @FXML
    private Button passwordButton;


    static File findFileOnUSBForWindows() {
        File[] paths;

        // Returns path names for files and directory
        paths = File.listRoots();

        for (File currentDrive : paths) {
            if ( currentDrive.listFiles() != null )
                for ( File currentFile : currentDrive.listFiles() )
                    if ( currentFile.getName().equals("security-new.properties") ) {
                        return currentFile;
                    }

        }

        return null;
    }

    static File findFileOnUSBForMac() {
        File volumes = new File("/Volumes");

        for (File currentDrive : volumes.listFiles()){
            if ( currentDrive.listFiles() != null )
                for ( File currentFile : currentDrive.listFiles() )
                    if ( currentFile.getName().equals("security-new.properties") )
                        return currentFile;
        }

        return null;
    }

    private Properties getLocalPropertiesFile() throws IOException {
        String filename = "credentials.properties";
        Properties properties = new Properties();

        InputStream inputStream = Main.class.getResourceAsStream(filename);

        properties.load(inputStream);

        return properties;
    }

    public void showNewPasswordForm() {
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
        waitForNewDevice();
    }

    private void waitForNewDevice() {

        switch (getOperatingSystemName()) {
            case "Windows 10":
                windowsHandler();
                break;

            case "Mac OS X":
                macHandler();
                break;
        }

    }

    private void createAndWriteTimestamp() {
        // Save data to local file
        timestampGlobal = NewPasswordController.createTimestamp();
        try {
            Properties properties = getLocalPropertiesFile();
            properties.setProperty("timestamp", timestampGlobal);

            File file = new File(System.getProperty("user.dir") + "/src/sample/credentials.properties");
            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");

            // It is needed because old properties file was in RAM but new in HDD.
            // But it should be same as in RAM as in HDD.
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Save data to USB file
        File file = null;
        if ( getOperatingSystemName().equals("Mac OS X") )
            file = findFileOnUSBForMac();
        else if ( getOperatingSystemName().equals("Windows 10") )
            file = findFileOnUSBForWindows();

        writeToFile(file);
    }

    static String getOperatingSystemName(){
        //writeToFile(findFileOnUSBForMac());

        return System.getProperty("os.name");
    }

    static void writeToFile(File file) {
        Properties properties = new Properties();
        InputStream inputStream = Main.class.getResourceAsStream("credentials.properties");

        try {
            OutputStream outputStream = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream, "UTF-8");
            BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter);
            properties.load(inputStream);

            byte[] loginBytes = properties.getProperty("login").getBytes(StandardCharsets.UTF_8);
            writeBytesToFile(loginBytes, bufferedWriter);

            byte[] passwordBytes = properties.getProperty("password").getBytes(StandardCharsets.UTF_8);
            writeBytesToFile(passwordBytes, bufferedWriter);

            byte[] timestampBytes = properties.getProperty("timestamp").getBytes(StandardCharsets.UTF_8);
            writeBytesToFile(timestampBytes, bufferedWriter);

            bufferedWriter.flush();
            bufferedWriter.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void writeBytesToFile(byte[] bytes, BufferedWriter bufferedWriter) {
        for (byte currentByte : bytes) {
            try {
                bufferedWriter.write(Byte.toString(currentByte));
                bufferedWriter.write(" ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Word "nwln" is shows end of each property
        try {
            bufferedWriter.write("nwln");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Properties parseFile(File file) {
        BufferedReader bufferedReader = null;
        Properties properties = null;

        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            StringBuilder stringBuilder = new StringBuilder();
            String line = bufferedReader.readLine();

            while (line != null) {
                stringBuilder.append(line);
                stringBuilder.append(System.lineSeparator());
                line = bufferedReader.readLine();
            }

            String text = stringBuilder.toString();
            String login = byteToString(text.split("nwln")[0].split(" "));
            String password = byteToString(text.split("nwln")[1].split(" "));
            String timestamp = byteToString(text.split("nwln")[2].split(" "));

            properties = new Properties();
            properties.put("login", login);
            properties.put("password", password);
            properties.put("timestamp", timestamp);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return properties;
    }

    private String byteToString(String[] strings) {
        StringBuilder result = new StringBuilder();
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        byteArrayOutputStream.reset();


        for (String current : strings)
            byteArrayOutputStream.write(new Byte(current));

        result.append(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));

        return result.toString();
    }

    private void windowsHandler(){
        javafx.application.Platform.runLater(() -> {
            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Wait for USB input or just find it on HDD
                if ( File.listRoots().length > initListOfWindowsDevices.length || findFileOnUSBForWindows() != null ) {

                    if ( findFileOnUSBForWindows() != null ) {
                        callFileFoundAlertBox();

                        File file = findFileOnUSBForWindows();
                        Properties properties = parseFile(file);
                        setLogin(properties.getProperty("login"));
                        setPassword(properties.getProperty("password"));
                        setTimestamp(properties.getProperty("timestamp"));

                        if ( properties.get("login") != null )
                            authorization(properties);

                        break;
                    }

                } else if ( File.listRoots().length < initListOfWindowsDevices.length ) {
                    initListOfWindowsDevices = File.listRoots();
                }

            }
        });


    }

    private void macHandler(){
        if ( getOperatingSystemName().equals("Mac OS X") )
            initListOfMacDevices = getListOfUSBDevicesOnMacOSX();

        // TODO maybe because of javafx thread there is no label on Mac?
        javafx.application.Platform.runLater(() -> {
            while (true) {
                if ( findDeviceOnMac() )
                    break;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private boolean findDeviceOnMac(){
        if ( getListOfUSBDevicesOnMacOSX() > initListOfMacDevices || findFileOnUSBForMac() != null ) {

            if ( findFileOnUSBForMac() != null ) {
                callFileFoundAlertBox();

                File file = findFileOnUSBForMac();
                Properties properties = parseFile(file);
                setLogin(properties.getProperty("login"));
                setPassword(properties.getProperty("password"));
                setTimestamp(properties.getProperty("timestamp"));

                if ( properties.get("login") != null )
                    authorization(properties);


                return true;
            }
        } else if ( getListOfUSBDevicesOnMacOSX() < initListOfMacDevices ) {
            initListOfMacDevices = getListOfUSBDevicesOnMacOSX();

        }

        return false;
    }

    private int getListOfUSBDevicesOnMacOSX(){
        return new File("/Volumes").listFiles().length;
    }


    private void callFileFoundAlertBox() {
        alertBox = new Alert(Alert.AlertType.INFORMATION);
        alertBox.setHeaderText("File found!");
        alertBox.setContentText("");
        alertBox.initOwner(mainStage.getScene().getWindow());
        alertBox.showAndWait();
    }

    private void callNotValidSecurityFileAlertBox() {
        alertBox.setAlertType(Alert.AlertType.ERROR);
        alertBox.setHeaderText("Not valid security file");
        alertBox.setContentText("");
        alertBox.initOwner(mainStage.getScene().getWindow());
        alertBox.showAndWait();
    }

    private void authorization(Properties properties) {
        if ( properties.get("login").equals(login) &&
                properties.get("password").equals(password) &&
                properties.get("timestamp").equals(timestamp) ) {

            helloLabel.setText("Welcome user!");
            passwordButton.setVisible(true);
            createAndWriteTimestamp();

        } else {
            callNotValidSecurityFileAlertBox();
        }
    }



    private void setLogin(String login) {
        this.login = login;
    }

    private void setPassword(String password) {
        this.password = password;
    }

    private void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
