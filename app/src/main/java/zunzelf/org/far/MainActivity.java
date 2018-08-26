package zunzelf.org.far;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.BarGraphSeries;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE = 100;
    ImageView imageView;
    Uri imageURI;
    TextView txt;
    Bitmap bitmap = null;
    RGBArr rgbArr;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        txt = (TextView) findViewById(R.id.tVi);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
        if(resultCode== RESULT_OK && requestCode == PICK_IMAGE){
            // Load Image File
            imageURI = data.getData();
            // imageView.setImageURI(imageURI); to show image -> will uncomment when layout sorted
            // Bitmap bitmap = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageURI);
            } catch (IOException e) {
                e.printStackTrace();
            }
            // > Image Processes :
            // Get RGBs value per-pixel
            rgbArr = getRGBs(bitmap);
            // plotting to Graph :
            //  TODO : need to simplify plotting method
            GraphView graph;       //an Object of the PointsGraphSeries for plotting scatter graphs
            graph = (GraphView) findViewById(R.id.rGraph); //RED
            setBarGraphSeries(graph, rgbArr.rArr, Color.RED);
            graph = (GraphView) findViewById(R.id.gGraph); //GREEN
            setBarGraphSeries(graph, rgbArr.gArr, Color.GREEN);
            graph = (GraphView) findViewById(R.id.bGraph); //BLUE
            setBarGraphSeries(graph, rgbArr.bArr, Color.BLUE);

//            graph = (GraphView) findViewById(R.id.rGraph); //RED
//            setLineGraphSeries(graph, rgbArr.rArr, Color.RED);
//            graph = (GraphView) findViewById(R.id.gGraph); //GREEN
//            setLineGraphSeries(graph, rgbArr.gArr, Color.GREEN);
//            graph = (GraphView) findViewById(R.id.bGraph); //BLUE
//            setLineGraphSeries(graph, rgbArr.bArr, Color.BLUE);
        }

    }

    private void setLineGraphSeries(GraphView gv, int[] vArr, int color){
        gv.removeAllSeries();
        LineGraphSeries<DataPoint> series;
        series= new LineGraphSeries<>(rgbToPlot(vArr));   //initializing/defining series
        series.setColor(color);
        gv.addSeries(series);
    }
    private void setBarGraphSeries(GraphView gv, int[] vArr, int color){
        gv.removeAllSeries();
        BarGraphSeries<DataPoint> series;
        series= new BarGraphSeries<>(rgbToPlot(vArr));   //initializing/defining series
//        series.setColor(color);
        gv.addSeries(series);
    }

    private RGBArr getRGBs(Bitmap bitmap){
        RGBArr arr = new RGBArr();
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        System.out.println("width : "+w);
        System.out.println("height : "+h);
        for (int y = 0; y < h; y++){
            for(int x = 0; x < w; x++){
                int pixel = bitmap.getPixel(x, y);
                arr.rArr[Color.red(pixel)] += 1;
                arr.bArr[Color.blue(pixel)] += 1;
                arr.gArr[Color.green(pixel)] += 1;
            }
        }
        return arr;

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

}
