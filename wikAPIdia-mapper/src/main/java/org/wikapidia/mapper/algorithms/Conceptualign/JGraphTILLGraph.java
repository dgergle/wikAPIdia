package org.wikapidia.mapper.algorithms.Conceptualign;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jgrapht.EdgeFactory;
import org.jgrapht.UndirectedGraph;

import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.dao.InterlanguageLinkDao;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.lang.LanguagedLocalId;

/**
 * Implementation of a JGraphT UndirectedGraph for the Wikipedia Interlanguage Link (ILL) graph
 */
public class JGraphTILLGraph implements
        UndirectedGraph<LanguagedLocalId, ILLEdge>{

    private final InterlanguageLinkDao illDao;
    private final LocalPageDao lpDao;


    public JGraphTILLGraph(InterlanguageLinkDao illDao, LocalPageDao lpDao){
        this.illDao = illDao;
        this.lpDao = lpDao;
    }

    @Override
    public ILLEdge addEdge(LanguagedLocalId arg0, LanguagedLocalId arg1) {
        throw new RuntimeException("Read only graph");
    }

    @Override
    public boolean addEdge(LanguagedLocalId arg0, LanguagedLocalId arg1,
                           ILLEdge arg2) {
        throw new RuntimeException("Read only graph");
    }

    @Override
    public boolean addVertex(LanguagedLocalId arg0) {
        throw new RuntimeException("Read only graph");
    }

    @Override
    public boolean containsEdge(ILLEdge arg0) {
        try{
            return illDao.hasLink(arg0.host, arg0.dest);
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsEdge(LanguagedLocalId arg0, LanguagedLocalId arg1) {
        try{
            return illDao.hasLink(arg0, arg1);
//            return illqs.hasLink(arg0.getLangId(), arg0.getLocalId(), arg1.getLangId(), arg1.getLocalId());
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsVertex(LanguagedLocalId arg0) {
        try{
            return (lpDao.getById(arg0.getLanguage(), arg0.getLocalId()) == null);
        }catch(DaoException e){
            throw new RuntimeException(e);
        }
    }


    @Override
    public Set<ILLEdge> edgeSet() {
        throw new RuntimeException("edgeSet() not supported in ILL Graph due to memory reasons");
    }

    @Override
    public Set<ILLEdge> edgesOf(LanguagedLocalId arg0) {
        try{
            HashSet<ILLEdge> rVal = new HashSet<ILLEdge>();
            List<LanguagedLocalId> outEdges = illDao.getOutILLs(arg0);
            List<LanguagedLocalId> inEdges = illDao.getInILLs(arg0);
            rVal.addAll(makeEdges(arg0, outEdges, true));
            rVal.addAll(makeEdges(arg0, inEdges, false));
            return rVal;
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    private List<ILLEdge> makeEdges(LanguagedLocalId single, List<LanguagedLocalId> manys, boolean outlinks){

        List<ILLEdge> rVal = new ArrayList<ILLEdge>();
        for (LanguagedLocalId many : manys){
            ILLEdge edge;
            if (outlinks){
                edge = new ILLEdge(single, many);
            }else{
                edge = new ILLEdge(many, single);
            }
            rVal.add(edge);
        }
        return rVal;

    }

    @Override
    public Set<ILLEdge> getAllEdges(LanguagedLocalId arg0, LanguagedLocalId arg1) {

        try{
            HashSet<ILLEdge> rVal = new HashSet<ILLEdge>();
            if (illDao.hasLink(arg0, arg1)){
                rVal.add(new ILLEdge(arg0, arg1));
            }
            if (illDao.hasLink(arg1, arg0)){
                rVal.add(new ILLEdge(arg1, arg0));
            }
            return rVal;
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public ILLEdge getEdge(LanguagedLocalId arg0, LanguagedLocalId arg1) {
        try{
            if (illDao.hasLink(arg0,arg1)){
                return new ILLEdge(arg0, arg1);
            }else{
                return null;
            }
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public EdgeFactory<LanguagedLocalId, ILLEdge> getEdgeFactory() {
        throw new RuntimeException("Read only graph");
    }

    @Override
    public LanguagedLocalId getEdgeSource(ILLEdge arg0) {
        return arg0.host;
    }

    @Override
    public LanguagedLocalId getEdgeTarget(ILLEdge arg0) {
        return arg0.dest;
    }

    @Override
    public double getEdgeWeight(ILLEdge arg0) {
        return 0;
    }

    @Override
    public boolean removeAllEdges(Collection<? extends ILLEdge> arg0) {
        throw new RuntimeException("Read-only graph");
    }

    @Override
    public Set<ILLEdge> removeAllEdges(LanguagedLocalId arg0,
                                       LanguagedLocalId arg1) {
        throw new RuntimeException("Read-only graph");
    }

    @Override
    public boolean removeAllVertices(Collection<? extends LanguagedLocalId> arg0) {
        throw new RuntimeException("Read-only graph");
    }

    @Override
    public boolean removeEdge(ILLEdge arg0) {
        throw new RuntimeException("Read-only graph");
    }

    @Override
    public ILLEdge removeEdge(LanguagedLocalId arg0, LanguagedLocalId arg1) {
        throw new RuntimeException("Read-only graph");
    }

    @Override
    public boolean removeVertex(LanguagedLocalId arg0) {
        throw new RuntimeException("Read-only graph");
    }

    @Override
    public Set<LanguagedLocalId> vertexSet() {

        try{
            HashSet<LanguagedLocalId> rVal = new HashSet<LanguagedLocalId>();
            LanguageSet ls = LanguageSet.getSetOfAllLanguages();
//            LanguageSet ls = lcqs.getLanguageSet();
            for (Language lang : ls){
                List<Integer> allLocalIds = lcqs.getAllLocalIds(curLangId); // *** NOT CURRENTLY POSSIBLE TO CONVERT ***
                for (Integer localId : allLocalIds){
                    rVal.add(new LanguagedLocalId(localId, lang));
                }
            }
            return rVal;
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }

    }


    public int inDegreeOf(LanguagedLocalId arg0) {
        return incomingEdgesOf(arg0).size();
    }


    public Set<ILLEdge> incomingEdgesOf(LanguagedLocalId arg0) {
        try{
            HashSet<ILLEdge> rVal = new HashSet<ILLEdge>();
            List<LanguagedLocalId> inlinks = illDao.getInILLs(arg0);
            if (inlinks != null){
                rVal.addAll(makeEdges(arg0,inlinks, false));
            }
            return rVal;
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }


    public int outDegreeOf(LanguagedLocalId arg0) {
        return outgoingEdgesOf(arg0).size();
    }


    public Set<ILLEdge> outgoingEdgesOf(LanguagedLocalId arg0) {
        try{
            HashSet<ILLEdge> rVal = new HashSet<ILLEdge>();
            List<LanguagedLocalId> outlinks = illDao.getOutILLs(arg0);
            for (LanguagedLocalId outlink : outlinks){
                if (outlink.getLocalId() == 0){
                    System.out.println(arg0.getLocalId() + " --> " + outlink.getLocalId());
                }
            }
            if (outlinks != null){
                rVal.addAll(makeEdges(arg0,outlinks, true));
            }
//			for(LanguagedLocalId outlink : outlinks){
//				System.out.println(arg0.toString() + "-->" + outlink.toString());
//			}
            return rVal;
        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public int degreeOf(LanguagedLocalId arg0) {
        return edgesOf(arg0).size();
    }





}

