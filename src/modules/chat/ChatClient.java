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
    
 // 추가 ChatView를 넘겨받는 생성자
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
    
    // 타이핑 알림 메시지 전송 (ChatView에서 호출)
    public void sendTypingNotification() {
        if (out != null) {
            // "TYPING:" 접두어와 함께 닉네임 전송 (서버에서 이를 받아 다른 클라이언트에 "TYPING_UPDATE:" 메시지로 브로드캐스트해야 함)
            out.println("TYPING:" + nickname);
        }
    }
    
    // 메시지 반응(리액션) 전송 (필요 시 추가)
    public void sendReaction(String sender, String messageTimestamp, String reaction) {
        if (out != null) {
            // 예: "REACTION:sender:timestamp:reaction"
            out.println("REACTION:" + sender + ":" + messageTimestamp + ":" + reaction);
        }
    }


//    private void receiveMessages() {
//        String message;
//        try {
//            while ((message = in.readLine()) != null) {
//                String finalMessage = message;
//
//                Platform.runLater(() -> {
//                    if (finalMessage.startsWith("USER_LIST:")) {
//                        String[] users = finalMessage.substring(10).split(",");
//                        chatView.updateUserList(List.of(users)); // ListView 갱신
//                    } else if (finalMessage.startsWith(" * ")) {
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
    
    public void changeRoom(int newRoomNumber) {
  
        if (out != null) {
            out.println("/changeRoom " + newRoomNumber);
            out.flush();
            System.out.println("ChatClient: 방 변경 메시지 전송됨 -> /changeRoom " + newRoomNumber);
        }
    }

 // 
    public void logout() {
        try {
            // 서버에게 먼저 나간다는 메시지 전송
            if (out != null) {
                out.println("/logout");
                out.flush();
            }

            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();

            System.out.println("클라이언트 로그아웃 완료");
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

 // 서버로부터 메시지 수신
    private void receiveMessages() {
        String message;
        try {
            while ((message = in.readLine()) != null) {
                String finalMessage = message;
                
                Platform.runLater(() -> {
                    if (finalMessage.startsWith("USER_LIST:")) {
                        String[] users = finalMessage.substring(10).split(",");
                        chatView.updateUserList(List.of(users));
                    } else if (finalMessage.startsWith("TYPING_UPDATE:")) {
                        // 타이핑 인디케이터 업데이트 메시지
                        // ChatView의 receiveMessage()에서 "TYPING_UPDATE:" 접두어를 확인하여 처리할 수 있음
                        chatView.receiveMessage(null, finalMessage);
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

}