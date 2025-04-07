import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import modules.board.BoardListView;
import modules.board.BoardView;
import modules.chat.ChatView;
import modules.player.PlayerListView;
import modules.player.PlayerView;
import modules.user.UserDAO;
import modules.user.UserView;

import java.util.Stack;
import java.util.function.Consumer;

public class Main extends Application {

    // 뒤로가기 버튼을 위한 스택
    private final Stack<Pane> viewHistory = new Stack<>();

   private Stage primaryStage; // setStage() 호출용
    
    private ChatView chatView; // 전역으로 한 번만 만들어서 재사용
    
    @Override
    public void start(Stage stage) {
    	
   	    this.primaryStage = stage; //  저장해둬야 setStage에 전달 가능.  창 정보 저장
    	
        // 상단 토글버튼
        ToggleGroup toggleGroup = new ToggleGroup(); // 토글 그룹
        ToggleButton songBtn = new ToggleButton("노래");
        ToggleButton eqBtn = new ToggleButton("이퀄라이저");
        songBtn.setToggleGroup(toggleGroup);
        eqBtn.setToggleGroup(toggleGroup);
        HBox toggleBox = new HBox(10, songBtn, eqBtn);// 버튼들 사이간격
        toggleBox.setAlignment(Pos.CENTER);// 내부 버튼들 가로 세로 모두 중앙 정렬
        toggleBox.setPadding(new Insets(10)); // 상하좌우 여백 10px
        // -----------------------------------

        // 음악 플레이어
        PlayerView playerView = new PlayerView(stage); // stage는 창(window)
        //
        PlayerListView playerListView = new PlayerListView(stage, playerView);
        //색깔 갱신시...
        playerView.setOnColorUpdated(() -> playerListView.refreshList());
        // 하단 버튼
        Button homeBtn = new Button("Home");
        Button backBtn = new Button("Back"); // 아직 보류
        Button playListBtn = new Button("Play List");
        Button communityBtn = new Button("Community");
        Button chatBtn = new Button("Chat");

        HBox navBar = new HBox(20, homeBtn, playListBtn, communityBtn, chatBtn); // 버튼들 사이간격
        navBar.setAlignment(Pos.CENTER); // 내부버튼들 똑같이 정중앙
        navBar.setPadding(new Insets(20));// 상하좌우 여백

        BorderPane root = new BorderPane(); // root라는 이름으로 BorderPane 레이아웃 생성
        root.setTop(toggleBox); // 상단에는 토글
        root.setCenter(playerView); // 중앙에는 플레이어
        root.setBottom(navBar); // 하단에는 네비게이션 버튼(home, 커뮤니티, chat...)

        // 버튼 액션
        // home버튼
        homeBtn.setOnAction(e -> {
            viewHistory.clear();// 뒤로가기 스택 초기화
            root.setCenter(playerView);
        });


        playListBtn.setOnAction(e -> {
            viewHistory.push((Pane) root.getCenter());
            root.setCenter(playerListView);
        });
        // back버튼
        backBtn.setOnAction(e -> {
            if (!viewHistory.isEmpty()) { // 스택안에 내용있을떄만 실행
                Pane previous = viewHistory.pop();
                root.setCenter(previous);
            }
        });

        // BoardView로 이동하는 공통 콜백 생성
        Runnable boardViewCallback = () -> {
            showBoardView(root);
        };

        // 커뮤니티버튼
        communityBtn.setOnAction(e -> {
            viewHistory.push((Pane) root.getCenter());// 현재 화면 스택에 저장

            // 로그인 상태 확인
            if (!UserView.isLogIn()) {
                // 로그인 상태가 아니면 UserView를 보여줌
                UserView userView = new UserView();
                // UserView에서 로그인 성공 후 BoardView로 이동하기 위한 콜백 설정
                userView.setOnLoginSuccess(boardViewCallback);
                // 안에 내용을 boardView로 전환
                root.setCenter(userView);
            } else {
                // 로그인 상태면 바로 BoardView를 보여줌
                showBoardView(root);
            }
        });


        // 채팅 버튼
        chatBtn.setOnAction(e -> {
            viewHistory.push((Pane) root.getCenter()); // 현재 화면 저장

            if (!UserView.isLogIn()) { 
                // 로그인 상태가 아니면 로그인 화면 표시
                UserView userView = new UserView();
                
                userView.setOnLoginSuccess(() -> {
                    openChatView(root);
                });

                root.setCenter(userView);
            } else {
                // 로그인 상태라면 바로 채팅방으로 이동
                openChatView(root);
            }
        });


//        chatBtn.setOnAction(e -> {
//            viewHistory.push((Pane) root.getCenter()); // 현재 화면 저장
//
//            if (!UserView.isLogIn()) {
//                // 로그인 상태가 아니면 로그인 화면 표시
//                UserView userView = new UserView();
//
//                userView.setOnLoginSuccess(() -> {
//                    String loggedInUserId = UserView.getCurrentUserId();
//                    UserDAO userDAO = new UserDAO();
//                    String nickname = userDAO.getNicknameById(loggedInUserId);
//
//                    showChatView(root, nickname);
//                });
//
//                root.setCenter(userView);
//            } else {
//                // 로그인 상태라면 닉네임을 가져와서 채팅방으로 이동
//                String loggedInUserId = UserView.getCurrentUserId();
//                UserDAO userDAO = new UserDAO();
//                String nickname = userDAO.getNicknameById(loggedInUserId);
//
//                showChatView(root, nickname);
//            }
//        });

        // 화면을 지정하고, 크기도 함꼐 지정
        Scene scene = new Scene(root, 400, 600);
        // window label 지정
        stage.setTitle("ColorGram");
        stage.setScene(scene); // 윈도우(창)에화면을 붙임.
        stage.show();
    }

    // BoardView 표시 헬퍼 메소드
    private void showBoardView(BorderPane root) {
        BoardListView boardListView = new BoardListView(new Consumer<Node>() {
            @Override
            public void accept(Node newView) {
                viewHistory.push((Pane) root.getCenter());
                root.setCenter(newView);
            }
        });
        root.setCenter(boardListView);
    }

    private void showChatView(BorderPane root, String nickname) {
        ChatView chatView = new ChatView(nickname);
        chatView.setStage(primaryStage); // 닫을 때 연결 종료 되게 함
        root.setCenter(chatView);
    }

    // 닉네임을 가져와서 ChatView를 여는 메소드 분리
    private void openChatView(BorderPane root) {
        if (chatView == null) {
            String loggedInUserId = UserView.getCurrentUserId();
            UserDAO userDAO = new UserDAO();
            String nickname = userDAO.getNicknameById(loggedInUserId);

            if (nickname != null && !nickname.isEmpty()) {
                chatView = new ChatView(nickname);
                chatView.setStage(primaryStage); // 연결 종료 핸들링
            } else {
                System.out.println("닉네임을 가져오는 데 실패했습니다.");
                return;
            }
        }

        // 화면 전환만 함
        root.setCenter(chatView);
    }

    public static void main(String[] args) {
        launch(args);
    }

}