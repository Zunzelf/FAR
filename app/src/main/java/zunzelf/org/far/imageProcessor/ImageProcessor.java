package zunzelf.org.far;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

public class ImageProcessor {

    public RGBArr RGBTOArr(Bitmap bitmap){
        RGBArr arr = new RGBArr();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        for (int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                int pixel = bitmap.getPixel(x, y);
                int r = Color.red(pixel);
                int b = Color.blue(pixel);
                int g = Color.green(pixel);
                int gr = (int) Math.round((0.3*r)+(0.3*b)+(0.3*g));
                arr.rArr[r] += 1;
                arr.bArr[b] += 1;
                arr.gArr[g] += 1;
                arr.grArr[gr] += 1;
            }
        }
        return arr;
    }

    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        if (bm == null){
            Log.d("PreProcess-LoadingImage", "Object Image Null!");
        }
        Log.d("PreProcess-LoadingImage", "resized Width  : "+newWidth);
        Log.d("PreProcess-LoadingImage", "resized Height : "+newHeight);
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }

    public DataPoint[] rgbToPlot(int[] arr){
        int n = arr.length;
        DataPoint[] values = new DataPoint[n];     //creating an object of type DataPoint[] of size 'n'
        for(int i=0;i<n;i++){
            DataPoint v = new DataPoint(i,arr[i]);
            values[i] = v;
        }
        return values;
    }

    public int[] accHist(int[] origin, double weight){
        int [] res = origin.clone();
        int count = 0;
        for (int x = 1;x < res.length; x++){
            count = count + res[x];
            res[x] = res[0] + (int)Math.round(weight*count);
        }
        return res;
    }

    public int[][] histEq(int[] origin, double weight){
        int [] cuh = accHist(origin, weight);
        int max = cuh[cuh.length-1];
        int [][] res = new int[2][cuh.length];
        for (int i = 0; i < cuh.length; i++){
            // equalized value
            double val = (cuh[i] + 0.0)/max;
            int beta = (int)Math.floor(val*255);
            // for lookup table
            res[0][i] = beta;
            // for histogram
            res[1][beta] = res[1][beta] + origin[i];
        }
        for (int i = 0; i<256; i++){
            Log.d("aaaaa", ""+res[1][i]);
            Log.d("transform", i+"->"+res[0][i]);
        }
        return res;
    }

    public Bitmap grayImgTransform(int[][] color, Bitmap bm){
        Bitmap res = bm.copy(Bitmap.Config.ARGB_8888, true);
        res.setHasAlpha(true);
        int h = res.getHeight();
        int w = res.getWidth();
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                int pixel = res.getPixel(x, y);
                int r = Color.red(pixel);
                int b = Color.blue(pixel);
                int g = Color.green(pixel);
                res.setPixel(x, y, Color.rgb(color[0][r],color[1][g],color[2][b]));
            }
        }
        return res;
    }

    public Bitmap smoothByMean(Bitmap bm, int size){
        Bitmap res = bm.copy(Bitmap.Config.ARGB_8888, true);
        res.setHasAlpha(true);
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(3);
        convMatrix.setAll(1);
        convMatrix.Matrix[1][1] = 5;
        convMatrix.Factor = 5 + 8;
        convMatrix.Offset = 1;
        return convMatrix.computeConvolution(bm, convMatrix);
    }

    public int[] histMatch(int[] inp, int[] mask){
        int[] res = inp.clone();

        return res;
    }
}
