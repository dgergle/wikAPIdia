package org.wikapidia.mapper.algorithms;

import com.jolbox.bonecp.BoneCPDataSource;
import org.junit.*;
import org.wikapidia.conf.Configuration;
import org.wikapidia.conf.Configurator;
import org.wikapidia.core.WikapidiaException;
import org.wikapidia.core.cmd.Env;
import org.wikapidia.core.dao.LocalCategoryMemberDao;
import org.wikapidia.core.dao.LocalLinkDao;
import org.wikapidia.core.dao.LocalPageDao;
import org.wikapidia.core.dao.sql.LocalCategoryMemberSqlDao;
import org.wikapidia.core.dao.sql.LocalLinkSqlDao;
import org.wikapidia.core.dao.sql.LocalPageSqlDao;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.lang.LocalId;
import org.wikapidia.core.model.LocalPage;
import org.wikapidia.core.model.NameSpace;
import org.wikapidia.core.model.Title;
import org.wikapidia.core.model.UniversalPage;
import org.wikapidia.mapper.algorithms.PureWikidataConceptMapper;

import java.util.Iterator;

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

        //            Configconfigurator.getConf().get().getList("languages");
//            Language lang = Language.getByLangCode("simple");
//            Title t = new Title("Barack Obama", lang);
//            LocalPage lp = lpDao.getByTitle(lang, t, NameSpace.ARTICLE);
//            System.out.println(lp.getTitle());

        try {

            Configurator configurator = new Configurator(new Configuration());
            LocalPageDao lpDao = configurator.get(LocalPageDao.class);

            LanguageSet langSet = new LanguageSet("simple,la");
            PureWikidataConceptMapper mapper = new PureWikidataConceptMapper(0, lpDao);
            Iterator<UniversalPage> uPages = mapper.getConceptMap(langSet);
            while(uPages.hasNext()){
                UniversalPage uPage = uPages.next();
                for (Language lang : langSet){
                    if (uPage.isInLanguage(lang)){
                        LocalId localId = uPage.getLocalPages(lang).iterator().next();
                        System.out.print(lpDao.getById(localId.getLanguage(),localId.getId()).getTitle().toString() + " ");
                    }
                }
                System.out.println();
            }

        } catch (Exception e) {
            e.printStackTrace();  //To chfange body of catch statement use File | Settings | File Templates.
        }


    }
}
