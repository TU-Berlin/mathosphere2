package de.tuberlin.dima.schubotz.fse.modules.inputs.raw;

import de.tuberlin.dima.schubotz.fse.mappers.cleaners.Cleaner;
import de.tuberlin.dima.schubotz.fse.modules.inputs.Input;
import de.tuberlin.dima.schubotz.fse.settings.DataStorage;
import de.tuberlin.dima.schubotz.fse.settings.SettingNames;
import de.tuberlin.dima.schubotz.fse.settings.Settings;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.io.TextInputFormat;
import org.apache.flink.api.java.operators.DataSource;
import org.apache.flink.api.java.typeutils.BasicTypeInfo;
import org.apache.flink.core.fs.Path;

import java.util.Collection;

/**
 * Configures input for ARXIV data input format.
 * Cleans data using mapper cleaners.
 */
public abstract class RawInput implements Input {
    private final Cleaner queryCleaner;
    private final Cleaner docCleaner;

    /**
     * TODO Consider moving these into command line options/settings properties
     */    //Add any options here
    private static final Option DATA_FILE = new Option(
            SettingNames.DATARAW_FILE.getLetter(), SettingNames.DATARAW_FILE.toString(),
            true, "Path to data file.");
    private static final Option QUERY_FILE = new Option(
            SettingNames.QUERY_FILE.getLetter(), SettingNames.QUERY_FILE.toString(),
            true, "Path to query file.");

    private static final Options options = new Options();

    static {
        //Initialize command line options here
        DATA_FILE.setRequired(true);
        DATA_FILE.setArgName("/path/to/data");

        QUERY_FILE.setRequired(true);
        QUERY_FILE.setArgName("/path/to/queries");

        options.addOption(DATA_FILE);
        options.addOption(QUERY_FILE);
    }

    protected RawInput(Cleaner queryCleaner, Cleaner docCleaner) {
        this.queryCleaner = queryCleaner;
        this.docCleaner = docCleaner;
    }


    @Override
    public Collection<Option> getOptionsAsIterable() {
        return options.getOptions();
    }


    public void configure(ExecutionEnvironment env, DataStorage data) {
        final TextInputFormat inputQueries = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.QUERY_FILE)));
        inputQueries.setDelimiter(queryCleaner.getDelimiter());
        final DataSet<String> rawQueryText = new DataSource<>(env, inputQueries, BasicTypeInfo.STRING_TYPE_INFO);
        data.setQuerySet(rawQueryText.flatMap(queryCleaner));

        final TextInputFormat inputData = new TextInputFormat(new Path(
                Settings.getProperty(SettingNames.DATARAW_FILE)));
        inputData.setDelimiter(docCleaner.getDelimiter());
        final DataSet<String> rawArticleText = new DataSource<>(env, inputData, BasicTypeInfo.STRING_TYPE_INFO);
        data.setDataSet(rawArticleText.flatMap(docCleaner));
    }

}
