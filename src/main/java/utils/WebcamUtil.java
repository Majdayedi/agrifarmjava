package utils;

import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.opencv.core.*;
import org.opencv.videoio.VideoCapture;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;


import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Timer;
import java.util.TimerTask;

public class WebcamUtil {
    private static final String FACE_DIR = "src/main/resources/faces";
    private static final String CASCADE_PATH = "src/main/resources/haarcascade_frontalface_default.xml";
    private static final String EYE_CASCADE_PATH = "src/main/resources/haarcascade_eye.xml";

    public static Mat captureAndReturnFace() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_PATH);
        VideoCapture camera = new VideoCapture(0);

        if (!camera.isOpened()) {
            System.out.println("Error: Camera not found");
            return null;
        }

        Mat frame = new Mat();
        Mat faceMat = null;

        while (true) {
            if (camera.read(frame)) {
                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(frame, faces);

                if (!faces.empty()) {
                    for (Rect rect : faces.toArray()) {
                        Mat face = new Mat(frame, rect);
                        Imgproc.resize(face, face, new Size(200, 200));
                        face = alignFace(face);
                        faceMat = face;
                        break;
                    }
                    break;
                }
            }
        }

        camera.release();
        return faceMat;
    }

    public static void captureAndSaveMultipleFacesWithPreview(String email, int numSamples, ImageView cameraView) {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        VideoCapture capture = new VideoCapture(0);

        if (!capture.isOpened()) {
            System.out.println("Camera not available");
            return;
        }

        final int[] capturedCount = {0};
        Mat frame = new Mat();
        CascadeClassifier faceDetector = new CascadeClassifier(CASCADE_PATH);
        CascadeClassifier eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH); // Load eye detector once

        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!capture.read(frame)) return;

                MatOfRect faces = new MatOfRect();
                faceDetector.detectMultiScale(frame, faces);

                for (Rect rect : faces.toArray()) {
                    Imgproc.rectangle(frame, rect.tl(), rect.br(), new Scalar(0, 255, 0), 2);

                    if (capturedCount[0] < numSamples) {
                        Mat face = new Mat(frame, rect);

                        // Align face using eyes
                        face = alignFaceUsingEyes(face, eyeDetector);

                        // Resize to standard size
                        Imgproc.resize(face, face, new Size(200, 200));

                        // Equalize histogram
                        Mat gray = new Mat();
                        Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGR2GRAY);
                        Imgproc.equalizeHist(gray, gray);
                        Imgproc.cvtColor(gray, face, Imgproc.COLOR_GRAY2BGR);

                        // Save image
                        String filePath = FACE_DIR + "/" + email + "_" + capturedCount[0] + ".jpg";
                        Imgcodecs.imwrite(filePath, face);
                        System.out.println("Captured face saved: " + filePath);

                        capturedCount[0]++;
                    }
                }

                // Show preview in UI
                Image fxImage = mat2Image(frame);
                Platform.runLater(() -> cameraView.setImage(fxImage));

                // Stop when finished
                if (capturedCount[0] >= numSamples) {
                    timer.cancel();
                    capture.release();
                    Platform.runLater(() -> System.out.println("Face capture completed."));
                }
            }
        }, 0, 2000); // Capture every 2 seconds
    }



    private static Image mat2Image(Mat frame) {
        MatOfByte buffer = new MatOfByte();
        Imgcodecs.imencode(".bmp", frame, buffer);
        return new Image(new ByteArrayInputStream(buffer.toArray()));
    }

    public static javafx.scene.image.Image convertBufferedImageToFXImage(BufferedImage bufferedImage) {
        if (bufferedImage == null) return null;
        return SwingFXUtils.toFXImage(bufferedImage, null);
    }

    private static Mat alignFace(Mat face) {
        CascadeClassifier eyeDetector = new CascadeClassifier(EYE_CASCADE_PATH);
        Mat gray = new Mat();
        Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGR2GRAY);

        MatOfRect eyes = new MatOfRect();
        eyeDetector.detectMultiScale(gray, eyes);

        Rect[] eyeArray = eyes.toArray();
        if (eyeArray.length >= 2) {
            Arrays.sort(eyeArray, Comparator.comparingInt(r -> r.x));

            Point leftEyeCenter = new Point(
                    eyeArray[0].x + eyeArray[0].width / 2.0,
                    eyeArray[0].y + eyeArray[0].height / 2.0
            );
            Point rightEyeCenter = new Point(
                    eyeArray[1].x + eyeArray[1].width / 2.0,
                    eyeArray[1].y + eyeArray[1].height / 2.0
            );

            double dy = rightEyeCenter.y - leftEyeCenter.y;
            double dx = rightEyeCenter.x - leftEyeCenter.x;
            double angle = Math.atan2(dy, dx) * 180.0 / Math.PI;

            Point center = new Point(face.width() / 2.0, face.height() / 2.0);
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
            Mat aligned = new Mat();
            Imgproc.warpAffine(face, aligned, rotationMatrix, face.size());
            return aligned;
        }

        return face;
    }
    private static Mat alignFaceUsingEyes(Mat face, CascadeClassifier eyeDetector) {
        Mat gray = new Mat();
        Imgproc.cvtColor(face, gray, Imgproc.COLOR_BGR2GRAY);
        MatOfRect eyes = new MatOfRect();
        eyeDetector.detectMultiScale(gray, eyes);

        Rect[] eyeArray = eyes.toArray();
        if (eyeArray.length >= 2) {
            Point eye1 = new Point(eyeArray[0].x + eyeArray[0].width / 2.0, eyeArray[0].y + eyeArray[0].height / 2.0);
            Point eye2 = new Point(eyeArray[1].x + eyeArray[1].width / 2.0, eyeArray[1].y + eyeArray[1].height / 2.0);

            // Determine left/right eye
            Point leftEye = eye1.x < eye2.x ? eye1 : eye2;
            Point rightEye = eye1.x < eye2.x ? eye2 : eye1;

            // Calculate angle
            double dx = rightEye.x - leftEye.x;
            double dy = rightEye.y - leftEye.y;
            double angle = Math.toDegrees(Math.atan2(dy, dx));

            // Rotate face to align eyes horizontally
            Point center = new Point(face.width() / 2.0, face.height() / 2.0);
            Mat rotationMatrix = Imgproc.getRotationMatrix2D(center, angle, 1.0);
            Mat alignedFace = new Mat();
            Imgproc.warpAffine(face, alignedFace, rotationMatrix, face.size());
            return alignedFace;
        }

        return face; // Return original if eyes not found
    }

}