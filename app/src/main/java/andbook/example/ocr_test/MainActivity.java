package andbook.example.ocr_test;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.googlecode.tesseract.android.TessBaseAPI;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import gun0912.tedbottompicker.TedBottomPicker;

import static java.sql.Types.INTEGER;
import static org.opencv.core.CvType.CV_8U;

public class MainActivity extends AppCompatActivity {
    static {
        System.loadLibrary("opencv_java3");
    }
    Bitmap image; //사용되는 이미지
    Bitmap[] imageArray_paper=null;
    Bitmap[] imageArray_answer=null;
    private TessBaseAPI mTess; //Tess API reference
    String datapath = "" ; //언어데이터가 있는 경로
    String OCRresult = null;
    DBHelper dbHelper;
    //잠깐 확인용
    int checkInt =0;
    int moveP=0;
    int currentTestId=0;
    int curProblemNumber=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_home);
        dbHelper= new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
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

    }

    //change Activity
    public void registerTwoThings(View v){
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
        //registerPaper 이거 boolean 이니까 나중에 예외처리하쉠
        int id=dbHelper.getLastID()+1;
        for(int i=0; i<imageArray_paper.length;i++){
            System.out.println("paper.length:"+i);
            insertDB_testPaper(id, getContour(imageArray_paper[i]),imageArray_paper[i]);
        }


        //답지 인식
        for(int _num2=0;_num2<imageArray_answer.length;_num2++){
            System.out.println("answer length:"+_num2);
            mTess.setImage(imageArray_answer[_num2]);
            mTess.setPageSegMode(1);
            OCRresult=mTess.getUTF8Text();
            String[] b = OCRresult.split("\n");
            String[] problemNum=null;
            String[] _answer=null;
            int checkNum2=0;
            for(int i=0; i<b.length; i++) {
                //null 은 예외처리
                if (b[i] != null) {
                    if (checkNum2 == 0) {
                        //첫 번 째 줄일 때
                        problemNum = b[i].split(" ");
                        checkNum2 = 1;
                    } else if (checkNum2 == 1) {
                        //두 번 째 줄일 때
                        _answer = b[i].split(" ");
                        //// 확인용 system.out.println
                        int forNum=problemNum.length;
                        if(problemNum.length>_answer.length)
                            forNum=_answer.length;
                        for (int j = 0; j < forNum; j++) {
                            try {
                                int number = Integer.parseInt(problemNum[j]);
                                dbHelper.update_answerToTestPaper(id, number, _answer[j]);
                            } catch (Exception e) {
                                break;
                            }
                        }
                        checkNum2 = 0;
                    }
                }
            }
        }

    }
    public void registerAnswerPaper(View v){
        selectImage();
        ImageView imageview = (ImageView) findViewById(R.id.image_registerAnswerPaper);
        //select picture & image setting
        imageview.setImageBitmap(image);
    }
    public void onClick(View v){
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
        DBHelper dbHelper2 = new DBHelper(getApplicationContext(), "USER.db", null, 1);

        switch(v.getId()){
            case R.id.btn_home:
                setContentView(R.layout.layout_home);
                break;
            case R.id.btn_startTest:
                setContentView(R.layout.testpaper_list);
                break;
            case R.id.btn_myMenu:
                setContentView(R.layout.mymenu);
                TextView mymenuTextView = (TextView) findViewById(R.id.text_myMenu);
                mymenuTextView.setText(dbHelper2.getResult_User());
                break;
            case R.id.btn_addTestPaper:
                setContentView(R.layout.register_problem);
                break;
            case R.id.btn_backToPaperList:
                setContentView(R.layout.testpaper_list);
                break;
            case R.id.btn_solveBack:
                setContentView(R.layout.testpaper_list);
                break;
            case R.id.btn_startTest_temp:
                //현재 문제 번호 셋팅
                currentTestId=1;
                dbHelper.deleteUserAnswer(currentTestId);
                setContentView(R.layout.testpaper_solve);
                try{
                    curProblemNumber= moveNextProblem(v);
                }catch(ArrayIndexOutOfBoundsException e){
                    Toast.makeText(this,"There's no testPaper, You have to register TestPaper",Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.testpaper_list);
                }
                break;
            case R.id.btn_startTest_temp2:
                //현재 문제 번호 셋팅
                currentTestId=2;
                dbHelper.deleteUserAnswer(currentTestId);
                setContentView(R.layout.testpaper_solve);
                try{
                    curProblemNumber= moveNextProblem(v);
                }catch(ArrayIndexOutOfBoundsException e){
                    Toast.makeText(this,"There's no testPaper, You have to register TestPaper",Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.testpaper_list);
                }
                break;
            case R.id.btn_startTest_temp3:
                //현재 문제 번호 셋팅
                currentTestId=3;
                dbHelper.deleteUserAnswer(currentTestId);
                setContentView(R.layout.testpaper_solve);
                try{
                    curProblemNumber= moveNextProblem(v);
                }catch(ArrayIndexOutOfBoundsException e){
                    Toast.makeText(this,"There's no testPaper, You have to register TestPaper",Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.testpaper_list);
                }
                break;
            case R.id.btn_startTest_temp4:
                //현재 문제 번호 셋팅
                currentTestId=4;
                dbHelper.deleteUserAnswer(currentTestId);
                setContentView(R.layout.testpaper_solve);
                try{
                    curProblemNumber= moveNextProblem(v);
                }catch(ArrayIndexOutOfBoundsException e){
                    Toast.makeText(this,"There's no testPaper, You have to register TestPaper",Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.testpaper_list);
                }

                break;
            case R.id.btn_startTest_temp5:
                //현재 문제 번호 셋팅
                currentTestId=5;
                dbHelper.deleteUserAnswer(currentTestId);
                setContentView(R.layout.testpaper_solve);
                try{
                    curProblemNumber= moveNextProblem(v);
                }catch(ArrayIndexOutOfBoundsException e){
                    Toast.makeText(this,"There's no testPaper, You have to register TestPaper",Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.testpaper_list);
                }
                break;
            case R.id.btn_startTest_temp6:
                //현재 문제 번호 셋팅
                currentTestId=6;
                dbHelper.deleteUserAnswer(currentTestId);
                setContentView(R.layout.testpaper_solve);
                try{
                    curProblemNumber= moveNextProblem(v);
                }catch(ArrayIndexOutOfBoundsException e){
                    Toast.makeText(this,"There's no testPaper, You have to register TestPaper",Toast.LENGTH_SHORT).show();
                    setContentView(R.layout.testpaper_list);
                }
                break;
            //////////testpaper_solve btn
            case R.id.btn_A:
                dbHelper.update_userAnswerToTestPaper(currentTestId,curProblemNumber,"A");
                curProblemNumber=moveNextProblem(v);
                break;
            case R.id.btn_B:
                dbHelper.update_userAnswerToTestPaper(currentTestId,curProblemNumber,"B");
                curProblemNumber=moveNextProblem(v);
                break;
            case R.id.btn_C:
                dbHelper.update_userAnswerToTestPaper(currentTestId,curProblemNumber,"C");
                curProblemNumber=moveNextProblem(v);
                break;
            case R.id.btn_D:
                dbHelper.update_userAnswerToTestPaper(currentTestId,curProblemNumber,"D");
                curProblemNumber=moveNextProblem(v);
                break;
        }
    }
    public void showScore(){
        //after last problems
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
        TextView scoreTextView = (TextView) findViewById(R.id.text_score);
        scoreTextView.setText(dbHelper.getResultSpecificTest(currentTestId));
        String[] temp = dbHelper.getResultSpecificTest(currentTestId).split("Score ::");
        insertDB_USER(temp[1]);
    }
    public void insertDB_USER(String score){
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "USER.db", null, 1);
        //날짜
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        // 출력될 포맷 설정
        String etDate ="";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy년 MM월 dd일");
        etDate= simpleDateFormat.format(date);
        dbHelper.insert_User(currentTestId,score,etDate);
    }
    public int moveNextProblem(View view){
        //다음 문제로 넘기는 것
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
        String problems="";
        String[] check=null;
        problems=dbHelper.getSpecificContents(currentTestId);
        check=problems.split("/@#/");
        String fulltext=check[++moveP];
        //get Current problem number
        int pageNum=getProblemNum(fulltext);
        //isolate Problem
        final ISOProblem prob1 = new ISOProblem(fulltext);
        prob1.isolateFull();
        ((TextView) findViewById(R.id.text_testPaper)).setText(prob1.problem);
        ((TextView) findViewById(R.id.btn_A)).setText(prob1.answerA);
        ((TextView) findViewById(R.id.btn_B)).setText(prob1.answerB);
        ((TextView) findViewById(R.id.btn_C)).setText(prob1.answerC);
        ((TextView) findViewById(R.id.btn_D)).setText(prob1.answerD);
        if(moveP == check.length-1){
            //끝까지 다 풀었다면 // 다음문제가 없다면
            setContentView(R.layout.score_list);
            //문제 변수 리셋
            moveP=0;
            showScore();
        }
        return pageNum;
    }
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
    public Vector<MatOfPoint> getContours() {
        //here separate Problem using openCV
        //get contours
        //bitmap to mat
        Mat src = new Mat();
        Mat src_gray=new Mat();
        Mat kernel;
        //default setting
        int row = 8;
        int col = 8;
        int sizeofContours=0;
        Vector<MatOfPoint> contours2= new Vector<MatOfPoint>();
        do{
            Vector<MatOfPoint> contours= new Vector<MatOfPoint>();
            Bitmap bmp32 = image.copy(Bitmap.Config.ARGB_8888, true);
            Utils.bitmapToMat(bmp32, src);
            Imgproc.cvtColor(src,src_gray,Imgproc.COLOR_BGR2GRAY);
            kernel = Mat.ones(row++,col++,CV_8U);
            Point a = new Point(-1,-1);
            Imgproc.erode(src_gray,src_gray,kernel,a,3);
            Imgproc.threshold(src_gray, src_gray, 200, 255,Imgproc.THRESH_BINARY);
            //next
            Mat hierarchy = new Mat();
            Imgproc.findContours(src_gray,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
            sizeofContours=contours.size();
            contours2= contours;
        }while(sizeofContours>15);
        return contours2;
    }
    public void tempFunc(Vector<MatOfPoint> contours2){
        //change Activity
        setContentView(R.layout.problem_1);
        TextView problemTextView = (TextView) findViewById(R.id.problemText);

        //get contour rect
        int x = Imgproc.boundingRect(contours2.get(checkInt)).x;
        int y = Imgproc.boundingRect(contours2.get(checkInt)).y;
        int w = Imgproc.boundingRect(contours2.get(checkInt)).width;
        int h = Imgproc.boundingRect(contours2.get(checkInt)).height;
        //specific region ocr
        Bitmap tempBit;
        ImageView imageview2 = (ImageView) findViewById(R.id.imageView);
        tempBit=image.createBitmap(image,x,y,w,h);
        //imageview2.setImageBitmap(tempBit);

        //ocr까지 돌려버리기
        mTess.setImage(tempBit);
        OCRresult="";
        OCRresult = mTess.getUTF8Text();
        problemTextView.setText(OCRresult);
        checkInt++;
    }
    public void openDB(View view){
        //contour를 확인하면서 db에 insert하는 함수
        // 현재 시간 구하기
    }
    public void tempChangeActivity(View v){
        setContentView(R.layout.activity_main);
    }
    //select image
    public void selectImage_paper(View v){
        TedBottomPicker bottomSheetDialogFragment = new TedBottomPicker.Builder(MainActivity.this)
                .setOnMultiImageSelectedListener(new TedBottomPicker.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(ArrayList<Uri> uriList) {
                        // here is selected uri list
                        try {
                            ImageView imageview2 = (ImageView) findViewById(R.id.image_registerPaper);
                            imageArray_paper=new Bitmap[uriList.size()];
                            for(int i=0; i<uriList.size();i++){
                                Bitmap image_paper = MediaStore.Images.Media.getBitmap(getContentResolver(), uriList.get(i));
                                imageArray_paper[i]=image_paper;
                            }
                            imageview2.setImageBitmap(imageArray_paper[0]);
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                })
                .showTitle(false)
                .setCompleteButtonText("Done")
                .setEmptySelectionText("No Select")
                .create();

        bottomSheetDialogFragment.show(getSupportFragmentManager());

        return ;
    }
    public void selectImage_answer(View v){
        TedBottomPicker bottomSheetDialogFragment = new TedBottomPicker.Builder(MainActivity.this)
                .setOnMultiImageSelectedListener(new TedBottomPicker.OnMultiImageSelectedListener() {
                    @Override
                    public void onImagesSelected(ArrayList<Uri> uriList) {
                        // here is selected uri list
                        try {
                            ImageView imageview2 = (ImageView) findViewById(R.id.image_registerAnswerPaper);
                            imageArray_answer=new Bitmap[uriList.size()];
                            for(int i=0; i<uriList.size();i++){
                                Bitmap image_paper = MediaStore.Images.Media.getBitmap(getContentResolver(), uriList.get(i));
                                imageArray_answer[i]=image_paper;
                            }
                            imageview2.setImageBitmap(imageArray_answer[0]);
                        } catch (FileNotFoundException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }

                    }
                })
                .showTitle(false)
                .setCompleteButtonText("Done")
                .setEmptySelectionText("No Select")
                .create();

        bottomSheetDialogFragment.show(getSupportFragmentManager());
        return ;
    }
    public void selectImage(){
        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(MainActivity.this)
                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        // here is selected uri
                        try {
                            Bitmap image_paper = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
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
        return ;
    }
    public Bitmap selectImage2(View v){
        final ImageView imageview = (ImageView) findViewById(R.id.imageView);
        TedBottomPicker tedBottomPicker = new TedBottomPicker.Builder(MainActivity.this)
                .setOnImageSelectedListener(new TedBottomPicker.OnImageSelectedListener() {
                    @Override
                    public void onImageSelected(Uri uri) {
                        // here is selected uri
                        try {
                            image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                            imageview.setImageBitmap(image);
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
        return image;
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

    //////////////////////////////
    //DB FUNCTION

    public boolean insertDB_testPaper(int id, Vector<MatOfPoint> contours2,Bitmap _image){

        //id 먼저 찾기

        int pageNum=0;
        //rectangle 별로 insert 하기
        for(int xx=contours2.size()-1;xx>0;xx--) {
            pageNum=0;
            //System.out.println("x :: " + mTess.getUTF8Text(tempRect))
            int x = Imgproc.boundingRect(contours2.get(xx)).x;
            int y = Imgproc.boundingRect(contours2.get(xx)).y;
            int w = Imgproc.boundingRect(contours2.get(xx)).width;
            int h = Imgproc.boundingRect(contours2.get(xx)).height;
            //specific region ocr
            Bitmap tempBit;
            ImageView imageview2 = (ImageView) findViewById(R.id.image_registerPaper);
            tempBit=_image.createBitmap(_image,x,y,w,h);
            String aa="";
            //imageview2.setImageBitmap(tempBit);
            //이미지 전체와 같거나, 아예 내용이 없는 경우 예외처리
            if(!tempBit.sameAs(_image)){
                mTess.setImage(tempBit);
                OCRresult = mTess.getUTF8Text();
                //trash 값  or null 예외처리
                if(OCRresult.length()>1){
                    //check if there's example?
                    if(OCRresult.contains("(")){
                        String str = OCRresult;
                        //문제가 안읽히는 경우 예외처리‘
                        str=str.replaceAll("‘",".");
                        str=str.replaceAll("103i","103");
                        if(str.contains(".")){
                            String result = str.substring(str.indexOf("."));
                            result= str.replace(result,"");
                            //문제 앞에 다른 문장이 있는 경우
                            try{
                                pageNum = Integer.parseInt(result);
                            }catch(NumberFormatException e){
                            }
                            //catch로 들어간 상황
                            if(pageNum==0){
                                String[] tmpString = result.split(" ");
                                //마지막꺼로
                                try{
                                    //첫번째가 숫자인지
                                    pageNum=Integer.parseInt(tmpString[0]);
                                }catch(NumberFormatException e2){
                                    //아니라면
                                    String[] temp2 = result.split("\n");
                                    aa =temp2[temp2.length-1];
                                    aa=aa.replaceAll("[^a-zA-Z0-9]","");
                                    pageNum = Integer.parseInt(aa);
                                }
                            }
                            System.out.println("paper in pageNum="+pageNum);
                            OCRresult=OCRresult.replace("_","__");
                            dbHelper.insert_TestPaper(id,1,pageNum,OCRresult,null,null);
                        }
                    }
                    }
                }
            }
        return true;
    }
    public int getProblemNum(String _str){
        int pageNum=0;
        String aa="";
        String str = _str;
        String result= str.replace("‘",".");
        result = str.substring(str.indexOf("."));
        result= str.replace(result,"");

        //result=result.replaceAll("[^a-zA-Z0-9]","");
        //문제 앞에 다른 문장이 있는 경우
        try{
            pageNum = Integer.parseInt(result);
        }catch(NumberFormatException e){
        }
        //catch로 들어간 상황
        if(pageNum==0){
            String[] tmpString = result.split(" ");
            //마지막꺼로
            System.out.println("result : " + result);
            try{
                //첫번째가 숫자인지
                System.out.println("tmpString[0] :" +tmpString[0]);
                pageNum=Integer.parseInt(tmpString[0].replaceAll("‘",""));
            }catch(NumberFormatException e2){
                //아니라면
                String[] temp2 = result.split("\n");
                aa =temp2[2].split(" ")[0];
                aa=aa.replaceAll("[^a-zA-Z0-9]","");
                aa=aa.replaceAll("‘","");
                System.out.println("aa:"+aa);
                pageNum = Integer.parseInt(aa);
            }
        }
        return pageNum;
    }
    public void selectDB(View view){
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
        TextView result = (TextView) findViewById(R.id.result);
        result.setText(dbHelper.getResultTestPaper());
    }
    public void deleteDB(View view){
        DBHelper dbHelper = new DBHelper(getApplicationContext(), "TESTPAPER.db", null, 1);
        dbHelper.deleteAll(0);
    }
    //////////////////////////////
    //OpenCV FUNCTION
    public Vector<MatOfPoint> getContour(Bitmap _image){
        Vector<MatOfPoint> contours2= new Vector<MatOfPoint>();
        //here separate Problem using openCV
        //get contours
        //bitmap to mat
        Mat src = new Mat();
        Mat src_gray=new Mat();
        Mat kernel;

        Bitmap bmp32 = _image.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, src);
        Imgproc.cvtColor(src,src_gray,Imgproc.COLOR_BGR2GRAY);
        //default
        int row = 8;
        int col = 10;

        //until find
        do{
            kernel = Mat.ones(row++,col++,CV_8U);
            Point a = new Point(-1,-1);
            Imgproc.erode(src_gray,src_gray,kernel,a,3);
            Imgproc.threshold(src_gray, src_gray, 200, 255,Imgproc.THRESH_BINARY);

            //next
            Vector<MatOfPoint> contours= new Vector<MatOfPoint>();
            Mat hierarchy = new Mat();
            Imgproc.findContours(src_gray,contours,hierarchy,Imgproc.RETR_TREE,Imgproc.CHAIN_APPROX_SIMPLE);
            if(contours.size()<13){
                return contours;
            }
        }while(true);
    }
}

