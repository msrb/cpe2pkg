/* Borrowed from Dependency Check */

package com.redhat.fabric8.analytics.cpe2pkg;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterFilter;
import org.apache.lucene.util.Version;

/**
 * A Lucene field analyzer used to analyzer queries against the CPE data.
 *
 * @author Jeremy Long
 */
public class SearchFieldAnalyzer extends Analyzer {

    /**
     * The Lucene Version used.
     */
    private final Version version;
    /**
     * The list of additional stop words to use.
     */
    private static final String[] ADDITIONAL_STOP_WORDS = {"software", "framework", "inc",
        "com", "org", "net", "www", "consulting", "ltd", "foundation", "project"};
    /**
     * The set of stop words to use in the analyzer.
     */
    private final CharArraySet stopWords;

    /**
     * Returns the set of stop words being used.
     *
     * @return the set of stop words being used
     */
    public static CharArraySet getStopWords() {
        final CharArraySet words = StopFilter.makeStopSet(ADDITIONAL_STOP_WORDS, true);
        words.addAll(StopAnalyzer.ENGLISH_STOP_WORDS_SET);
        return words;
    }

    /**
     * Constructs a new SearchFieldAnalyzer.
     *
     * @param version the Lucene version
     */
    public SearchFieldAnalyzer(Version version) {
        this.version = version;
        stopWords = getStopWords();
    }

    /**
     * Creates a the TokenStreamComponents used to analyze the stream.
     *
     * @param fieldName the field that this lucene analyzer will process
     * @param reader a reader containing the tokens
     * @return the token stream filter chain
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new AlphaNumericTokenizer();

        TokenStream stream = source;

        stream = new WordDelimiterFilter(stream,
                WordDelimiterFilter.GENERATE_WORD_PARTS
                | WordDelimiterFilter.GENERATE_NUMBER_PARTS
                | WordDelimiterFilter.PRESERVE_ORIGINAL
                | WordDelimiterFilter.SPLIT_ON_CASE_CHANGE
                | WordDelimiterFilter.SPLIT_ON_NUMERICS
                | WordDelimiterFilter.STEM_ENGLISH_POSSESSIVE, null);

        stream = new LowerCaseFilter(stream);
//        stream = new UrlTokenizingFilter(stream);
        stream = new StopFilter(stream, stopWords);
//        stream = new TokenPairConcatenatingFilter(stream);

        return new TokenStreamComponents(source, stream);
    }
}
