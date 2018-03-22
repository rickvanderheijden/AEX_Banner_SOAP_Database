package bannerclient;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.rmi.RemoteException;

public class AEXBannerClient extends Application {

    private static final double TEXT_POSITION_Y = 20.0;
    private static final int WIDTH = 1000;
    private static final int HEIGHT = 160;
    private static final int NANO_TICKS = 20000000;

    private static final double TEXT_SPEED = 5.0;

    private Text text;
    private double textLength;
    private double textPositionX;
    private Label labelRequestType;
    private BannerController controller;
    private AnimationTimer animationTimer;

    @Override
    public void start(Stage primaryStage) throws RemoteException {

        Font font = new Font("Arial", HEIGHT - 30);
        text = new Text();
        text.setFont(font);
        text.setFill(Color.BLACK);

        Pane root = new Pane();
        Button buttonRequestType = new Button("Toggle request");
        buttonRequestType.setOnAction(e -> {
            if (controller != null) {
                controller.toggleRequestType();
            }
        });

        labelRequestType = new Label();
        HBox hBox = new HBox();
        hBox.setAlignment(Pos.CENTER_LEFT);
        hBox.setSpacing(10.0);
        hBox.getChildren().addAll(buttonRequestType, labelRequestType);
        root.getChildren().addAll(text, hBox);

        Scene scene = new Scene(root, WIDTH, HEIGHT + 20);

        primaryStage.setTitle("AEX banner");
        primaryStage.setScene(scene);
        primaryStage.show();
        primaryStage.toFront();

        // Start animation: text moves from right to left
        animationTimer = new AnimationTimer() {
            private long prevUpdate;

            @Override
            public void handle(long now) {
                long lag = now - prevUpdate;
                if (lag >= NANO_TICKS) {
                    textPositionX -= TEXT_SPEED;
                    text.relocate(textPositionX,TEXT_POSITION_Y);
                    prevUpdate = now;
                }

                if ((textPositionX + textLength < TEXT_POSITION_Y)) {
                    textPositionX = WIDTH;
                }
            }

            @Override
            public void start() {
                prevUpdate = System.nanoTime();
                textPositionX = WIDTH;
                text.relocate(textPositionX, TEXT_POSITION_Y);
                setKoersen("Nothing to display");
                super.start();
            }
        };
        animationTimer.start();

        controller = new BannerController(this);
    }

    public void setKoersen(final String koersen) {
        Platform.runLater(() -> {
            text.setText(koersen);
            textLength = text.getLayoutBounds().getWidth();
        });
    }

    @Override
    public void stop() {
        animationTimer.stop();
        controller.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }

    public void setText(String text) {
        Platform.runLater(() -> labelRequestType.setText(text));
    }
}
