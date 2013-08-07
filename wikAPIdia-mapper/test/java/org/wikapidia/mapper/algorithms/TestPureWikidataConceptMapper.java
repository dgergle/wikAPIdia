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
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.model.LocalPage;
import org.wikapidia.core.model.NameSpace;
import org.wikapidia.core.model.Title;
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

            Configurator configurator = new Configurator(new Configuration());
            LocalPageDao lpDao = configurator.get(LocalPageDao.class);
            Language lang = Language.getByLangCode("simple");
            Title t = new Title("Barack Obama", lang);
            LocalPage lp = lpDao.getByTitle(lang, t, NameSpace.ARTICLE);
            System.out.println(lp.getTitle());


//                    PureWikidataConceptMapper mapper = new PureWikidataConceptMapper(0, lpDao);
//            mapper.getConceptMap(LanguageSet.ALL);

        } catch (Exception e) {
            e.printStackTrace();  //To chfange body of catch statement use File | Settings | File Templates.
        }


    }
}
