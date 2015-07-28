package aidev.cocis.makerere.org.whiteflycounter;

/**
 * Created by User on 7/7/2015.
 */

public final class FieldItem {

    private final Field field;

    FieldItem(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public String getDisplayAndDetails() {
        StringBuilder displayResult = new StringBuilder();

            //displayResult.append(field.getFieldno());

        if (field.getSummary() != null) {
            displayResult.append(field.getSummary());
        }

        return displayResult.toString();
    }

}