package org.wikapidia.mapper.algorithms.Conceptualign;

import org.jgrapht.traverse.BreadthFirstIterator;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.InterlanguageLinkDao;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.lang.LanguagedLocalId;
import org.wikapidia.core.model.UniversalPage;
import org.wikapidia.mapper.ConceptMapper;
import org.wikapidia.mapper.MapperIterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 6/27/13
 * Time: 5:34 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class ConceptualignConceptMapper extends ConceptMapper {

    private final InterlanguageLinkDao illDao;
    protected final List<ConnectedComponentHandler> ccHandlers;

    protected ConceptualignConceptMapper(InterlanguageLinkDao illDao, LocalPageDao lpDao) {
        super(lpDao);
        this.illDao = illDao;
        this.ccHandlers = getCCHandlers();
    }

    protected abstract List<ConnectedComponentHandler> getCCHandlers();

    @Override
    public Iterator<UniversalPage> getConceptMap(LanguageSet ls) throws WikapidiaException {

        JGraphTILLGraph illGraph = new JGraphTILLGraph(illDao, localPageDao);

        BreadthFirstIterator<LanguagedLocalId, ILLEdge> bfi = new BreadthFirstIterator<LanguagedLocalId, ILLEdge>(illGraph);

        ConnectedComponentTraversalListener listener =
                new ConnectedComponentTraversalListener(illGraph, ccHandlers, this.getId(), this.localPageDao);
        bfi.addTraversalListener(listener);
        while (bfi.hasNext()){
            LanguagedLocalId localId = bfi.next();
        }

        return null;

//        return new ConceptMapper(listener.getFoundUniversalPages());
    }


}
