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
import org.wikapidia.core.dao.MetadataDao;
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
import java.util.*;
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

    private final String getWikidataMappingFilePath;
    private final MetadataDao metadataDao;


    protected PureWikidataConceptMapper(int id, LocalPageDao<LocalPage> localPageDao, MetadataDao metadataDao, String wikidataDumpFilePath) {
        super(id, localPageDao);    //To change body of overridden methods use File | Settings | File Templates.
        this.metadataDao = metadataDao;
        this.getWikidataMappingFilePath = wikidataDumpFilePath;
    }

    @Override
    public int getId() {
        return super.getId();    //To change body of overridden methods use File | Settings | File Templates.
    }

    private Map<Language, Map<Integer, Integer>> getWikidataMappings(LanguageSet ls) throws DaoException{

        File wikiDataDumpFile = new File(WIKIDATA_MAPPING_FILE_PATH);

        final Map<Language, Map<Integer, Integer>> wikidataMappings = Maps.newHashMap();

        // loop through sql dump
        MySqlDumpParser dumpParser = new MySqlDumpParser();
        Iterable<Object[]> lines = dumpParser.parse(wikiDataDumpFile);
        int counter = 0;
        int validCounter = 0;

        for (Object[] line : lines){
            String langCode = ((String)line[2]).replaceAll("wiki","");
            if (ls.containsLanguage(langCode)){
                Language lang = Language.getByLangCode(langCode);
                if (!wikidataMappings.containsKey(lang)) wikidataMappings.put(lang, new HashMap<Integer, Integer>());
                Integer univId = (Integer)line[1];
                String strTitle = (String)line[3];
                Title title = new Title(strTitle, true, LanguageInfo.getByLangCode(lang.getLangCode()));
                LocalPage localPage = localPageDao.getByTitle(lang, title, title.getNamespace());
                wikidataMappings.get(lang).put(localPage.getLocalId(), univId);
                validCounter++;
                if (validCounter % 1000 == 0){
                    LOG.log(Level.INFO, "Number of valid LocalPages mapped: " + validCounter);
                }else{
                    LOG.log(Level.INFO, "Found local id in Wikidata dump not in LocalPageDao: " + StringUtils.join(line,","));
                }
            }
            counter++;
            if (counter % 1000000 == 0){
                LOG.log(Level.INFO, "Done with " + counter + " rows of the WikiData file. Number of rows is not known a priori.");
            }
        }

        return wikidataMappings;

    }

    @Override
    public Iterator<UniversalPage> getConceptMap(LanguageSet ls) throws DaoException {



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
