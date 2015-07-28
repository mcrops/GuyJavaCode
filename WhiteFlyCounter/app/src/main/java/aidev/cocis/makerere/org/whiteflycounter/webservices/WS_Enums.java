package aidev.cocis.makerere.org.whiteflycounter.webservices;


public class WS_Enums {

    public enum SoapProtocolVersion{
        	Default(0),
        	Soap11(1),
        	Soap12(2);
        
        private int code;
        
        SoapProtocolVersion(int code){
            this.code = code;
        }
        
        public int getCode(){
            return code;
        }
        
        public static SoapProtocolVersion fromString(String str)
        {
            if (str.equals("Default"))
                return Default;
            if (str.equals("Soap11"))
                return Soap11;
            if (str.equals("Soap12"))
                return Soap12;
            return null;
        }
    }
}
