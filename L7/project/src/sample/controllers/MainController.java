package sample.controllers;


import javafx.application.Platform;
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
import java.io.FileNotFoundException;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.ResourceBundle;

public class MainController implements Initializable{

    /**
     * -- Example of working credentials.properties --
     *   login=7815696ecbf1c96e6894b779456d330e
     *   password=jzW/VCYp+vAoqDJcjRb6zA
     *   timestamp=2016-05-24---20//41//21.881
     *
     * -- Example of working security-new.properties --
     *   55 56 49 53 54 57 54 101 99 98 102 49 99 57 54 101 54 56 57 52 98 55 55 57 52 53 54 100 51 51 48 101
     *   nwln106 122 87 47 86 67 89 112 43 118 65 111 113 68 74 99 106 82 98 54 122 65
     *   nwln50 48 49 54 45 48 53 45 50 52 45 45 45 49 57 47 47 50 53 47 47 50 48 46 51 49 49 nwln
     */

    private Alert alertBox = new Alert(Alert.AlertType.CONFIRMATION);

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

    private boolean isAuthorized = false;

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
        if ( isAuthorized )
            Platform.runLater(() -> {
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
        else showNotAuthorizedAlertBox();
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
            File file;
            OutputStream outputStream = null;

            try {
                file = new File(System.getProperty("user.dir") + "/src/sample/credentials.properties");
                outputStream = new FileOutputStream(file);
            } catch (FileNotFoundException ignored) {
            }

            if ( outputStream == null )
                try {
                    file = new File(System.getProperty("user.dir") + "/L7/project/src/sample/credentials.properties");
                    outputStream = new FileOutputStream(file);
                } catch (FileNotFoundException ignored) {
                }

            try {
                properties.store(outputStream, "");

                // It is needed because old properties file was in RAM but new in HDD.
                // But it should be same as in RAM as in HDD.
                outputStream.flush();
                outputStream.close();
            } catch (NullPointerException e) {
                showFileFoundAlertBox();
            }

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

        if ( strings.length != 0 ) {
            for (String current : strings)
                byteArrayOutputStream.write(new Byte(current));

            result.append(new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8));
        } else {
            showNotValidSecurityFileAlertBox();
            try {
                throw new Exception("Error");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result.toString();
    }

    private void windowsHandler(){
        new Thread(() -> {
            while (true) {
                // Wait for USB input or just find it on HDD
                if (File.listRoots().length > initListOfWindowsDevices.length || findFileOnUSBForWindows() != null) {

                    if (findFileOnUSBForWindows() != null) {
                        showFileFoundAlertBox();

                        File file = findFileOnUSBForWindows();
                        Properties properties = parseFile(file);
                        setLogin(properties.getProperty("login"));
                        setPassword(properties.getProperty("password"));
                        setTimestamp(properties.getProperty("timestamp"));

                        if (properties.get("login") != null)
                            authorization(properties);

                        break;
                    }

                } else if (File.listRoots().length < initListOfWindowsDevices.length) {
                    initListOfWindowsDevices = File.listRoots();
                }
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }).start();


    }

    private void macHandler(){
        if ( getOperatingSystemName().equals("Mac OS X") )
            initListOfMacDevices = getListOfUSBDevicesOnMacOSX();

        new Thread(()-> {
            while (true) {
                if ( findDeviceOnMac() )
                    break;

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private boolean findDeviceOnMac(){
        if ( getListOfUSBDevicesOnMacOSX() > initListOfMacDevices || findFileOnUSBForMac() != null ) {

            if ( findFileOnUSBForMac() != null ) {
                showFileFoundAlertBox();

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


    private void showFileFoundAlertBox() {
        Platform.runLater(()-> {
            alertBox.setAlertType(Alert.AlertType.INFORMATION);
            alertBox.setHeaderText("File found!");
            alertBox.setContentText("");
            alertBox.showAndWait();
        });
    }

    private void showNotValidSecurityFileAlertBox() {
        Platform.runLater(()-> {
            alertBox.setAlertType(Alert.AlertType.ERROR);
            alertBox.setHeaderText("Not valid security file");
            alertBox.setContentText("");
            alertBox.showAndWait();
        });
    }

    private void showNotAuthorizedAlertBox() {
        Platform.runLater(()-> {
            alertBox.setAlertType(Alert.AlertType.INFORMATION);
            alertBox.setHeaderText("Sorry, but you are not authorized");
            alertBox.setContentText("");
            alertBox.showAndWait();
        });
    }

    private void authorization(Properties properties) {
        if ( properties.get("login").equals(login) &&
                properties.get("password").equals(password) &&
                properties.get("timestamp").equals(timestamp) ) {

            Platform.runLater(()-> {
                helloLabel.setText("Welcome user!");
                isAuthorized = true;
            });

            createAndWriteTimestamp();

        } else {
            showNotValidSecurityFileAlertBox();
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
