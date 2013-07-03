package org.wikapidia.core.dao;

import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 7/2/13
 * Time: 1:51 PM
 * To change this template use File | Settings | File Templates.
 */
public interface MetadataDao {

    public LanguageSet getParsedLanguages() throws DaoException;
    public Language getBestEnglishLanguage() throws DaoException;

}
