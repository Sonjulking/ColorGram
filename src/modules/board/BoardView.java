package modules.board;

import database.dao.BoardDAO;
import database.vo.BoardVO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import modules.user.*;

import modules.board.comment.CommentDAO;
import modules.board.comment.CommentVO;
import modules.user.UserDAO;
import modules.user.UserVO;
import modules.user.UserView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Optional;
import java.util.function.Consumer;

public class BoardView extends BorderPane {

    private final BoardVO board;
    private final Consumer<Node> navigationCallback;
    private final BoardDAO boardDAO;
    
    
    private UserVO currentUser;
    private final CommentDAO commentDAO;
    private final UserDAO userDAO;

    // 댓글 관련 컴포넌트
    private VBox commentsContainer;
    private TextField commentField;
    private ArrayList<CommentVO> commentList;

    public BoardView(BoardVO board, Consumer<Node> navigationCallback) {
        this.board = board;
        this.navigationCallback = navigationCallback;
        this.boardDAO = new BoardDAO();
        this.userDAO = new UserDAO();
        this.commentDAO = new CommentDAO();
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

        // 댓글 영역 추가
        VBox commentBox = createCommentSection();

        // 버튼 영역
        HBox buttonBox = createButtons();

        // 전체 레이아웃
        VBox mainContent = new VBox(10);
        mainContent.getChildren().addAll(contentBox, commentBox);

        this.setPadding(new Insets(15));
        this.setTop(headerBox);
        this.setCenter(mainContent);
        this.setBottom(buttonBox);

        // 댓글 목록 로드
        loadComments();
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
        
        Label infoLabel = new Label(String.format("작성자: %d | 작성일: %s | 조회수: %d | 좋아요: %d",
                board.getBoardWriterNum(), dateStr, board.getBoardViewCnt(), board.getBoardLikeCnt()));

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
        contentArea.setPrefHeight(200);
        contentArea.setStyle("-fx-background-color: transparent; -fx-focus-color: transparent;");

        VBox contentBox = new VBox(contentArea);
        contentBox.setPadding(new Insets(0, 0, 10, 0));

        // 구분선
        HBox divider = new HBox();
        divider.setStyle("-fx-background-color: #CCCCCC; -fx-min-height: 1px; -fx-max-height: 1px;");
        contentBox.getChildren().add(divider);

        return contentBox;
    }

    private VBox createCommentSection() {
        VBox commentSection = new VBox(10);
        commentSection.setPadding(new Insets(10, 0, 10, 0));

        // 댓글 섹션 헤더
        Label commentSectionLabel = new Label("댓글");
        commentSectionLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // 댓글 목록을 표시할 컨테이너
        commentsContainer = new VBox(5);
        commentsContainer.setPadding(new Insets(10, 0, 10, 0));
        ScrollPane scrollPane = new ScrollPane(commentsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(150);
        scrollPane.setStyle("-fx-background-color: #F5F5F5; -fx-border-color: #ddd; -fx-border-radius: 5px;");

        // 로그인 상태 확인
        if (UserView.isLogIn()) {
            // 댓글 입력 영역 - ChatView 스타일 적용
            commentField = new TextField();
            commentField.setPromptText("댓글을 입력하세요");
            commentField.setPrefWidth(310); // 너비 넓게 설정

            // 등록 버튼
            Button postButton = new Button("등록");
            postButton.setOnAction(e -> handlePostComment());

            // 입력 필드와 버튼을 수평으로 배치 (ChatView 스타일)
            HBox inputBox = new HBox(5, commentField, postButton);
            inputBox.setAlignment(Pos.CENTER);
            inputBox.setPadding(new Insets(5));

            // 엔터 키 이벤트 추가
            commentField.setOnAction(e -> handlePostComment());

            commentSection.getChildren().addAll(commentSectionLabel, scrollPane, inputBox);
        } else {
            Label loginMessageLabel = new Label("댓글을 작성하려면 로그인이 필요합니다.");
            loginMessageLabel.setStyle("-fx-text-fill: #888888;");
            commentSection.getChildren().addAll(commentSectionLabel, scrollPane, loginMessageLabel);
        }

        return commentSection;
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

        // 이벤트 설정
        likeButton.setOnAction(e -> handleLike());
        editButton.setOnAction(e -> handleEdit());
        deleteButton.setOnAction(e -> handleDelete());
        listButton.setOnAction(e -> goToList());

        return buttonBox;
    }

    // 댓글 목록 로드
    private void loadComments() {
        commentList = commentDAO.findByBoardNum(board.getBoardNum());
        commentsContainer.getChildren().clear();

        if (commentList.isEmpty()) {
            Label noCommentsLabel = new Label("댓글이 없습니다.");
            noCommentsLabel.setStyle("-fx-text-fill: #888888;");
            commentsContainer.getChildren().add(noCommentsLabel);
        } else {
            for (CommentVO comment : commentList) {
                createCommentNode(comment);
            }
        }
    }

    // 댓글 노드 생성
    private void createCommentNode(CommentVO comment) {
        VBox commentBox = new VBox(5);
        commentBox.setPadding(new Insets(5));
        commentBox.setStyle("-fx-border-color: #EEEEEE; -fx-border-radius: 5;");

        // 댓글 작성자 및 날짜 정보
        HBox headerBox = new HBox();

        // 현재 로그인한 사용자와 댓글 작성자 비교
        boolean isCurrentUserComment = false;
        if (UserView.isLogIn()) {
            String currentUserId = UserView.getCurrentUserId();
            UserDAO userDAO = new UserDAO();
            UserVO currentUser = userDAO.selectUser(currentUserId);
            
            if (currentUser != null) {
                isCurrentUserComment = (currentUser.getUserNo() == comment.getCommentWriterNum());
            }
        }

        // 자신이 작성한 댓글이면 닉네임을 빨간색으로 표시
        Label nicknameLabel = new Label(comment.getWriterNickname());
        if (isCurrentUserComment) {
            nicknameLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: red;");
        } else {
            nicknameLabel.setStyle("-fx-font-weight: bold;");
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String dateStr = comment.getCommentCreateTime() != null ?
                dateFormat.format(comment.getCommentCreateTime()) : "";
        Label dateLabel = new Label(dateStr);
        dateLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 11;");

        // 수정된 댓글인 경우 수정 날짜 표시
        VBox dateBox = new VBox(2);
        dateBox.setAlignment(Pos.CENTER_RIGHT); // 날짜 박스를 오른쪽 정렬
        dateBox.getChildren().add(dateLabel);
        
        if (comment.getCommentUpdateTime() != null) {
            String updateDateStr = dateFormat.format(comment.getCommentUpdateTime());
            Label editedLabel = new Label("*수정됨 (" + updateDateStr + ")");
            editedLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10;");
            dateBox.getChildren().add(editedLabel);
        }
        
        // headerBox 구성 - 닉네임은 왼쪽, 날짜는 오른쪽
        headerBox.getChildren().addAll(nicknameLabel, spacer, dateBox);
        headerBox.setAlignment(Pos.CENTER_LEFT); // 전체 박스는 왼쪽 정렬 유지

        // 댓글 내용
        Label contentLabel = new Label(comment.getCommentContent());
        contentLabel.setWrapText(true);

        // 수정/삭제/추천 버튼 영역
        HBox actionBox = new HBox(5);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        // 추천 버튼 추가 (로그인한 모든 사용자에게 표시)
        if (UserView.isLogIn()) {
            String currentUserId = UserView.getCurrentUserId();
            UserDAO userDAO = new UserDAO();
            UserVO currentUser = userDAO.selectUser(currentUserId);
            
            if (currentUser != null) {
                int currentUserNo = currentUser.getUserNo();
                boolean hasLiked = commentDAO.hasUserLikedComment(currentUserNo, comment.getCommentNum());
                
                Hyperlink likeLink = new Hyperlink(String.format("추천(%d)", comment.getCommentLikeCnt()));
                
                // 이미 추천한 경우 진한 파란색으로 스타일 변경
                if (hasLiked) {
                    likeLink.setStyle("-fx-text-fill: #0000CD; -fx-font-weight: bold; -fx-font-size: 11;"); // 진한 파란색
                } else {
                    likeLink.setStyle("-fx-text-fill: #4682B4; -fx-font-size: 11;"); // 기본 파란색
                }
                
                likeLink.setOnAction(e -> {
                    boolean success;
                    
                    if (hasLiked) {
                        // 이미 추천한 경우 추천 취소
                        success = commentDAO.removeCommentLike(currentUserNo, comment.getCommentNum());
                        if (success) {
                            comment.setCommentLikeCnt(comment.getCommentLikeCnt() - 1);
                        }
                    } else {
                        // 추천하지 않은 경우 추천 추가
                        success = commentDAO.addCommentLike(currentUserNo, comment.getCommentNum());
                        if (success) {
                            comment.setCommentLikeCnt(comment.getCommentLikeCnt() + 1);
                        }
                    }
                    
                    if (success) {
                        // 댓글 목록 새로고침하여 추천 상태 및 순서 업데이트
                        loadComments();
                    } else {
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("오류");
                        alert.setHeaderText(null);
                        alert.setContentText("댓글 추천 처리에 실패했습니다.");
                        alert.showAndWait();
                    }
                });
                
                actionBox.getChildren().add(likeLink);
            }
        }

        // 현재 로그인한 사용자와 댓글 작성자 비교 (수정/삭제 버튼 표시)
        if (UserView.isLogIn()) {
            String currentUserId = UserView.getCurrentUserId();
            UserVO currentUser = userDAO.selectUser(currentUserId);
            
            if (currentUser != null && currentUser.getUserNo() == comment.getCommentWriterNum()) {
                Hyperlink editLink = new Hyperlink("수정");
                Hyperlink deleteLink = new Hyperlink("삭제");

                editLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");
                deleteLink.setStyle("-fx-text-fill: #666666; -fx-font-size: 11;");

                editLink.setOnAction(e -> handleEditComment(comment, contentLabel));
                deleteLink.setOnAction(e -> handleDeleteComment(comment));

                actionBox.getChildren().addAll(editLink, deleteLink);
            }
        }

        commentBox.getChildren().addAll(headerBox, contentLabel);

        // 액션 버튼이 있는 경우에만 추가
        if (!actionBox.getChildren().isEmpty()) {
            commentBox.getChildren().add(actionBox);
        }

        commentsContainer.getChildren().add(commentBox);
    }

    // 댓글 작성 처리
    private void handlePostComment() {
        String content = commentField.getText().trim();

        if (content.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("알림");
            alert.setHeaderText(null);
            alert.setContentText("댓글 내용을 입력해주세요.");
            alert.showAndWait();
            return;
        }

        if (!UserView.isLogIn()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("알림");
            alert.setHeaderText(null);
            alert.setContentText("댓글을 작성하려면 로그인이 필요합니다.");
            alert.showAndWait();
            return;
        }

        try {
            // 현재 로그인한 사용자 정보 가져오기
            String currentUserId = UserView.getCurrentUserId();
            int userNum = userDAO.selectUser(currentUserId).getUserNo();

            // 댓글 객체 생성
            CommentVO comment = new CommentVO();
            comment.setCommentBoardNum(board.getBoardNum());
            comment.setCommentWriterNum(userNum);
            comment.setCommentContent(content);

            // 댓글 저장
            int result = commentDAO.insert(comment);

            if (result > 0) {
                // 입력 필드 초기화
                commentField.clear();

                // 댓글 목록 새로고침
                loadComments();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("오류");
                alert.setHeaderText(null);
                alert.setContentText("댓글 저장에 실패했습니다.");
                alert.showAndWait();
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("오류");
            alert.setHeaderText(null);
            alert.setContentText("오류가 발생했습니다: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // 댓글 수정 처리
    private void handleEditComment(CommentVO comment, Label contentLabel) {
        TextInputDialog dialog = new TextInputDialog(comment.getCommentContent());
        dialog.setTitle("댓글 수정");
        dialog.setHeaderText(null);
        dialog.setContentText("수정할 내용을 입력하세요:");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(newContent -> {
            if (newContent.trim().isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("댓글 내용을 입력해주세요.");
                alert.showAndWait();
                return;
            }

            comment.setCommentContent(newContent);
            int updateResult = commentDAO.update(comment);

            if (updateResult > 0) {
                // 화면에 업데이트
                contentLabel.setText(newContent);
                
                // 화면 새로고침으로 변경하여 수정 날짜가 표시되도록 함
                loadComments(); // 전체 댓글 목록 새로고침
                
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("댓글이 수정되었습니다.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("오류");
                alert.setHeaderText(null);
                alert.setContentText("댓글 수정에 실패했습니다.");
                alert.showAndWait();
            }
        });
    }

    // 댓글 삭제 처리
    private void handleDeleteComment(CommentVO comment) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("댓글 삭제");
        confirmAlert.setHeaderText("댓글을 삭제하시겠습니까?");
        confirmAlert.setContentText("삭제된 댓글은 복구할 수 없습니다.");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int deleteResult = commentDAO.delete(comment.getCommentNum());

            if (deleteResult > 0) {
                // 목록 새로고침
                loadComments();
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("댓글이 삭제되었습니다.");
                alert.showAndWait();
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("오류");
                alert.setHeaderText(null);
                alert.setContentText("댓글 삭제에 실패했습니다.");
                alert.showAndWait();
            }
        }
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
            // 게시글에 달린 댓글도 모두 삭제 처리
            commentDAO.deleteByBoardNum(board.getBoardNum());

            // 게시글 삭제 처리
            int deleteResult = boardDAO.delete(board.getBoardNum());

            if (deleteResult > 0) {
                // 삭제 성공 메시지
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("삭제 완료");
                alert.setHeaderText(null);
                alert.setContentText("게시글이 삭제되었습니다.");
                alert.showAndWait();

                // 삭제 후 목록 화면으로 이동
                goToList();
            } else {
                // 삭제 실패 메시지
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("삭제 실패");
                alert.setHeaderText(null);
                alert.setContentText("게시글 삭제에 실패했습니다.");
                alert.showAndWait();
            }
        }
    }

    // 목록으로 이동하는 메서드
    private void goToList() {
        try {
            BoardListView listView = new BoardListView(navigationCallback);
            navigationCallback.accept(listView);
        } catch (Exception e) {
            System.out.println("목록으로 이동 중 오류 발생: " + e.getMessage());
            e.printStackTrace();

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("오류");
            alert.setHeaderText(null);
            alert.setContentText("목록으로 이동 중 오류가 발생했습니다.");
            alert.showAndWait();
        }
    }
}