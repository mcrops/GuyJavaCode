package aidev.cocis.makerere.org.whiteflycounter.webservices.field;
public class WSField {
    
    public String Name="";
    public String Description;
    public boolean Active;
    public int FieldID;
    
    public WSField(){}

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public boolean isActive() {
        return Active;
    }

    public void setActive(boolean active) {
        Active = active;
    }

    public int getFieldID() {
        return FieldID;
    }

    public void setFieldID(int fieldID) {
        FieldID = fieldID;
    }
}
