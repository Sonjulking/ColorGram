package modules.chat;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javafx.scene.input.KeyEvent;
public class ChatView extends VBox {
    private String nickname;
    private VBox chatArea;
    private ChatClient chatClient;
    //접속리스트
    private ListView<String> userListView;
    private boolean isUserListVisible = false;
    
    private int roomNumber = 0;  // 기본 채팅방 번호
    
 // 채팅방 타이틀(방 이름)을 표시할 라벨
    private Label chatRoomTitle;

    // 타이핑 인디케이터용 레이블 (초기엔 숨김)
    private Label typingIndicatorLabel = new Label();
    
    // 닉네임을 매개변수로 받도록 수정
    public ChatView(String nickname) {
        this.nickname = nickname;
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER);
        // 닉네임을 이미 알고 있으므로 바로 채팅방 표시
        showChatRoom();
    }
    public ChatClient getChatClient() {
        return chatClient;
    }
    private void showChatRoom() {
        getChildren().clear();


        // 1. 상단 토글 버튼
        Button toggleUserListBtn = new Button(" ☰ ");
//        HBox topBar = new HBox(toggleUserListBtn);
        toggleUserListBtn.setPrefWidth(40);
        
     // 채팅방 타이틀 라벨 설정 (초기 채팅방 번호 사용)
        chatRoomTitle = new Label("채팅방 " + roomNumber);
        chatRoomTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold;");
        
        // BorderPane으로 상단 영역 구성: 중앙은 채팅방 이름, 우측은 토글 버튼
        BorderPane topBar = new BorderPane();
        topBar.setPadding(new Insets(5));
        topBar.setCenter(chatRoomTitle);
        topBar.setRight(toggleUserListBtn);

        // 2. 채팅 메시지 출력 영역
        chatArea = new VBox(5);
        chatArea.setPadding(new Insets(10));
        chatArea.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #ddd; -fx-border-radius: 5px;");
        chatArea.setPrefHeight(360);
//        chatArea.setPrefHeight(Region.USE_COMPUTED_SIZE); 

        ScrollPane scrollPane = new ScrollPane(chatArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(380);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.vvalueProperty().bind(chatArea.heightProperty());//자동스크롤

 
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

        
         // 6. 하단 입력창
        TextField messageField = new TextField();
        messageField.setPromptText("메시지를 입력하세요.");
        messageField.setPrefWidth(280);
        messageField.setPrefHeight(30);
        
        // 타이핑 인디케이터 관련 이벤트
        messageField.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            if(chatClient != null) {
                // 타이핑 중임을 알리는 메시지를 서버로 전송
                chatClient.sendTypingNotification();
            }
        });

     // 이모지 버튼 추가
        Button emojiButton = new Button("😀");
        emojiButton.setMinWidth(30);
        emojiButton.setPrefHeight(30);
       
     // 이모지 버튼 클릭 시 컨텍스트 메뉴 생성
        emojiButton.setOnAction(e -> {
            ContextMenu emojiMenu = new ContextMenu();

            
            String[] emojis = {"😀", "😂", "😊", "😍", "😢", "👍", "🙌"};
            for (String emoji : emojis) {
                MenuItem item = new MenuItem(emoji);
                item.setOnAction(event -> {
                    // 선택된 이모지를 메시지 필드에 추가
                    messageField.appendText(emoji);
                });
                emojiMenu.getItems().add(item);
            }
            // 아래쪽으로 보여주기
            emojiMenu.show(emojiButton, Side.BOTTOM, 0, 0);
        });
        
        Button sendButton = new Button("전송");
        sendButton.setMinWidth(50); //  최소 너비 설정
        sendButton.setPrefHeight(30);
        
        sendButton.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
        });
        
        // 이모티콘 버튼, 메시지 입력 필드, 전송 버튼을 하나의 HBox (inputBox)에 배치
        HBox inputBox = new HBox(5, emojiButton, messageField, sendButton);
        inputBox.setAlignment(Pos.CENTER);
        inputBox.setPadding(new Insets(10));
        
     // 타이핑 인디케이터 레이블 (초기에는 보이지 않음)
        typingIndicatorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        typingIndicatorLabel.setVisible(false);
        
        // 입력창과 타이핑 인디케이터를 포함하는 VBox 구성
        VBox inputArea = new VBox(3, typingIndicatorLabel, inputBox);
        inputArea.setAlignment(Pos.CENTER);
        inputArea.setPadding(new Insets(10));

        messageField.setOnAction(e -> {
            sendMessage(messageField.getText());
            messageField.clear();
        });

  
        
    
        // 7. 전체 레이아웃 구성
        VBox layout = new VBox(10, topBar, chatContent, inputArea);
        layout.setPadding(new Insets(10));

     

        // 클라이언트 연결
        // 여기서 roomNumber도 함께 보내기 위해, 생성 후 방 변경 메시지를 보내도록 
        chatClient = new ChatClient("127.0.0.1", 5000, chatArea, nickname, this);
        

        // 연결 후 현재 방 번호를 서버에 알여줌
        chatClient.changeRoom(roomNumber);
     // 화면 적용
        getChildren().add(layout);

    }
    
 // 타이핑 인디케이터 업데이트 메서드 (서버로부터 수신한 경우)
    public void updateTypingIndicator(String typist) {
        // 내 메시지는 표시할 필요 없음
        if (typist.equals(nickname)) return;
        Platform.runLater(() -> {
            typingIndicatorLabel.setText(typist + " is typing...");
            typingIndicatorLabel.setVisible(true);
            // 3초 후에 자동으로 숨김 (새로운 타이핑 이벤트가 없으면)
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    Platform.runLater(() -> typingIndicatorLabel.setVisible(false));
                }
            }, 3000);
        });
    }
    
//채팅방 번호 벼ㅛㄴ경 메서드
//    public void changeRoom(int newRoomNumber) {
//       
//        if (this.roomNumber == 0) {
//            this.roomNumber = newRoomNumber;  
//            if (chatClient != null) {
//                chatClient.changeRoom(newRoomNumber);  
//                chatArea.getChildren().clear();
//                System.out.println("채팅방 번호가 " + newRoomNumber + "로 변경되었습니다. (입장 메시지만 발송)");
//                // 서버에서는 새 방에 입장 메시지만 전송하도록 처리
//            }
//        } else if (this.roomNumber != newRoomNumber) {  
//            int oldRoom = roomNumber;  
//           
//            chatClient.changeRoom(newRoomNumber);  
//           
//            this.roomNumber = newRoomNumber; 
//            chatArea.getChildren().clear();  
//        } else {
//            System.out.println("이미 채팅방 " + newRoomNumber + "에 있습니다.");
//        }
//    }
    // 채팅방 번호를 변경하면서 상단의 채팅방 타이틀을 업데이트
    public void changeRoom(int newRoomNumber) {
        // 같은 방이면 아무 작업 없이 리턴
        if(this.roomNumber == newRoomNumber) {
            System.out.println("이미 채팅방 " + newRoomNumber + "에 있습니다.");
            return;
        }
        // 채팅 클라이언트에 새로운 방 번호 전달
        chatClient.changeRoom(newRoomNumber);
        // 채팅 영역 초기화 및 방 번호 업데이트
        this.roomNumber = newRoomNumber;
        chatArea.getChildren().clear();
        updateChatRoomTitle(newRoomNumber);
    }
    
    private void updateChatRoomTitle(int roomNumber) {
        chatRoomTitle.setText("채팅방 " + roomNumber);
    }
    
    //창 닫힐때 연결종료해서 정상적으로 종료되는지 확인하기위해 너
    public void setStage(Stage stage) {
        stage.setOnCloseRequest(event -> {
            if (chatClient != null) {
                chatClient.logout();  // 로그아웃 메시지를 보내고 연결 종료
                System.out.println("창 닫힘: chatClient.logout() 호출됨");
            }
        });
    }
    


    private void sendMessage(String message) {
        if (message.trim().isEmpty()) return;
        chatClient.sendMessage(message);
    }

//    public void receiveMessage(String sender, String message) {
//        Platform.runLater(() -> {
//            if (sender == null) {
//                Label systemMessage = new Label(message);
//                systemMessage.setStyle("-fx-text-fill: gray; -fx-font-style: italic;");
//                HBox systemBox = new HBox(systemMessage);
//                systemBox.setAlignment(Pos.CENTER);
//                chatArea.getChildren().add(systemBox);
//            } else {
//                boolean isMine = sender.equals(nickname);
//                addChatMessage(sender, message, isMine);
//            }
//        });
//    }
 // receiveMessage의 시스템 메시지 처리와 채팅 메시지 처리
    public void receiveMessage(String sender, String message) {
        Platform.runLater(() -> {
            // 만약 서버에서 "TYPING_UPDATE:" 접두어를 붙인 메시지라면 타이핑 인디케이터 업데이트
            if (message.startsWith("TYPING_UPDATE:")) {
                String typist = message.substring("TYPING_UPDATE:".length());
                updateTypingIndicator(typist);
            } else if (sender == null) {
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
    public void logoutChat() {
        if (chatClient != null) {
            chatClient.logout();
            System.out.println("ChatView: 로그아웃 처리 완료!");
        } else {
            System.out.println("ChatView: chatClient가 null입니다.");
        }
    }
    
    // 접속자 리스트 갱신용 메서드
    public void updateUserList(List<String> nicknames) {
        Platform.runLater(() -> {
            userListView.getItems().setAll(nicknames);
        });
    }

}
