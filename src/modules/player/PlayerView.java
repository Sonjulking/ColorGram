package modules.player;

import java.io.FileWriter;
import java.io.IOException;

import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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

import java.io.File;
import java.util.Scanner;

public class PlayerView extends VBox {

    // 곡 제목
    private final Label titleLabel = new Label("선택된 노래가 없습니다."); // 초기값

    //미디어 플레이어
    private MediaPlayer mediaPlayer;

    private final Slider progressBar = new Slider(0, 100, 0); // 재생바

    private final Rectangle borderRect = new Rectangle(200, 200); // 테두리

    // 선택된 색을 저장할 변수
    private Color leftColor = Color.RED;
    private Color rightColor = Color.YELLOW;

    // 색상 선택 버튼
    private final Button redBtn = new Button();
    private final Button greenBtn = new Button();
    private final Button yellowBtn = new Button();
    private final Button purpleBtn = new Button();
    private final ImageView albumImage = new ImageView(new Image("assets/player/empty.png"));

    private File currentFile; // 현재 재생 중인 파일

    //call back (나중에 실행될 함수(코드)를 미리 등록)  저장하는 변수
    private Runnable onColorUpdated;

    public PlayerView(Stage stage) {
        //가운데 정렬
        this.setAlignment(Pos.CENTER);
        //위아래좌우 패딩 10px
        this.setPadding(new Insets(10));

        //자식요소들 사이의 간격
        this.setSpacing(10);

        //사이즈 설정
        albumImage.setFitWidth(200);
        albumImage.setFitHeight(200);

        // 테두리 설정
        //안쪽색 없앰
        borderRect.setFill(null);
        //테두리 두께 설정 30px
        borderRect.setStrokeWidth(30);
        this.updateGradient();

        //앨범아트 담길 Pane
        StackPane albumBox = new StackPane(borderRect, albumImage);

        // 곡 제목
        //Label titleLabel = new Label("설법 - 김세훈");
        titleLabel.setStyle("-fx-text-fill: red; -fx-font-size: 18px; -fx-font-weight: bold;");

        // 색상 버튼 UI
        HBox colorButtons = createColorButtonBox();

        // 재생바
        progressBar.setPrefWidth(300);

        // 컨트롤 버튼
        Button playBtn = new Button("▶");
        Button prevBtn = new Button("⏮");
        Button nextBtn = new Button("⏭");
        Button openBtn = new Button("📂");

        playBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.play();
        });
        prevBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.seek(Duration.ZERO);
        });
        nextBtn.setOnAction(e -> {
            if (mediaPlayer != null) mediaPlayer.seek(mediaPlayer.getTotalDuration());
        });

        //파일 윈도우 열림
        openBtn.setOnAction(e -> openFile(stage));

        //색깔 저장 버튼
        Button saveColorBtn = new Button("🎨 저장");

        //
        saveColorBtn.setOnAction(e -> saveColorsToFile(stage));
        HBox controlButtons = new HBox(15, openBtn, prevBtn, playBtn, nextBtn, saveColorBtn);
        controlButtons.setAlignment(Pos.CENTER);

        getChildren().addAll(
                albumBox,
                titleLabel,
                colorButtons,
                progressBar,
                controlButtons
        );
    }

    private HBox createColorButtonBox() {

        // 버튼 스타일
        redBtn.setStyle("-fx-background-color: red; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0,0, 0.25); -fx-border-width: 2px;");
        greenBtn.setStyle("-fx-background-color: green; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0,0, 0.25); -fx-border-width: 2px;");
        yellowBtn.setStyle("-fx-background-color: yellow; -fx-min-width: 40px; -fx-min-height: 40px;-fx-border-color: rgba(0, 0,0, 0.25); -fx-border-width: 2px;");
        purpleBtn.setStyle("-fx-background-color: darkblue; -fx-min-width: 40px; -fx-min-height: 40px;-fx-border-color: rgba(0, 0,0, 0.25); -fx-border-width: 2px;");

        // 클릭 이벤트
        redBtn.setOnAction(e -> {
            leftColor = Color.RED;
            updateGradient();
        });
        greenBtn.setOnAction(e -> {
            leftColor = Color.GREEN;
            updateGradient();
        });
        yellowBtn.setOnAction(e -> {
            rightColor = Color.YELLOW;
            updateGradient();
        });
        purpleBtn.setOnAction(e -> {
            rightColor = Color.DARKBLUE;
            updateGradient();
        });

        // 좌/우 박스
        VBox leftBox = new VBox(5, new Label(""), new HBox(5, redBtn, greenBtn));
        leftBox.setAlignment(Pos.CENTER);

        VBox rightBox = new VBox(5, new Label(""), new HBox(5, yellowBtn, purpleBtn));
        rightBox.setAlignment(Pos.CENTER);


        //가운데 띄우기 (버튼 4개들 사이에서)
        Region spacer = new Region();
        spacer.setPrefWidth(40);

        HBox box = new HBox(20, leftBox, spacer, rightBox);
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void updateGradient() {
        //테두리색깔
        //setStroke : 테두리 색상

 /*       borderRect.setStroke(new LinearGradient(
                0, 0,             // 시작점 X, Y (startX, startY)
                1, 0,             // 끝점 X, Y (endX, endY)
                true,             // proportional
                CycleMethod.NO_CYCLE, //색상 반복 안하기
                new Stop(0, leftColor), // 그라디언트 시작 지점 색
                new Stop(1, rightColor) // 그라디언트 끝 지점 색
        ));*/

        borderRect.setStroke(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, leftColor),      // 왼쪽 전체
                new Stop(0.499, leftColor),    // 거의 중간까지 왼쪽 색
                new Stop(0.5, rightColor),     // 딱 중간에서 오른쪽 색
                new Stop(1.0, rightColor)      // 오른쪽 끝까지
        ));

    }


    //파일열기
    private void openFile(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("MP3 파일 선택");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Audio", "*.mp3", "*.wav")
        );
        //파일선택창을 윈도우로 띄움
        File file = fileChooser.showOpenDialog(stage);

        //파일이 있을때
        if (file != null) {

            //이전에 재생중이던 음악멈추기
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }

            //현재 파일
            currentFile = file;
            //새 미디어 객체 생성 및 재생 준비
            Media media = new Media(file.toURI().toString()); //file.toURI().toString() :파일 객체를 URI(주소)로 바꾸고 그거를 문자열로 바꿈.
            mediaPlayer = new MediaPlayer(media);
            //색깔불러옴..
            loadColorsFromFile(file);
            // 🔽 메타데이터 로딩 시 제목 업데이트
            media.getMetadata().addListener((MapChangeListener<? super String, ? super Object>) change -> {
                if (change.wasAdded()) {
                    String title = (String) media.getMetadata().get("title");
                    String artist = (String) media.getMetadata().get("artist");

                    if (title != null && artist != null) {
                        titleLabel.setText(title + " - " + artist);
                    } else if (title != null) {
                        titleLabel.setText(title);
                    } else {
                        titleLabel.setText(file.getName()); // fallback
                    }
                    titleLabel.setStyle("-fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold;");
                    // 앨범 이미지 설정
                    if (media.getMetadata().get("image") instanceof javafx.scene.image.Image image) {
                        System.out.println("이미지있음");
                        albumImage.setImage(image);
                    } else {
                        System.out.println("이미지없음");
                    }
                }
            });

            //미디어가 준비되면은 프로그레스바 최대갑 설정
            mediaPlayer.setOnReady(() -> progressBar.setMax(media.getDuration().toSeconds()));

            //현재 재생 시간에 따라 프로그레스 바 업데이트
            mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
                if (!progressBar.isValueChanging()) { //마우스로 클릭안했을 때
                    progressBar.setValue(now.toSeconds());
                }
            });

            //마우스로 클릭하면 재생시간 재 설정
            //Duration.seconds  : 초단위 시간 정보를 담는...
            //progressBar.getValue() : 현재 재생위치
            progressBar.setOnMousePressed(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));

            //마우스로 드래그하면 재생시간 재설정
            progressBar.setOnMouseDragged(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));

        }
    }

    // 외부에서 파일을 재생하게 할 때 호출
    public void playFile(File file) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        //현재파일
        currentFile = file;

        Media media = new Media(file.toURI().toString());
        mediaPlayer = new MediaPlayer(media);
        loadColorsFromFile(file);

        media.getMetadata().addListener((MapChangeListener<? super String, ? super Object>) change -> {
            if (change.wasAdded()) {
                String title = (String) media.getMetadata().get("title");
                String artist = (String) media.getMetadata().get("artist");

                if (title != null && artist != null) {
                    titleLabel.setText(title + " - " + artist);
                } else if (title != null) {
                    titleLabel.setText(title);
                } else {
                    titleLabel.setText(file.getName());
                }

                if (media.getMetadata().get("image") instanceof javafx.scene.image.Image image) {
                    albumImage.setImage(image);
                }
            }
        });

        mediaPlayer.setOnReady(() -> progressBar.setMax(media.getDuration().toSeconds()));

        mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
            if (!progressBar.isValueChanging()) {
                progressBar.setValue(now.toSeconds());
            }
        });

        progressBar.setOnMousePressed(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));
        progressBar.setOnMouseDragged(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));

        mediaPlayer.play();
    }

    //색깔 저장
    private void saveColorsToFile(Stage stage) {
        //현재 파일이없음
        if (currentFile == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("알림");
            alert.setHeaderText(null);
            alert.setContentText("현재 열려 있는 음악 파일이 없습니다.");
            alert.showAndWait();
            return;
        }

        // 파일 이름에서 확장자 제거하고 .txt 붙이기
        String baseName = currentFile.getName();
        //점 위치
        int dotIndex = baseName.lastIndexOf('.');
        //점이 있으면
        if (dotIndex != -1) {
            //점앞에까지 글자 추출
            baseName = baseName.substring(0, dotIndex);
        }
        //노래파일명.txt
        File txtFile = new File(currentFile.getParentFile(), baseName + ".txt");

        try (FileWriter writer = new FileWriter(txtFile)) {
            String left = colorToHex(leftColor);
            String right = colorToHex(rightColor);
            writer.write("Left Color: " + left + "\n");
            writer.write("Right Color: " + right + "\n");
            //어디에 저장되었는지!!
            System.out.println("색상 정보 저장 완료: " + txtFile.getAbsolutePath());
        } catch (IOException e) {
            System.out.println("예외 : " + e.getMessage());
        }

        if (onColorUpdated != null) {
            onColorUpdated.run(); // 콜백 실행
        }


    }


    //rgb -> hex 코드
    private String colorToHex(Color color) {
        int r = (int) Math.round(color.getRed() * 255);
        int g = (int) Math.round(color.getGreen() * 255);
        int b = (int) Math.round(color.getBlue() * 255);
        return String.format("#%02X%02X%02X", r, g, b);
    }


    //파일에서 색깔정보를 불러옴
    private void loadColorsFromFile(File musicFile) {

        String baseName = musicFile.getName();
        int dotIndex = baseName.lastIndexOf('.');
        if (dotIndex != -1) {
            baseName = baseName.substring(0, dotIndex);
        }
        //musicFile.getParentFile()이 저장된 부모폴더 (디렉토리!!!)
        File colorFile = new File(musicFile.getParentFile(), baseName + ".txt");

        //색깔파일이 있다면!
        if (colorFile.exists()) {
            try (Scanner scanner = new Scanner(colorFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Left Color:")) {
                        leftColor = Color.web(line.substring("Left Color:".length()).trim());
                    } else if (line.startsWith("Right Color:")) {
                        rightColor = Color.web(line.substring("Right Color:".length()).trim());
                    }
                }
                // 읽은 후 테두리 반영
                updateGradient();
                System.out.println("색상 정보 로드 완료");
            } catch (Exception e) {
                System.out.println("색상 정보를 불러오는 중 오류 발생: " + e.getMessage());
            }
        } else {
            System.out.println("색상 정보 파일 없음");
        }
    }

    public void setOnColorUpdated(Runnable callback) {
        this.onColorUpdated = callback;
    }

}