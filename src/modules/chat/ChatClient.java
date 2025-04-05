package modules.chat;

//퇴장메세지,
//입장퇴장, 채팅 메세지 스타일 적용

import java.io.*;
import java.net.*;
import javafx.application.Platform;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class ChatClient {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private VBox chatArea;
    private String nickname;

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

    public void sendMessage(String message) {
        if (out != null && !message.trim().isEmpty()) {
            out.println( message); // 닉네임 포함 메시지 전송
        }
    }


    private void receiveMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println("클라이언트 수신: " + message);

                // 퇴장 메시지일 경우 별도로 처리
                if (message.startsWith("[EXIT] ")) {
                    String exitMessage = message.substring(7); // "[EXIT] " 제거
                    Platform.runLater(() -> showExitMessage(exitMessage));
                } else {
                    String finalMessage = message;
                    Platform.runLater(() -> {
                        Label messageLabel = new Label(finalMessage);
                        chatArea.getChildren().add(messageLabel);
                    });
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void receiveMessages() {
//        String message;
//        try {
//            while ((message = in.readLine()) != null) {
//                String finalMessage = message;
//                Platform.runLater(() -> {
//                    Label messageLabel = new Label(finalMessage);
//                    chatArea.getChildren().add(messageLabel);
//                });
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}