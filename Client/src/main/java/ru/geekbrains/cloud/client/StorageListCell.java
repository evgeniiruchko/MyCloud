package ru.geekbrains.cloud.client;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
public class StorageListCell extends ListCell<Path> {

    private ImageView imageView = new ImageView();

    private final static Image IMAGE_FOLDER = new Image("icons/folder.png");
    private final static Image IMAGE_FILE = new Image("icons/file.png");

    @Override
    public void updateSelected(boolean selected) {
        super.updateSelected(selected);
    }

    @Override
    protected void updateItem(Path path, boolean empty) {
        super.updateItem(path, empty);
        if (empty || path == null) {
            setText(null);
            setGraphic(null);
        } else {

            if (Files.isDirectory(path))
                imageView.setImage(IMAGE_FOLDER);
            else
                imageView.setImage(IMAGE_FILE);

            setText(path.getFileName().toString());
            setGraphic(imageView);
        }
    }
}
