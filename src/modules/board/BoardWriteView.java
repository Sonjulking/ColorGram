package modules.board;

import database.dao.BoardDAO;
import database.vo.BoardVO;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import modules.user.*;

import java.util.function.Consumer;

public class BoardWriteView extends BorderPane {

	private final Consumer<Node> navigationCallback;
	private final Runnable refreshCallback;
	private final BoardDAO boardDAO;
	private final BoardVO editBoard; // 수정할 경우 사용
	private final boolean isEdit;
	private static String currentUserId = null;

	private TextField titleField;
	private ComboBox<String> typeComboBox;
	private TextArea contentArea;

	// 사용자 ID 설정 메서드
	public static void setCurrentUserId(String userId) {
		currentUserId = userId;
	}

	// 현재 사용자 ID 반환 메서드
	public static String getCurrentUserId() {
		return currentUserId;
	}

	// 새 글 작성 시 생성자
	public BoardWriteView(Consumer<Node> navigationCallback, Runnable refreshCallback) {
		this(navigationCallback, refreshCallback, null);
	}

	// 수정 시 생성자
	public BoardWriteView(Consumer<Node> navigationCallback, Runnable refreshCallback, BoardVO editBoard) {
		this.navigationCallback = navigationCallback;
		this.refreshCallback = refreshCallback;
		this.boardDAO = new BoardDAO();
		this.editBoard = editBoard;
		this.isEdit = (editBoard != null);

		setupUI();

		// 수정 모드일 경우 데이터 채우기
		if (isEdit) {
			populateData();
		}
	}

	private void setupUI() {
		// 상단 제목
		Label titleLabel = new Label(isEdit ? "게시글 수정" : "새 게시글 작성");
		titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
		titleLabel.setPadding(new Insets(15, 0, 15, 10));
		titleLabel.setTooltip(new Tooltip(titleLabel.getText()));

		// 입력 폼 영역
		VBox formBox = createForm();

		// 버튼 영역
		HBox buttonBox = createButtons();

		this.setPadding(new Insets(10));
		this.setTop(titleLabel);
		this.setCenter(formBox);
		this.setBottom(buttonBox);
	}

	private VBox createForm() {
		// 게시글 유형 선택
		Label typeLabel = new Label("분류: ");
		typeComboBox = new ComboBox<>();
		typeComboBox.getItems().addAll("음악", "자유");
		typeComboBox.setValue("자유");
		typeComboBox.setPrefWidth(150);
		HBox typeBox = new HBox(10, typeLabel, typeComboBox);
		typeBox.setAlignment(Pos.CENTER_LEFT);

		// 제목 입력
		Label titleFieldLabel = new Label("제목: ");
		titleFieldLabel.setMinWidth(40); // 라벨에 고정 너비 부여
		titleField = new TextField();
		titleField.setPrefWidth(400);
		HBox titleBox = new HBox(5, titleFieldLabel, titleField); // 간격 줄임
		titleBox.setAlignment(Pos.CENTER_LEFT);

		// 내용 입력
		Label contentLabel = new Label("내용: ");
		contentLabel.setMinWidth(60); // 라벨에 동일한 고정 너비 부여
		contentArea = new TextArea();
		contentArea.setPrefHeight(300);
		contentArea.setWrapText(true);

		VBox formBox = new VBox(15);
		formBox.setPadding(new Insets(10));
		formBox.getChildren().addAll(typeBox, titleBox, contentLabel, contentArea);

		return formBox;
	}

	private HBox createButtons() {
		Button saveButton = new Button(isEdit ? "수정하기" : "등록하기");
		Button cancelButton = new Button("취소");

		HBox buttonBox = new HBox(10, saveButton, cancelButton);
		buttonBox.setAlignment(Pos.CENTER_RIGHT);
		buttonBox.setPadding(new Insets(15, 0, 0, 0));

		// 이벤트 설정
		saveButton.setOnAction(e -> handleSave());
		cancelButton.setOnAction(e -> handleCancel());

		return buttonBox;
	}

	private void populateData() {
		if (editBoard != null) {
			titleField.setText(editBoard.getBoardTitle());
			typeComboBox.setValue(editBoard.getBoardType());
			contentArea.setText(editBoard.getBoardContent());
		}
	}

	private void handleSave() {
		// 입력 데이터 검증
		if (titleField.getText().trim().isEmpty()) {
			showAlert("제목을 입력해주세요.");
			return;
		}

		if (contentArea.getText().trim().isEmpty()) {
			showAlert("내용을 입력해주세요.");
			return;
		}

		try {
			if (isEdit) {
				// 수정 모드
				editBoard.setBoardTitle(titleField.getText());
				editBoard.setBoardType(typeComboBox.getValue());
				editBoard.setBoardContent(contentArea.getText());

				// DB 업데이트 로직
				int result = boardDAO.update(editBoard);

				if (result > 0) {
					// 업데이트 성공 메시지
					Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
					successAlert.setTitle("수정 완료");
					successAlert.setHeaderText(null);
					successAlert.setContentText("게시글이 성공적으로 수정되었습니다.");
					successAlert.showAndWait();

					// 상세 화면으로 이동
					if (refreshCallback != null) {
						refreshCallback.run();
					}
					BoardView boardView = new BoardView(editBoard, navigationCallback);
					navigationCallback.accept(boardView);
				} else {
					showAlert("게시글 수정에 실패했습니다.");
				}
			} else {
				// 새 글 작성 모드
				BoardVO newBoard = new BoardVO();
				newBoard.setBoardTitle(titleField.getText());
				newBoard.setBoardType(typeComboBox.getValue());
				newBoard.setBoardContent(contentArea.getText());

				// 임시 설정 (실제로는 로그인한 사용자 정보를 사용해야 함)
				UserDAO userDAO = new UserDAO();
				UserVO userInfo = new UserVO();
				System.out.println("currentUserId : " +  currentUserId);
				userInfo = userDAO.selectUser(currentUserId);
				newBoard.setBoardWriterNum(userInfo.getUserNo()); // 임시 사용자 번호
				newBoard.setBoardViewCnt(0);
				newBoard.setBoardLikeCnt(0);
				newBoard.setBoardIsDeleted("N");

				int result = boardDAO.insert(newBoard);

				if (result > 0) {
					// 저장 성공 시 목록 화면으로 이동
					if (refreshCallback != null) {
						refreshCallback.run();
					}
					BoardListView listView = new BoardListView(navigationCallback);
					navigationCallback.accept(listView);
				} else {
					showAlert("게시글 저장에 실패했습니다.");
				}
			}
		} catch (Exception e) {
			showAlert("오류가 발생했습니다: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private void handleCancel() {
		// 목록 화면으로 이동
		BoardListView listView = new BoardListView(navigationCallback);
		navigationCallback.accept(listView);
	}

	private void showAlert(String message) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("알림");
		alert.setHeaderText(null);
		alert.setContentText(message);
		alert.showAndWait();
	}
}