package zunzelf.org.far.pager;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.SeekBar;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.IOException;
import java.util.Arrays;

import zunzelf.org.far.R;
import zunzelf.org.far.imageProcessor.ImageProcessor;
import zunzelf.org.far.imageProcessor.RGBArr;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    private static final String TAG = "MainActivity";
    ImageView imageView;
    ProgressDialog myDialog;
    Uri imageURI;
    Bitmap bitmap, mBm;
    RGBArr rgbArr;
    ScrollView sv;
    SeekBar seekbar;
    int[] cfd, cug, dummyArr;
    int step = 1;
    int max = 180;
    int min = 60;
    double weight;
    boolean init = false;
    ImageProcessor proc = new ImageProcessor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        weight = 0.5;
        sv = (ScrollView) findViewById(R.id.scrollView2);
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        viewPager.setAdapter(new CustomPagerAdapter(this));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        imageView = (ImageView) findViewById(R.id.imageView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGallery(){
        Intent gallery =  new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        seekbar = (SeekBar) findViewById(R.id.seekBar);
        if (!init) {
            seekbar.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            Log.d("seekBar", "weight @"+weight);
                            updateImage(weight);
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                        }

                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress,
                                                      boolean fromUser) {
                            // Ex :
                            // And finally when you want to retrieve the value in the range you
                            // wanted in the first place -> [3-5]
                            //
                            // if progress = 13 -> value = 3 + (13 * 0.1) = 4.3

                            Log.d("seekBar", "progress @"+progress);
                            weight = (progress+0.0)/100.0;
                        }
                    }
            );
            init = true;
        }

        if(resultCode== RESULT_OK && requestCode == PICK_IMAGE){
            // Load Image File
            imageURI = data.getData();
//            imageView.setImageURI(imageURI); //to show image -> will uncomment when layout sorted
            // Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            bitmap = null;
            Log.d(TAG, "Load image");
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
                bitmap = proc.getResizedBitmap(bitmap, 300, 300);
                imageView = (ImageView) findViewById(R.id.imageView);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.d(TAG, "Image get!");
            // > Image Processes :
            // Get RGBs value per-pixel
            rgbArr = proc.RGBTOArr(bitmap);
            // Transform Images
            updateImage(weight);
            // plotting to Graph :
            GraphView graph;
            graph = (GraphView) findViewById(R.id.rGraph); //RED
            setBarGraphSeries(graph, rgbArr.rArr, Color.RED);
            graph = (GraphView) findViewById(R.id.gGraph); //GREEN
            setBarGraphSeries(graph, rgbArr.gArr, Color.GREEN);
            graph = (GraphView) findViewById(R.id.bGraph); //BLUE
            setBarGraphSeries(graph, rgbArr.bArr, Color.BLUE);
            graph = (GraphView) findViewById(R.id.grGraph); //GRAY
            setBarGraphSeries(graph, rgbArr.grArr, Color.GRAY);

            graph = (GraphView) findViewById(R.id.acGraph); //cumHist
            graph.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            setBarGraphSeries(graph, cug, Color.GRAY);

            graph = (GraphView) findViewById(R.id.eqGraph); //eqHist
            graph.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            setBarGraphSeries(graph, cfd, Color.MAGENTA);

            graph = (GraphView) findViewById(R.id.maskGraph); //Filter
            graph.setBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            setBarGraphSeries(graph, dummyArr, Color.GRAY);
        }

    }

    private void updateImage(double weight){

        // get cummulative histogram
        cug = proc.accHist(rgbArr.grArr, weight);
        cfd = proc.histEq(rgbArr.grArr, weight)[0];

        // get equalized histogram(RGB)
        int[][] cfdRGB = new int[3][256];
        cfdRGB[0] = proc.histEq(rgbArr.rArr, weight)[0]; // RED
        cfdRGB[1] = proc.histEq(rgbArr.gArr, weight)[0]; // GREEN
        cfdRGB[2] = proc.histEq(rgbArr.bArr, weight)[0]; // BLUE

        // get equalized image
        mBm = proc.eqTransform(cfdRGB, bitmap); //equalized image
        imageView = (ImageView) findViewById(R.id.imageView2);
        imageView.setImageBitmap(mBm);
        Log.d("update Image", "equalized Image update! weight@"+weight);
        // get smoothed image
        mBm = proc.smoothTransform(bitmap, 3); //Image smoothing
        imageView = (ImageView) findViewById(R.id.imageView3);
        imageView.setImageBitmap(mBm);

        //Histogram spesification matching
        // Histogram to match against
        int[] specHist = proc.generateHistogram(200, 125, 300);
        dummyArr = new int[]{147,493,364,388,494,657,805,758,769,749,719,660,692,700,720,809,808,898,910,867,956,933,1167,1386,1572,1591,1489,1460,1318,1378,1363,1190,956,847,736,719,642,534,619,633,714,851,803,714,831,749,742,788,863,945,972,921,905,946,1196,1420,1159,1053,919,955,988,1090,1248,1428,1480,1429,1347,1353,1339,1465,1575,1568,1634,1718,1721,1585,1543,1381,1320,1266,1253,1216,1113,1014,1004,993,927,928,882,786,794,655,594,553,507,407,436,447,489,487,499,501,513,490,449,430,408,392,370,363,303,358,334,325,285,330,328,317,354,332,326,321,299,338,318,293,311,313,288,308,291,326,293,284,277,289,317,264,314,346,321,310,342,352,431,460,493,548,524,695,718,637,667,638,698,708,637,665,610,634,685,752,871,772,702,702,687,612,614,659,663,627,628,627,726,695,709,760,793,799,848,833,899,902,876,907,912,920,948,959,1146,1043,1196,1376,1439,1319,1279,1528,2043,1893,1625,1341,1333,1208,1168,1577,2064,2020,2035,2119,2267,2400,3005,2743,2427,2360,2271,1942,1762,2028,2509,2439,2405,2016,2040,2718,2664,3657,2890,2631,2316,1484,815,774,1115,944,1071,1213,1067,994,1085,1093,1675,1471,1160,1145,1033,891,1096,1645,2090,1872,2186,3173,2954,579};
        int[][] masked = proc.histMatchGray(rgbArr.rArr, dummyArr, 1);
        // apply matching histogram to image
        mBm = proc.specTransform(cfdRGB, dummyArr, bitmap); //Histogram Match
        imageView = (ImageView) findViewById(R.id.imageView4);
        imageView.setImageBitmap(mBm);
        Log.d("update Image", "filtered Image update! weight@"+weight);
    }
    private void setLineGraphSeries(GraphView gv, int[] vArr, int color){
        gv.removeAllSeries();
        LineGraphSeries<DataPoint> series;
        series= new LineGraphSeries<>(proc.rgbToPlot(vArr));   //initializing/defining series
        series.setColor(color);
        gv.addSeries(series);
    }
    private void setBarGraphSeries(GraphView gv, int[] vArr, int color){
        gv.removeAllSeries();
        BarGraphSeries<DataPoint> series;
        series= new BarGraphSeries<>(proc.rgbToPlot(vArr));   //initializing/defining series
        series.setColor(color);
        gv.addSeries(series);
        gv.getViewport().setScalable(true);
    }

}