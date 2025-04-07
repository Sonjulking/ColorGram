package modules.chat;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

public class ChatRoomListView extends VBox {
    private ChatView chatView;  // 메인 ChatView와 연결
    private BorderPane root; // 메인 화면의 root를 받아서 나중에 채팅창으로 전환할 때 사용
    
    // ChatView와 BorderPane을 매개변수로 받는 생성자 추가
    public ChatRoomListView(ChatView chatView, BorderPane root) {
        this.chatView = chatView;
        this.root = root;  //null안됨
        setSpacing(10);
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);

       
        Button room1Btn = new Button("채팅방 1");
        Button room2Btn = new Button("채팅방 2");
        Button room3Btn = new Button("채팅방 3");
        Button room4Btn = new Button("채팅방 3");

        Button[] buttons = { room1Btn, room2Btn, room3Btn, room4Btn };
        for (Button btn : buttons) {
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setPrefHeight(80);  
            btn.setStyle(
                "-fx-font-size: 20px;" +
                "-fx-text-fill: #000000;" +
                "-fx-background-color: #B3E5FC" +
                "-fx-background-radius: 25;" // 둥근모서리
            );
        }
        
     
        room1Btn.setOnAction(e -> {
            chatView.changeRoom(1); 
            root.setCenter(chatView);
        });
        room2Btn.setOnAction(e -> {
            chatView.changeRoom(2);
            root.setCenter(chatView);
        });
        room3Btn.setOnAction(e -> {
            chatView.changeRoom(3);
            root.setCenter(chatView);
        });
        room4Btn.setOnAction(e -> {
            chatView.changeRoom(4);
            root.setCenter(chatView);
        });

       
        getChildren().addAll(room1Btn, room2Btn, room3Btn , room4Btn);
    }
}