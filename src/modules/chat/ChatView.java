package modules.chat;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
public class ChatView extends VBox {
    private String nickname;
    private VBox chatArea;
    private ChatClient chatClient;
    //접속리스트
    private ListView<String> userListView;
    private boolean isUserListVisible = false;

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


        // 1. 상단 토글 버튼
        Button toggleUserListBtn = new Button(" ☰ ");
        HBox topBar = new HBox(toggleUserListBtn);
        topBar.setAlignment(Pos.CENTER_RIGHT);
        topBar.setPadding(new Insets(5));

        // 2. 채팅 메시지 출력 영역
        chatArea = new VBox(5);
        chatArea.setPadding(new Insets(10));
        chatArea.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #ddd; -fx-border-radius: 5px;");
        chatArea.setPrefHeight(340);
//        chatArea.setPrefHeight(Region.USE_COMPUTED_SIZE); 

        ScrollPane scrollPane = new ScrollPane(chatArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.vvalueProperty().bind(chatArea.heightProperty());//자동스크롤
//        Platform.runLater(() -> scrollPane.setVvalue(1.0));  // 아래로 스크롤
        // 3. 접속자 목록 ListView
//        userListView = new ListView<>();
//        userListView.setPrefWidth(150);
//        userListView.setPrefHeight(400);
 
        userListView = new ListView<>();
        userListView.setPrefHeight(380);
        userListView.setVisible(false); // 처음엔 숨김
        userListView.managedProperty().bind(userListView.visibleProperty()); // 공간 반응 여부도 같이 제어
        
        //셀 커마 나중에 더 다듬기
        userListView.setStyle(
        	    """
        	    -fx-background-color: transparent;
        	    -fx-control-inner-background: #F5F5F5;
        	    -fx-background-insets: 0;
        	    -fx-padding: 0;
        	    """
        	);
     // 4. 중간 영역: 채팅 + 접속자 목록
        HBox chatContent = new HBox(10, scrollPane, userListView);
        chatContent.setAlignment(Pos.TOP_CENTER);
        chatContent.setPadding(new Insets(5));
        chatContent.setPrefHeight(380);// 높이 고정
        
     // 채팅 텍스트 영역 + 접속자 목록 (가로 배치)
//        HBox chatContent = new HBox(10, scrollPane);
//        chatContent.setAlignment(Pos.TOP_CENTER);
//        chatContent.setPadding(new Insets(5));
//        chatContent.setPrefHeight(380); // 높이 고정
        
     // 동적 비율 바인딩
        scrollPane.prefWidthProperty().bind(
            chatContent.widthProperty().multiply(
                Bindings.when(userListView.visibleProperty())
                    .then(0.7)
                    .otherwise(1.0)
            )
        );
        userListView.prefWidthProperty().bind(
                chatContent.widthProperty().multiply(
                    Bindings.when(userListView.visibleProperty())
                        .then(0.3)
                        .otherwise(0.0)
                )
            );
        // 5. 토글 버튼 클릭 동작
        toggleUserListBtn.setOnAction(e -> {
            isUserListVisible = !isUserListVisible;
            userListView.setVisible(isUserListVisible);
            toggleUserListBtn.setText(isUserListVisible ? "☰ 숨기기" : " ☰ ");
        });
        // 토글 버튼으로 접속자 목록 on/off
//        toggleUserListBtn.setOnAction(e -> {
//            isUserListVisible = !isUserListVisible;
//            if (isUserListVisible) {
//                toggleUserListBtn.setText("☰ 숨기기");
//                if (!chatContent.getChildren().contains(userListView)) {
//                    chatContent.getChildren().add(userListView);
//                }
//            } else {
//                toggleUserListBtn.setText(" ☰ ");
//                chatContent.getChildren().remove(userListView);
//            }
//        });
        
         // 6. 하단 입력창
        TextField messageField = new TextField();
        messageField.setPromptText("메시지를 입력하세요.");
        messageField.setPrefWidth(320);

        Button sendButton = new Button("전송");
        sendButton.setMinWidth(60); //  최소 너비 설정!
        sendButton.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
        });

        messageField.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
        });

        HBox inputBox = new HBox(5, messageField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        
    

    
//        HBox inputBox = new HBox(5, messageField, sendButton);
//        inputBox.setAlignment(Pos.CENTER);
//        inputBox.setPadding(new Insets(5));

     

        // 채팅창 + 입력창 묶기
//        VBox chatAreaWithInput = new VBox(10, scrollPane, inputBox);
//        chatAreaWithInput.setAlignment(Pos.TOP_CENTER);

        // 7. 전체 배치
        VBox layout = new VBox(10, topBar, chatContent, inputBox);
        layout.setPadding(new Insets(10));

     

        // 클라이언트 연결
        chatClient = new ChatClient("172.30.1.98", 4000, chatArea, nickname, this);

     // 화면 적용
        getChildren().add(layout);
//        getChildren().addAll(topBar, chatContent);
    }
//    private void showChatRoom() {
//        getChildren().clear();
//
//        // 상단 토글 버튼
//        Button toggleUserListBtn = new Button("☰ 접속자 목록 보기");
//        HBox topBar = new HBox(toggleUserListBtn);
//        topBar.setAlignment(Pos.CENTER_RIGHT);
//        topBar.setPadding(new Insets(5));
//
//        // 채팅 메시지 출력 영역(ScrollPane)
//        chatArea = new VBox(5);
//        chatArea.setPadding(new Insets(10));
//        chatArea.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #ddd; -fx-border-radius: 5px;");
//        chatArea.setPrefHeight(400);
//
//     // ScrollPane으로 감싸기
//        ScrollPane scrollPane = new ScrollPane(chatArea);
//        scrollPane.setFitToWidth(true);
//        scrollPane.setPrefHeight(400); // 고정 높이 설정
//        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
//        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
//        scrollPane.vvalueProperty().bind(chatArea.heightProperty());
//
//        // 클라이언트 초기화
//        chatClient = new ChatClient("127.0.0.1", 4000, chatArea, nickname, this);
//
////        VBox (전체 레이아웃)구조를 바꿔줌
////        └─ HBox (mainContent)
////           ├─ VBox (centerContent)
////           │  ├─ ScrollPane(chatArea)
////           │  └─ HBox(messageField + sendButton)
////           └─ ListView (userListView)
////        이렇게 
////        VBox (전체 레이아웃)
////        ├─ HBox (topBar, toggle 버튼)
////        ├─ HBox (chatArea + userListView) ← 이 부분만 가로 분할
////        └─ HBox (messageField + sendButton) ← 무조건 하단 고정
//        
////      scrollPane 선언
////      messageField, sendButton, inputBox 선언
////      centerContent = new VBox(scrollPane, inputBox)
////      mainContent = new HBox(centerContent)
//    
//        // 접속자 목록 ListView
//        userListView = new ListView<>();
//        userListView.setPrefWidth(150);
////        userListView.setMaxHeight(380); // max값이 있어야 설정이 되나?
//        userListView.setPrefHeight(400); // 채팅창과 동일한 높이로 설정하고싶음
////        userListView.setVisible(false);  // 처음엔 안 보이게
//        
//     // chatContent: chatArea + (선택적으로) userListView
//        HBox chatContent = new HBox(10, scrollPane);
//        chatContent.setAlignment(Pos.TOP_CENTER);
//        // 전체 화면 레이아웃 구성
////        HBox mainContent = new HBox(10);
////        mainContent.setAlignment(Pos.TOP_CENTER);
//        
////      HBox inputBox = new HBox(5, messageField, sendButton);
////      inputBox.setAlignment(Pos.CENTER);
////      inputBox.setPadding(new Insets(5));
//        
//     
//        
//        
//        // 입력창
//        TextField messageField = new TextField();
//        messageField.setPromptText("메시지를 입력하세요.");
//        messageField.setPrefWidth(320);
//
//        Button sendButton = new Button("전송");
//        sendButton.setOnAction(e -> {
//            sendMessage(messageField.getText());
//            messageField.clear();
//        });
//
//        messageField.setOnAction(e -> {
//            sendMessage(messageField.getText());
//            messageField.clear();
//        });
//        
//        
//        
//
//        
//        HBox inputBox = new HBox(5, messageField, sendButton);
//        inputBox.setAlignment(Pos.CENTER);
//        inputBox.setPadding(new Insets(5));
////        VBox centerContent = new VBox(10, scrollPane, inputBox);
////        centerContent.setAlignment(Pos.CENTER);
////        centerContent.setPrefHeight(450); // 필요 시 고정
//        
//        // ⭐ centerContent = scrollPane + inputBox
//        VBox centerContent = new VBox(10, scrollPane, inputBox);
//        centerContent.setAlignment(Pos.CENTER);
//
//        // ⭐ mainContent = centerContent + (optional) userListView
//        HBox mainContent = new HBox(10, centerContent);
//        mainContent.setAlignment(Pos.TOP_CENTER);
//        
//        // 토글 버튼 동작
//        toggleUserListBtn.setOnAction(e -> {
//            isUserListVisible = !isUserListVisible;
//            if (isUserListVisible) {
//                toggleUserListBtn.setText("☰ 숨기기");
//                if (!mainContent.getChildren().contains(userListView)) {
//                    mainContent.getChildren().add(userListView);
//                }
//            } else {
//                toggleUserListBtn.setText("☰ ");
//                mainContent.getChildren().remove(userListView);
//            }
//        });
//
////        mainContent.getChildren().add(centerContent); // 기본 채팅창만 보이게
//
//      
//
//        // 구성요소 순서대로 배치
////        getChildren().addAll(topBar, mainContent);
//     // 전체 레이아웃 구성
//        getChildren().clear();
//        getChildren().addAll(topBar, chatContent, inputBox);
//    }
    
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
    
    // 접속자 리스트 갱신용 메서드
    public void updateUserList(List<String> nicknames) {
        Platform.runLater(() -> {
            userListView.getItems().setAll(nicknames);
        });
    }

}
