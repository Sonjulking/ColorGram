package modules.chat;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class ChatView extends VBox {
    private String nickname;
    private VBox chatArea;
    private ChatClient chatClient;

    public ChatView() {
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER);

        Button enterButton = new Button("입장하기");
        enterButton.setStyle("-fx-font-size: 16px; -fx-padding: 10px 20px;");
        enterButton.setOnAction(e -> showNicknameInput());

        getChildren().add(enterButton);
    }

    private void showNicknameInput() {
        getChildren().clear();

        Label nicknameLabel = new Label("닉네임을 입력하세요:");
        TextField nicknameField = new TextField();
        Button enterChatButton = new Button("입장");

        enterChatButton.setOnAction(e -> {
            String inputNickname = nicknameField.getText().trim();
            if (!inputNickname.isEmpty()) {
                this.nickname = inputNickname;
                showChatRoom();
            }
        });

        getChildren().addAll(nicknameLabel, nicknameField, enterChatButton);
    }

    private void showChatRoom() {
        getChildren().clear();

        chatArea = new VBox(5);
        chatArea.setPadding(new Insets(10));
        chatArea.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #ddd; -fx-border-radius: 5px;");
        chatArea.setPrefHeight(300);

        chatClient = new ChatClient("127.0.0.1", 4000, chatArea, nickname);

        TextField messageField = new TextField();
        messageField.setPromptText("메시지를 입력하세요.");

        Button sendButton = new Button("전송");
        Button plusButton = new Button("+");
        plusButton.setStyle("-fx-font-size: 14px; -fx-padding: 5px 10px;");

        HBox inputBox = new HBox(5, plusButton, messageField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(5));

        sendButton.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
        });

        messageField.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
        });

        getChildren().addAll(chatArea, inputBox);
    }

    private void sendMessage(String message) {
        if (message.trim().isEmpty()) return;
        chatClient.sendMessage(message);
    }

    public void receiveMessage(String sender, String message) {
        Platform.runLater(() -> {
            boolean isMine = sender.equals(nickname);
            addChatMessage(sender, message, isMine);
        });
    }

    private void addChatMessage(String sender, String message, boolean isMine) {
        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(5, 10, 5, 10));

        Text messageText = new Text(sender + ": " + message);
        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setPadding(new Insets(8));
        textFlow.setStyle("-fx-background-radius: 15px; -fx-border-radius: 15px; -fx-padding: 8px;");

        if (isMine) {
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle("-fx-background-color: #B3E5FC; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-padding: 8px;");
        } else {
            messageBox.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle("-fx-background-color: #DFE3E6; -fx-background-radius: 15px; -fx-border-radius: 15px; -fx-padding: 8px;");
        }

        messageBox.getChildren().add(textFlow);
        chatArea.getChildren().add(messageBox);
    }
}
