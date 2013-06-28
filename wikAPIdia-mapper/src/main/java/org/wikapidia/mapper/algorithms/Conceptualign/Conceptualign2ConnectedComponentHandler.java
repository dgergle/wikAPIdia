package org.wikapidia.mapper.algorithms.Conceptualign;

import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.LanguagedLocalId;

import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/27/13
 * Time: 4:52 PM
 * Implements Conceptualign2 connected component handling
 */
public class Conceptualign2ConnectedComponentHandler implements ConnectedComponentHandler {

    private final double minVotesRatio;
    private final int maxVotesPerLang;
    private final boolean print;
    private int curUnivId;
    private LocalPageDao lpDao;

    public Conceptualign2ConnectedComponentHandler(double minVotesRatio,
                                                   int maxVotesPerLang, boolean print, LocalPageDao lpDao) throws WikapidiaException {
        this.minVotesRatio = minVotesRatio;
        this.maxVotesPerLang = maxVotesPerLang;
        this.print = print;
        this.curUnivId = 0;

    }

    public int getCurUnivId(){
        curUnivId++;
        return curUnivId;
    }

    @Override
    public List<ClusterResult> handle(List<LanguagedLocalId> curVertices,
                                      JGraphTILLGraph graph, int componentId)
            throws WikapidiaException {

        // if its unambiguous, revert to Conceptualign1
        ConceptualignHelper.ScanResult origScanResult = ConceptualignHelper.scanVerticesOfComponent(curVertices);
        boolean origNotAmbiguous = origScanResult.clarity.equals(1.0);
        if (origNotAmbiguous){
            return ConceptualignHelper.getSingletonClusterResult(getCurUnivId(),curVertices, componentId);
        }

        // if it is ambiguous...
        Map<LanguagedLocalId, List<LanguagedLocalId>> ills = new HashMap<LanguagedLocalId, List<LanguagedLocalId>>();
        for (LanguagedLocalId curVertex : curVertices){
            Set<ILLEdge> edges = graph.outgoingEdgesOf(curVertex);
            List<LanguagedLocalId> dests = new ArrayList<LanguagedLocalId>();
            for (ILLEdge edge : edges){
                dests.add(edge.dest);
            }
            ills.put(curVertex, dests);
        }

        List<ClusterResult> rVal = new ArrayList<ClusterResult>();
        int minLangVotes = (int)Math.floor(minVotesRatio*origScanResult.langCount);
        Set<Set<LanguagedLocalId>> clusters = ILLSplitter.split(ills, minLangVotes, maxVotesPerLang, print, lpDao);
        for (Set<LanguagedLocalId> curCluster : clusters){
            int clusterUnivId = getCurUnivId();
            List<LanguagedLocalId> vertexList = new ArrayList<LanguagedLocalId>();
            vertexList.addAll(curCluster);
            ConceptualignHelper.ScanResult scanResult = ConceptualignHelper.scanVerticesOfComponent(vertexList);
            ClusterResult clusterResult = new ClusterResult(clusterUnivId, vertexList);
            rVal.add(clusterResult);
        }
        return rVal;

    }

}
