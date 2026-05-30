package org.tensorflow.lite.examples.shravan.tflite;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.tensorflow.lite.Interpreter;
import android.content.res.AssetFileDescriptor;

public class TFLiteImageClassifier implements Classifier {
    private Interpreter interpreter;
    private int inputSize;
    private List<String> labels;
    private boolean isQuantized;
    private int numClasses;

    private TFLiteImageClassifier() {}

    public static TFLiteImageClassifier create(
            AssetManager assetManager,
            String modelFilename,
            String labelFilename,
            int inputSize,
            boolean isQuantized) throws IOException {
        TFLiteImageClassifier classifier = new TFLiteImageClassifier();
        classifier.labels = loadLabels(assetManager, labelFilename);
        classifier.interpreter = new Interpreter(loadModelFile(assetManager, modelFilename));
        classifier.inputSize = inputSize;
        classifier.isQuantized = isQuantized;

        int[] outputShape = classifier.interpreter.getOutputTensor(0).shape(); // e.g., [1, numClasses]
        classifier.numClasses = outputShape[outputShape.length - 1];

        return classifier;
    }

    private static MappedByteBuffer loadModelFile(AssetManager assetManager, String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = assetManager.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    private static List<String> loadLabels(AssetManager assetManager, String labelFilename) throws IOException {
        List<String> labels = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(labelFilename.replace("file:///android_asset/", ""))));
        String line;
        while ((line = reader.readLine()) != null) {
            labels.add(line);
        }
        reader.close();
        return labels;
    }

    @Override
    public List<Recognition> recognizeImage(Bitmap bitmap) {
        ByteBuffer imgData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * (isQuantized ? 1 : 4));
        imgData.order(ByteOrder.nativeOrder());
        int[] intValues = new int[inputSize * inputSize];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        imgData.rewind();
        for (int i = 0; i < inputSize; ++i) {
            for (int j = 0; j < inputSize; ++j) {
                int pixelValue = intValues[i * inputSize + j];
                if (isQuantized) {
                    imgData.put((byte) ((pixelValue >> 16) & 0xFF));
                    imgData.put((byte) ((pixelValue >> 8) & 0xFF));
                    imgData.put((byte) (pixelValue & 0xFF));
                } else {
                    imgData.putFloat((((pixelValue >> 16) & 0xFF) - 0f) / 255f);
                    imgData.putFloat((((pixelValue >> 8) & 0xFF) - 0f) / 255f);
                    imgData.putFloat(((pixelValue & 0xFF) - 0f) / 255f);
                }
            }
        }

        float[][] output = new float[1][numClasses];
        interpreter.run(imgData, output);

        PriorityQueue<Recognition> pq = new PriorityQueue<>(
                3,
                new Comparator<Recognition>() {
                    @Override
                    public int compare(Recognition lhs, Recognition rhs) {
                        return Float.compare(rhs.getConfidence(), lhs.getConfidence());
                    }
                });

        for (int i = 0; i < numClasses; ++i) {
            pq.add(new Recognition("" + i, labels.size() > i ? labels.get(i) : "unknown", output[0][i], null, i));
        }

        final ArrayList<Recognition> recognitions = new ArrayList<>();
        int recognitionsSize = Math.min(pq.size(), 3);
        for (int i = 0; i < recognitionsSize; ++i) {
            recognitions.add(pq.poll());
        }
        return recognitions;
    }

    @Override
    public void enableStatLogging(boolean debug) {}

    @Override
    public String getStatString() { return ""; }

    @Override
    public void close() { interpreter.close(); }

    @Override
    public void setNumThreads(int num_threads) {
        // Threads are set during interpreter creation via Options
    }

    @Override
    public void setUseNNAPI(boolean isChecked) {}

    @Override
    public float getObjThresh() { return 0.5f; }
}
