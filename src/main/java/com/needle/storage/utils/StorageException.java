package com.needle.storage.utils;

public class StorageException extends RuntimeException {
	public StorageException(String message) {
		super(message);
	}

	public StorageException(String message, Throwable cause) {
		super(message, cause);
	}
}
