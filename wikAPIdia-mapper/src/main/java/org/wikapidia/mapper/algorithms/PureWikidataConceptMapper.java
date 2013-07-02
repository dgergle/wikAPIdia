package org.wikapidia.mapper.algorithms;


import com.google.common.collect.*;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.ConfigurationException;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.DaoException;
import org.wikapidia.core.dao.DaoFilter;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageInfo;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.model.LocalPage;
import org.wikapidia.core.model.NameSpace;
import org.wikapidia.core.model.Title;
import org.wikapidia.core.model.UniversalPage;
import org.wikapidia.mapper.ConceptMapper;
import org.wikapidia.mapper.MapperIterator;
import org.wikapidia.parser.sql.MySqlDumpParser;

import java.io.*;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Brent Hecht
 *
 * Maps concepts directly to their Wikidata item numbers. Conflicts are handled in the same way they are handled in Wikidata.
 *
 */


public class PureWikidataConceptMapper extends ConceptMapper {

    private static final String WIKIDATA_MAPPING_FILE_PATH = "/Users/bjhecht/Downloads/wikidatawiki-20130527-wb_items_per_site.sql";
    private static Logger LOG = Logger.getLogger(PureWikidataConceptMapper.class.getName());


    protected PureWikidataConceptMapper(int id, LocalPageDao<LocalPage> localPageDao) {
        super(id, localPageDao);    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public int getId() {
        return super.getId();    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public Iterator<UniversalPage> getConceptMap(LanguageSet ls) throws DaoException {
//        UniversalPage up = new UniversalPage(

        File wikiDataDumpFile = new File(WIKIDATA_MAPPING_FILE_PATH);

        final Map<Integer, Multimap<Language, LocalPage>> backend = Maps.newHashMap();
        final Map<Integer, NameSpace> nsBackend = Maps.newHashMap();

        // loop through sql dump
        MySqlDumpParser dumpParser = new MySqlDumpParser();
        Iterable<Object[]> lines = dumpParser.parse(wikiDataDumpFile);
        Set<String> illegalLangs = Sets.newHashSet();
        int counter = 0;
        int validCounter = 0;

        for (Object[] line : lines){
            String langCode = ((String)line[2]).replaceAll("wiki","");
            try{
                Language lang = Language.getByLangCode(langCode);
                if (ls.containsLanguage(lang)){
                    Integer univId = (Integer)line[1];
                    String strTitle = (String)line[3];
                    Title title = new Title(strTitle, true, LanguageInfo.getByLangCode(lang.getLangCode()));
                    LocalPage localPage = localPageDao.getByTitle(lang, title, title.getNamespace());
                    if (localPage != null){
                        if (!backend.containsKey(univId)){
                            Multimap<Language, LocalPage> mmap = HashMultimap.create();
                            backend.put(univId, mmap);
                            nsBackend.put(univId, localPage.getNameSpace()); // defines the universal page as having the namespace of the first LocalPage encountered
                        }
                        backend.get(univId).put(lang, localPage);
                        validCounter++;
                        if (validCounter % 1000 == 0){
                            LOG.log(Level.INFO, "Number of valid LocalPages mapped: " + validCounter);
                        }
                    }else{
                        LOG.log(Level.INFO, "Found local id in Wikidata dump not in LocalPageDao: " + StringUtils.join(line,","));
                    }
                }
            }catch(IllegalArgumentException e){
                illegalLangs.add(langCode);
            }
            counter++;
            if (counter % 1000000 == 0){
                LOG.log(Level.INFO, "Done with " + counter + " rows of the WikiData file. Number of rows is not known a priori.");
            }
        }

        if (illegalLangs.size() > 0){
            LOG.warning("Found some languages in WikiData dump not supported by wikAPIdia: " + StringUtils.join(illegalLangs));
        }

        return new MapperIterator<UniversalPage>(backend.keySet()) {
            @Override
            public UniversalPage transform(Object obj) {
                Integer univId = (Integer)obj;
                return new UniversalPage<LocalPage>(univId, getId(), nsBackend.get(univId), backend.get(univId));
            }
        };

    }

    public static class Provider extends org.wikapidia.conf.Provider<ConceptMapper> {
        public Provider(Configurator configurator, Configuration config) throws ConfigurationException {
            super(configurator, config);
        }

        @Override
        public Class getType() {
            return ConceptMapper.class;
        }

        @Override
        public String getPath() {
            return "mapper";
        }

        @Override
        public ConceptMapper get(String name, Config config) throws ConfigurationException {
            if (!config.getString("type").equals("purewikidata")) {
                return null;
            }
            return new PureWikidataConceptMapper(
                    config.getInt("algorithmId"),
                    getConfigurator().get(
                            LocalPageDao.class,
                            config.getString("localPageDao"))
            );
        }
    }
}
