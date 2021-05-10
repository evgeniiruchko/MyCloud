package ru.geekbrains.cloud.client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;
import ru.geekbrains.cloud.common.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainController implements Initializable {
    @FXML
    ListView<Path> localStorage, cloudStorage;

    @FXML
    Label filesDragAndDrop, labelDragWindow;

    @FXML
    VBox mainVBox;

    @FXML
    StackPane mainStackPane;


    private final static String DEFAULT_LOCAL_FOLDER = "Files/localStorage";
    private byte[] data = new byte[FileCommand.PART_SIZE];

    private Comparator<Path> fileListComparator = (p1, p2) -> Boolean.compare(Files.isDirectory(p2), Files.isDirectory(p1));

    private static Deque<Path> currentLocalDir = new ArrayDeque<>();
    private static Deque<Path> currentCloudDir = new ArrayDeque<>();

    double dragDeltaX, dragDeltaY;

    static {
        clearLocalStorage();
        clearCloudStorage();
    }

    private static void clearCloudStorage() {
        currentCloudDir.clear();
        currentCloudDir.add((Path) Paths.get("Files/cloudStorage"));
    }

    private static void clearLocalStorage() {
        currentLocalDir.clear();
        currentLocalDir.add((Path) Paths.get(DEFAULT_LOCAL_FOLDER));
    }

    public void btnExit() {
        System.exit(0);
    }

    public void uploadFiles() {
        localStorage.getSelectionModel().getSelectedItems().forEach(path -> {
            Client.sendCommand(new UploadRequest(path.toString()));
            System.out.println("Попытка открытия файла: " + path);
        });
        refreshCloudFilesList();
    }

    public void downloadFiles() {
        cloudStorage.getSelectionModel().getSelectedItems().forEach(filename -> {
            Client.sendCommand(new FileRequest(filename.toString()));
            System.out.println("Попытка открытия файла: " + filename);
        });
        cloudStorage.getSelectionModel().clearSelection();
        refreshLocalFilesList();
    }

    public void renameFile() {
        Path src = cloudStorage.getSelectionModel().getSelectedItem();

        TextInputDialog dialog = new TextInputDialog(src.getFileName().toString());

        dialog.setTitle("Переименовать файл");
        dialog.setHeaderText("Введите новое имя файла");

        Optional<String> result;
        AtomicBoolean moved = new AtomicBoolean(false);
        do {
            result = dialog.showAndWait();

            result.ifPresent(name -> {
                if (!Files.exists(Paths.get(src.toString()).subpath(0, src.getNameCount() - 1).resolve(name))) {
                    try {
                        Files.move(src, src.resolveSibling(name));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    moved.set(true);
                } else {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "File already exists!");
                    alert.showAndWait();
                }
            });
        } while (!moved.get() && result.isPresent());
    }

    public void deleteFiles() {
        cloudStorage.getSelectionModel().getSelectedItems().forEach(filename -> {
            Client.sendCommand(new DeleteRequest(filename.toString()));
            System.out.println("Попытка удаления файла: " + filename);
        });
        refreshCloudFilesList();
    }

    public void logout(ActionEvent actionEvent) {
    }

    public void refreshLocalFilesList() {
        updateUI(() -> {
            try {
                localStorage.getItems().clear();
//                if (currentLocalDir.size() > 1) {
//                    localStorage.getItems().add(currentLocalDir.peek());
//                }
                if (currentLocalDir.peekLast() != null) {
                    Files.list(Paths.get(currentLocalDir.peekLast().toString()))
                            .sorted(fileListComparator)
                            .forEach(localStorage.getItems()::add);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void localStorageAction(@NotNull MouseEvent mouseEvent) {
        Path selected = localStorage.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && Files.isDirectory(selected)) {
            currentLocalDir.add(selected);
            refreshLocalFilesList();
        }
    }

    public void refreshCloudFilesList() {
        updateUI(() -> {
            try {
                cloudStorage.getItems().clear();
                if (currentCloudDir.peekLast() != null) {
                    Files.list(Paths.get(currentCloudDir.peekLast().toString()))
                            .sorted(fileListComparator)
                            .forEach(cloudStorage.getItems()::add);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void cloudStorageAction(@NotNull MouseEvent mouseEvent) {
        Path selected = cloudStorage.getSelectionModel().getSelectedItem();
        if (mouseEvent.getClickCount() == 2 && Files.isDirectory(selected)) {
            currentCloudDir.add(selected);
            refreshCloudFilesList();
        }
    }

    public void btnShow2SceneStage(ActionEvent actionEvent) {
        Parent root = null;
        try {
            root = FXMLLoader.load(getClass().getResource("/Scene1.fxml"));
            Stage stage = new Stage();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initializeLocalStorageListView();
        initializeCloudStorageListView();
        initializeDragAndDropLabel();
        initializeListeningThread();
    }

    private void initializeListeningThread() {
        Thread t = new Thread(() -> {
            try {
                while (true) {
                    Commands command = Client.readObject();
                    if (command.isType(Commands.CommandType.FILE)) {
                        FileCommand fc = (FileCommand) command;
                        receiveFile(fc);

                        if (fc.getNumberPart() == fc.getCountParts()) {
                            System.out.println("Загрузка завершена: " + fc.getPath());
                            refreshLocalFilesList();
                        }
                    } else if (command.isType(Commands.CommandType.ACCEPT)) {
                        System.out.println("Upload accepted by server");
                        AcceptCommand ac = (AcceptCommand) command;
                        Path path = Paths.get(ac.getPath());
                        FileCommand fc = new FileCommand(path);

                        uploadFile(fc, path);
                        System.out.println("Выгрузка завершена: " + fc.getPath());
                    } else if (command.isType(Commands.CommandType.FILE_LIST)) {
                        FileListCommand flm = (FileListCommand) command;
                        clearCloudStorage();
                        while (flm.getFileList().size() > 0) {
                            currentCloudDir.add(flm.getFileList().pollFirst());
                        }
                        refreshCloudFilesList();
                    } else
                        System.out.println(command);
                }
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            } finally {
                Client.stop();
            }
        });
        t.setDaemon(true);
        t.start();
    }

    private void uploadFile(FileCommand fc, Path path) throws IOException {
        if (Files.isDirectory(path)) {
            Files.walkFileTree(path, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    uploadFile(fc, file);
                    return FileVisitResult.CONTINUE;
                }
            });
        } else {
            InputStream is = Files.newInputStream(path);
            int dataSize;
            while (is.available() > 0) {
                dataSize = is.read(data);
                if (dataSize == FileCommand.PART_SIZE) {
                    fc.setData(data);
                    Client.sendCommand(fc);
                } else {
                    byte[] lastData = new byte[dataSize];
                    System.arraycopy(data, 0, lastData, 0, dataSize);
                    fc.setData(lastData);
                    Client.sendCommand(fc);
                }
            }
        }
    }

    private void initializeDragAndDropLabel() {
        filesDragAndDrop.setOnDragOver(event -> {
            if (event.getGestureSource() != filesDragAndDrop && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });

        filesDragAndDrop.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                filesDragAndDrop.setText("");
                for (File o : db.getFiles()) {
                    filesDragAndDrop.setText(filesDragAndDrop.getText() + o.getAbsolutePath() + " ");
                }
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });
    }

    private void initializeCloudStorageListView() {
        cloudStorage.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshCloudFilesList();
        cloudStorage.setCellFactory(storageListView -> new StorageListCell());
    }

    private void initializeLocalStorageListView() {
        localStorage.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        refreshLocalFilesList();
        localStorage.setCellFactory(storageListView -> new StorageListCell());
    }
    private static void updateUI(Runnable r) {
        if (Platform.isFxApplicationThread()) {
            r.run();
        } else {
            Platform.runLater(r);
        }
    }

    private void receiveFile(FileCommand fc) throws IOException {
        Path filePath = Paths.get(DEFAULT_LOCAL_FOLDER + fc.getRootPath());
        Path folderForFile = filePath.subpath(0, filePath.getNameCount() - 1);
        if (!Files.exists(folderForFile))
            Files.createDirectories(folderForFile);

        if (Files.isDirectory(filePath))
            Files.createDirectories(filePath);
        else {
            if (fc.getNumberPart() == 1)
                Files.write(filePath, fc.getData(), StandardOpenOption.CREATE);
            else
                Files.write(filePath, fc.getData(), StandardOpenOption.APPEND);
        }
    }

    public void initializeWindowDragAndDropLabel() {
        Platform.runLater(() -> {
            Stage stage = (Stage) mainVBox.getScene().getWindow();

            labelDragWindow.setOnMousePressed(mouseEvent -> {
                dragDeltaX = stage.getX() - mouseEvent.getScreenX();
                dragDeltaY = stage.getY() - mouseEvent.getScreenY();
            });
            labelDragWindow.setOnMouseDragged(mouseEvent -> {
                stage.setX(mouseEvent.getScreenX() + dragDeltaX);
                stage.setY(mouseEvent.getScreenY() + dragDeltaY);
            });
        });
    }

    private void initializeSceneStyle() {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                mainStackPane.setPadding(new Insets(20, 20, 20, 20));
                mainStackPane.getChildren().get(0).setEffect(new DropShadow(15, Color.BLACK));
            }
        });
    }
    private static boolean isDirEmpty(final Path directory) throws IOException {
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }
}
