package aidev.cocis.makerere.org.whiteflycounter.webservices.user;
public class WSUser {
    
    public String Name="";
    public Boolean Blocked;
    public Boolean Active;
    public int FailedLogins;
    
    public WSUser(){}

    public int getFailedLogins() {
        return FailedLogins;
    }

    public void setFailedLogins(int failedLogins) {
        FailedLogins = failedLogins;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public Boolean getBlocked() {
        return Blocked;
    }

    public void setBlocked(Boolean blocked) {
        Blocked = blocked;
    }

    public Boolean getActive() {
        return Active;
    }

    public void setActive(Boolean active) {
        Active = active;
    }
}
