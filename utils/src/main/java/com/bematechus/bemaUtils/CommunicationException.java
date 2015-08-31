package com.bematechus.bemaUtils;

/**
 * Created by b1107005 on 5/23/2015.
 */
public class CommunicationException extends Exception {

    private Integer dataTransmitted = 0;


    public ErrorCode getErr() {
        return err;
    }

    public void setErr(ErrorCode err) {
        this.err = err;
    }

    private ErrorCode err = ErrorCode.Unassigned;


    public CommunicationException() {

    }

    @Override
    public String getMessage() {
        if (  super.getMessage() == null )
            return "";
        return super.getMessage();
    }

    public CommunicationException(String detailMessage, ErrorCode err) {

        super(detailMessage);
        this.err = err;
    }

    public CommunicationException(String detailMessage, Throwable throwable, ErrorCode err) {

        super(detailMessage, throwable);
        this.err = err;
    }


    public CommunicationException(Throwable throwable, ErrorCode err) {
        super(throwable);
        this.err = err;
    }

    public Integer getDataTransmitted() {
        return dataTransmitted;
    }

    public void setDataTransmitted(Integer dataTransmitted) {
        this.dataTransmitted = dataTransmitted;
    }

    /**
     * Created by b1107005 on 5/23/2015.
     */
    public static enum ErrorCode {
        Unassigned (-1), ServiceNotInitialized(-2), PortNotAvailable(-3),
        AccessDenied(-4),ConnectionRefused(-5), ReadTimeout(-6),
        WriteTimeout(-7), ReadError(-8), WriteError(-9), CloseError(-10),SetupError(-11);

        int value;
        ErrorCode (int value)
        {
            this.value = value;
        }
        public int getValue()
        {
            return value;
        }

    }
}
