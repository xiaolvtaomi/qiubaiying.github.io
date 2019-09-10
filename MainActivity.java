package com.baidu.paddle.lite;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String textOutput = "";
        Tensor output;

        Date start = new Date();
        output = setInputAndRunImageModel("ocr_optimize", this);
        Date end = new Date();
        textOutput += "\nocr_optimize test: " + testOcrOutput(output) + "\n";
        textOutput += "time: " + (end.getTime() - start.getTime()) + " ms\n";

        TextView textView = findViewById(R.id.text_view);
        textView.setText(textOutput);
    }

    public static String copyFromAssetsToCache(String modelPath, Context context) {
        String newPath = context.getCacheDir() + "/" + modelPath;
        // String newPath = "/sdcard/" + modelPath;
        File desDir = new File(newPath);

        try {
            if (!desDir.exists()) {
                desDir.mkdir();
            }
            for (String fileName : context.getAssets().list(modelPath)) {
                InputStream stream = context.getAssets().open(modelPath + "/" + fileName);
                OutputStream output = new BufferedOutputStream(new FileOutputStream(newPath + "/" + fileName));

                byte data[] = new byte[1024];
                int count;

                while ((count = stream.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                output.flush();
                output.close();
                stream.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return desDir.getPath();
    }

    public static Tensor runModel(String modelName, long[] dims, float[] inputBuffer, Context context) {
        String modelPath = copyFromAssetsToCache(modelName, context);

        Date start = new Date();

        MobileConfig config = new MobileConfig();
        config.setModelDir(modelPath);
        config.setPowerMode(PowerMode.LITE_POWER_HIGH);
        config.setThreads(1);
        PaddlePredictor predictor = PaddlePredictor.createPaddlePredictor(config);
        Date end = new Date();
        Log.e("XXX","load predictor time: " + (end.getTime() - start.getTime()) + " ms\n");
        start = new Date();


        Tensor input = predictor.getInput(1);
        input.resize(dims);
        input.setData(inputBuffer);

        Tensor inputTensor_init_ids = predictor.getInput(0);
        float[] init_ids = new float[11];
        long[] inputShape_ids = new long[]{1, 1};
        inputTensor_init_ids.resize(inputShape_ids);
        inputTensor_init_ids.setData(init_ids);

        Tensor inputTensor_init_scores = predictor.getInput(2);
        float[] init_scores = new float[11];
        long[] inputShape_scores = new long[]{1, 1};
        inputTensor_init_scores.resize(inputShape_scores);
        inputTensor_init_scores.setData(init_scores);


        predictor.run();

        Tensor output = predictor.getOutput(0);
        end = new Date();
        Log.e("XXX","run predictor time: " + (end.getTime() - start.getTime()) + " ms\n");

        return output;
    }

    /**
     * Input size is 3 * 224 * 224
     *
     * @param modelName
     * @return
     */
    public static Tensor setInputAndRunImageModel(String modelName, Context context) {
        long[] dims = {1, 1, 100, 380};
        int item_size = 1 * 100 * 380;
        float[] inputBuffer = new float[item_size];
        for (int i = 0; i < item_size; ++i) {
            inputBuffer[i] = 1;
        }
        return runModel(modelName, dims, inputBuffer, context);
    }

    public boolean equalsNear(float a, float b, float delta) {
        return a >= b - delta && a <= b + delta;
    }

    public boolean expectedResult(float[] expected, Tensor result) {
        if (expected.length != 20) {
            return false;
        }

        long[] shape = result.shape();

        if (shape.length != 2) {
            return false;
        }

        if (shape[0] != 1 || shape[1] != 1000) {
            return false;
        }

        float[] output = result.getFloatData();

        if (output.length != 1000) {
            return false;
        }

        int step = 50;
        for (int i = 0; i < expected.length; ++i) {
            if (!equalsNear(output[i * step], expected[i], 1e-6f)) {
                return false;
            }
        }

        return true;
    }

    public boolean testOcrOutput(Tensor output) {
//        float[] expected = {0.00017082224f, 5.699624e-05f, 0.000260885f, 0.00016412718f,
//                0.00034818667f, 0.00015230637f, 0.00032959113f, 0.0014772735f,
//                0.0009059976f, 9.5378724e-05f, 5.386537e-05f, 0.0006427285f,
//                0.0070957416f, 0.0016094646f, 0.0018807327f, 0.00010506048f,
//                6.823785e-05f, 0.00012269315f, 0.0007806194f, 0.00022354358f};
//        return expectedResult(expected, output);
        return false ;
    }

}

