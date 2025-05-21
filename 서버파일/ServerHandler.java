package chat_server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;

public class ServerHandler extends Thread {
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private List<ServerHandler> clients;
    private String nickname;
    private Map<String, ServerHandler> clientMap;
    
    private int roomNumber = 0; // 기본 채팅방은 1번
    

    public ServerHandler(Socket socket, Map<String, ServerHandler> clientMap, List<ServerHandler> clients) {
        this.socket = socket;
        this.clientMap = clientMap;
        this.clients = clients;
    }

    public String getNickname() {
        return nickname;
    }
    public int getRoomNumber() {
        return roomNumber;
    }
    public PrintWriter getOut() {
        return out;
    }
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            nickname = in.readLine();  // 닉네임 받기
            synchronized (clientMap) {
                if(clientMap.containsKey(nickname)){
                    ServerHandler oldHandler = clientMap.get(nickname);
                    oldHandler.sendMessage("중복 로그인으로 인해 기존 연결 종료됨.");
                    oldHandler.interrupt(); // 또는 적절하게 소켓을 닫는 로직
                    clientMap.remove(nickname);
                    ChatServer.removeClient(oldHandler);
                    ChatServer.updateUserList(oldHandler.getRoomNumber());
                }
                clientMap.put(nickname, this);
            }
            // 초기 입장: 기본 방 번호(roomNumber)가 1
            broadcastMessage(" * " + nickname + "님이 채팅방 " + roomNumber + "에 입장하셨습니다 *", roomNumber);
            ChatServer.updateUserList(roomNumber);
            System.out.println("서버: " + nickname + "님이 입장하셨습니다. (채팅방 " + roomNumber + ")");

            String message;
            while ((message = in.readLine()) != null) {
                // 방 변경 명령어 처리
            	if (message.startsWith("/changeRoom")) {
            	    String[] parts = message.split(" ");
            	    if (parts.length == 2) {
            	        int newRoom = Integer.parseInt(parts[1]);
            	        if (newRoom == roomNumber) {
            	            System.out.println("이미 채팅방 " + newRoom + "에 있습니다. 변경 없음.");
            	            continue;
            	        }
            	        int oldRoom = roomNumber;
            	        // 먼저 사용자의 roomNumber를 새로운 방 번호로 업데이트함으로써
            	        // 이후 oldRoom 갱신 메시지 전송 시 자신이 목록에서 제외되도록 함.
            	        roomNumber = newRoom;
            	        
            	        // 이전 방에 퇴장 메시지 및 접속자 목록 갱신
            	        broadcastMessage(" * " + nickname + "님이 채팅방 " + oldRoom + "에서 퇴장하셨습니다 *", oldRoom, this);
            	        ChatServer.updateUserList(oldRoom);
            	        
            	        // 새 방에 입장 메시지 및 접속자 목록 갱신
            	        broadcastMessage(" * " + nickname + "님이 채팅방 " + newRoom + "에 입장하셨습니다 *", newRoom);
            	        ChatServer.updateUserList(newRoom);
            	        continue;
            	    }
            	}

                
                // 로그아웃 처리
                if (message.equals("/logout")) {
                    broadcastMessage(" * " + nickname + "님이 채팅방 " + roomNumber + "에서 퇴장하셨습니다 *", roomNumber);
                    clientMap.remove(nickname);
                    ChatServer.removeClient(this);
                    ChatServer.updateUserList(roomNumber);
                    break;
                }
                // === 타이핑 인디케이터 처리 추가 ===
                if (message.startsWith("TYPING:")) {
                    // 메시지에서 "TYPING:" 이후의 닉네임을 추출 (보통 클라이언트의 닉네임)
                    String typist = message.substring("TYPING:".length());
                    // 같은 방(roomNumber)에 있는 다른 클라이언트들에게 브로드캐스트
                    broadcastMessage("TYPING_UPDATE:" + typist, roomNumber, this);
                    continue;
                }
                
                // 일반 채팅 메시지: 현재 roomNumber에 해당하는 사용자에게 전송
                broadcastMessage(nickname + ": " + message, roomNumber);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            clients.remove(this);
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
  
    

private void sendPrivateMessage(String toNickname, String message) {
    ServerHandler target = clientMap.get(toNickname);
    if (target != null) {
        target.out.println(message);
    } else {
        out.println(" * 해당 사용자를 찾을 수 없습니다 *");
    }
}
private void broadcast(String message, ServerHandler exclude) {
    for (ServerHandler client : clientMap.values()) {
        if (client != exclude) {
            client.sendMessage(message);
        }
    }
}


private void broadcastMessage(String message, int targetRoom) {
    for (ServerHandler client : ChatServer.clients) {
        if (client.getRoomNumber() == targetRoom) { // targetRoom과 일치하는 경우에만 전송
            client.getOut().println(message);
        }
    }
}
private void broadcastMessage(String message, int targetRoom, ServerHandler exclude) {
    for (ServerHandler client : ChatServer.clients) {
        if (client.getRoomNumber() == targetRoom && client != exclude) {
            client.getOut().println(message);
        }
    }
}
    
private void broadcastUserList() {
    StringBuilder sb = new StringBuilder();
    sb.append("USER_LIST:");
    for (String name : clientMap.keySet()) {
        sb.append(name).append(",");
    }

    // 마지막 쉼표 제거
    if (sb.charAt(sb.length() - 1) == ',') {
        sb.deleteCharAt(sb.length() - 1);
    }

    String userListMessage = sb.toString();

    for (ServerHandler client : clientMap.values()) {
        client.out.println(userListMessage);
    }
}
    
public void sendMessage(String message) {
    if (out != null) {
        out.println(message);
    }
}
    
    
}