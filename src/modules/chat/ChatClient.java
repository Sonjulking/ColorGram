package modules.chat;


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
        if (out != null) {
            out.println(message);
        }
    }

    private void receiveMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                System.out.println("클라이언트 수신: " + message); // 로그 추가
                String finalMessage = message;
                Platform.runLater(() -> {
                    Label messageLabel = new Label(finalMessage);
                    
                    // 퇴장 메시지 강조
                    if (finalMessage.contains("님이 퇴장하셨습니다.")) {
                        messageLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                    
                    chatArea.getChildren().add(messageLabel);
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void closeConnection() {
        try {
            if (out != null) {
                out.println("EXIT"); // 서버에게 종료 메시지 보내기
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}