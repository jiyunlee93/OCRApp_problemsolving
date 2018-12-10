package andbook.example.ocr_test;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    // DBHelper 생성자로 관리할 DB 이름과 버전 정보를 받음
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    // DB를 새로 생성할 때 호출되는 함수
    @Override
    public void onCreate(SQLiteDatabase db) {
        // 새로운 테이블 생성
        //db.execSQL("CREATE TABLE TESTPAPER (_id INTEGER PRIMARY KEY AUTOINCREMENT, contents TEXT, page INTEGER, create_at TEXT);");
        db.execSQL("CREATE TABLE TESTPAPER (id INTEGER, page INTEGER, number INTEGER, contents TEXT, answer TEXT, userAnswer TEXT);");
        db.execSQL("CREATE TABLE USER (id INTEGER , score TEXT, create_at TEXT);");
    }

    // DB 업그레이드를 위해 버전이 변경될 때 호출되는 함수
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
    public void insert(String create_at, String contents, int page) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        contents=contents.replaceAll("'","\"");
        db.execSQL("INSERT INTO TESTPAPER VALUES(null, '" + contents + "', " + page + ", '" + create_at + "');");
        db.close();
    }
    public void insert_TestPaper(int _id, int _page,int _number, String _contents,String _answer, String _userAnswer) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        _contents=_contents.replaceAll("'","\"");
        //db.execSQL("INSERT INTO TESTPAPER VALUES(null, '" + contents + "', " + page + ", '" + create_at + "');");
        db.execSQL("INSERT INTO TESTPAPER VALUES(" + _id + "," + _page + "," + _number + ",'"+_contents + "','"+_answer+"','"+_userAnswer+"');");
        db.close();
    }
    public void insert_User(int _id, String _score, String _create_at) {
        // 읽고 쓰기가 가능하게 DB 열기
        SQLiteDatabase db = getWritableDatabase();
        // DB에 입력한 값으로 행 추가
        //db.execSQL("INSERT INTO TESTPAPER VALUES(null, '" + contents + "', " + page + ", '" + create_at + "');");
        db.execSQL("INSERT INTO USER VALUES(" + _id + ",'" + _score + "','" + _create_at+ "');");
        db.close();
    }
    public void update_answerToTestPaper(int _id, int _problemNum, String _answer) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE TESTPAPER SET answer=" +"'"+ _answer +"'"+ " WHERE number=" +_problemNum+ " and id = "+_id+";");
        db.close();
    }
    public void update_userAnswerToTestPaper(int _id ,int _problemNum, String _userAnswer) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE TESTPAPER SET userAnswer=" +"'"+ _userAnswer +"'"+ " WHERE number=" +_problemNum+  " and id = "+_id+";");
        db.close();
    }
    public void deleteUserAnswer(int _id){
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행의 가격 정보 수정
        db.execSQL("UPDATE TESTPAPER SET userAnswer=" +"''"+ " WHERE id = "+_id+";");
        db.close();
    }
    public void delete(String contents) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        db.execSQL("DELETE FROM TESTPAPER WHERE item='" + contents + "';");
        db.close();
    }
    public void deleteAll(int num) {
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        //db.execSQL("delete from TESTPAPER");
        if(num==0){
            db.execSQL("DELETE FROM TESTPAPER;");
        }
        else if(num==1){
            db.execSQL("DELETE FROM USER;");
        }
        db.close();
    }
    public String getResult_User(){
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM USER", null);
        while (cursor.moveToNext()) {
            result +="ID: " +cursor.getInt(0)
                    +"■score: " +cursor.getString(1)
                    +"■created at: " +cursor.getString(2)+"\n";
        }
        return result;
    }
    public String getResult() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";

        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TESTPAPER", null);
        while (cursor.moveToNext()) {
            result += cursor.getString(0)
                    + " > "
                    + cursor.getString(1)
                    + " > "
                    + cursor.getInt(2)
                    + " > "
                    + cursor.getString(3)
                    + "\n";
        }
        return result;
    }
    public String getResultTestPaper() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TESTPAPER", null);
        while(cursor.moveToNext()){
            result+="ID: " +cursor.getInt(0)
                    +"■page: " +cursor.getInt(1)
                    +"■number: " +cursor.getInt(2)
                    +"■answer: "+cursor.getString(4)
                    +"■useranswer: "+cursor.getString(5)
                    +"\n■contents: " + cursor.getString(3) +"\n";
           // result +=cursor.getString(3) + "\n /////////////////////////////////\n ";
        }
        return result;
    }
    public String getResultSpecificTest(int num){
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        int fullNum=0;
        int correctNum=0;
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        String sql = "SELECT answer, userAnswer FROM TESTPAPER where id = "+num;
        System.out.println("sql = " + sql);
        Cursor cursor = db.rawQuery("SELECT answer, userAnswer FROM TESTPAPER where id = "+num, null);
        while(cursor.moveToNext()){
            if(!cursor.getString(0).matches("null") && !cursor.getString(1).matches("")){
                result+="    Answer: " +cursor.getString(0)
                        +"      ☞ UserAnswer : " +cursor.getString(1)
                        +"\n";
                fullNum++;
                if((cursor.getString(0).trim()).matches(cursor.getString(1).trim())) {
                    correctNum++;
                }
            }
        }
        result+= "   ▼▼▼▼▼▼▼▼▼▼▼▼▼ \nScore :: "
                +correctNum+ " / "+fullNum+ "\n";
        return result;
    }

    public String getResultUser() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM USER", null);
        while(cursor.moveToNext()){
            result+="ID: " +cursor.getInt(0)
                    +"■score: " +cursor.getInt(1)
                    +"■create_at: " +cursor.getInt(2)
                    +"\n";
        }
        return result;
    }
    public String getSpecificContents(int num) {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result="";
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT * FROM TESTPAPER where id = "+num, null);
        while (cursor.moveToNext()) {
            result+= cursor.getString(3) + "/@#/";
                    }
        return result;
    }
    public int getLastID() {
        // 읽기가 가능하게 DB 열기
        SQLiteDatabase db = getReadableDatabase();
        String result = "";
        // DB에 있는 데이터를 쉽게 처리하기 위해 Cursor를 사용하여 테이블에 있는 모든 데이터 출력
        Cursor cursor = db.rawQuery("SELECT MAX(id) FROM TESTPAPER", null);
        while (cursor.moveToNext()) {
            result = cursor.getString(0);
        }
        if(result == null){
            return 0;
        }
        else
            return Integer.parseInt(result);
    }
    public  void dropTable(int num){
        SQLiteDatabase db = getWritableDatabase();
        // 입력한 항목과 일치하는 행 삭제
        //db.execSQL("delete from TESTPAPER");
        if(num==0){
            db.execSQL("DROP TABLE TESTPAPER;");
        }
        else if(num==1){
            db.execSQL("DROP TABLE USER;");
        }
        db.close();
    }

}
