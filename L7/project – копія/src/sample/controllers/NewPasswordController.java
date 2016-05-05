package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import sample.Main;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

/**
 * Created by Administrator on 06.04.2016.
 */
public class NewPasswordController {
    @FXML
    private PasswordField loginField;

    @FXML
    private PasswordField passwordField;


    public void commitNewCredentials() {
        saveNewLocalPropertiesFile(getLocalPropertiesFile());
        saveNewPropertirsFileToUSB(getLocalPropertiesFile());
    }

    private Properties getLocalPropertiesFile() {
        Properties properties = new Properties();
        InputStream inputStream = Main.class.getResourceAsStream("credentials.properties");
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }

    private String encryptLogin() {
        String message = loginField.getText();
        byte[] encoded = null;
        StringBuffer sb = null;

        try {
            byte[] bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            encoded = md5.digest(bytesOfMessage);

            // Convert bytes to hex format
            sb = new StringBuffer();
            for (int i = 0; i < encoded.length; i++)
                sb.append(Integer.toString((encoded[i] & 0xff) + 0x100, 16).substring(1));

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String encryptPassword(){
        return "";
    }

    private void saveNewLocalPropertiesFile(Properties properties){
        try {
            properties.setProperty("login", encryptLogin());
            File file = new File(new File("").getAbsoluteFile() + "\\src\\sample\\credentials.properties");
            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveNewPropertirsFileToUSB(Properties properties){
        try {
            properties.setProperty("login", encryptLogin());
            File file = MainController.findFile();
            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
