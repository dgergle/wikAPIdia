package org.wikapidia.lucene;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.wikapidia.core.lang.Language;
import org.wikapidia.core.lang.LanguageSet;
import org.wikapidia.core.model.NameSpace;
import org.wikapidia.core.model.RawPage;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 *
 * @author Ari Weiland
 *
 * This class is used to index raw pages during the load process.
 *
 */
public class LuceneIndexer {

    private final File root;
    private final Map<Language, WikapidiaAnalyzer> analyzers;
    private final Map<Language, IndexWriter> writers;
    private final Collection<NameSpace> nameSpaces;
    private final LuceneOptions options;

    /**
     * Constructs a LuceneIndexer that will index any RawPage within a
     * specified LanguageSet and a Collection of NameSpaces. Indexes are
     * then placed in language-specific subdirectories in the specified file.
     * @param languages
     * @param nameSpaces
     * @param root
     */
    public LuceneIndexer(LanguageSet languages, Collection<NameSpace> nameSpaces, File root) {
        this(languages, nameSpaces, root, LuceneOptions.getDefaultOptions());
    }

    /**
     * Constructs a LuceneIndexer that will index any RawPage within a
     * specified LanguageSet. Indexes are then placed in language-specific
     * subdirectories specified by options.
     * @param languages
     * @param options a LuceneOptions object containing specific options for lucene
     */
    public LuceneIndexer(LanguageSet languages, LuceneOptions options) {
        this(languages, options.nameSpaces, options.luceneRoot, options);
    }

    private LuceneIndexer(LanguageSet languages, Collection<NameSpace> nameSpaces, File root, LuceneOptions options) {
        try {
            this.root = root;
            analyzers = new HashMap<Language, WikapidiaAnalyzer>();
            writers = new HashMap<Language, IndexWriter>();
            for (Language language : languages) {
                WikapidiaAnalyzer analyzer = new WikapidiaAnalyzer(language, options);
                analyzers.put(language, analyzer);
                Directory directory = FSDirectory.open(new File(root, language.getLangCode()));
                IndexWriterConfig iwc = new IndexWriterConfig(options.matchVersion, analyzer);
                writers.put(language, new IndexWriter(directory, iwc));
            }
            this.nameSpaces = nameSpaces;
            this.options = options;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LanguageSet getLanguageSet() {
        return new LanguageSet(analyzers.keySet());
    }

    public LuceneOptions getOptions() {
        return options;
    }

    /**
     * Indexes a specific RawPage
     * @param page
     */
    public void indexPage(RawPage page) {
        Language language = page.getLang();
        if (nameSpaces.contains(page.getNamespace()) && analyzers.containsKey(language)) {
            try {
                IndexWriter writer = writers.get(language);
                Document document = new Document();
                Field localIdField = new IntField(LuceneOptions.LOCAL_ID_FIELD_NAME, page.getPageId(), Field.Store.YES);
                Field langIdField = new IntField(LuceneOptions.LANG_ID_FIELD_NAME, page.getLang().getId(), Field.Store.YES);
                Field wikiTextField = new TextField(LuceneOptions.WIKITEXT_FIELD_NAME, page.getBody(), Field.Store.YES);
                Field plainTextField = new TextField(LuceneOptions.PLAINTEXT_FIELD_NAME, page.getPlainText(), Field.Store.YES);
                document.add(localIdField);
                document.add(langIdField);
                document.add(wikiTextField);
                document.add(plainTextField);
                writer.addDocument(document);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
