package org.wikapidia.mapper.algorithms.Conceptualign;

import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.lang.LanguagedLocalId;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/27/13
 * Time: 4:50 PM
 *
 * Handles connected components for Conceptualign1 and Conceptualign2 (which are implemented against this interface)
 *
 */
public interface ConnectedComponentHandler {

    public List<ClusterResult> handle(List<LanguagedLocalId> curVertices, JGraphTILLGraph graph, int componentId) throws WikapidiaException;

    public class ClusterResult{

        public final Integer univId;
        public final List<LanguagedLocalId> vertices;

        public ClusterResult(Integer univId, List<LanguagedLocalId> vertices){
            this.univId = univId;
            this.vertices = vertices;
        }

    }

}
