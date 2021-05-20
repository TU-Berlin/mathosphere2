package com.formulasearchengine.mathosphere.mlp.contracts;

import com.formulasearchengine.mathosphere.mlp.cli.TagsCommandConfig;
import com.formulasearchengine.mathosphere.mlp.pojos.MathTag;
import com.formulasearchengine.mathosphere.mlp.pojos.RawWikiDocument;
import com.formulasearchengine.mathosphere.mlp.text.WikiTextParser;
import org.apache.flink.api.common.functions.FlatMapFunction;
import org.apache.flink.util.Collector;

/**
 * Created by Moritz on 04.08.2017.
 */
public class TagExtractionMapper implements FlatMapFunction<RawWikiDocument, MathTag> {

    private final TagsCommandConfig config;

    public TagExtractionMapper(TagsCommandConfig c) {
        config = c;
    }

    @Override
    public void flatMap(RawWikiDocument rawWikiDocument, Collector<MathTag> collector) throws Exception {
        final WikiTextParser converter = new WikiTextParser(rawWikiDocument, config);
        converter.setSkipHiddenMath(true);
        converter.processTags();
        for (MathTag tag : converter.getMetaLibrary().getFormulaLib().values()) {
            collector.collect(tag);
        }
    }
}
