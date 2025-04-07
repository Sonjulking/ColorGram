package modules.board;

import database.dao.BoardDAO;
import database.vo.BoardVO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import modules.user.*;

import java.text.SimpleDateFormat;
import java.util.Optional;
import java.util.function.Consumer;

public class BoardView extends BorderPane {

    private final BoardVO board;
    private final Consumer<Node> navigationCallback;
    private final BoardDAO boardDAO;
    private final UserDAO userDAO;
    private UserVO currentUser;

    public BoardView(BoardVO board, Consumer<Node> navigationCallback) {
        this.board = board;
        this.navigationCallback = navigationCallback;
        this.boardDAO = new BoardDAO();
        this.userDAO = new UserDAO();
        
        // 디버깅 로그 추가
        System.out.println("BoardView 생성 - 게시글 번호: " + board.getBoardNum());
        System.out.println("BoardView 생성 - 게시글 작성자: " + board.getBoardWriterNum());
        
        // 현재 로그인한 사용자 정보 가져오기
        String currentUserId = BoardWriteView.getCurrentUserId();
        System.out.println("BoardView 생성 - 현재 로그인 ID: " + currentUserId);
        
        if (currentUserId != null && !currentUserId.isEmpty()) {
            this.currentUser = userDAO.selectUser(currentUserId);
            if (currentUser != null) {
                System.out.println("BoardView 생성 - 현재 사용자 번호: " + currentUser.getUserNo());
            } else {
                System.out.println("BoardView 생성 - 사용자 정보를 가져오지 못함");
            }
        } else {
            System.out.println("BoardView 생성 - 로그인 ID가 null 또는 빈 문자열");
        }
        
        // 조회수 증가
        boardDAO.increaseViewCount(board.getBoardNum());
        
        // 헤더 영역 (제목 및 정보)
        VBox headerBox = createHeader();
        
        // 내용 영역
        VBox contentBox = createContent();
        
        // 버튼 영역
        HBox buttonBox = createButtons();
        
        this.setPadding(new Insets(15));
        this.setTop(headerBox);
        this.setCenter(contentBox);
        this.setBottom(buttonBox);
    }
    
    private VBox createHeader() {
        // 제목
        Label titleLabel = new Label(board.getBoardTitle());
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        // 작성 정보
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateStr = board.getBoardCreateTime() != null ? 
                dateFormat.format(board.getBoardCreateTime()) : "날짜 정보 없음";
        
        // 작성자 정보
        String writerInfo = "작성자: " + board.getBoardWriterNum(); // 기본값은 작성자 번호
        
        try {
            if (userDAO != null) {
                UserVO writer = userDAO.selectUserByNo(board.getBoardWriterNum());
                if (writer != null) {
                    // 작성자 정보가 있으면 ID나 닉네임 표시
                    writerInfo = "작성자: " + (writer.getUserNickname() != null ? writer.getUserNickname() : writer.getUserId());
                }
            }
        } catch (Exception e) {
            System.out.println("작성자 정보 조회 실패: " + e.getMessage());
            e.printStackTrace();
        }
        
        Label infoLabel = new Label(String.format("%s | 작성일: %s | 조회수: %d | 좋아요: %d", 
                writerInfo, dateStr, board.getBoardViewCnt(), board.getBoardLikeCnt()));
        
        // 구분선
        HBox divider = new HBox();
        divider.setStyle("-fx-background-color: #CCCCCC; -fx-min-height: 1px; -fx-max-height: 1px;");
        
        VBox headerBox = new VBox(10, titleLabel, infoLabel, divider);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        
        return headerBox;
    }
    
    private VBox createContent() {
        // 내용
        TextArea contentArea = new TextArea(board.getBoardContent());
        contentArea.setEditable(false);
        contentArea.setWrapText(true);
        contentArea.setPrefHeight(300);
        contentArea.setStyle("-fx-background-color: transparent; -fx-focus-color: transparent;");
        
        VBox contentBox = new VBox(contentArea);
        return contentBox;
    }
    
    private HBox createButtons() {
        Button likeButton = new Button("좋아요");
        Button editButton = new Button("수정");
        Button deleteButton = new Button("삭제");
        Button listButton = new Button("목록");
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));
        
        // 디버깅 로그 추가
        System.out.println("===== 버튼 생성 디버깅 =====");
        System.out.println("현재 로그인 ID: " + BoardWriteView.getCurrentUserId());
        System.out.println("currentUser 객체: " + (currentUser == null ? "null" : "not null"));
        
        if (currentUser != null) {
            System.out.println("현재 사용자 번호: " + currentUser.getUserNo());
            System.out.println("현재 사용자 ID: " + currentUser.getUserId());
        }
        
        System.out.println("게시글 번호: " + board.getBoardNum());
        System.out.println("게시글 작성자 번호: " + board.getBoardWriterNum());
        
        // 임시 해결책: 항상 모든 버튼 표시
        buttonBox.getChildren().addAll(likeButton, editButton, deleteButton, listButton);
        
        // 원래 코드 (주석 처리)
        /*
        // 모든 사용자에게 보이는 버튼
        buttonBox.getChildren().addAll(likeButton, listButton);
        
        // 작성자 확인 - 작성자인 경우에만 수정/삭제 버튼 표시
        boolean isAuthor = false;
        
        if (currentUser != null) {
            System.out.println("작성자 여부 계산: " + board.getBoardWriterNum() + " == " + currentUser.getUserNo());
            isAuthor = (board.getBoardWriterNum() == currentUser.getUserNo());
            System.out.println("작성자 여부: " + isAuthor);
        }
        
        if (isAuthor) {
            // 좋아요와 목록 사이에 수정, 삭제 버튼 추가
            buttonBox.getChildren().add(1, editButton);
            buttonBox.getChildren().add(2, deleteButton);
        }
        */
        
        // 이벤트 설정
        likeButton.setOnAction(e -> handleLike());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        listButton.setOnAction(e -> handleBackToList());
        
        return buttonBox;
    }
    
    private void handleLike() {
        // 좋아요 카운트 증가 로직
        int result = boardDAO.increaseLikeCount(board.getBoardNum());
        if (result > 0) {
            board.setBoardLikeCnt(board.getBoardLikeCnt() + 1);
            
            // 좋아요 버튼을 눌렀다는 알림 표시
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("알림");
            alert.setHeaderText(null);
            alert.setContentText("이 게시글을 좋아합니다!");
            alert.showAndWait();
            
            // 화면 갱신 - 현재 화면을 새로 불러오기
            BoardView refreshedView = new BoardView(boardDAO.findById(board.getBoardNum()), navigationCallback);
            navigationCallback.accept(refreshedView);
        }
    }
    
    private void handleEdit() {
        // 권한 확인
        boolean hasPermission = false;
        
        if (currentUser != null) {
            System.out.println("수정 권한 확인 - 게시글 작성자: " + board.getBoardWriterNum());
            System.out.println("수정 권한 확인 - 현재 사용자: " + currentUser.getUserNo());
            hasPermission = (board.getBoardWriterNum() == currentUser.getUserNo());
            System.out.println("수정 권한 여부: " + hasPermission);
        }
        
        if (!hasPermission) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("권한 오류");
            alert.setHeaderText(null);
            alert.setContentText("자신이 작성한 글만 수정할 수 있습니다.");
            alert.showAndWait();
            return;
        }
        
        // 수정 화면으로 이동
        BoardWriteView editView = new BoardWriteView(navigationCallback, () -> {
            // 수정 후 돌아왔을 때 갱신된 게시글 보여주기 위한 콜백
            BoardView refreshedView = new BoardView(boardDAO.findById(board.getBoardNum()), navigationCallback);
            navigationCallback.accept(refreshedView);
        }, board);
        navigationCallback.accept(editView);
    }
    
    private void handleDelete() {
        // 권한 확인
        boolean hasPermission = false;
        
        if (currentUser != null) {
            System.out.println("삭제 권한 확인 - 게시글 작성자: " + board.getBoardWriterNum());
            System.out.println("삭제 권한 확인 - 현재 사용자: " + currentUser.getUserNo());
            hasPermission = (board.getBoardWriterNum() == currentUser.getUserNo());
            System.out.println("삭제 권한 여부: " + hasPermission);
        }
        
        if (!hasPermission) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("권한 오류");
            alert.setHeaderText(null);
            alert.setContentText("자신이 작성한 글만 삭제할 수 있습니다.");
            alert.showAndWait();
            return;
        }
        
        // 삭제 확인 다이얼로그
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("게시글 삭제");
        confirmAlert.setHeaderText("게시글을 삭제하시겠습니까?");
        confirmAlert.setContentText("삭제된 게시글은 복구할 수 없습니다.");
        
        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            // 삭제 처리
            int deleteResult = boardDAO.delete(board.getBoardNum());
            
            if (deleteResult > 0) {
                // 삭제 성공 메시지
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("삭제 완료");
                successAlert.setHeaderText(null);
                successAlert.setContentText("게시글이 삭제되었습니다.");
                successAlert.showAndWait();
                
                // 삭제 후 목록 화면으로 이동
                BoardListView listView = new BoardListView(navigationCallback);
                navigationCallback.accept(listView);
            } else {
                // 삭제 실패 메시지
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("삭제 실패");
                errorAlert.setHeaderText(null);
                errorAlert.setContentText("게시글 삭제에 실패했습니다.");
                errorAlert.showAndWait();
            }
        }
    }
    
    private void handleBackToList() {
        // 목록 화면으로 이동
        BoardListView listView = new BoardListView(navigationCallback);
        navigationCallback.accept(listView);
    }
}