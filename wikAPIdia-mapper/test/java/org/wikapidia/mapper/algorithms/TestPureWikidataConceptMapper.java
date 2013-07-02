package org.wikapidia.mapper.algorithms;

import com.jolbox.bonecp.BoneCPDataSource;
import org.junit.*;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.dao.LocalCategoryMemberDao;
import org.wikapidia.core.dao.LocalLinkDao;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.dao.sql.LocalCategoryMemberSqlDao;
import org.wikapidia.core.dao.sql.LocalLinkSqlDao;
import org.wikapidia.core.dao.sql.LocalPageSqlDao;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.mapper.algorithms.PureWikidataConceptMapper;

/**
* Created with IntelliJ IDEA.
* User: bjhecht
* Date: 6/25/13
* Time: 2:33 PM
* To change this template use File | Settings | File Templates.
*/
public class TestPureWikidataConceptMapper {

    @Test
    public void testBasic(){

        try {

            Configuration configuration = new Configuration(null);
            Configurator conf = new Configurator(configuration);

            LocalPageDao lpDao = conf.get(LocalPageDao.class);
            PureWikidataConceptMapper mapper = new PureWikidataConceptMapper(lpDao);
            LanguageSet ls = new LanguageSet("simple,la");
            System.out.println(ls);
            mapper.getConceptMap(ls);


        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }
}
