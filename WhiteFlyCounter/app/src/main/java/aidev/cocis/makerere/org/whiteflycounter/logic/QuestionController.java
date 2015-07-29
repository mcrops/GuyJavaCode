package aidev.cocis.makerere.org.whiteflycounter.logic;

import aidev.cocis.makerere.org.whiteflycounter.webservices.field.VectorWSQuestion;
import aidev.cocis.makerere.org.whiteflycounter.webservices.field.WSQuestion;

public class QuestionController {

    private static final String t = "QuestionController";

    VectorWSQuestion questions;
    
    public int questionCount =0;

    // Field descriptor #47 I
    public static final int ANSWER_OK = 0;
    
    // Field descriptor #47 I
    public static final int EVENT_BEGINNING_OF_FORM = 0;
    
    // Field descriptor #47 I
    public static final int EVENT_END_OF_FORM = 1;
    
    // Field descriptor #47 I
    public static final int EVENT_PROMPT_NEW_REPEAT = 2;
    
    // Field descriptor #47 I
    public static final int EVENT_QUESTION = 4;
    
    public int QUESTION_COUNTER = -1;
    
    public QuestionController(VectorWSQuestion questions) {
    	this.questions = questions;
    }
    
    public WSQuestion getNxtQuestion() {
    	if(QUESTION_COUNTER < questionCount)
    		QUESTION_COUNTER++;
        return questions.get(QUESTION_COUNTER);
    }
    public WSQuestion getCurrentQuestion() {
        return questions.get(QUESTION_COUNTER);
    }
    public WSQuestion getPrevQuestion() {
    	if(QUESTION_COUNTER > 0)
    		QUESTION_COUNTER--;
        return questions.get(QUESTION_COUNTER);
    }
    public WSQuestion getQuestion(int index) {
        return questions.get(index);
    }


    public boolean currentPromptIsQuestion() {
        return (getEvent() == EVENT_QUESTION);
    }
  


    /**
     * Navigates forward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToNextEvent(boolean stepIntoGroup) {
		return 0;
       
    }


   
    /**
     * Move the current form index to the index of the previous question in the form.
     * Step backward out of repeats and groups as needed. If the resulting question
     * is itself within a field-list, move upward to the group or repeat defining that
     * field-list.
     *
     * @return
     */
    public int stepToPreviousScreenEvent() {
		return 0;
      

    }

    /**
     * Move the current form index to the index of the next question in the form.
     * Stop if we should ask to create a new repeat group or if we reach the end of the form.
     * If we enter a group or repeat, return that if it is a field-list definition.
     * Otherwise, descend into the group or repeat searching for the first question.
     *
     * @return
     */
    public int stepToNextQuestion() {
        
        return getEvent();
    }


    
    private int getEvent() {
		return QUESTION_COUNTER++;
	}



    /**
     * Navigates backward in the form.
     *
     * @return the event that should be handled by a view.
     */
    public int stepToPreviousEvent() {
		return 0;
     

    }

}
