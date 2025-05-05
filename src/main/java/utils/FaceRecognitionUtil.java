package utils;

import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;

import java.util.Arrays;

public class FaceRecognitionUtil {

    public static boolean compareFaces(Mat img1, Mat img2) {
        if (img1.empty() || img2.empty()) {
            System.out.println("One of the images is empty");
            return false;
        }

        // Convert to grayscale and resize
        Mat gray1 = new Mat();
        Mat gray2 = new Mat();
        Imgproc.cvtColor(img1, gray1, Imgproc.COLOR_BGR2GRAY);
        Imgproc.cvtColor(img2, gray2, Imgproc.COLOR_BGR2GRAY);
        Imgproc.resize(gray1, gray1, new Size(200, 200));
        Imgproc.resize(gray2, gray2, new Size(200, 200));

        // Equalize histograms
        Imgproc.equalizeHist(gray1, gray1);
        Imgproc.equalizeHist(gray2, gray2);

        // Compute LBP images
        Mat lbp1 = computeLBP(gray1);
        Mat lbp2 = computeLBP(gray2);

        // Compute histograms
        Mat hist1 = new Mat();
        Mat hist2 = new Mat();
        Imgproc.calcHist(Arrays.asList(lbp1), new MatOfInt(0), new Mat(), hist1, new MatOfInt(256), new MatOfFloat(0, 256));
        Imgproc.calcHist(Arrays.asList(lbp2), new MatOfInt(0), new Mat(), hist2, new MatOfInt(256), new MatOfFloat(0, 256));

        Core.normalize(hist1, hist1, 0, 1, Core.NORM_MINMAX);
        Core.normalize(hist2, hist2, 0, 1, Core.NORM_MINMAX);

        // Compare histograms
        double score = Imgproc.compareHist(hist1, hist2, Imgproc.CV_COMP_CORREL);
        System.out.println("LBP Face similarity score: " + score);

        return score > 0.85; // Adjust threshold if needed
    }

    public static Mat computeLBP(Mat grayImage) {
        Mat lbpImage = Mat.zeros(grayImage.size(), CvType.CV_8UC1);

        for (int y = 1; y < grayImage.rows() - 1; y++) {
            for (int x = 1; x < grayImage.cols() - 1; x++) {
                double center = grayImage.get(y, x)[0];
                int code = 0;

                code |= (grayImage.get(y - 1, x - 1)[0] > center ? 1 : 0) << 7;
                code |= (grayImage.get(y - 1, x)[0] > center ? 1 : 0) << 6;
                code |= (grayImage.get(y - 1, x + 1)[0] > center ? 1 : 0) << 5;
                code |= (grayImage.get(y, x + 1)[0] > center ? 1 : 0) << 4;
                code |= (grayImage.get(y + 1, x + 1)[0] > center ? 1 : 0) << 3;
                code |= (grayImage.get(y + 1, x)[0] > center ? 1 : 0) << 2;
                code |= (grayImage.get(y + 1, x - 1)[0] > center ? 1 : 0) << 1;
                code |= (grayImage.get(y, x - 1)[0] > center ? 1 : 0);

                lbpImage.put(y, x, code);
            }
        }

        return lbpImage;
    }


}