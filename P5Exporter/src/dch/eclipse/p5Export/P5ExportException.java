package dch.eclipse.p5Export;

/**
 * Simple tagged exception for exporter plugin
 */
public class P5ExportException extends Exception
{
  public P5ExportException() {
    super();
  }

  public P5ExportException(String message, Throwable cause) {
    super(getMessage(message), cause);
  }

  public P5ExportException(String message) {
    super(getMessage(message));
  }

  public P5ExportException(Throwable cause) {
    super(cause);
  }
  
  private static String getMessage(String msg) {
    if (!msg.startsWith("["))
      msg = "[ERROR] "+msg;
    return msg;
  }
 
}// end
