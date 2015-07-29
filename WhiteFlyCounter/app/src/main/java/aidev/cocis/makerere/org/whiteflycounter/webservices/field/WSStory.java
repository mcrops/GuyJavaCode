package aidev.cocis.makerere.org.whiteflycounter.webservices.field;
public class WSStory  {
    
    public int StoryID;
    public int StoryAutoNumber;
    public String Title;
    public String Status;
    public int Version;
    
    public WSStory(){}

    public int getStoryID() {
        return StoryID;
    }

    public void setStoryID(int storyID) {
        StoryID = storyID;
    }

    public int getStoryAutoNumber() {
        return StoryAutoNumber;
    }

    public void setStoryAutoNumber(int storyAutoNumber) {
        StoryAutoNumber = storyAutoNumber;
    }

    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public int getVersion() {
        return Version;
    }

    public void setVersion(int version) {
        Version = version;
    }
}
