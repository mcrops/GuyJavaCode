package aidev.cocis.makerere.org.whiteflycounter;

/**
 * Created by User on 7/7/2015.
 */
/**
 * <p>Encapsulates the result of decoding a barcode within an image.</p>
 *
 * @author Sean Owen
 */
public final class Field {

    private final String fieldno;
    private final String summary;
    private final long timestamp;

    public Field(String _fieldno,
                 String _summary) {
        this(_fieldno, _summary, System.currentTimeMillis());
    }

    public Field(String _fieldno,
                 String _summary,
                 long timestamp) {
        this.fieldno = _fieldno;
        this.summary = _summary;
        this.timestamp = timestamp;
    }

    public String getFieldno() {
        return fieldno;
    }
    public String getSummary() {
        return summary;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return fieldno;
    }

}