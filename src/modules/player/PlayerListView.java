package modules.player;


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

public class PlayerListView extends VBox {

    private final ListView<File> fileListView = new ListView<>();
    private File currentDirectory;

    public PlayerListView(Stage stage, PlayerView playerView) {
        setSpacing(10);
        setPadding(new javafx.geometry.Insets(10));

        Button folderSelectBtn = new Button("폴더 선택");
        folderSelectBtn.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("음악 폴더 선택");
            File dir = chooser.showDialog(stage);

            if (dir != null && dir.isDirectory()) {
                currentDirectory = dir;
                File[] audioFiles = dir.listFiles(file ->
                        file.getName().endsWith(".mp3") || file.getName().endsWith(".wav"));

                fileListView.getItems().clear();
                if (audioFiles != null) {
                    fileListView.getItems().addAll(Arrays.asList(audioFiles));
                }
            }
        });

        fileListView.setCellFactory(param -> new ListCell<>() {
            private final ImageView thumbnail = new ImageView();

            @Override
            protected void updateItem(File file, boolean empty) {
                super.updateItem(file, empty);

                if (empty || file == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(file.getName());

                    // 이미지 불러오기
                    ImageView imageView;
                    File imageFile = new File(file.getParent(), getBaseName(file.getName()) + ".png");
                    if (imageFile.exists()) {
                        imageView = new ImageView(new Image(imageFile.toURI().toString(), 40, 40, true, true));
                    } else {
                        imageView = new ImageView(new Image("assets/player/empty.png", 40, 40, true, true));
                    }

                    // 기본 색상
                    Color leftColor = Color.LIGHTGRAY;
                    Color rightColor = Color.LIGHTGRAY;

                    // 색상 파일 읽기
                    File colorFile = new File(file.getParent(), getBaseName(file.getName()) + ".txt");
                    if (colorFile.exists()) {
                        try (java.util.Scanner scanner = new java.util.Scanner(colorFile)) {
                            while (scanner.hasNextLine()) {
                                String line = scanner.nextLine();
                                if (line.startsWith("Left Color:")) {
                                    leftColor = Color.web(line.substring("Left Color:".length()).trim());
                                } else if (line.startsWith("Right Color:")) {
                                    rightColor = Color.web(line.substring("Right Color:".length()).trim());
                                }
                            }
                        } catch (Exception e) {
                            System.out.println("색상 읽기 오류: " + e.getMessage());
                        }
                    }

                    // 테두리용 사각형
                    Rectangle border = new Rectangle(44, 44); // 이미지보다 약간 크게
                    border.setFill(null);
                    //테두리 두께
                    border.setStrokeWidth(6);
                    border.setStroke(new LinearGradient(
                            0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
                            new Stop(0.0, leftColor),
                            new Stop(0.35, leftColor),
                            new Stop(0.65 , rightColor),
                            new Stop(1.0, rightColor)
                    ));

                    // StackPane으로 이미지 + 테두리 겹치기
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

        getChildren().addAll(folderSelectBtn, fileListView);
    }

    private String getBaseName(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? filename : filename.substring(0, dotIndex);
    }

    public void refreshList() {
        fileListView.refresh();
    }
}
