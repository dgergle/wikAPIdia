package org.wikapidia.mapper.algorithms;

import org.apache.commons.lang3.StringUtils;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.parser.sql.MySqlDumpParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;

/**
 * Created with IntelliJ IDEA.
 * User: bjhecht
 * Date: 8/7/13
 * Time: 16:32
 * To change this template use File | Settings | File Templates.
 */
public class CreateWikidataSubsetForTesting {

    private static final String WIKIDATA_MAPPING_FILE_PATH = "/Users/bjhecht/Downloads/wikidatawiki-latest-wb_items_per_site.sql";

    public static void main(String[] args){

        try{

            // configuration
            LanguageSet langSet = new LanguageSet("simple,la");
            File outputFile = new File("/scratch/wikidata-subset.sql");

            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputFile),"UTF-8"));
            MySqlDumpParser dumpParser = new MySqlDumpParser();
            Iterable<Object[]> lines = dumpParser.parse(WIKIDATA_MAPPING_FILE_PATH);
            for (Object[] line : lines){
                String langCode = ((String)line[2]).replaceAll("wiki","");
                try{
                    Language lang = Language.getByLangCode(langCode);
                    if (langSet.containsLanguage(lang)){
                        String joinedLine = StringUtils.join(line,","
                    }
                }catch(IllegalArgumentException e){
                    // This occurs
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }


}
