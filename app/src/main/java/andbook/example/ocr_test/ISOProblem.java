package andbook.example.ocr_test;

public class ISOProblem {
    String fulltext;
    String problem;
    String answerA;
    String answerB;
    String answerC;
    String answerD;
    public ISOProblem(String _full){
        fulltext = _full;
        problem="";
        answerA="";
        answerB="";
        answerC="";
        answerD="";
    }
    public void isolateFull(){
        //공백제거
        fulltext=fulltext.replace("\n\n","");

        String[] temp=fulltext.split("\\(");
        for (int i=0; i<temp.length;i++){
            switch(i){
                case 0 :
                    problem=temp[0];
                    break;
                case 1 :
                    answerA=temp[1];
                    break;
                case 2 :
                    answerB=temp[2];
                    break;
                case 3 :
                    answerC=temp[3];
                    break;
                case 4:
                    answerD=temp[4];
                    break;

            }

        }
    }
}
