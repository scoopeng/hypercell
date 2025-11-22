/**
 * 
 */
package scoop;

/**
 * @author bradpeters
 *
 */
public class ScoopException extends Exception
{
	private String message;
	private String userMessage;
	private Exception e;
	private int errorCode;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ScoopException(Exception e)
	{
		this.message = e.getMessage();
		this.e = e;
	}

	public ScoopException(String message, Exception e)
	{
		this.message = message;
		this.e = e;
	}

	public ScoopException(String message)
	{
		this.message = message;
	}

	public ScoopException(String message, String userMessage, Exception e)
	{
		this.message = message;
		this.userMessage = userMessage;
		this.e = e;
	}

	public ScoopException(String message, String userMessage)
	{
		this.message = message;
		this.userMessage = userMessage;
	}

	public ScoopException(String message, int errorCode)
	{
		this.message = message;
		this.errorCode = errorCode;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public int getErrorCode()
	{
		return errorCode;
	}

	@Override
	public StackTraceElement[] getStackTrace()
	{
		if (e != null)
			return e.getStackTrace();
		return super.getStackTrace();
	}

	public String getUserMessage()
	{
		return userMessage;
	}

	public void setUserMessage(String userMessage)
	{
		this.userMessage = userMessage;
	}
}
