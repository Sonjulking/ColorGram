package modules.player;

import java.io.FileWriter;
import java.io.IOException;
import javafx.animation.TranslateTransition;
import javafx.beans.value.ChangeListener;
import javafx.collections.MapChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
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
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

import java.io.File;
import java.util.Scanner;

public class PlayerView extends VBox {

    // 곡 제목
    private final Label titleLabel = new Label(""); 

    private final Label artistLabel = new Label("선택된 노래가 없습니다.");// 초기값
    
    //미디어 플레이어
    private MediaPlayer mediaPlayer;

    private final Slider progressBar = new Slider(0, 100, 0); // 재생바

    //앨범테두리?
    private final Rectangle borderRect = new Rectangle(200, 200); // 테두리
    private final ImageView albumImage = new ImageView(new Image("assets/player/empty.png"));
    
    // 선택된 색을 저장할 변수
    private Color leftColor = Color.web("#fc4949");
    private Color rightColor = Color.web("#f0d362");


    // 색상 선택 버튼들
    private final Button redBtn = new Button();
    private final Button greenBtn = new Button();
    private final Button yellowBtn = new Button();
    private final Button purpleBtn = new Button();
   


    //색상 선택 슬라이더
    private Slider redGreenSlider;
    private Slider yellowPurpleSlider;
    private final ChangeListener<Number> redGreenListener = (obs, oldVal, newVal) -> updateGradient();
    private final ChangeListener<Number> yellowPurpleListener = (obs, oldVal, newVal) -> updateGradient();

    private File currentFile; // 현재 재생 중인 파일

    //call back (나중에 실행될 함수(코드)를 미리 등록)  저장하는 변수
    private Runnable onColorUpdated;

    private boolean isShuffle = false; // 랜덤 재생 여부
    private final Button playBtn;//재생버튼
    private final Slider volumeSlider = new Slider(0, 1, 0.5); // 기본 볼륨 50%
    private final Button volumeBtn = new Button("🔊");
    private final Popup volumePopup = new Popup();
    private final Button shuffleBtn = new Button("🔀"); // 랜덤 버튼


    private PlayerListView playerListView; // 연결된 플레이어 리스트뷰
    
    
 // **토글 UI 관련**  
    // 토글버튼 좌측에 배치
    private final ToggleButton toggleButton = new ToggleButton("♡");
    // 색상 저장 버튼 – 토글 활성 시 우측
    private final Button saveColorBtn = new Button("🎨");
    
    private HBox toggleBar;
    // 색상 제어 컨트롤 토글 활성 시 보이게
    private HBox colorControlsPane;
    
    private VBox infoControlArea;

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
//        titleLabel.setTranslateY(20); // 살짝 올리기
        //titleLabel.setPadding(new Insets(10, 0, -35, 0));
        VBox.setMargin(albumBox, new Insets(10, 0, 0, 0));
        
        // 색상 버튼 UI
        HBox colorButtons = createColorButtonBox();

        
        
        
        
        
        //토글 영역(infoControlArea)에 들어갈 색상 조절 슬라이더, 버튼 등 미리 초기화
        redGreenSlider = new Slider(0, 1, 0.5);
        yellowPurpleSlider = new Slider(0, 1, 0.5);
        redGreenSlider.setShowTickMarks(true);
        yellowPurpleSlider.setShowTickMarks(true);
        redGreenSlider.setPrefWidth(115);
        yellowPurpleSlider.setPrefWidth(115);
        redGreenSlider.valueProperty().addListener(redGreenListener);
        yellowPurpleSlider.valueProperty().addListener(yellowPurpleListener);

        HBox colorSliders = new HBox(
                40, // 슬라이더 그룹 간격
                createColorBox("", "", redBtn, greenBtn, redGreenSlider),
                createColorBox("", "", yellowBtn, purpleBtn, yellowPurpleSlider)
        );
        colorSliders.setAlignment(Pos.CENTER);
        colorControlsPane = colorSliders; 
        colorControlsPane.setVisible(false); // 초기에는 보이지 않음

        // ─────────────────────────────
        // 토글 영역(infoControlArea) 구성  
       
        
        HBox topRow = new HBox(10);
        topRow.setAlignment(Pos.CENTER_LEFT);
        topRow.getChildren().add(toggleButton);
        HBox.setMargin(saveColorBtn, new Insets(0, 40, 0, 0));//살짝 왼쪽으로 

        // 확장 가능한 spacer 추가 (토글 버튼과 저장 버튼 사이의 공간을 채움)
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        topRow.getChildren().add(spacer);

        // 저장 버튼을 topRow에 추가하고, 초기에는 숨김 처리
        topRow.getChildren().add(saveColorBtn);
        saveColorBtn.setVisible(false);

       
        StackPane bottomRow = new StackPane();
        bottomRow.setPrefHeight(50);  // 토글 영역의 하단 영역 높이를 고정
        artistLabel.setOpacity(1);    // 초기에는 artistLabel이 보임
        colorControlsPane.setOpacity(0);  // 색상 컨트롤은 숨김
        colorControlsPane.setVisible(false);
        bottomRow.getChildren().addAll(artistLabel, colorControlsPane);

        // infoControlArea는 위의 topRow와 bottomRow를 포함하는 VBox
        infoControlArea = new VBox(5, topRow, bottomRow);
        infoControlArea.setAlignment(Pos.CENTER);

        // ─────────────────────────────
        // 토글 버튼 스타일 및 이벤트 처리
        toggleButton.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-border-color: transparent;" +
            "-fx-font-size: 30px;" +
            "-fx-translate-x: 30px;"  // 오른쪽으로 이동
        );

        toggleButton.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            // 토글 아이콘 전환 및 스케일 애니메이션
            toggleButton.setText(isSelected ? "♡" : "♡");
            ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(200), toggleButton);
            scaleTransition.setFromX(1.0);
            scaleTransition.setFromY(1.0);
            scaleTransition.setToX(1.3);
            scaleTransition.setToY(1.3);
            scaleTransition.setAutoReverse(true);
            scaleTransition.setCycleCount(2);
            scaleTransition.play();

            // infoControlArea의 상단 행(topRow)을 사용
            HBox currentTopRow = (HBox) infoControlArea.getChildren().get(0);
            if (isSelected) {
                // 저장 버튼을 보이게
                saveColorBtn.setVisible(true);

                // 하단: artistLabel fade out, colorControlsPane fade in
                FadeTransition fadeOutArtist = new FadeTransition(Duration.millis(300), artistLabel);
                fadeOutArtist.setFromValue(1);
                fadeOutArtist.setToValue(0);
                fadeOutArtist.play();
                colorControlsPane.setVisible(true);
                FadeTransition fadeInColorControls = new FadeTransition(Duration.millis(300), colorControlsPane);
                fadeInColorControls.setFromValue(0);
                fadeInColorControls.setToValue(1);
                fadeInColorControls.play();
            } else {
                // 저장 버튼을 숨김 처리
                saveColorBtn.setVisible(false);

                // 하단: colorControlsPane fade out, artistLabel fade in
                FadeTransition fadeOutColorControls = new FadeTransition(Duration.millis(300), colorControlsPane);
                fadeOutColorControls.setFromValue(1);
                fadeOutColorControls.setToValue(0);
                fadeOutColorControls.setOnFinished(e -> colorControlsPane.setVisible(false));
                fadeOutColorControls.play();
                FadeTransition fadeInArtist = new FadeTransition(Duration.millis(300), artistLabel);
                fadeInArtist.setFromValue(0);
                fadeInArtist.setToValue(1);
                fadeInArtist.play();
            }
        });

        // ──────────────────────────────────────────────────────────────────────────────────────────────────────────────
        // 색상 저장 버튼 설정 (기능 유지)
        saveColorBtn.setStyle("-fx-font-size: 16px; -fx-padding: 5px 15px;");
        saveColorBtn.setOnAction(e -> saveColorsToFile(stage));

        // ──────────────────────────────────────────────────────────────────────────────────────────────────────────────
        // 재생 컨트롤 관련 UI 구성 
        playBtn = new Button("▶");
        Button prevBtn = new Button("⏮");
        Button nextBtn = new Button("⏭");
        Button openBtn = new Button("📂");

        prevBtn.setPrefSize(40, 40);
        prevBtn.setStyle("-fx-font-size: 18px;");
        playBtn.setPrefSize(50, 50);
        playBtn.setStyle("-fx-font-size: 20px;");
        nextBtn.setPrefSize(40, 40);
        nextBtn.setStyle("-fx-font-size: 18px;");

        playBtn.setOnAction(e -> {
            if (mediaPlayer == null) {
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("알림");
                alert.setHeaderText(null);
                alert.setContentText("재생할 음악 파일이 없습니다.\n파일을 먼저 선택해주세요!");
                alert.showAndWait();
                return;
            }
            MediaPlayer.Status status = mediaPlayer.getStatus();
            if (status == MediaPlayer.Status.PLAYING) {
                mediaPlayer.pause();
                playBtn.setText("▶");
            } else {
                mediaPlayer.play();
                playBtn.setText("⏸");
            }
        });

        prevBtn.setOnAction(e -> {
            if (playerListView != null) {
                File prevFile = playerListView.getPreviousFile();
                if (prevFile != null) {
                    playFile(prevFile);
                } else if (mediaPlayer != null) {
                    mediaPlayer.seek(Duration.ZERO);
                }
            } else if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.ZERO);
            }
        });

        nextBtn.setOnAction(e -> {
            if (playerListView != null) {
                File nextFile;
                if (isShuffle) {
                    nextFile = playerListView.getRandomFile();
                } else {
                    nextFile = playerListView.getNextFile();
                }
                if (nextFile != null) {
                    playFile(nextFile);
                } else if (mediaPlayer != null) {
                    mediaPlayer.seek(mediaPlayer.getTotalDuration());
                }
            }
        });

        shuffleBtn.setOnAction(e -> {
            isShuffle = !isShuffle;
            shuffleBtn.setStyle(isShuffle ? "-fx-background-color: lightblue;" : "");
        });

        openBtn.setOnAction(e -> openFile(stage));

        // ──────────────────────────────────────────────────────────────────────────────────────────────────────────────
        // 볼륨 슬라이더 UI 구성
        volumeSlider.setOrientation(Orientation.VERTICAL);
        volumeSlider.setPrefHeight(100);
        volumeSlider.setShowTickMarks(true);
        volumeSlider.setMajorTickUnit(0.5);
        volumeSlider.setMinorTickCount(4);
        volumeSlider.setBlockIncrement(0.1);
        volumeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (mediaPlayer != null) {
                mediaPlayer.setVolume(newVal.doubleValue());
            }
        });

        VBox volumeBox = new VBox(volumeSlider);
        volumeBox.setPadding(new Insets(10));
        volumeBox.setStyle("-fx-background-color: white; -fx-border-color: gray; -fx-border-width: 1px;");
        volumePopup.getContent().add(volumeBox);

        volumeBtn.setOnAction(e -> {
            if (volumePopup.isShowing()) {
                volumePopup.hide();
            } else {
                double x = volumeBtn.localToScreen(volumeBtn.getBoundsInLocal()).getMinX();
                double y = volumeBtn.localToScreen(volumeBtn.getBoundsInLocal()).getMinY();
                volumePopup.show(volumeBtn, x + 20, y - 110);
            }
        });

        HBox controlButtons = new HBox(35, shuffleBtn, prevBtn, playBtn, nextBtn, volumeBtn);
        controlButtons.setAlignment(Pos.CENTER);
        for (Button btn : new Button[]{shuffleBtn, prevBtn, playBtn, nextBtn, volumeBtn}) {
            btn.setStyle("-fx-background-color: transparent;" +
                         "-fx-border-color: transparent;" +
                         "-fx-padding: 0;" +
                         "-fx-font-size: " + (int)(btn.getPrefHeight() * 0.5) + "px;");
        }

        // ─────────────────────────────
        // 최종 전체 UI 레이아웃 구성:
    
        getChildren().clear();
        getChildren().addAll(
            titleLabel,
            albumBox,
            infoControlArea,
            progressBar,
            controlButtons
        );
    }
        //
//        saveColorBtn.setOnAction(e -> saveColorsToFile(stage));
//        saveColorBtn.setStyle("-fx-font-size: 16px; -fx-padding: 5px 15px;");
//        HBox controlButtons = new HBox( 35 ,shuffleBtn, prevBtn, playBtn, nextBtn,  volumeBtn);
//        controlButtons.setAlignment(Pos.CENTER);
//
//        for (Button btn : new Button[]{shuffleBtn, prevBtn, playBtn, nextBtn, volumeBtn}) {
//            btn.setStyle(
//                "-fx-background-color: transparent;" +
//                "-fx-border-color: transparent;" +
//                "-fx-padding: 0;" + 
//                "-fx-font-size: " + (int)(btn.getPrefHeight() * 0.5) + "px;" 
//            );
//        }
//
//        getChildren().addAll(
//                albumBox,
//                titleLabel,
////                colorButtons,
//                colorSliders,
//                saveColorBtn,
//                progressBar,
//                controlButtons
//        );
//    }

    private HBox createColorButtonBox() {

        // 버튼 스타일 적용
        redBtn.setStyle("-fx-background-color: #fc4949; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");
        greenBtn.setStyle("-fx-background-color: #8cdb86; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");
        yellowBtn.setStyle("-fx-background-color: #f0d362; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");
        purpleBtn.setStyle("-fx-background-color: #39a2f7; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");

        // 클릭 이벤트
        redBtn.setOnAction(e -> {
            redGreenSlider.setValue(0); // 빨강 쪽으로 이동
   /*         leftColor = Color.web("#fc4949");
            updateGradient();*/
        });
        greenBtn.setOnAction(e -> {
            redGreenSlider.setValue(1); // 초록 쪽으로 이동
     /*       leftColor = Color.web("#8cdb86");
            updateGradient();*/
        });
        yellowBtn.setOnAction(e -> {
            yellowPurpleSlider.setValue(0); // 노랑
       /*     rightColor = Color.web("#f0d362");
            updateGradient();*/
        });
        purpleBtn.setOnAction(e -> {
            yellowPurpleSlider.setValue(1); // 파랑
 /*           rightColor = Color.web("#39a2f7");
            updateGradient();*/
        });

        // 좌/우 박스
        VBox leftBox = new VBox(5, new Label(""), new HBox(40, redBtn, greenBtn));
        leftBox.setAlignment(Pos.CENTER);

        VBox rightBox = new VBox(5, new Label(""), new HBox(40, yellowBtn, purpleBtn));
        rightBox.setAlignment(Pos.CENTER);


        //가운데 띄우기 (버튼 4개들 사이에서)
        Region spacer = new Region();
        spacer.setPrefWidth(40);

        HBox box = new HBox(20, leftBox, spacer, rightBox);
        box.setAlignment(Pos.CENTER);
        return box;
    }
    
    
 

    // 슬라이더와  + 버튼
    private VBox createColorBox(
            String label1,
            String label2,
            Button leftBtn,
            Button rightBtn,
            Slider slider
    ) {
        Label l1 = new Label(label1);
        Label l2 = new Label(label2);

        // 버튼 두 개를 나란히
        HBox buttons = new HBox(40, leftBtn, rightBtn);
        buttons.setAlignment(Pos.CENTER);


        // 트랙용 Rectangle (두꺼운 바)
        Rectangle gradientTrack = new Rectangle(115, 12); // ← 여기서 두께 조절 (12px)
        gradientTrack.setArcWidth(20);
        gradientTrack.setArcHeight(20);

        // 색상 구분
        if (slider == redGreenSlider) {
            gradientTrack.setFill(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#fc4949")),
                    new Stop(1, Color.web("#8cdb86"))
            ));
        } else {
            gradientTrack.setFill(new LinearGradient(
                    0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#f0d362")),
                    new Stop(1, Color.web("#39a2f7"))
            ));
        }

        // 겹치기
        StackPane sliderStack = new StackPane(slider);
        
        sliderStack.setAlignment(Pos.CENTER_LEFT);

        VBox box = new VBox(
                5,
                new HBox(5, l1, l2),
                buttons,
                sliderStack
        );
        box.setAlignment(Pos.CENTER);
        return box;
    }

    private void updateGradient() {
        // 기본 색상 정의
        Color red = Color.web("#fc4949");
        Color green = Color.web("#8cdb86");
        Color yellow = Color.web("#f0d362");
        Color purple = Color.web("#39a2f7");

        // 슬라이더 값이 존재하면 보간, 없으면 기본값 사용
        double redGreenValue = redGreenSlider != null ? redGreenSlider.getValue() : 0;
        double yellowPurpleValue = yellowPurpleSlider != null ? yellowPurpleSlider.getValue() : 0;

        // 색 보간
        leftColor = red.interpolate(green, redGreenValue);
        rightColor = yellow.interpolate(purple, yellowPurpleValue);

        // 테두리 색상 적용
        borderRect.setStroke(new LinearGradient(
                0, 0,
                1, 0,
                true,
                CycleMethod.NO_CYCLE,
                new Stop(0.0, leftColor),
                new Stop(0.35, leftColor),
                new Stop(0.65, rightColor),
                new Stop(1.0, rightColor)
        ));


//        borderRect.setStroke(new LinearGradient(
//                0, 0,             // 시작점 X, Y (startX, startY)
//                1, 0,             // 끝점 X, Y (endX, endY)
//                true,             // proportional
//                CycleMethod.NO_CYCLE, //색상 반복 안하기
//                new Stop(0, leftColor), // 그라디언트 시작 지점 색
//                new Stop(1, rightColor) // 그라디언트 끝 지점 색
//        ));

/*        borderRect.setStroke(new LinearGradient(
                0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                new Stop(0.0, leftColor),      // 왼쪽 전체
                new Stop(0.499, leftColor),    // 거의 중간까지 왼쪽 색
                new Stop(0.5, rightColor),     // 딱 중간에서 오른쪽 색
                new Stop(1.0, rightColor)      // 오른쪽 끝까지
        ));*/

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
                    titleLabel.setTooltip(new Tooltip(titleLabel.getText()));

                    // 앨범 이미지 설정
                    if (media.getMetadata().get("image") instanceof javafx.scene.image.Image image) {
                        System.out.println("이미지있음");
                        albumImage.setImage(image);
                    } else {
                        loadLocalAlbumImage(file); // ← 추가
                    }
                }
            });

            //볼륨 조절
            mediaPlayer.setVolume(volumeSlider.getValue());
            //미디어가 준비되면은 프로그레스바 최대값 설정
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
            mediaPlayer.setOnEndOfMedia(() -> playBtn.setText("⏸"));

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


        // 콜백 호출해서 ListView 갱신
        if (onColorUpdated != null) {
            onColorUpdated.run();
        }


        media.getMetadata().addListener((MapChangeListener<? super String, ? super Object>) change -> {
            if (change.wasAdded()) {
                String title = (String) media.getMetadata().get("title");
                String artist = (String) media.getMetadata().get("artist");

                if (title != null && artist != null) {
                    titleLabel.setText(title );
                } else if (title != null) {
                    titleLabel.setText(title );
                } else {
                    titleLabel.setText(file.getName());
                }
                
                if (artist != null) {
                    artistLabel.setText(artist);
                } else {
                    artistLabel.setText("");
                }
                titleLabel.setStyle("-fx-text-fill: black; -fx-font-size: 18px; -fx-font-weight: bold;");
                titleLabel.setTooltip(new Tooltip(titleLabel.getText()));

                if (media.getMetadata().get("image") instanceof javafx.scene.image.Image image) {
                    albumImage.setImage(image);
                } else {
                    loadLocalAlbumImage(file); // ← 추가
                }
            }
        });

        mediaPlayer.setOnReady(() -> progressBar.setMax(media.getDuration().toSeconds()));

        mediaPlayer.currentTimeProperty().addListener((obs, old, now) -> {
            if (!progressBar.isValueChanging()) {
                progressBar.setValue(now.toSeconds());
            }
        });

        mediaPlayer.setVolume(volumeSlider.getValue());

        progressBar.setOnMousePressed(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));
        progressBar.setOnMouseDragged(e -> mediaPlayer.seek(Duration.seconds(progressBar.getValue())));
        mediaPlayer.setOnEndOfMedia(() -> {
            playBtn.setText("⏸");

            if (playerListView != null) {
                File nextFile = isShuffle ? playerListView.getRandomFile() : playerListView.getNextFile();
                if (nextFile != null) {
                    playFile(nextFile);
                }
            }
        });

        playBtn.setText("⏸");
        mediaPlayer.play();
        System.out.println("음악재생중!");
        titleLabel.setTooltip(new Tooltip(titleLabel.getText()));
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


        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("알림");
        alert.setHeaderText(null);
        alert.setContentText("색상 정보가 저장 되었습니다.");
        alert.showAndWait();

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

        File colorFile = new File(musicFile.getParentFile(), baseName + ".txt");

        if (colorFile.exists()) {
            try (Scanner scanner = new Scanner(colorFile)) {

                // ✅ 1. 리스너 잠시 제거
                redGreenSlider.valueProperty().removeListener(redGreenListener);
                yellowPurpleSlider.valueProperty().removeListener(yellowPurpleListener);

                // 색상 정보 읽기
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Left Color:")) {
                        leftColor = Color.web(line.substring("Left Color:".length()).trim());
                    } else if (line.startsWith("Right Color:")) {
                        rightColor = Color.web(line.substring("Right Color:".length()).trim());
                    }
                }

                // ✅ 2. 슬라이더 값 직접 지정 (색상에 맞게)
                redGreenSlider.setValue(redGreenSliderValueFromColor(leftColor));
                yellowPurpleSlider.setValue(yellowPurpleSliderValueFromColor(rightColor));

                // ✅ 3. 테두리에 색상 반영
                updateGradient();

                // ✅ 4. 리스너 다시 등록
                redGreenSlider.valueProperty().addListener(redGreenListener);
                yellowPurpleSlider.valueProperty().addListener(yellowPurpleListener);

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

    //PlayerListVeiw랑 연결
    public void connectListView(PlayerListView listView) {
        this.playerListView = listView;
    }

    //슬라이더 그라데이션 그리기!
    private double redGreenSliderValueFromColor(Color color) {
        Color green = Color.web("#8cdb86");
        Color red = Color.web("#fc4949");
        return estimateMixRatio(green, red, color);  // 순서 반대로
    }

    private double yellowPurpleSliderValueFromColor(Color color) {
        Color purple = Color.web("#39a2f7");
        Color yellow = Color.web("#f0d362");
        return estimateMixRatio(purple, yellow, color);  // 순서 반대로
    }

    private double estimateMixRatio(Color start, Color end, Color result) {
        double distStart = colorDistance(result, start);
        double distEnd = colorDistance(result, end);
        return (distStart + distEnd == 0) ? 0 : distEnd / (distStart + distEnd);
    }

    private double colorDistance(Color c1, Color c2) {
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        double b = c1.getBlue() - c2.getBlue();
        return Math.sqrt(r * r + g * g + b * b);
    }

    //앨범아트 불러오기
    private void loadLocalAlbumImage(File audioFile) {
        File parentDir = audioFile.getParentFile();
        String baseName = audioFile.getName().substring(0, audioFile.getName().lastIndexOf('.'));

        File pngFile = new File(parentDir, baseName + ".png");
        File jpgFile = new File(parentDir, baseName + ".jpg");

        if (pngFile.exists()) {
            albumImage.setImage(new Image(pngFile.toURI().toString()));
        } else if (jpgFile.exists()) {
            albumImage.setImage(new Image(jpgFile.toURI().toString()));
        } else {
            albumImage.setImage(new Image("assets/player/empty.png")); // 기본 이미지
        }
    }}