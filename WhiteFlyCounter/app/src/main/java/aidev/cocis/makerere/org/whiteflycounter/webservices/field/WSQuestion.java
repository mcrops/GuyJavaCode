package aidev.cocis.makerere.org.whiteflycounter.webservices.field;
public class WSQuestion  {
    
    public int QuestionOrder;
    public int FieldID;
    public int QuestionID;
    public String Question;
    
    public WSQuestion(){}

    public int getQuestionOrder() {
        return QuestionOrder;
    }

    public void setQuestionOrder(int questionOrder) {
        QuestionOrder = questionOrder;
    }

    public int getFieldID() {
        return FieldID;
    }

    public void setFieldID(int fieldID) {
        FieldID = fieldID;
    }

    public int getQuestionID() {
        return QuestionID;
    }

    public void setQuestionID(int questionID) {
        QuestionID = questionID;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }
}
