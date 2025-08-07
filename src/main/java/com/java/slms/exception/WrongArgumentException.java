package com.java.slms.exception;

public class WrongArgumentException extends RuntimeException
{
    private String message;

    public WrongArgumentException(String message)
    {
        super(message);
    }

}
