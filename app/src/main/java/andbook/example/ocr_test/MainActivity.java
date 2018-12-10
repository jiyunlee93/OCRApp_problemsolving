package andbook.example.ocr_test;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import gun0912.tedbottompicker.TedBottomPicker;

import static org.opencv.core.CvType.CV_8U;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java3");
    }
    Bitmap image; //사용되는 이미지
    private TessBaseAPI mTess; //Tess API reference
    String datapath = "" ; //언어데이터가 있는 경로
    String OCRresult = null;

    //잠깐 확인용
    Vector<MatOfPoint> contours2= new Vector<MatOfPoint>();
    int checkInt =0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //이미지 디코딩을 위한 초기화
        image = BitmapFactory.decodeResource(getResources(), R.drawable.s1full); //샘플이미지파일
        //언어파일 경로
        datapath = getFilesDir()+ "/tesseract/";

        //트레이닝데이터가 카피되어 있는지 체크
        checkFile(new File(datapath + "tessdata/"));

        //Tesseract API
        String lang = "eng";

        mTess = new TessBaseAPI();
        mTess.init(datapath, lang);

        ////////////////////////////////////////////////////
        //SQLite
        final DBHelper dbHelper = new DBHelper(getApplicationContext(), "MoneyBook.db", null, 1);
        // 테이블에 있는 모든 데이터 출력
        final TextView result = (TextView) findViewById(R.id.result);
        // DB에 데이터 추가
        Button insert = (Button) findViewById(R.id.insert);
        insert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //날짜
                // 날짜는 현재 날짜로 고정
                // 현재 시간 구하기
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                // 출력될 포맷 설정
                String etDate ="";
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
                etDate= simpleDateFormat.format(date);
                //text
                TextView OCRTextView2 = (TextView) findViewById(R.id.OCRTextView);
                String item = OCRTextView2.getText().toString();

                dbHelper.insert(etDate, item, 1);
                result.setText(dbHelper.getResult());
            }
        });
        // DB에 있는 데이터 조회
        Button select = (Button) findViewById(R.id.select);
        select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                result.setText(dbHelper.getResult());
            }
        });


    }
    //change Activity
    public void makeProblem(View view) {
        //////
        TextView problemTextView = (TextView) findViewById(R.id.problemText);
        String fulltext=problemTextView.getText().toString();
        final ISOProblem prob1 = new ISOProblem(fulltext);
        prob1.isolateFull();
        ((TextView) findViewById(R.id.problemView)).setText(prob1.problem);
        ((TextView) findViewById(R.id.buttonA)).setText(prob1.answerA);
        ((TextView) findViewById(R.id.buttonB)).setText(prob1.answerB);
        ((TextView) findViewById(R.id.buttonC)).setText(prob1.answerC);
        ((TextView) findViewById(R.id.buttonD)).setText(prob1.answerD);
    }
    //change Activity
    public void backMain(View view) {
        setContentView(R.layout.activity_main);
    }
    //change Activity
    public void changeActivity(View view) {
        setContentView(R.layout.problem_1);
        TextView problemTextView = (TextView) findViewById(R.id.problemText);
        problemTextView.setText(OCRresult);
    }
    public void separateProblem(View view) {
        //here separate Problem using openCV
        Toast.makeText(this,"Clicked ",Toast.LENGTH_SHORT).show();

        //bitmap to mat
        Mat src = new Mat();
        Mat src_gray=new Mat();
        Mat kernel;

        //Bitmap image33 = BitmapFactory.decodeResource(getResources(), image);
        Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, src);

        Imgproc.cvtColor(src,src_gray,Imgproc.COLOR_BGR2GRAY);
        kernel = Mat.ones(7,7,CV_8U);
        Point a = new Point(-1,-1);
        Imgproc.erode(src_gray,src_gray,kernel,a,2);
        Imgproc.threshold(src_gray, src_gray, 200, 255,Imgproc.THRESH_BINARY);


        //next
        Vector<MatOfPoint> contours= new Vector<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(src_gray,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
        System.out.println("contours :: " + contours.size());
        System.out.println("hierarchy :: " + hierarchy.size());
        contours2=contours;
        /////ehere
        for(int xx=0;xx<contours.size();xx++){
            Rect ocrRect= new Rect();
            org.opencv.core.Rect tempRect= new org.opencv.core.Rect();

            //System.out.println("x :: " + mTess.getUTF8Text(tempRect));
            int x = Imgproc.boundingRect(contours.get(xx)).x;
            int y = Imgproc.boundingRect(contours.get(xx)).y;
            int w = Imgproc.boundingRect(contours.get(xx)).width;
            int h = Imgproc.boundingRect(contours.get(xx)).height;
            System.out.println("x :: " + x);
            System.out.println("y :: " + y);
            System.out.println("w :: " + w);
            System.out.println("h :: " + h);

            Point xy = new Point(x ,y);
            Point xwyh = new Point(x+w ,y+h);
            Scalar sctemp= new Scalar(0,255,0);
            Imgproc.rectangle(src_gray,xy,xwyh,sctemp,3 );
            Bitmap bmp = null;
            Mat tmp = new Mat (src.height(), src.width(), CvType.CV_8U, new Scalar(4));
            try {
                Imgproc.cvtColor(src_gray, tmp, Imgproc.COLOR_GRAY2RGBA, 4);
                bmp = Bitmap.createBitmap(tmp.cols(), tmp.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(tmp, bmp);
            }
            catch (CvException e){Log.d("Exception",e.getMessage());}
            //change Picture
            ImageView imageview2 = (ImageView) findViewById(R.id.imageView);
            //imageview2.setImageBitmap(bmp);

        }
    }
    public void tempFunc(View view){
        //contours2
        int x = Imgproc.boundingRect(contours2.get(checkInt)).x;
        int y = Imgproc.boundingRect(contours2.get(checkInt)).y;
        int w = Imgproc.boundingRect(contours2.get(checkInt)).width;
        int h = Imgproc.boundingRect(contours2.get(checkInt)).height;
        System.out.println("x :: " + x);
        System.out.println("y :: " + y);
        System.out.println("w :: " + w);
        System.out.println("h :: " + h);



        //specific region ocr
        Bitmap tempBit=image;
        System.out.println("imagesize :: " + image.getWidth());
        System.out.println("tempBit :: " + tempBit.getWidth());
        ImageView imageview2 = (ImageView) findViewById(R.id.imageView);
        tempBit=tempBit.createBitmap(tempBit,x,y,w,h);
        imageview2.setImageBitmap(tempBit);
        checkInt++;

        //ocr까지 돌려버리기
        mTess.setImage(tempBit);
        OCRresult = mTess.getUTF8Text();
        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        OCRTextView.setText(OCRresult);
    }
    //select image
    public void selectImage(View view){
        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(MainActivity.this)
                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        // here is selected uri
                        ImageView imageview2=(ImageView) findViewById(R.id.imageView);
                        try {
                            image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageview2.setImageBitmap(image);

                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .create();
        tedBottomPicker.show(getSupportFragmentManager());

    }
    //Process an Image
    public void processImage(View view) {
        mTess.setImage(image);
        OCRresult = mTess.getUTF8Text();
        TextView OCRTextView = (TextView) findViewById(R.id.OCRTextView);
        OCRTextView.setText(OCRresult);
    }


    //copy file to device
    private void copyFiles() {
        try{
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream instream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outstream = new FileOutputStream(filepath);
            byte[] buffer = new byte[1024];
            int read;
            while ((read = instream.read(buffer)) != -1) {
                outstream.write(buffer, 0, read);
            }
            outstream.flush();
            outstream.close();
            instream.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //check file on the device
    private void checkFile(File dir) {
        //디렉토리가 없으면 디렉토리를 만들고 그후에 파일을 카피
        if(!dir.exists()&& dir.mkdirs()) {
            copyFiles();
        }
        //디렉토리가 있지만 파일이 없으면 파일카피 진행
        if(dir.exists()) {
            String datafilepath = datapath+ "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if(!datafile.exists()) {
                copyFiles();
            }
        }
    }
}

