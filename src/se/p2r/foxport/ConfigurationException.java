package se.p2r.foxport;

/**
 * Indicate problems when reading configuration or parameters.
 * @author peer
 *
 */
class ConfigurationException extends Exception {
	private static final long serialVersionUID = 8929701975216314212L;

	public ConfigurationException(String message, Throwable cause) {
		super(message, cause);
	}

	public ConfigurationException(Throwable cause) {
		super(cause);
	}

}