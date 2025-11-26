package hxc.services.caisim;

import java.net.Socket;

import hxc.services.caisim.model.Subscriber;

/**
 * An interface that allows synchronized access all active subscribers in CAISIM.
 * 
 * @author petar
 *
 */
public interface ICaiData
{	
	/**
	 * Gets a subscriber already created in CAISIM.
	 * 
	 * @param msisdn the MSISDN of the subscriber
	 * @param type the type of subscription requested
	 * @return the subscriber if existing or null if not existing
	 */
	public abstract Subscriber getSubscriber(String msisdn, SubscriptionType type);
	
	/**
	 * Creates a subscriber in CAISIM.
	 * 
	 * @param msisdn the MSISDN of the subscriber
	 * @return the created subscriber
	 */
	public abstract Subscriber createSubscriber(String msisdn);
	
	/**
	 * Check if a CAI user is authenticated.
	 * 
	 * @param clientSocket the client's TCP socket
	 * @return true if already authenticated, false otherwise
	 */
	public abstract boolean isAuthenticated(Socket clientSocket);
	
	/**
	 * Authenticate a CAI user and return true if successful.
	 * 
	 * @param user the user the client supplied
	 * @param password the password the client supplied
	 * @param clientSocket the client's TCP socket
	 * @return true if successful, false otherwise
	 */
	public abstract boolean authenticate(String user, String password, Socket clientSocket);
	
	/**
	 * Returns a lock to use when modifying CAI data.
	 * 
	 * @return the lock object
	 */
	public abstract Object getLock();
	
	/**
	 * Returns the injectedResponse (if set) for the CAI command and, if configured, modifies skipCounts and failCounts.
	 * 
	 * Example: command = SET, commandType = SAPCSUB, e.g. SET:SAPCSUB
	 * 
	 * @param command the command that is being executed
	 * @param commandType the command type that is being executed
	 * @return the injected response for the passed command and command type
	 */
	public Integer handleInjectedResponse(String command, String commandType);
}
