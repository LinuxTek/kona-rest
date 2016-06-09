/*
 * Copyright (C) 2016 LinuxTek, Inc  All Rights Reserved.
 */
package com.linuxtek.kona.rest.exception;

import com.linuxtek.kona.rest.AbstractRestException;

public abstract class ApiException extends AbstractRestException {
	private static final long serialVersionUID = 1L;
    
	public ApiException() {
        super();
    }

    public ApiException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public ApiException(final String message) {
        super(message);
    }

    public ApiException(final Throwable cause) {
        super(cause);
    }
}
