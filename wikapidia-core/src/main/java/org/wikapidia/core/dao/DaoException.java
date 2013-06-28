package org.wikapidia.core.dao;

import org.wikapidia.core.WikapidiaException;

public class DaoException extends WikapidiaException {
    public DaoException(Exception e) {
        super(e);
    }

    public DaoException(String string) {
        super(string);
    }

    public DaoException(){
        super();
    }

    public DaoException(String string, Exception e){
        super(string, e);
    }
}
