/**
 * This is an interface to the webservic
 * 
 **/
package aidev.cocis.makerere.org.whiteflycounter.webservices;
/**
 * This interface is used by android activities that access information from
 * WHITEFLYCOUNTER Backend.
 * 
 * @author Acellam Guy
 *
 */
public interface IHTTPEvents {
	/**
	 * Executed when the web service has just been called
	 */
    public void HTTPStartedRequest();
    /**
     * Called when the web service has finished executing
     * @param methodName the name of the action that called the web service
     * @param Data the {@link Object} data that has been sent by the service
     */
    public void HTTPFinished(String methodName, Object Data);
    /**
     * Executed when the web service has finished with errors
     * @param ex the exception that was thrown by the web service
     */
    public void HTTPFinishedWithException(Exception ex);
    /**
     * Executed when the request to service has just ended
     */
    public void HTTPEndedRequest();
}
