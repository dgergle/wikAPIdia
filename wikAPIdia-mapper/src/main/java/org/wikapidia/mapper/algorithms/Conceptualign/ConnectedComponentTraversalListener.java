package org.wikapidia.mapper.algorithms.Conceptualign;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.jgrapht.event.ConnectedComponentTraversalEvent;
import org.jgrapht.event.EdgeTraversalEvent;
import org.jgrapht.event.TraversalListener;
import org.jgrapht.event.VertexTraversalEvent;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguagedLocalId;
import org.wikapidia.core.model.LocalPage;
import org.wikapidia.core.model.NameSpace;
import org.wikapidia.core.model.UniversalPage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/27/13
 * Time: 5:06 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConnectedComponentTraversalListener implements TraversalListener<LanguagedLocalId, ILLEdge> {

    private List<LanguagedLocalId> curVertices;
    private final JGraphTILLGraph graph;
    private final List<ConnectedComponentHandler> ccHandlers;
    private int curComponentId;
    private List<LocalPage> curLocalPages;
    private final LocalPageDao lpDao;
    private Set<UniversalPage> universalPages;
    private final int algId;

    private static Logger LOG = Logger.getLogger(ConnectedComponentTraversalListener.class.getName());

    public ConnectedComponentTraversalListener(JGraphTILLGraph graph, List<ConnectedComponentHandler> ccHandlers, int algId, LocalPageDao lpDao) throws WikapidiaException {
        this.graph = graph;
        this.ccHandlers = ccHandlers;
        this.lpDao = lpDao;
        this.algId = algId;
        this.curComponentId = 0;
        this.universalPages = Sets.newHashSet();
        newConnectedComponent();
    }

    private void newConnectedComponent(){
        //log.trace(""); // stupid hack for a new line
        this.curVertices = new ArrayList<LanguagedLocalId>();
        curComponentId++;
    }

    @Override
    public void connectedComponentFinished(ConnectedComponentTraversalEvent arg0) {

        if (curVertices.size() > 0){ // make sure we're dealing with a valid component (e.g. not a disambig page)
            try{
                for (ConnectedComponentHandler ccHandler : ccHandlers){
                    List<ConnectedComponentHandler.ClusterResult> clusters = ccHandler.handle(curVertices, graph, curComponentId);
                    for (ConnectedComponentHandler.ClusterResult cluster : clusters){

                        NameSpace curNameSpace = null;// uses the namespace of the first LocalPage in the component. Will warn when conflicts are encountered.
                        Multimap<Language, LocalPage> map = HashMultimap.create();
                        for (LanguagedLocalId llid : cluster.vertices){
                            LocalPage curLocalPage = lpDao.getById(llid.getLanguage(), llid.getLocalId()); // this second call to the dao appears to be necessary at this junction; possibly could be eliminated with additional work.
                            map.put(curLocalPage.getLanguage(), curLocalPage);
                            if (curNameSpace == null){
                                curNameSpace = curLocalPage.getNameSpace();
                            }else if(!curNameSpace.equals(curLocalPage.getNameSpace())){
                                LOG.warning("Found two or more namespaces in a single final component: " + curLocalPage.toString() + " is not a " + curNameSpace.toString());
                            }
                        }

                        UniversalPage up = new UniversalPage (
                                curComponentId,
                                this.algId,
                                curNameSpace,
                                map
                        );

                        universalPages.add(up);

                    }
                }

                if(curComponentId % 1000 == 0){
                    LOG.log(Level.INFO, String.format("Traversed through %d connected components", curComponentId));
                }

            }catch(WikapidiaException e){
                e.printStackTrace();
                throw new RuntimeException("Major error while processing connected component");
            }
        }

    }
    @Override
    public void connectedComponentStarted(ConnectedComponentTraversalEvent arg0) {
        newConnectedComponent();
    }
    @Override
    public void edgeTraversed(EdgeTraversalEvent<LanguagedLocalId, ILLEdge> arg0) {
        // TODO Auto-generated method stub

    }
    @Override
    public void vertexFinished(VertexTraversalEvent<LanguagedLocalId> arg0) {


    }
    @Override
    public void vertexTraversed(VertexTraversalEvent<LanguagedLocalId> arg0) {


        try{

            LanguagedLocalId vertex = arg0.getVertex();
            LocalPage localPage = lpDao.getById(vertex.getLanguage(), vertex.getLocalId());//LocalConcept.getLocalConcept(vertex.getLangId(), vertex.getLocalId(), w);


            if(localPage == null){
                LOG.warning("Could not find local article: " + vertex.toString());
                return;
            }

            // exclude disambiguation pages from concepts (part of the definition of Conceptualign 1 + 2)
            Boolean valid = !localPage.isDisambig();
            if (valid){
                curVertices.add(vertex);
            }


        }catch(WikapidiaException e){
            throw new RuntimeException(e);
        }
    }

    public Set<UniversalPage> getFoundUniversalPages(){
        return universalPages;
    }

}
