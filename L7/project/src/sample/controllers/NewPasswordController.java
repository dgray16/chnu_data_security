package sample.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import sample.Main;
import sun.misc.BASE64Encoder;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Properties;

/**
 * Created by Administrator on 06.04.2016.
 */
public class NewPasswordController {

    @FXML
    private PasswordField loginField;

    @FXML
    private PasswordField passwordField;

    private Cipher DESCipher;

    static Stage currentStage;


    public void commitNewCredentials() {
        if ( !Objects.equals(loginField.getText(), "") && !Objects.equals(passwordField.getText(), "") ){
            saveNewLocalPropertiesFile(getLocalPropertiesFile());
            saveNewPropertirsFileToUSB(getLocalPropertiesFile());
            currentStage.close();
        } else {
            Alert alertBox = new Alert(Alert.AlertType.ERROR);
            alertBox.setContentText("");
            alertBox.setHeaderText("Fields are empty");
            alertBox.initOwner(MainController.mainStage.getScene().getWindow());
            alertBox.showAndWait();
        }

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
        byte[] encoded;
        StringBuffer sb = null;

        try {
            byte[] bytesOfMessage = message.getBytes("UTF-8");
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            encoded = md5.digest(bytesOfMessage);

            // Convert bytes to hex format
            sb = new StringBuffer();
            for (int i = 0; i < encoded.length; i++)
                sb.append(Integer.toString((encoded[i] & 0xff) + 0x100, 16).substring(1));

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private String encryptPassword(){
        // DES
        // Take password and divide it on 2 parts.
        // 1-st and 2-nd part proceed with DES.
        // Concatenate 2 encrypted parts

        String message = passwordField.getText();

        String encryptedLoginPart1 = encryptLogin().substring(0, (encryptLogin().length() / 4));

        String encryptedLoginPart2 =
                encryptLogin().substring(((encryptLogin().length() / 4)), (encryptLogin().length() / 2));

        SecretKey secretKey1 = new SecretKeySpec(encryptedLoginPart1.getBytes(), "DES");
        SecretKey secretKey2 = new SecretKeySpec(encryptedLoginPart2.getBytes(), "DES");


        String messagePart1 = message.substring(0, (message.length() / 2));
        String messagePart2 = message.substring((message.length() / 2), message.length());

        String enctyptedPart1 = "";
        String enctyptedPart2 = "";

        try {
            // ECB = Electronic Codebook mode.
            // PKCS5Padding = PKCS #5-style padding
            // Output stream encode: Base64
            DESCipher = Cipher.getInstance("DES/ECB/PKCS5Padding");

            DESCipher.init(Cipher.ENCRYPT_MODE, secretKey1);
            enctyptedPart1 = desEncrypt(messagePart1);
            DESCipher.init(Cipher.ENCRYPT_MODE, secretKey2);
            enctyptedPart2 = desEncrypt(messagePart2);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }

        return enctyptedPart1 + enctyptedPart2;
    }

    private String desEncrypt(String message){
        byte[] encrypted = null;
        try {

            byte[] utf8 = message.getBytes("UTF-8");

            encrypted = DESCipher.doFinal(utf8);

        } catch (UnsupportedEncodingException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return new BASE64Encoder().encode(encrypted);
    }

    private void saveNewLocalPropertiesFile(Properties properties){
        try {
            properties.setProperty("login", encryptLogin());
            properties.setProperty("password", encryptPassword());
            properties.setProperty("timestamp", MainController.timestampGlobal);

            File file = new File(new File("").getAbsoluteFile() + "\\src\\sample\\credentials.properties");
            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveNewPropertirsFileToUSB(Properties properties){
        try {
            properties.setProperty("login", encryptLogin());
            properties.setProperty("password", encryptPassword());
            properties.setProperty("timestamp", MainController.timestampGlobal);

            File file;
            if ( MainController.getOperatingSystemName().equals("Mac OS X") )
                file = MainController.findFileOnUSBForMac();
            else file = MainController.findFileOnUSBForWindows();

            OutputStream outputStream = new FileOutputStream(file);
            properties.store(outputStream, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static String createTimestamp(){
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
        Timestamp timestamp = new Timestamp(now.getTime());

        return timestamp.toString();
    }
}
