package modules.board;

import database.dao.BoardDAO;
import database.vo.BoardVO;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.function.Consumer;

public class BoardListView extends BorderPane {

    private final Consumer<Node> navigationCallback;
    private final TableView<BoardVO> boardTable;
    private final Pagination pagination;
    private final BoardDAO boardDAO;
    private List<BoardVO> boardList;
    private final int rowsPerPage = 10;

    public BoardListView(Consumer<Node> navigationCallback) {
        this.navigationCallback = navigationCallback;
        this.boardDAO = new BoardDAO();

        // 상단 제목
        Label titleLabel = new Label("게시판");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setPadding(new Insets(15, 0, 15, 10));

        // 테이블 생성
        boardTable = createBoardTable();

        // 페이지네이션 생성
        boardList = boardDAO.findAll();
        int pageCount = (boardList.size() / rowsPerPage) + ((boardList.size() % rowsPerPage) > 0 ? 1 : 0);
        pagination = new Pagination(Math.max(pageCount, 1), 0);
        pagination.setPageFactory(this::createPage);

        // 버튼 영역
        Button writeButton = new Button("글쓰기");
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(10));
        buttonBox.getChildren().add(writeButton);

        // 전체 레이아웃 구성
        VBox centerBox = new VBox(5);
        centerBox.setPadding(new Insets(10));
        centerBox.getChildren().addAll(boardTable, pagination);

        this.setTop(titleLabel);
        this.setCenter(centerBox);
        this.setBottom(buttonBox);

        // 이벤트 핸들러
        writeButton.setOnAction(e -> openWriteView());

        // 데이터 로드
        loadBoardData();
    }

    private TableView<BoardVO> createBoardTable() {
        TableView<BoardVO> table = new TableView<>();

        // 컬럼 설정
        TableColumn<BoardVO, Integer> numCol = new TableColumn<>("번호");
        numCol.setCellValueFactory(new PropertyValueFactory<>("boardNum"));
        numCol.setPrefWidth(50);

        TableColumn<BoardVO, String> typeCol = new TableColumn<>("타입");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("boardType"));
        typeCol.setPrefWidth(70);

        TableColumn<BoardVO, String> titleCol = new TableColumn<>("제목");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("boardTitle"));
        titleCol.setPrefWidth(200);

        TableColumn<BoardVO, java.sql.Date> dateCol = new TableColumn<>("작성일");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("boardCreateTime"));
        dateCol.setPrefWidth(100);
        dateCol.setCellFactory(column -> new TableCell<BoardVO, java.sql.Date>() {
            private final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            @Override
            protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(format.format(item));
                }
            }
        });

        TableColumn<BoardVO, Integer> viewCol = new TableColumn<>("조회");
        viewCol.setCellValueFactory(new PropertyValueFactory<>("boardViewCnt"));
        viewCol.setPrefWidth(50);

        TableColumn<BoardVO, Integer> likeCol = new TableColumn<>("좋아요");
        likeCol.setCellValueFactory(new PropertyValueFactory<>("boardLikeCnt"));
        likeCol.setPrefWidth(50);

        table.getColumns().addAll(numCol, typeCol, titleCol, dateCol, viewCol, likeCol);

        // 테이블 행 클릭 이벤트
        table.setRowFactory(tv -> {
            TableRow<BoardVO> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    BoardVO selectedBoard = row.getItem();
                    openBoardView(selectedBoard);
                }
            });
            return row;
        });

        return table;
    }

    private void loadBoardData() {
        // 백그라운드 스레드에서 데이터 로드
        new Thread(() -> {
            try {
                boardList = boardDAO.findAll();

                Platform.runLater(() -> {
                    // UI 스레드에서 페이지네이션 업데이트
                    int pageCount = (boardList.size() / rowsPerPage) + ((boardList.size() % rowsPerPage) > 0 ? 1 : 0);
                    pagination.setPageCount(Math.max(pageCount, 1));
                    pagination.setCurrentPageIndex(0);

                    // 첫 페이지 데이터 표시
                    updateTableData(0);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("데이터 로드 오류");
                    alert.setHeaderText(null);
                    alert.setContentText("게시판 데이터를 불러오는 중 오류가 발생했습니다: " + e.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private Node createPage(int pageIndex) {
        updateTableData(pageIndex);
        return new BorderPane(boardTable);
    }

    private void updateTableData(int pageIndex) {
        int fromIndex = pageIndex * rowsPerPage;
        int toIndex = Math.min(fromIndex + rowsPerPage, boardList.size());

        if (fromIndex > toIndex) {
            boardTable.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<BoardVO> pageData = FXCollections.observableArrayList(
                boardList.subList(fromIndex, toIndex));
        boardTable.setItems(pageData);
    }

    private void openBoardView(BoardVO board) {
        // 조회수 증가 로직 추가 필요
        board.setBoardViewCnt(board.getBoardViewCnt() + 1);
        // DB 업데이트 로직 필요

        BoardView boardView = new BoardView(board, navigationCallback);
        navigationCallback.accept(boardView);
    }

    private void openWriteView() {
        BoardWriteView writeView = new BoardWriteView(navigationCallback, this::refreshBoardList);
        navigationCallback.accept(writeView);
    }

    // 글쓰기 후 목록 새로고침을 위한 메서드
    public void refreshBoardList() {
        loadBoardData();
    }
}
