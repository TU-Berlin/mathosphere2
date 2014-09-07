package de.tuberlin.dima.schubotz.fse.mappers;

import org.junit.Ignore;


/**
 * Used as template for test classes to compare actual and expected output.
 * This file is dependent on the cleaners working. TODO REFACTOR
 */
@Ignore
public abstract class WikiAbstractSubprocessTest {
    /*
    protected static final String STR_SPLIT = WikiProgram.STR_SPLIT;
    private static final String QUERY_SEPARATOR = WikiProgram.QUERY_SEPARATOR;
    private static final String WIKI_SEPARATOR = WikiProgram.WIKI_SEPARATOR;
    //TODO REFACTOR THIS METHOD OF PASSING SEPARATORS to a getChar() method or setFormat
    private static final String CSV_LINE_SEPARATOR = WikiProgram.CSV_LINE_SEPARATOR;
    private static final String CSV_FIELD_SEPARATOR = WikiProgram.CSV_FIELD_SEPARATOR;
    private static final Log LOG = LogFactory.getLog(WikiAbstractSubprocessTest.class);

    private final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

    /**
     * Executes plan, compares test files. This method should be called by implementing classes.
     * @param outputSet dataset to output and compare to expected
     * @param expectedOutputFile expected file
     * @throws Exception execute(plan) can throw any exception
     */
    /*
    protected void testDataMap(DataSet<?> outputSet,
                               String expectedOutputFile) throws Exception {

        final File outputFile = File.createTempFile(this.getClass().getSimpleName(), "csv");
        outputFile.deleteOnExit();
        //File outputFile = new File("/home/jjl4/", "csv");

        outputSet.writeAsCsv(outputFile.getCanonicalPath(), CSV_LINE_SEPARATOR,
                CSV_FIELD_SEPARATOR, FileSystem.WriteMode.OVERWRITE);

        final Plan plan = env.createProgramPlan();
        LocalExecutor.execute(plan);

        final File expectedFile = new File(WikiAbstractSubprocessTest.class
                .getClassLoader().getResource(expectedOutputFile).getPath());
        assertTrue("Files could not be read!", expectedFile.canRead());
        assertTrue("Output does not match expected output!", FileUtils.contentEquals(outputFile, expectedFile));
    }

    protected DataSet<?> getCleanedData(String filename) throws Exception {
        String dir = null;
        try {
            dir = WikiAbstractSubprocessTest.class.getClassLoader().getResource(filename).getPath();
        } catch (NullPointerException e) {
            //Try again with absolute path
            dir = new File(filename).getPath();
        }
        final TextInputFormat format = new TextInputFormat(new Path(dir));
        if (dir.contains("expected")) { //Process as csv with tuples
            CsvReader reader = env.readCsvFile(dir);
            reader = reader.lineDelimiter(CSV_LINE_SEPARATOR);
            reader = reader.fieldDelimiter(CSV_FIELD_SEPARATOR.charAt(0));
            if (dir.contains("Query")) {
                return reader.tupleType(WikiQueryTuple.class);
            } else {
                return reader.tupleType(WikiTuple.class);
            }
        } else { //Process as normal data
            final FlatMapFunction<String, String> cleaner;
            if (dir.contains("Query")) {
                format.setDelimiter(QUERY_SEPARATOR);
                cleaner = new WikiQueryCleaner();
            } else { //WikiData
                format.setDelimiter(WIKI_SEPARATOR);
                cleaner = new WikiCleaner();
            }
            return new DataSource<String>(env,format, BasicTypeInfo.STRING_TYPE_INFO)
                    .flatMap(cleaner);
        }
    }
    */
}
