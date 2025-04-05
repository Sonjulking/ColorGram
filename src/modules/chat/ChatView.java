package modules.chat;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
public class ChatView extends VBox {
    private String nickname;
    private VBox chatArea;
    private ChatClient chatClient;

    // 닉네임을 매개변수로 받도록 수정
    public ChatView(String nickname) {
        this.nickname = nickname;
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER);
        

        // 닉네임을 이미 알고 있으므로 바로 채팅방 표시
        showChatRoom();
    }

    private void showChatRoom() {
        getChildren().clear();

        chatArea = new VBox(5);
        chatArea.setPadding(new Insets(10));
        chatArea.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #ddd; -fx-border-radius: 5px;");
        chatArea.setPrefHeight(400);
        
        
     // chatArea를 ScrollPane에 넣기
        ScrollPane scrollPane = new ScrollPane(chatArea);
        scrollPane.setFitToWidth(true); // 너비 맞춤
        scrollPane.setPrefHeight(400); //  높이
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
     // 새로운 메시지가 도착했을 때 자동으로 아래로 스크롤
        scrollPane.vvalueProperty().bind(chatArea.heightProperty());

//        chatClient = new ChatClient("127.0.0.1", 4000, chatArea, nickname);
        chatClient = new ChatClient("127.0.0.1", 4000, chatArea, nickname, this);

        TextField messageField = new TextField();
        messageField.setPromptText("메시지를 입력하세요.");
        messageField.setPrefWidth(320); // 너비 넓게 설정
        
        Button sendButton = new Button("전송");
        sendButton.setOnAction(e -> {
            chatClient.sendMessage(messageField.getText());
            messageField.clear();
        });

        messageField.setOnAction(e -> {
            chatClient.sendMessage(messageField.getText());
            messageField.clear();
        });

        HBox inputBox = new HBox(5, messageField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(5));

//        getChildren().addAll(chatArea, inputBox);
        getChildren().addAll(scrollPane, inputBox);
    }
    
    //창 닫힐때 연결종료해서 정상적으로 종료되는지 확인하기위해 너
    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            if (chatClient != null) {
                chatClient.sendMessage("EXIT");  // 창닫힐때 EXIT 메시지보내기 테스트
                chatClient.closeConnection();
            }
        });
    }
    


    private void sendMessage(String message) {
        if (message.trim().isEmpty()) return;
        chatClient.sendMessage(message);
    }

    public void receiveMessage(String sender, String message) {
        Platform.runLater(() -> {
            if (sender == null) {
                Label systemMessage = new Label(message);
                systemMessage.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
                HBox systemBox = new HBox(systemMessage);
                systemBox.setAlignment(Pos.CENTER);
                chatArea.getChildren().add(systemBox);
            } else {
                boolean isMine = sender.equals(nickname);
                addChatMessage(sender, message, isMine);
            }
        });
    }
    

    private void addChatMessage(String sender, String message, boolean isMine) {
        VBox messageContainer = new VBox();
        messageContainer.setSpacing(2); // 간격 조절

        // 시간 생성
        String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
        Label timestamp = new Label(time);
        timestamp.setStyle("-fx-text-fill: gray; -fx-font-size: 10px;");

        // 말풍선 텍스트
        Text messageText = new Text(message);
        TextFlow textFlow = new TextFlow(messageText);
        textFlow.setPadding(new Insets(8));
        textFlow.setMaxWidth(300);
        textFlow.setLineSpacing(1.5);

        HBox messageBox = new HBox();
        messageBox.setPadding(new Insets(0, 10, 0, 10));

        if (isMine) {
            // 내 메시지: 오른쪽 정렬
            messageBox.setAlignment(Pos.CENTER_RIGHT);
            textFlow.setStyle("-fx-background-color: #B3E5FC; -fx-background-radius: 15px;");
            messageBox.getChildren().add(textFlow);

            HBox timeBox = new HBox(timestamp);
            timeBox.setAlignment(Pos.CENTER_RIGHT);
            timeBox.setPadding(new Insets(0, 12, 5, 12));

            HBox timeAndMessage = new HBox(timestamp, textFlow);
            timeAndMessage.setAlignment(Pos.CENTER_RIGHT);
            timeAndMessage.setSpacing(5);

            messageContainer.getChildren().addAll(timeAndMessage);
            messageContainer.setAlignment(Pos.CENTER_RIGHT);

        } else {
            // 상대방 메시지: 닉네임, 말풍선, 시간
            Label nameLabel = new Label(sender);
            nameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 13px;");

            messageBox.setAlignment(Pos.CENTER_LEFT);
            textFlow.setStyle("-fx-background-color: #DFE3E6; -fx-background-radius: 15px;");
            messageBox.getChildren().add(textFlow);

            HBox timeBox = new HBox(timestamp);
            timeBox.setAlignment(Pos.CENTER_LEFT);
            timeBox.setPadding(new Insets(0, 12, 5, 12));

            HBox timeAndMessage = new HBox(textFlow, timestamp);
            timeAndMessage.setAlignment(Pos.CENTER_LEFT);
            timeAndMessage.setSpacing(5);

            messageContainer.getChildren().addAll(nameLabel, timeAndMessage);
            messageContainer.setAlignment(Pos.CENTER_LEFT);
        }

        chatArea.getChildren().add(messageContainer);
    }


}
