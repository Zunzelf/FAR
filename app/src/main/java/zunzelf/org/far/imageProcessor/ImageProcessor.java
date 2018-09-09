package zunzelf.org.far.imageProcessor;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;

import com.jjoe64.graphview.series.DataPoint;

import java.lang.invoke.MethodHandles;

public class ImageProcessor {

    // retrieve all RGB from image and converts to Array of RGB
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

    // resize image using matrix transformation
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

    // Image transformation using equalization
    public Bitmap eqTransform(int[][] cfd, Bitmap bm){
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
                res.setPixel(x, y, Color.rgb(cfd[0][r],cfd[1][g],cfd[2][b]));
            }
        }
        return res;
    }

    // Image transformation by matching other histogram
    public Bitmap specTransform(int[][] cfd, int[] mask, Bitmap bm){
        Bitmap res = bm.copy(Bitmap.Config.ARGB_8888, true);
        int[][] lookUp = new int[3][256];
        int h = res.getHeight();
        int w = res.getWidth();
        res.setHasAlpha(true);
        // get normalized histograms
        double[][] normCfd = new double[3][256];
        normCfd[0] = normalize(cfd[0]);
        normCfd[1] = normalize(cfd[1]);
        normCfd[2] = normalize(cfd[2]);
        double [] mskAcc = normalize(accHist(mask, 1));
        // matching to mask
        for(int i = 0; i < 256; i++){
            lookUp[0][i] = getClosestIndex(mskAcc, normCfd[0][i]); //RED
            lookUp[1][i] = getClosestIndex(mskAcc, normCfd[1][i]); //GREEN
            lookUp[2][i] = getClosestIndex(mskAcc, normCfd[2][i]); //BLUE
        }
        // map to bitmap
        for(int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                int pixel = res.getPixel(x, y);
                int r = Color.red(pixel);
                int b = Color.blue(pixel);
                int g = Color.green(pixel);
                res.setPixel(x, y, Color.rgb(lookUp[0][r],lookUp[1][g],lookUp[2][b]));
            }
        }
        return res;
    }

    // Image smoothing using filter based on convolution
    public Bitmap smoothTransform(Bitmap bm, int size){
        Bitmap res = bm.copy(Bitmap.Config.ARGB_8888, true);
        res.setHasAlpha(true);
        ConvolutionMatrix convMatrix = new ConvolutionMatrix(size);
        convMatrix.setAll(1);
        convMatrix.Matrix[1][1] = 5;
        convMatrix.Factor = 5 + 8;
        convMatrix.Offset = 1;
        return convMatrix.computeConvolution(bm, convMatrix);
    }

    // Histogram matching single channel only
    public int[][] histMatchGray(int[] inp, int[] mask, double w){
        int[] res = inp.clone();
        int[] hist = new int[256];
        // get normalized histograms
        double[] inpAcc = normalize(accHist(inp, w));
        double [] mskAcc = normalize(accHist(mask, w));
        // matching to mask
        for(int i = 0;i < 256;i++){
            int pointer = getClosestIndex(mskAcc, inpAcc[i]);
            res[i] = pointer;
            hist[pointer] += 1;
        }
        return new int[][] {res, hist};
    }

    // Cumulative histogram single channel
    public int[] accHist(int[] origin, double weight){
        int [] res = origin.clone();
        int count = 0;
        for (int x = 1;x < res.length; x++){
            count = count + res[x];
            res[x] = res[0] + (int)Math.round(weight*count);
        }
        return res;
    }

    // Histogram equalization single channel
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
        return res;
    }


    // utilities
    public double[] normalize(int[] hist){
        int len = hist.length;
        double[] res = new double[len];
        double max = hist[len-1]+0.0;
        for (int i = 0; i < len; i++){
            res[i] = hist[i]/max;
        }
        return res;
    }
    public int[] getClosestK(int[] a, int x) {
        // returning int[] consisting {index, value}
        int low = 0;
        int high = a.length - 1;

        if (high < 0)
            throw new IllegalArgumentException("The array cannot be empty");
        while (low < high) {
            int mid = (low + high) / 2;
            assert(mid < high);
            int d1 = Math.abs(a[mid  ] - x);
            int d2 = Math.abs(a[mid+1] - x);
            if (d2 <= d1) {
                low = mid+1;
            }
            else {
                high = mid;
            }
        }
        return new int[] {high, a[high]};
    }
    public int getClosestIndex(double[] a, double x) {
        int low = 0;
        int high = a.length - 1;

        if (high < 0)
            throw new IllegalArgumentException("The array cannot be empty");
        while (low < high) {
            int mid = (low + high) / 2;
            assert(mid < high);
            double d1 = Math.abs(a[mid  ] - x);
            double d2 = Math.abs(a[mid+1] - x);
            if (d2 <= d1) {
                low = mid+1;
            }
            else {
                high = mid;
            }
        }
        return high;
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

    // experimental
    // Todo : finishing histogram generator by 3-input values
    public int[] generateHistogram(int a, int b, int c){
        int[] res = new int[256];
        double tempY = 3000.0;
        res[0] = a;
        res[255] = c;
        double x = a;
        double m = (tempY-a)/b;
        for (int i = 1; i < 255; i++){
            if(i == b){
                x = tempY*10;
                m = (c - tempY)/(255.0-b);
                Log.d("gradients", "c : "+ x +", m : "+m);
            }
            res[i] = (int) Math.round((m*i)+x);
            Log.d("gradients", "c : "+ x +", m : "+ m +", x : "+ i +", y : "+ res[i]);
        }
        return res;
    }
}
