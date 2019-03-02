package csci2020.group3;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.mail.*;
import javax.naming.AuthenticationException;
import java.io.IOException;
import java.util.Properties;


public class Controller {

    Preferences preferences = Preferences.getPreferences();

    // onclick method to generate new email window
    public void newButtonClicked() {
        System.out.println("User pressed new button...");

        // Create new send email window
        final Stage new_email = new Stage();
        new_email.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);

        GridPane pane = new GridPane();
        pane.setPadding(new Insets(30, 10, 10, 20));
        pane.setHgap(10);
        pane.setVgap(10);

        // From email ** Temporary, should be autofilled by current users email
        Label from_lbl = new Label("From");
        final TextField from = new TextField(preferences.getEmail());
        from.getText();
        from.setAlignment(Pos.CENTER_LEFT);
        GridPane.setConstraints(from_lbl, 0, 0);
        GridPane.setConstraints(from, 1, 0);
        pane.getChildren().addAll(from_lbl, from);

        // To email
        Label to_lbl = new Label("To");
        final TextField to = new TextField();
        to.setPromptText("Recipients");
        to.getText();
        to.setAlignment(Pos.CENTER_LEFT);
        GridPane.setConstraints(to_lbl, 0, 1);
        GridPane.setConstraints(to, 1, 1);
        pane.getChildren().addAll(to_lbl, to);

        // Subject
        Label subject_lbl = new Label("Subject");
        final TextField subject = new TextField();
        subject.setPromptText("Subject");
        subject.getText();
        to.setAlignment(Pos.CENTER_LEFT);
        GridPane.setConstraints(subject_lbl, 0, 2);
        GridPane.setConstraints(subject, 1, 2);
        pane.getChildren().addAll(subject_lbl, subject);

        // Message
        Label msg_lbl = new Label("Message");
        final TextArea msg = new TextArea();
        msg.setPromptText("Message");
        msg.getText();
        GridPane.setConstraints(msg_lbl, 0, 3);
        GridPane.setConstraints(msg, 1, 3);
        pane.getChildren().addAll(msg_lbl, msg);

        // Send Button
        Button send_btn = new Button("Send");
        send_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                SendEmail.sendMail(from.getText(), to.getText(), preferences.getPassword(), subject.getText(), msg.getText());
                new_email.close();
            }
        });

        GridPane.setConstraints(send_btn, 1, 4);
        GridPane.setHalignment(send_btn, HPos.RIGHT);
        pane.getChildren().add(send_btn);

        // Creating Scene and showing stage
        new_email.setTitle("New Message");
        Scene sendEmailScene = new Scene(pane,600 , 400);
        new_email.setScene(sendEmailScene);
        new_email.show();

    }

    public void loadButtonClicked() {
        System.out.println("User pressed load button");
        StoreEmails.ReadSentMail(preferences.getEmail(), preferences.getPassword());
    }

    // Creates the signIn window on button click
    public void signIn() throws Exception{

        // FXML Setup
//        final Stage login_menu = new Stage();
//        login_menu.initModality(Modality.APPLICATION_MODAL);
//
//        Parent login_disp = FXMLLoader.load(getClass().getResource("signin.fxml"));
//
//        Scene sendEmailScene = new Scene(login_disp,600 , 400);
//        login_menu.setScene(sendEmailScene);
//        login_menu.show();

        System.out.println("User attempting to login");

        // Create new send email window
        final Stage login_menu = new Stage();
        login_menu.initModality(Modality.APPLICATION_MODAL);
        //dialog.initOwner(primaryStage);

        GridPane pane = new GridPane();
        pane.setPadding(new Insets(30, 10, 10, 25));
        pane.setHgap(10);
        pane.setVgap(10);

        // Collecting user email address
        Label email_lbl = new Label("Email");
        final TextField email = new TextField();
        email.setPromptText("Email");
        email.getText();
        email.setAlignment(Pos.CENTER_LEFT);
        GridPane.setConstraints(email_lbl, 0, 0);
        GridPane.setConstraints(email, 1, 0);
        pane.getChildren().addAll(email_lbl, email);

        // Collecting User email password
        Label pwd_lbl = new Label("Password");
        final PasswordField pwd = new PasswordField();
        pwd.setPromptText("Recipients");
        pwd.getText();
        pwd.setAlignment(Pos.CENTER_LEFT);
        GridPane.setConstraints(pwd_lbl, 0, 1);
        GridPane.setConstraints(pwd, 1, 1);
        pane.getChildren().addAll(pwd_lbl, pwd);

        // Login Button
        Button login_btn = new Button("Login");
        login_btn.setPrefHeight(35);
        login_btn.setPrefWidth(100);
        login_btn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {

                // Authenticating..

                Properties props = new Properties();
                props.put("mail.smtp.host", "true");
                props.put("mail.smtp.starttls.enable", "true");
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.port", "587");
                props.put("mail.smtp.auth", "true");
                props.put("mail.transport.protocol", "smtp");


                Session emailSession = Session.getInstance(props,
                        new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(
                                        email.getText(), pwd.getText());
                            }
                        });
                // emailSession.setDebug(true);

                try {
                    emailSession.getTransport().connect();

                    // If authentication passes store email and password
                    System.out.println("Successful user sign in!");

                    // Updates config file
                    Preferences preferences = Preferences.getPreferences();
                    preferences.setEmail(email.getText());
                    preferences.setPassword(pwd.getText());
                    preferences.initConfig();

                    // Invalid login attempt
                } catch (MessagingException e) {
                    e.printStackTrace();
                }

                // Close login window
                login_menu.close();
            }
        });

        GridPane.setConstraints(login_btn, 1, 4);
        GridPane.setHalignment(login_btn, HPos.CENTER);
        pane.getChildren().add(login_btn);

        // Creating Scene and showing stage
        login_menu.setTitle("Sign In");
        Scene sendEmailScene = new Scene(pane,270 , 180);
        login_menu.setScene(sendEmailScene);
        login_menu.show();


    }

}
