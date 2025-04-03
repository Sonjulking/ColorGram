/*
package modules.player;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingNode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import modules.board.BoardView;

import javax.swing.*;
import java.io.File;
import java.util.Stack;

public class Player extends Application {

    private MediaPlayer mediaPlayer;
    private Slider progressBar;
    private Rectangle borderRect;
    private Slider redGreenSlider;
    private Slider yellowPurpleSlider;

    // 이전 화면을 저장하는 스택
    private final Stack<Node> viewHistory = new Stack<>();

    @Override
    public void start(Stage stage) {

        // 상단 토글 버튼 (노래/이퀄라이저 선택용)
        ToggleButton songBtn = new ToggleButton("노래");
        ToggleButton eqBtn = new ToggleButton("이퀄라이저");
        ToggleGroup toggleGroup = new ToggleGroup();
        songBtn.setToggleGroup(toggleGroup);
        eqBtn.setToggleGroup(toggleGroup);

        HBox toggleBox = new HBox(10, songBtn, eqBtn);
        toggleBox.setAlignment(Pos.CENTER);
        toggleBox.setPadding(new Insets(10));

        // 앨범 이미지 (test.png는 resources 폴더에 있어야 함)
        ImageView albumImage = new ImageView(new Image("assets/player/test.png"));
        albumImage.setFitWidth(200);
        albumImage.setFitHeight(200);

        // 앨범 테두리 사각형
        borderRect = new Rectangle(200, 200);
        borderRect.setFill(null);
        borderRect.setStrokeWidth(10);
        borderRect.setStroke(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.RED), new Stop(1, Color.YELLOW)
        ));

        StackPane albumBox = new StackPane(borderRect, albumImage);

        // 곡 제목 라벨
        Label titleLabel = new Label("설법 - 김세훈");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // 색상 조절 슬라이더 초기화
        redGreenSlider = new Slider(0, 1, 0.5);
        yellowPurpleSlider = new Slider(0, 1, 0.5);
        redGreenSlider.setShowTickMarks(true);
        yellowPurpleSlider.setShowTickMarks(true);

        // 슬라이더 값 변경 시 테두리 색상 갱신
        redGreenSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateGradient());
        yellowPurpleSlider.valueProperty().addListener((obs, oldVal, newVal) -> updateGradient());

        // 색상 조절 슬라이더 묶음
        HBox colorSliders = new HBox(
                20,
                createColorBox("Red", "Green", redGreenSlider),
                createColorBox("Yellow", "Purple", yellowPurpleSlider)
        );
        colorSliders.setAlignment(Pos.CENTER);

        // 음악 재생 위치 슬라이더
        progressBar = new Slider(0, 100, 0);
        progressBar.setPrefWidth(300);

        // 재생 컨트롤 버튼들
        Button playBtn = new Button("▶");
        Button prevBtn = new Button("⏮");
        Button nextBtn = new Button("⏭");
        Button openBtn = new Button("📂");

        HBox controlButtons = new HBox(15, openBtn, prevBtn, playBtn, nextBtn);
        controlButtons.setAlignment(Pos.CENTER);

        // 하단 네비게이션 버튼들
        Button homeBtn = new Button("홈");
        Button backBtn = new Button("뒤로");
        Button themeBtn = new Button("Theme Color");
        Button communityBtn = new Button("Community");
        Button chatBtn = new Button("Chat");

        // 플레이어 화면 (centerBox) 구성
        VBox centerBox = new VBox(
                10,
                albumBox,
                titleLabel,
                colorSliders,
                progressBar,
                controlButtons
        );
        centerBox.setAlignment(Pos.CENTER);
        centerBox.setPadding(new Insets(10));

        // 전체 레이아웃 구성 (BorderPane)
        BorderPane root = new BorderPane();
        root.setTop(toggleBox);
        root.setCenter(centerBox);

        HBox navBar = new HBox(30, homeBtn, backBtn, themeBtn, communityBtn, chatBtn);
        navBar.setAlignment(Pos.CENTER);
        navBar.setPadding(new Insets(20));
        root.setBottom(navBar);

        // communityBtn 클릭 시 커뮤니티 화면(BoardPanel)으로 전환 (현재 화면을 스택에 저장)
        communityBtn.setOnAction(e -> {
            // 현재 화면 저장
            viewHistory.push(root.getCenter());

            SwingNode swingNode = new SwingNode();
            SwingUtilities.invokeLater(() -> {
                BoardView boardView = new BoardView();
                swingNode.setContent(boardView);
                Platform.runLater(() -> {
                    root.setCenter(swingNode);

                    // 레이아웃 갱신 시도
                    for (int i = 0; i < 5; i++) {
                        Platform.runLater(() -> {
                            boardView.invalidate();
                            boardView.validate();
                            boardView.revalidate();
                            boardView.repaint();
                            SwingUtilities.updateComponentTreeUI(boardView);
                            root.layout();
                            stage.sizeToScene();
                        });
                    }
                });
            });
        });

        // homeBtn 클릭 시 플레이어 화면(centerBox)으로 복귀하고, 히스토리 초기화
        homeBtn.setOnAction(e -> {
            viewHistory.clear();
            root.setCenter(centerBox);
        });

        // backBtn 클릭 시 스택에서 이전 화면을 꺼내어 복귀
        backBtn.setOnAction(e -> {
            if (!viewHistory.isEmpty()) {
                Node previousView = viewHistory.pop();
                root.setCenter(previousView);
            }
        });

        // 파일 열기 버튼 동작
        openBtn.setOnAction(e -> openFile(stage));

        // ▶ 버튼 동작
        playBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });

        // ⏮ 버튼 동작
        prevBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.seek(Duration.ZERO);
        });

        // ⏭ 버튼 동작
        nextBtn.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(mediaPlayer.getTotalDuration());
            }
        });

        Scene scene = new Scene(root, 400, 550);
        stage.setTitle("ColorGram");
        stage.setScene(scene);
        stage.show();
    }

    // 슬라이더와 텍스트를 함께 묶는 UI 요소 생성
    private VBox createColorBox(String label1, String label2, Slider slider) {
        Label l1 = new Label(label1);
        Label l2 = new Label(label2);
        VBox box = new VBox(5, new HBox(5, l1, l2), slider);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    // 테두리 색상 업데이트 함수
    private void updateGradient() {
        double redGreenValue = redGreenSlider.getValue();
        double yellowPurpleValue = yellowPurpleSlider.getValue();

        Color color1 = Color.RED.interpolate(Color.GREEN, redGreenValue);
        Color color2 = Color.YELLOW.interpolate(Color.PURPLE, yellowPurpleValue);

        borderRect.setStroke(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0, color1),
                new Stop(1, color2)
        ));
    }

    // 음악 파일 선택 및 재생 설정
    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("MP3 파일 선택");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav"));

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            if (mediaPlayer != null) mediaPlayer.stop();
            Media media = new Media(file.toURI().toString());
            mediaPlayer = new MediaPlayer(media);

            mediaPlayer.setOnReady(() -> progressBar.setMax(media.getDuration().toSeconds()));

            mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
                if (!progressBar.isValueChanging()) {
                    progressBar.setValue(now.toSeconds());
                }
            });

            progressBar.setOnMousePressed(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));
            progressBar.setOnMouseDragged(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
*/
//package modules;




