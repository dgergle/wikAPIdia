package org.wikapidia.mapper.algorithms.Conceptualign;

import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguagedLocalId;
import org.wikapidia.utils.SummingHashMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/26/13
 * Time: 4:20 PM
 *
 * Splits ILL components with conflicts (aka non-complete components).
 *
 */
public class ILLSplitter {

    private static Logger LOG = Logger.getLogger(ILLSplitter.class.getName());


    /**
     * Parameters are those described in (Bao et al. 2012)
     * @param ills
     * @param minVotes
     * @param maxVotesPerLang
     * @param print
     * @return
     * @throws WikapidiaException
     */
    public static Set<Set<LanguagedLocalId>> split(Map<LanguagedLocalId, List<LanguagedLocalId>> ills,
                                                   int minVotes, int maxVotesPerLang, boolean print, LocalPageDao lpDao) throws WikapidiaException, DaoException {

        HashMap<LanguagedLocalId, SummingHashMap<Language>> counter = new HashMap<LanguagedLocalId, SummingHashMap<Language>>();
        HashMap<LanguagedLocalId, SummingHashMap<Language>> outCounter = new HashMap<LanguagedLocalId, SummingHashMap<Language>>();
        HashMap<LanguagedLocalId, LanguagedLocalId> outFoundLinks = new HashMap<LanguagedLocalId, LanguagedLocalId>();
        for (LanguagedLocalId curSource : ills.keySet()){
            outCounter.put(curSource, new SummingHashMap<Language>());
            for(LanguagedLocalId curDest : ills.get(curSource)){
                if (!outCounter.get(curSource).containsKey(curDest.getLanguage())){
                    outCounter.get(curSource).incrementValue(curDest.getLanguage());
                    outFoundLinks.put(curSource, curDest);
                }else{
                    if(!outFoundLinks.get(curSource).equals(curDest)){ // prevent duplicates from counting as second links
                        outCounter.get(curSource).addValue(curDest.getLanguage(), 1.0);
                    }
                }
                if (!counter.containsKey(curDest)){
                    counter.put(curDest, new SummingHashMap<Language>());
                }
                counter.get(curDest).addValue(curSource.getLanguage(), 1.0);
            }
        }

        int edgeCounter = 0;
        DirectedSparseGraph<LanguagedLocalId,Integer> graph = new DirectedSparseGraph<LanguagedLocalId, Integer>();
        for (LanguagedLocalId curSource : ills.keySet()){
            graph.addVertex(curSource);
            for (LanguagedLocalId curDest : ills.get(curSource)){
                if (outCounter.get(curSource).get(curDest.getLanguage()) <= maxVotesPerLang){
                    int totalVotes = counter.get(curDest).keySet().size();
                    if (totalVotes >= minVotes){
                        if (counter.get(curDest).get(curSource.getLanguage()) <= maxVotesPerLang){
                            graph.addEdge(edgeCounter++, curSource, curDest);
                        }
                    }
                }else{
                    LOG.log(Level.FINEST, "Found duplicate ILLs to same lang from same article exceeding maxVotes! " +
                            "Enforcing policy not allowing this!:\t" +curSource + " ---> " + curDest);
                }
            }
        }

        WeakComponentClusterer<LanguagedLocalId, Integer> clusterer = new WeakComponentClusterer<LanguagedLocalId, Integer>();
        Set<Set<LanguagedLocalId>> clusters = clusterer.transform(graph);

        if (print){
            int maxSize = 0;
            Set<LanguagedLocalId> maxCluster = null;
            for (Set<LanguagedLocalId> cluster : clusters){
                StringBuilder sb = new StringBuilder();
                for (LanguagedLocalId clusterMemb : cluster){
                    sb.append(lpDao.getById(clusterMemb.getLanguage(), clusterMemb.getLocalId()).getTitle().toString());
                    sb.append(",");
                }
                LOG.log(Level.FINEST, "Cluster:\t" + sb.toString());
                maxSize = (maxSize > cluster.size()) ? maxSize : cluster.size();
                maxCluster = (maxSize > cluster.size()) ? maxCluster : cluster;
            }
            LOG.log(Level.FINEST,"Clusters identified = " + clusters.size());
            LOG.log(Level.FINEST, "Maximum Size = " + maxSize);
        }

        return clusters;
    }

}
