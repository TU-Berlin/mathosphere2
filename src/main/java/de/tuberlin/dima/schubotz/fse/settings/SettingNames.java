package de.tuberlin.dima.schubotz.fse.settings;

/**
 * Enum all settings here.
 */
public enum SettingNames {
    /**
     * Set classname for input class.
     */
    INPUT_OPTION("i"),
    /**
     * Filename and path to data file (raw)
     */
    DATARAW_FILE("d"),
    /**
     * Filename and path to data file (tuple csv)
     */
    DATATUPLE_FILE("e"),
    /**
     * Filename and path to query file
     */
    QUERY_FILE("q"),
    /**
     * Runtag name
     */
    RUNTAG("r"),
    /**
     * Filename and path to latex docs map file
     */
    LATEX_DOCS_MAP("l"),
    /**
     * Filename and path to keyword docs map file
     */
    KEYWORD_DOCS_MAP("k"),

    /**
     * Parellelization
     */
    NUM_SUB_TASKS("p"),

    /**
     * Output directory
     */
    OUTPUT_DIR("o"),

    /**
     * Document total
     */
    NUM_DOC("n"),
    QUERY_SEPARATOR("qs"),
    DOCUMENT_SEPARATOR("ds");


    private final String cmdLineOptionLetter;

    SettingNames(String letter) {
        this.cmdLineOptionLetter = letter;
    }

    public String getLetter() {
        return cmdLineOptionLetter;
    }
}
