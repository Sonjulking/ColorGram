package modules.player;


import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class PlayerListView extends VBox {

    private final ListView<File> fileListView = new ListView<>();
    private File currentDirectory;

    //색깔 선택 슬라이더
    private final Slider redGreenSlider = new Slider(0, 1, 0.5);
    private final Slider yellowPurpleSlider = new Slider(0, 1, 0.5);


    private final File lastFolderFile = new File("lastFolderFile.txt");

    public PlayerListView(Stage stage, PlayerView playerView) {
        setSpacing(10);
        setPadding(new javafx.geometry.Insets(10));

        Button folderSelectBtn = new Button("📂");

        File lastDir = loadLastFolder();
        if (lastDir != null) {
            loadDirectory(lastDir);
        }

        folderSelectBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("음악 폴더 선택"); //다이얼로그 상단 제목

            File dir = chooser.showDialog(stage); //선택한 폴더를 File객체로 가져옴.

            if (dir != null && dir.isDirectory()) { //폴더를 선택하고, 폴더일 경우
                saveLastFolder(dir);
                loadDirectory(dir);
            }
        });


        // 슬라이더 UI와 버튼 추가
        Button redBtn = createColorButton("#fc4949", () -> redGreenSlider.setValue(0));
        Button greenBtn = createColorButton("#8cdb86", () -> redGreenSlider.setValue(1));
        Button yellowBtn = createColorButton("#f0d362", () -> yellowPurpleSlider.setValue(0));
        Button purpleBtn = createColorButton("#39a2f7", () -> yellowPurpleSlider.setValue(1));

        //슬라이더 크기 설정
        redGreenSlider.setPrefWidth(100);
        yellowPurpleSlider.setPrefWidth(100);
        // 버튼 스타일 적용
        redBtn.setStyle("-fx-background-color: #fc4949; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");
        greenBtn.setStyle("-fx-background-color: #8cdb86; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");
        yellowBtn.setStyle("-fx-background-color: #f0d362; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");
        purpleBtn.setStyle("-fx-background-color: #39a2f7; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: rgba(0, 0, 0, 0.25); -fx-border-width: 2px;");

        HBox buttonRow = new HBox(10, redBtn, greenBtn);
        buttonRow.setAlignment(Pos.CENTER);

        VBox redGreenBox = new VBox(5, buttonRow, redGreenSlider);
        redGreenBox.setAlignment(Pos.CENTER);


        HBox buttonRow2 = new HBox(10, yellowBtn, purpleBtn);
        buttonRow2.setAlignment(Pos.CENTER);

        VBox yellowBlueBox = new VBox(5, buttonRow2, yellowPurpleSlider);
        yellowBlueBox.setAlignment(Pos.CENTER);


        HBox sliderRow = new HBox(40, redGreenBox, yellowBlueBox);
        sliderRow.setSpacing(80); // 두 박스 사이 간격 벌리기!
        //obs : 변화 감지하는 객체
        redGreenSlider.valueProperty().addListener((obs, oldVal, newVal) -> refreshFilteredList());
        yellowPurpleSlider.valueProperty().addListener((obs, oldVal, newVal) -> refreshFilteredList());

        sliderRow.setAlignment(Pos.CENTER);
        fileListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);

                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(file.getName());

                    File baseDir = file.getParentFile();
                    String baseName = getBaseName(file.getName());

                    File pngFile = new File(baseDir, baseName + ".png");
                    File jpgFile = new File(baseDir, baseName + ".jpg");

                    File imageFile = pngFile.exists() ? pngFile :
                            (jpgFile.exists() ? jpgFile : null);


                    ImageView imageView = new ImageView(new Image(
                            imageFile != null ? imageFile.toURI().toString() : "assets/player/empty.png",
                            40, 40, true, true
                    ));

                    Color[] colors = readColorsFromFile(file);
                    Color leftColor = colors[0], rightColor = colors[1];

                    Rectangle border = new Rectangle(44, 44);
                    border.setFill(null);
                    border.setStrokeWidth(6);
                    border.setStroke(new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0.0, leftColor),
                            new Stop(0.35, leftColor),
                            new Stop(0.65, rightColor),
                            new Stop(1.0, rightColor)
                    ));

                    StackPane imageBox = new StackPane(border, imageView);
                    setGraphic(imageBox);
                }
            }
        });


        // 항목 클릭 시 PlayerView로 재생
        fileListView.setOnMouseClicked(e -> {
            File selected = fileListView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                playerView.playFile(selected);
            }
        });


        getChildren().addAll(folderSelectBtn, sliderRow, fileListView);
    }

    //.앞에 이름..
    private String getBaseName(String filename) {
        int dotIndex = filename.lastIndexOf('.');

        if (dotIndex != -1) {
            return filename.substring(0, dotIndex);
        } else {
            return filename;
        }
    }

    //ListView getter
    public ListView<File> getFileListView() {
        return fileListView;
    }

    // PlayerListView.java에 추가
    public File getNextFile() {
        int currentIndex = fileListView.getSelectionModel().getSelectedIndex();
        int size = fileListView.getItems().size();

        if (size == 0) return null;

        // 선택된 항목이 없으면 첫 번째 항목 반환
        if (currentIndex == -1) {
            fileListView.getSelectionModel().select(0);
            return fileListView.getItems().get(0);
        }

        // 다음 항목이 있으면 반환
        if (currentIndex < size - 1) {
            fileListView.getSelectionModel().select(currentIndex + 1);
            return fileListView.getItems().get(currentIndex + 1);
        }

        // 다음 항목이 없으면 null 반환 (또는 첫 번째 항목으로 순환)
        // 순환하려면 아래 주석 해제
        // fileListView.getSelectionModel().select(0);
        // return fileListView.getItems().get(0);

        return null;
    }

    public File getPreviousFile() {
        int currentIndex = fileListView.getSelectionModel().getSelectedIndex();
        int size = fileListView.getItems().size();

        if (size == 0) return null;

        // 선택된 항목이 없으면 첫 번째 항목 반환
        if (currentIndex == -1) {
            fileListView.getSelectionModel().select(0);
            return fileListView.getItems().get(0);
        }

        // 이전 항목이 있으면 반환
        if (currentIndex > 0) {
            fileListView.getSelectionModel().select(currentIndex - 1);
            return fileListView.getItems().get(currentIndex - 1);
        }

        // 이전 항목이 없으면 null 반환 (또는 마지막 항목으로 순환)
        // 순환하려면 아래 주석 해제
        // fileListView.getSelectionModel().select(size - 1);
        // return fileListView.getItems().get(size - 1);

        return null;
    }

    //새로고침
    public void refreshList() {
        fileListView.refresh();
    }

    public File getRandomFile() {
        int size = fileListView.getItems().size();
        if (size == 0) return null;

        int randomIndex = new java.util.Random().nextInt(size);
        fileListView.getSelectionModel().select(randomIndex);
        return fileListView.getItems().get(randomIndex);
    }

    //컬러간 거리 계산...
    //숫자가 작아야 비슷한 컬러
    private double colorDistance(Color c1, Color c2) {
        double r = c1.getRed() - c2.getRed();
        double g = c1.getGreen() - c2.getGreen();
        double b = c1.getBlue() - c2.getBlue();
        return Math.sqrt(r * r + g * g + b * b);
    }

    //버튼 생성
    private Button createColorButton(String colorHex, Runnable onClick) {
        Button btn = new Button();
        btn.setStyle("-fx-background-color: " + colorHex + "; -fx-min-width: 40px; -fx-min-height: 40px; -fx-border-color: gray; -fx-border-width: 1px;");
        btn.setOnAction(e -> onClick.run());
        return btn;
    }

    private void refreshFilteredList() {
        if (currentDirectory == null) {
            return;
        }

        File[] audioFiles = currentDirectory.listFiles(file -> file.getName().endsWith(".mp3") || file.getName().endsWith(".wav"));
        //중간색 출력
        Color targetLeft = interpolate(Color.web("#fc4949"), Color.web("#8cdb86"), redGreenSlider.getValue());
        Color targetRight = interpolate(Color.web("#f0d362"), Color.web("#39a2f7"), yellowPurpleSlider.getValue());

        //audioFiles 배열을 Stream으로 변환!
        List<File> sorted = Arrays.stream(audioFiles)
                                  .sorted(Comparator.comparingDouble(file -> { //sorted : 정렬 기준정하기 여기는 숫자(double값 기준으로 정렬)
                                      Color[] colors = readColorsFromFile(file);//좌우색 불러오고...
                                      //왼쪽 컬러간 길이
                                      double leftDist = colorDistance(targetLeft, colors[0]);
                                      //오른쪽 컬러간 길이
                                      double rightDist = colorDistance(targetRight, colors[1]);

                                      //두 거리의 합이 작을 수록 슬라이더와 비슷한 색
                                      return leftDist + rightDist;
                                  }))
                                  .collect(Collectors.toList()); //정렬된 스트림을 리스트로 변환

        fileListView.getItems().setAll(sorted); //정렬된 리스트를 화면에 보여지는 ListView에 적용

    }

    //중간색!!
    private Color interpolate(Color c1, Color c2, double t) {
        return c1.interpolate(c2, t);
    }

    private Color[] readColorsFromFile(File file) {
        Color left = Color.LIGHTGRAY;
        Color right = Color.LIGHTGRAY;
        File colorFile = new File(file.getParent(), getBaseName(file.getName()) + ".txt");
        if (colorFile.exists()) {
            try (Scanner scanner = new Scanner(colorFile)) {
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (line.startsWith("Left Color:")) {
                        left = Color.web(line.substring("Left Color:".length()).trim());
                    } else if (line.startsWith("Right Color:")) {
                        right = Color.web(line.substring("Right Color:".length()).trim());
                    }
                }
            } catch (Exception e) {
                System.out.println("색상 읽기 오류: " + e.getMessage());
            }
        }
        //좌우색 리턴...
        return new Color[]{left, right};
    }

    private void saveLastFolder(File folder) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(lastFolderFile)) {
            writer.println(folder.getAbsolutePath());
        } catch (Exception e) {
            System.out.println("폴더 저장 실패: " + e.getMessage());
        }
    }

    private File loadLastFolder() {
        if (lastFolderFile.exists()) {
            try (Scanner scanner = new Scanner(lastFolderFile)) {
                if (scanner.hasNextLine()) {
                    File folder = new File(scanner.nextLine());
                    if (folder.exists() && folder.isDirectory()) {
                        return folder;
                    }
                }
            } catch (Exception e) {
                System.out.println("폴더 불러오기 실패: " + e.getMessage());
            }
        }
        return null;
    }

    private void loadDirectory(File dir) {
        currentDirectory = dir;

        File[] audioFiles = dir.listFiles(file ->
                file.getName().endsWith(".mp3") || file.getName().endsWith(".wav"));

        fileListView.getItems().clear();
        if (audioFiles != null) {
            fileListView.getItems().addAll(Arrays.asList(audioFiles));
        }
    }


}
