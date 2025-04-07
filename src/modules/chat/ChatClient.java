package modules.chat;

import java.io.BufferedReader;

//퇴장메세지,
//입장퇴장, 채팅 메세지 스타일 적용

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private VBox chatArea;
    private String nickname;
    private ChatView chatView;

    public ChatClient(String serverAddress, int port, VBox chatArea, String nickname) {
        this.chatArea = chatArea;
        this.nickname = nickname;
        

        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // 서버에 닉네임 전송
            out.println(nickname);

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
 // 추가: ChatView를 넘겨받는 생성자
    public ChatClient(String serverAddress, int port, VBox chatArea, String nickname, ChatView chatView) {
        this.chatArea = chatArea;
        this.nickname = nickname;
        this.chatView = chatView;

        try {
            socket = new Socket(serverAddress, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            out.println(nickname); // 서버에 닉네임 전송

            new Thread(this::receiveMessages).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(String message) {
        if (out != null && !message.trim().isEmpty()) {
            out.println( message); // 닉네임 포함 메시지 전송
        }
    }

//    private void receiveMessages() {
//        String message;
//        try {
//            while ((message = in.readLine()) != null) {
//                String finalMessage = message;
//
//                Platform.runLater(() -> {
//                    if (finalMessage.startsWith(" * ")) {
//                        chatView.receiveMessage(null, finalMessage.trim());
//                    } else {
//                        int colonIndex = finalMessage.indexOf(":");
//                        if (colonIndex != -1) {
//                            String sender = finalMessage.substring(0, colonIndex);
//                            String msg = finalMessage.substring(colonIndex + 1).trim();
//                            chatView.receiveMessage(sender, msg);
//                        }
//                    }
//                });
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    private void receiveMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                String finalMessage = message;

                Platform.runLater(() -> {
                    if (finalMessage.startsWith("USER_LIST:")) {
                        String[] users = finalMessage.substring(10).split(",");
                        chatView.updateUserList(List.of(users)); // ListView 갱신
                    } else if (finalMessage.startsWith(" * ")) {
                        chatView.receiveMessage(null, finalMessage.trim());
                    } else {
                        int colonIndex = finalMessage.indexOf(":");
                        if (colonIndex != -1) {
                            String sender = finalMessage.substring(0, colonIndex);
                            String msg = finalMessage.substring(colonIndex + 1).trim();
                            chatView.receiveMessage(sender, msg);
                        }
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


 // 퇴장 메시지 스타일링
    private void showExitMessage(String message) {
        Label exitLabel = new Label(message);
        exitLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        chatArea.getChildren().add(exitLabel);
    }
    public void closeConnection() {
        try {
            if (out != null) {
                out.println("EXIT");
                out.flush();
                System.out.println("클라이언트 종료: EXIT 메시지 전송됨");
            }

            Thread.sleep(100);

            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }


}