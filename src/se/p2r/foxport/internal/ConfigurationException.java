package se.p2r.foxport.internal;

/**
 * Indicate problems when reading configuration or parameters.
 * @author peer
 *
 */
public class ConfigurationException extends Exception {
	private static final long serialVersionUID = 8929701975216314212L;

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

}