package io.gestura;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import org.bytedeco.javacv.*;
import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.nio.ByteBuffer; 

public class GesturaApp extends Application {
 private volatile boolean running=false; private OpenCVFrameGrabber camera;
 private final ImageView view=new ImageView(); private final Label status=new Label("Camera stopped");
 private final Deque<Circle> dots=new ArrayDeque<>(); private AnimationTimer animation;
 @Override public void start(Stage stage){
  Label tag=new Label("GESTURA / INTERACTIVE VISION"); tag.setStyle("-fx-text-fill:#b8a5ff;-fx-font-weight:bold;");
  Label title=new Label("Move beyond the mouse."); title.setStyle("-fx-text-fill:white;-fx-font-size:28px;-fx-font-weight:bold;");
  Label subtitle=new Label("Java desktop foundation for real-time hand interaction."); subtitle.setStyle("-fx-text-fill:#b7b5c8;");
  VBox heading=new VBox(8,tag,title,subtitle); heading.setPadding(new Insets(24));
  view.setFitWidth(760);view.setFitHeight(440);view.setPreserveRatio(true);
  StackPane canvas=new StackPane(view);canvas.setMinHeight(440);canvas.setStyle("-fx-background-color:#10111c;-fx-background-radius:18;");
  canvas.setOnMouseMoved(e->{Circle c=new Circle(5,Color.web("#b8a5ff"));c.setMouseTransparent(true);c.setTranslateX(e.getX()-canvas.getWidth()/2);c.setTranslateY(e.getY()-canvas.getHeight()/2);canvas.getChildren().add(c);dots.add(c);while(dots.size()>90)canvas.getChildren().remove(dots.removeFirst());});
  Button start=new Button("Start camera"),stop=new Button("Stop camera"),clear=new Button("Clear trail");
  start.setOnAction(e->startCamera());stop.setOnAction(e->stopCamera());clear.setOnAction(e->{canvas.getChildren().removeAll(dots);dots.clear();});
  HBox controls=new HBox(12,start,stop,clear,status);controls.setPadding(new Insets(16,0,0,0));status.setStyle("-fx-text-fill:#b7b5c8;");
  VBox content=new VBox(12,canvas,controls);content.setPadding(new Insets(0,24,24,24));
  BorderPane root=new BorderPane();root.setTop(heading);root.setCenter(content);root.setStyle("-fx-background-color:#171724;-fx-font-family:Arial;");
  stage.setTitle("Gestura");stage.setScene(new Scene(root,900,680));stage.setOnCloseRequest(e->stopCamera());stage.show();
  animation=new AnimationTimer(){public void handle(long now){for(Circle c:dots){c.setRadius(Math.max(1,c.getRadius()-.12));c.setOpacity(Math.max(0,c.getOpacity()-.008));}}};animation.start();
 }
 private void startCamera(){
  if (running) {
        return;
    }

    try {
        OpenCVFrameGrabber activeCamera = new OpenCVFrameGrabber(0);
        activeCamera.start();

        camera = activeCamera;
        running = true;
        status.setText("Camera running");

        Thread worker = new Thread(() -> {
            OpenCVFrameConverter.ToMat converter =
                    new OpenCVFrameConverter.ToMat();

            try {
                while (running) {
                    Frame frame = activeCamera.grab();

                    if (frame == null || frame.image == null) {
                        continue;
                    }

                    Mat source = converter.convert(frame);

                    if (source == null || source.empty()) {
                        continue;
                    }

                    Mat rgb = new Mat();
                    cvtColor(source, rgb, COLOR_BGR2RGB);

                    int width = rgb.cols();
                    int height = rgb.rows();

                    byte[] pixels = new byte[width * height * 3];
                    ByteBuffer pixelBuffer = (ByteBuffer) rgb.createBuffer();
                    pixelBuffer.get(pixels);

                    WritableImage image =
                            new WritableImage(width, height);

                    image.getPixelWriter().setPixels(
                            0, 0, width, height,
                            PixelFormat.getByteRgbInstance(),
                            pixels, 0, width * 3
                    );

                    rgb.release();

                    Platform.runLater(() -> {
                        if (running) {
                            view.setImage(image);
                        }
                    });
                }
            } catch (Exception ex) {
                ex.printStackTrace();

                Platform.runLater(() ->
                        status.setText(
                                "Camera error: "
                                        + ex.getClass().getSimpleName()
                                        + ": " + ex.getMessage()
                        )
                );
            }
        }, "gestura-camera");

        worker.setDaemon(true);
        worker.start();

    } catch (Exception ex) {
        running = false;
        ex.printStackTrace();

        if (camera != null) {
            try {
                camera.stop();
                camera.release();
            } catch (Exception ignored) {
                // Camera may not have fully initialized.
            }

            camera = null;
        }

        status.setText(
                "Camera failed: "
                        + ex.getClass().getSimpleName()
                        + ": " + ex.getMessage()
        );
    }
} 
 private void stopCamera(){running=false;OpenCVFrameGrabber c=camera;camera=null;if(c!=null)try{c.stop();c.release();}catch(Exception ignored){}status.setText("Camera stopped");view.setImage(null);}
 @Override public void stop(){if(animation!=null)animation.stop();stopCamera();}
 public static void main(String[] args){launch(args);}
}
