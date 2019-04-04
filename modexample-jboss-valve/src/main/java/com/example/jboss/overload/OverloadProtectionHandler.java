package com.example.jboss.overload;

public interface OverloadProtectionHandler {
	/**
	 * This is an enum constant presenting OLP status.
	 */

	public static enum OverloadProtectionStatus {
		/**
		 * System is overloaded; for example, the CPU or memory upper threshold is
		 * reached.
		 */
		OVERLOADED,
		/**
		 * System is highly overloaded; for example, the CPU or memory maximum threshold
		 * is reached.
		 */
		BLOCKED,
		/**
		 * OLP service is disabled through IMM.
		 */
		DISABLED,
		/**
		 * system is running under normal conditions.
		 */
		OK
	}

	/**
	 * This field is used in OLP BLOCK state when HTTP error code 503 returned with
	 * Retry-After header. The value will be retryAfterInterval multiples the
	 * OLPRetryTimeoutMultiplier.
	 */
	public static final int OLPRetryTimeoutMultiplier = 3;

	/**
	 * This method is used to indicate if any of the CPU or Memory the first OLP
	 * threshold
	 * 
	 * @return while the system is overloaded or not
	 */
	boolean isOverloadReached();

	/**
	 * This method is used to indicate if any of the CPU or Memory has passed the
	 * second OLP threshold.
	 * 
	 * @return while the system is Blocked or not
	 */
	boolean isMaximumOverloadReached();

	/**
	 * This method is used to indicate if the system has passed the first OLP
	 * threshold
	 * 
	 * @param isOR This parameter is set to true to indicate that if at least one
	 *             observer passed the threshold, the system will be overloaded.
	 *             <BR>
	 *             However if this parameter is set to false, all the observers have
	 *             to pass the threshold for the system to be overloaded.
	 * @return while the system is overloaded or not
	 */

	boolean isOverloadReached(boolean isOR);

	/**
	 * This method is used to indicate if the system has passed the second OLP
	 * threshold
	 * 
	 * @param isOR This parameter is set to true to indicate that if at least one
	 *             observer passed the threshold, the system will be blocked. <BR>
	 *             However if this parameter is set to false, all the observers have
	 *             to pass the threshold for the system to be blocked.
	 * @return while the system is overloaded or not
	 */

	boolean isMaximumOverloadReached(boolean isOR);

	/**
	 * This method is used to return the number of seconds the client has to wait
	 * before retrying the request.
	 * 
	 * @return number of seconds to wait before retry
	 */
	public int getRetryAfterInterval();

	/**
	 * This method is used to indicate the system status
	 * {@code OverloadProtectionStatus} if at least one observer passed the
	 * threshold, the system will be overloaded or blocked.
	 * 
	 * @return {@code OverloadProtectionStatus} The current status of the system
	 */
	public OverloadProtectionStatus getStatus();

	/**
	 * This method is used to indicate the system status
	 * {@code OverloadProtectionStatus}
	 * 
	 * @param isOR This parameter is set to true to indicate that if at least one
	 *             observer passed the threshold, the system will be overloaded or
	 *             blocked. <BR>
	 *             However if this parameter is set to false, all the observers have
	 *             to pass the threshold for the system to be overloaded or blocked.
	 * @return {@code OverloadProtectionStatus} The current status of the system
	 */
	public OverloadProtectionStatus getStatus(boolean isOR);
}
