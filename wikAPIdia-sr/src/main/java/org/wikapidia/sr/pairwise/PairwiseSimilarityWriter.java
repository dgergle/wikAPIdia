package org.wikapidia.sr.pairwise;

import gnu.trove.impl.hash.TIntFloatHash;
import gnu.trove.map.TIntDoubleMap;
import gnu.trove.map.TIntFloatMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.lang.Language;
import org.wikapidia.matrix.SparseMatrixRow;
import org.wikapidia.matrix.SparseMatrixWriter;
import org.wikapidia.matrix.ValueConf;
import org.wikapidia.sr.LocalSRMetric;
import org.wikapidia.sr.UniversalSRMetric;
import org.wikapidia.utils.ParallelForEach;
import org.wikapidia.utils.Procedure;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class PairwiseSimilarityWriter {
    private static final Logger LOG = Logger.getLogger(PairwiseSimilarityWriter.class.getName());
    private SparseMatrixWriter writer;
    private SparseMatrixWriter tmpWriter;
    private AtomicInteger idCounter = new AtomicInteger();
    private ValueConf vconf;
    private TIntSet validIds;
    private TIntSet usedIds = new TIntHashSet();
    private LocalSRMetric localSRMetric = null;
    private UniversalSRMetric universalSRMetric = null;
    private Language language;


    public PairwiseSimilarityWriter(File outputFile, LocalSRMetric metric, Language language) throws IOException {
        this.vconf = new ValueConf();
        this.writer = new SparseMatrixWriter(outputFile, vconf);
        File tmpFile = File.createTempFile("matrix", null);
        tmpFile.deleteOnExit();
        this.tmpWriter = new SparseMatrixWriter(tmpFile, new ValueConf());
        this.language = language;
        this.localSRMetric = metric;
    }

    public PairwiseSimilarityWriter(File outputFile, UniversalSRMetric metric) throws IOException {
        this.vconf = new ValueConf();
        this.writer = new SparseMatrixWriter(outputFile, vconf);
        File tmpFile = File.createTempFile("matrix", null);
        tmpFile.deleteOnExit();
        this.tmpWriter = new SparseMatrixWriter(tmpFile, new ValueConf());
        this.universalSRMetric = metric;
    }

    public void setValidIds(TIntSet validIds) {
        this.validIds = validIds;
    }

    public void writeSims(final int wpIds[], final int threads, final int maxSimsPerDoc) throws WikapidiaException, InterruptedException {
        List<Integer> wpIds2 = new ArrayList<Integer>();
        for (int id : wpIds) { wpIds2.add(id); }
        writeSims(wpIds2, threads, maxSimsPerDoc);
    }

    public void writeSims(List<Integer> wpIds, int threads, final int maxSimsPerDoc) throws WikapidiaException, InterruptedException{
        ParallelForEach.loop(wpIds, threads, new Procedure<Integer>() {
            public void call(Integer wpId) throws IOException, DaoException, WikapidiaException {
                writeSim(wpId, maxSimsPerDoc);
            }
        }, Integer.MAX_VALUE);
        try {
            this.writer.finish();
        } catch (IOException e){
            throw new WikapidiaException(e);
        }


    }

    private void writeSim(Integer id, int maxSimsPerDoc) throws WikapidiaException {
        if (idCounter.incrementAndGet() % 10000 == 0) {
            String nValidStr  = (validIds == null) ? "infinite" : ("" + validIds.size());
            System.err.println("" + new Date() +
                    ": finding matches for doc " + idCounter.get() +
                    ", used " + usedIds.size() + " of " + nValidStr);
        }
        TIntDoubleMap scores;
        try {
            if (localSRMetric!=null) {
                scores = localSRMetric.getVector(id, language);
            } else if (universalSRMetric!=null) {
                scores = universalSRMetric.getVector(id);
            } else {
                throw new IllegalStateException("PairwiseSimilarityWriter does not have a local or universal metric defined.");
            }
        } catch (DaoException e) {
            throw new WikapidiaException(e);
        }
        LinkedHashMap<Integer,Float> linkedHashMap = new LinkedHashMap<Integer, Float>();
        for (int i : scores.keys()) {
            linkedHashMap.put(i,(float)scores.get(i));
        }
        try {
            tmpWriter.writeRow(new SparseMatrixRow(new ValueConf(), id, linkedHashMap));
        } catch (IOException e) {
            throw new WikapidiaException(e);
        }
    }

}

