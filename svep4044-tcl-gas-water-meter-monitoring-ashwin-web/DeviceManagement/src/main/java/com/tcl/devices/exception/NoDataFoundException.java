/*
 * @category Customer Management
 * @copyright Copyright (C) 2018 Contus. All rights reserved.
 * @license http://www.apache.org/licenses/LICENSE-2.0
 */

package com.tcl.devices.exception;

/*
 * User defined exception to throw an error.
 */
public class NoDataFoundException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	/*
	 * This method throws an error whenever it gets called the error message gets
	 * passes to it in the method call the message gets returned to it. Thus
	 * creating a new user defined exception.
	 */
	public NoDataFoundException(String message) {
		super(message);
	}
}
