package org.wikapidia.core.dao;

import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.lang.LanguagedLocalId;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/26/13
 * Time: 3:51 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class InterlanguageLinkDao {

    public abstract List<LanguagedLocalId> getOutILLs(LanguagedLocalId llid) throws WikapidiaException;
    public abstract List<LanguagedLocalId> getInILLs(LanguagedLocalId llid) throws WikapidiaException;
    public abstract boolean hasLink(LanguagedLocalId hostLlid, LanguagedLocalId destLlid) throws WikapidiaException;


}
