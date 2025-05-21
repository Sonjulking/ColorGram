package chat_server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatServer {
    private static final int PORT = 5000;
    
    //모든 클라이언트 리스트
    public static List<ServerHandler> clients = new ArrayList<>();

    
    //이건이제 안필요한가..
    private static final Map<String, ServerHandler> clientMap = new HashMap<>();
    
    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
        	System.out.println("채팅 서버가 시작되었습니다. 포트: " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();

                ServerHandler handler = new ServerHandler(socket, clientMap, clients);

                addClient(handler);
                
                handler.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }}
    

    public static synchronized void addClient(ServerHandler handler) {
        clients.add(handler);
    }

    public static synchronized void removeClient(ServerHandler handler) {
        clients.remove(handler);
    }
    

//    public static synchronized void updateUserList() {
//        StringBuilder sb = new StringBuilder("USER_LIST:");
//        for (String nickname : clientMap.keySet()) {
//            sb.append(nickname).append(",");
//        }
//        if (sb.charAt(sb.length() - 1) == ',') {
//            sb.deleteCharAt(sb.length() - 1);
//        }
//
//        String userListMessage = sb.toString();
//        for (ServerHandler client : clients) {
//            client.sendMessage(userListMessage);
//        }
//    }
    // 접속자 목록 갱신 (각 방별로 보내기)
    public static synchronized void updateUserList(int roomNumber) {
        StringBuilder sb = new StringBuilder("USER_LIST:");
        for (ServerHandler client : clients) {
            if (client.getRoomNumber() == roomNumber) {
                sb.append(client.getNickname()).append(",");
            }
        }
        if (sb.charAt(sb.length() - 1) == ',') {
            sb.deleteCharAt(sb.length() - 1);
        }
        String userListMessage = sb.toString();
        for (ServerHandler client : clients) {
            if (client.getRoomNumber() == roomNumber) {
                client.getOut().println(userListMessage);
            }
        }
    }
    
}
