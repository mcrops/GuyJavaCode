package aidev.cocis.makerere.org.whiteflycounter.webservices.field;

public class WSFieldSetting  {

	public WSField field;
	public WSReportingPeriod reportingPeriod;
	public VectorWSQuestion questions;

	public WSFieldSetting() {
	}

	public WSField getField() {
		return field;
	}

	public void setField(WSField field) {
		this.field = field;
	}

	public WSReportingPeriod getReportingPeriod() {
		return reportingPeriod;
	}

	public void setReportingPeriod(WSReportingPeriod reportingPeriod) {
		this.reportingPeriod = reportingPeriod;
	}

	public VectorWSQuestion getQuestions() {
		return questions;
	}

	public void setQuestions(VectorWSQuestion questions) {
		this.questions = questions;
	}
}
