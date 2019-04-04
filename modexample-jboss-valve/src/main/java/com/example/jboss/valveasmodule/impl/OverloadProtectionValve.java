package com.example.jboss.valveasmodule.impl;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.Valve;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;

import com.example.jboss.overload.OverloadProtectionHandler;
import com.example.jboss.overload.OverloadProtectionHandler.OverloadProtectionStatus;
import com.example.jboss.overload.OverloadProtectionHandlerFactory;

/**
 * Convenience class implementing <b>ValveBase</b> abstract class. It implements
 * an <code>invoke()</code> method to provide the required functionality.
 *
 * This implementation can be used as an Application Valve <b>OR</b> as Engine
 * Valve
 * 
 */

public class OverloadProtectionValve extends ValveBase {
	private static Logger logger = Logger.getLogger(OverloadProtectionValve.class.getCanonicalName());

	/**
	 * Descriptive information about this Valve implementation.
	 */
	protected static String info = "com.example.jboss.valveasmodule.impl.OverloadProtectionValve";

	public OverloadProtectionValve() {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("Overload Protection Valve installed successfully.");
		}
	}

	/**
	 * Valve implementation-specific logic represented by this Valve.
	 * 
	 * @param request  Servlet request to be processed
	 * @param response Servlet response to be created
	 * 
	 * @exception IOException      if an i/o error occurs
	 * @exception ServletException if a servlet error occurs
	 */
	@Override
	public void invoke(Request request, Response response) throws IOException, ServletException {
		try {
			OverloadProtectionStatus overloadStatus = OverloadProtectionHandlerFactory.getOLPHandlerInstance().getStatus();
			int retryTimeout = OverloadProtectionHandlerFactory.getOLPHandlerInstance().getRetryAfterInterval();

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("OverloadStatus: " + overloadStatus + ". Retry Interval: " + retryTimeout);
			}

			switch (overloadStatus) {
			case BLOCKED: {
				/* Increase the retry timeout to a higher value BUT not higher than MAX_VALUE */
				long newRetryTimeout = OverloadProtectionHandler.OLPRetryTimeoutMultiplier * retryTimeout;
				retryTimeout = (int) Math.min(newRetryTimeout, Integer.MAX_VALUE);

				sendServiceUnavailable(request, response, retryTimeout);
				response.flushBuffer();

				break;
			}
			case OVERLOADED: {
				sendServiceUnavailable(request, response, retryTimeout);
				response.flushBuffer();

				break;
			}
			default: {
				invokeNextValve(request, response);
			}
			}

			if (logger.isLoggable(Level.FINE)) {
				logger.fine("Valve invoke executed.");
			}

		} catch (IOException ioex) {
			throw new IOException(ioex);
		} catch (ServletException servletException) {
			throw new ServletException(servletException);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Valve Invoke Error: ", e);
			throw new ServletException(e);

		}
	}

	/*
	 * Private method - execute next valve if any.
	 */
	private void invokeNextValve(Request request, Response response) throws IOException, ServletException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("OverloadProtectionValve - invoke next Valve if any.");
		}

		Valve next = getNext();
		if (next != null) {
			getNext().invoke(request, response);
		}
	}

	/*
	 * Private method - used to send to client a 503 http code
	 */
	private void sendServiceUnavailable(Request request, Response response, int retryTimeout)
			throws IOException, ServletException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("OverloadProtectionValve - sending Error response: HTTP Service Unavailable");
		}

		try {
			response.setIntHeader("Retry-After", retryTimeout);

			response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Server is too busy");
		} catch (IOException ioex) {
			logger.log(Level.SEVERE, "Catalina Response SendError - set Internal Error: ", ioex);

			setServiceUnavailable(request, response, ioex);

			throw new IOException(ioex);
		}
	}

	/*
	 * Private method - used to send to client a 404 http code
	 */
	@SuppressWarnings("unused")
	private void sendServiceNotFound(Request request, Response response) throws IOException, ServletException {
		// TODO: Add OLP method to know if blocking
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("OverloadProtectionValve - sending Error response: HTTP Service Not Found");
		}

		try {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Server is unable to process incoming msgs.");
		} catch (IOException ioex) {
			logger.log(Level.SEVERE, "Catalina Response SendError: ", ioex);

			setServiceUnavailable(request, response, ioex);

			throw new IOException(ioex);
		}
	}

	/*
	 * Private method - used to set a 503 http code if an exception occurs while
	 * processing this valve
	 */
	private void setServiceUnavailable(Request request, Response response, Throwable exception) throws IOException {
		if (logger.isLoggable(Level.FINE)) {
			logger.fine("OverloadProtectionValve - set Service Internal Error.");
		}

		request.setAttribute(RequestDispatcher.ERROR_EXCEPTION, exception);
		try {
			int retryTimeout = OverloadProtectionHandlerFactory.getOLPHandlerInstance().getRetryAfterInterval();
			long newRetryTimeout = OverloadProtectionHandler.OLPRetryTimeoutMultiplier * retryTimeout;
			retryTimeout = (int) Math.min(newRetryTimeout, Integer.MAX_VALUE);

			response.setIntHeader("Retry-After", retryTimeout);
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
		} catch (Exception ex) {
			logger.log(Level.SEVERE, "Valve Invoke Error: ", ex);

			throw new IOException(ex);
		}
	}
}
