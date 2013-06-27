package org.wikapidia.mapper.algorithms.Conceptualign;

import org.wikapidia.core.lang.LanguagedLocalId;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/26/13
 * Time: 3:58 PM
 *
 * Edge in JGraphTILLGraph. Encodes connection between two LanguagedLocalIds
 */

public class ILLEdge {

    public final LanguagedLocalId host;
    public final LanguagedLocalId dest;

    public ILLEdge(LanguagedLocalId host, LanguagedLocalId dest){
        this.host = host;
        this.dest = dest;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append(host.toString());
        sb.append("_");
        sb.append(dest.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o){
        if (o instanceof ILLEdge){
            ILLEdge theirs = (ILLEdge)o;
            return (theirs.host.equals(this.host) && theirs.dest.equals(this.dest));
        }
        return false;
    }

    @Override
    public int hashCode(){
        return this.toString().hashCode();
    }
}
