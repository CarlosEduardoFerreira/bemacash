package com.bematechus.bemaUtils;


/**
 * Created by b1107005 on 5/23/2015.
 */

public abstract class CommunicationPort {


    abstract public boolean open ( PortInfo info) throws CommunicationException;
    abstract public void close () throws CommunicationException;
    abstract public Integer write ( final byte[] data, int sizeToWrite  ) throws  CommunicationException;
    abstract public Integer read( byte[] data, int sizeToRead ) throws CommunicationException;

    public Integer read( byte[] data, int offset, int sizeToRead) throws CommunicationException {

        int ret = 0;
        if ( data != null && sizeToRead > 0 && offset >= 0)
        {
            byte [] buffer = new byte[sizeToRead];
            ret = read (buffer, sizeToRead);
            if ( ret > 0)
                System.arraycopy(buffer,0,data,offset,ret);
        }


        return ret;
    }

    abstract public boolean isOpen();

    protected Integer readTimeout = 0;
    protected Integer writeTimeout = 0;
    protected Integer connectionTimeout = 0;

    public Integer write ( byte[] data ) throws CommunicationException
    {
        return write ( data, data.length);
    }


    public void setWriteTimeout(Integer timeout) {
        writeTimeout = timeout;
    }

    public void setReadTimeout(Integer timeout) {
        readTimeout = timeout;
    }
    public Integer getReadTimeout () { return readTimeout;}



    public void setConnectionTimeout(Integer timeout) {
        connectionTimeout = timeout;
    }

    public byte getBasicStatus() throws CommunicationException
    {
        return 0;
    }

}
