package org.wikapidia.mapper.algorithms.Conceptualign;

import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguagedLocalId;
import org.wikapidia.utils.SummingHashMap;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/27/13
 * Time: 4:54 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConceptualignHelper {


    public static List<ConnectedComponentHandler.ClusterResult> getSingletonClusterResult(int curUnivId,
                                                                List<LanguagedLocalId> curVertices, int componentId){

        ScanResult origScanResult = ConceptualignHelper.scanVerticesOfComponent(curVertices);

        List<ConnectedComponentHandler.ClusterResult> rVal = new ArrayList<ConnectedComponentHandler.ClusterResult>();
        ConnectedComponentHandler.ClusterResult singleton = new ConnectedComponentHandler.ClusterResult(curUnivId, curVertices);
        rVal.add(singleton);
        return rVal;
    }

    public static ScanResult scanVerticesOfComponent(List<LanguagedLocalId> curVertices){

        assert(curVertices.size() > 0);
        SummingHashMap<Language> dict = new SummingHashMap<Language>();
        for (LanguagedLocalId curVertex : curVertices){
            dict.addValue(curVertex.getLanguage(), 1);
        }

        Integer langCount = dict.keySet().size();
        Integer articleCount = curVertices.size();
        assert(langCount > 0 && articleCount > 0);
        Double clarity = ((double)langCount/(double)articleCount);

        ScanResult scanResult = new ScanResult(clarity, langCount, articleCount);
        return scanResult;

    }

    public static class ScanResult{

        public final Double clarity;
        public final Integer langCount;
        public final Integer articleCount;

        public ScanResult(Double clarity, Integer langCount, Integer articleCount){
            this.clarity = clarity;
            this.langCount = langCount;
            this.articleCount = articleCount;
        }

    }

}
