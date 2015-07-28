package aidev.cocis.makerere.org.whiteflycounter.webservices.field;
public class WSQuestionAnswer {
    
    public boolean NewQA;
    public String Question;
    public String Answer;
    
    public WSQuestionAnswer(){}

    public boolean isNewQA() {
        return NewQA;
    }

    public void setNewQA(boolean newQA) {
        NewQA = newQA;
    }

    public String getQuestion() {
        return Question;
    }

    public void setQuestion(String question) {
        Question = question;
    }

    public String getAnswer() {
        return Answer;
    }

    public void setAnswer(String answer) {
        Answer = answer;
    }
}
