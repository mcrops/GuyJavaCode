package aidev.cocis.makerere.org.whiteflycounter.webservices.field;
import java.util.Date;

public class WSReportingPeriod  {
    
    public Date ResearchStartDate;
    public Date ResearchEndDate;
    public Date StoryInputDeadline;
    public Date ReportPeriodCloseDate;
    public String PeriodDescription;
    public boolean Active;
    public String Status;
    
    public WSReportingPeriod(){}

    public Date getResearchStartDate() {
        return ResearchStartDate;
    }

    public void setResearchStartDate(Date researchStartDate) {
        ResearchStartDate = researchStartDate;
    }

    public Date getResearchEndDate() {
        return ResearchEndDate;
    }

    public void setResearchEndDate(Date researchEndDate) {
        ResearchEndDate = researchEndDate;
    }

    public Date getStoryInputDeadline() {
        return StoryInputDeadline;
    }

    public void setStoryInputDeadline(Date storyInputDeadline) {
        StoryInputDeadline = storyInputDeadline;
    }

    public Date getReportPeriodCloseDate() {
        return ReportPeriodCloseDate;
    }

    public void setReportPeriodCloseDate(Date reportPeriodCloseDate) {
        ReportPeriodCloseDate = reportPeriodCloseDate;
    }

    public String getPeriodDescription() {
        return PeriodDescription;
    }

    public void setPeriodDescription(String periodDescription) {
        PeriodDescription = periodDescription;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
