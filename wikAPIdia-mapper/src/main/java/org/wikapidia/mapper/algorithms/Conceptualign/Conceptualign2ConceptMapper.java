//package org.wikapidia.mapper.algorithms.Conceptualign;
//
//import org.jgrapht.traverse.BreadthFirstIterator;
//import org.wikapidia.core.WikapidiaException;
//import org.wikapidia.core.dao.DaoException;
//import org.wikapidia.core.dao.InterlanguageLinkDao;
//import org.wikapidia.core.dao.LocalPageDao;
//import org.wikapidia.core.lang.LanguageSet;
//import org.wikapidia.core.lang.LanguagedLocalId;
//import org.wikapidia.core.model.LocalPage;
//import org.wikapidia.core.model.UniversalPage;
//import org.wikapidia.mapper.ConceptMapper;
//
//import java.util.ArrayList;
//import java.util.Iterator;
//import java.util.List;
//
///**
// * Created with IntelliJ IDEA.
// * User: bjhecht
// * Date: 6/26/13
// * Time: 3:46 PM
// * To change this template use File | Settings | File Templates.
// */
//public class Conceptualign2ConceptMapper extends ConceptMapper{
//
//    private final InterlanguageLinkDao illDao;
//
//    private static final double MIN_VOTES_RATIO = 0.5;
//    private static final int MAX_VOTES_PER_LANG = 1;
//
//    public Conceptualign2ConceptMapper(InterlanguageLinkDao illDao, LocalPageDao lpDao) {
//        super(lpDao);
//        this.illDao = illDao;
//    }
//
//    @Override
//    public Iterator<UniversalPage> getConceptMap(LanguageSet ls) throws WikapidiaException{
//
//        JGraphTILLGraph illGraph = new JGraphTILLGraph(illDao, localPageDao);
//
//        BreadthFirstIterator<LanguagedLocalId, ILLEdge> bfi = new BreadthFirstIterator<LanguagedLocalId, ILLEdge>(illGraph);
//
//        List<ConnectedComponentHandler> ccHandlers = new ArrayList<ConnectedComponentHandler>();
//        ccHandlers.add(new Conceptualign2ConnectedComponentHandler(MIN_VOTES_RATIO, MAX_VOTES_PER_LANG, true, wikapidia));
//
//        ConnectedComponentTraversalListener listener =
//                new ConnectedComponentTraversalListener(illGraph, ccHandlers, this.localPageDao);
//        bfi.addTraversalListener(listener);
//        while (bfi.hasNext()){
//            LanguagedLocalId localId = bfi.next();
//        }
//    }
//}
