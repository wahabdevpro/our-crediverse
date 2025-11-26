/**
 *
 */
package hxc.services.transactions;

/**
 * Base Class for Reversal Closures
 * 
 * @author AndriesdB
 * 
 */
public abstract class Reversal
{
	/**
	 * This method performs the reversal
	 * 
	 * @throws Exception
	 */
	public abstract void reverse() throws Exception;
}
