package com.formulasearchengine.mathosphere.basex;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.formulasearchengine.mathmlquerygenerator.NtcirPattern;
import com.formulasearchengine.mathmlquerygenerator.XQueryGenerator;
import net.xqj.basex.BaseXXQDataSource;
import org.basex.query.QueryException;
import org.intellij.lang.annotations.Language;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import javax.xml.xquery.*;
import java.io.IOException;
import java.util.List;

/**
 * Created by Moritz on 08.11.2014.
 */
public class Client {
	private Results results = new Results();
	private Results.Run currentRun = results.new Run( "baseX" + System.currentTimeMillis(), "automated" );
	private Results.Run.Result currentResult;
	private Long measurement;
	private boolean useXQ = true;
	private boolean success = false;

	public Client() {}

	public Client( List<NtcirPattern> patterns ) {
		for ( NtcirPattern pattern : patterns ) {
			processPattern( pattern );
		}
		results.addRun( currentRun );
	}

	private void processPattern( NtcirPattern pattern ) {
		currentResult = currentRun.new Result( pattern.getNum() );
		basex( pattern.getxQueryExpression() );
		currentRun.addResult( currentResult );
	}

	/**
	 * Connects with the BaseX database, sending the given query and saves the
	 * result in a list
	 */
	public Long basex( String query ) {
		try {
			runQuery( query );
		} catch ( XQException | IOException | QueryException e ) {
			e.printStackTrace();
			return -1L;
		}
		return measurement;
	}

	protected int runQuery( String query ) throws XQException, IOException, QueryException {
		int score = 10;
		int rank = 1;
		if ( useXQ ) {
			XQConnection conn = getXqConnection();
			XQPreparedExpression xqpe = conn.prepareExpression( query );
			measurement = System.nanoTime();
			XQResultSequence rs = xqpe.executeQuery();
			measurement = System.nanoTime() - measurement;
			currentResult.setTime( measurement );
			while (rs.next()) {
				currentResult.addHit( rs.getItemAsString( null ).replaceAll( "\r", "" ), "", score, rank );
				rank++;
			}
			conn.close();
		} else {
			//TODO: This does not yet work
/*			measurement = System.nanoTime();
			new Open("math").execute( Server.context );
			QueryProcessor proc = new QueryProcessor(query, Server.context );
			Iter iter = proc.iter();
			for(Item item; (item = iter.next()) != null;) {
				Object o = item.toJava();
				String s;
				if(o instanceof String){
					s = (String) o;
				} else {
					s = item.toString();
				}
				currentResult.addHit( s, "", score, rank );
				rank++;
			}*/
		}
		return rank--;
	}

	private static XQConnection getXqConnection() throws XQException {
		XQDataSource xqs = new BaseXXQDataSource();
		xqs.setProperty( "serverName", "localhost" );
		xqs.setProperty( "port", "1984" );
		xqs.setProperty( "databaseName", "math" );

		return xqs.getConnection( "admin", "admin" );
	}

	public String getXML() {
		return results.toXML();
	}

	public String getCSV() {
		return results.toCSV();
	}
	
	public String runTexQuery( String tex ){
		if (tex == null || tex.equals( "" )){
			success = false;
			return "TeX query was empty.";
		}
		TexQueryGenerator t = new TexQueryGenerator();
		String mmlString = t.request( tex );
		if ( mmlString != null ){
			Document doc = XMLHelper.String2Doc( mmlString );
			return runMWSQuery( doc );
		}
		success = false;
		try {
			return t.getErrorMessage() ;
		} catch ( JsonProcessingException ignore ) {
			return "Tex parsing failed. Can not parse error message.";
		}
	}

	public String runMWSQuery( Document mwsQuery ) {
		if ( mwsQuery == null ){
			success = false;
			return "got empty MathML document";
		}
		XQueryGenerator generator = new XQueryGenerator( mwsQuery );
		generator.setHeader( Benchmark.BASEX_HEADER );
		generator.setFooter( Benchmark.BASEX_FOOTER );
		return runXQuery( generator.toString() );
	}

	public String runXQuery (String query) {
		currentResult = currentRun.new Result( "" );
		try {
			runQuery( query );
			success = true;
			if ( currentResult.size() > 0 ) {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"" + currentResult.size() + "\">\n"
					+ currentResult.toXML() + "</results>\n";
			} else {
				return "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
					+ "<results xmlns=\"http://ntcir-math.nii.ac.jp/\" total=\"0\" />\n";
			}
		} catch ( Exception e ) {
			success = false;
			return "Query :\n" + query + "\n\n failed " + e.getLocalizedMessage();
		}
	}

	public void setShowTime (boolean showTime) {
		this.results.setShowTime(showTime);
	}
	public void setUseXQ (boolean useXQ) {
		this.useXQ = useXQ;
	}

	public boolean isSuccess () {
		return success;
	}

	String directXQuery(String query) throws IOException, QueryException, XQException {
		String out = "";
		XQConnection conn = getXqConnection();
		XQPreparedExpression xqpe = conn.prepareExpression( query );
		XQResultSequence rs = xqpe.executeQuery();
		while (rs.next()) {
			out +=  rs.getItemAsString( null ).replaceAll( "\r", "" );
		}
		conn.close();
		return out;
	}

	public int countRevisionFormula(int rev){
		try {
			return Integer.parseInt( directXQuery( "count(//*:" + getRevFormula( rev ) + ")"
			) );
		} catch ( XQException | IOException | QueryException e ) {
			e.printStackTrace();
			return 0;
		}
	}
	public int countAllFormula(){
		try {
			return Integer.parseInt( directXQuery( "count(./*/*)" ) );
		} catch ( XQException | IOException | QueryException e ) {
			e.printStackTrace();
			return 0;
		}
	}

	private String getRevFormula( int rev ) {
		return "expr[matches(@url, '" + rev + "#(.*)')]";
	}

	public boolean deleteRevisionFormula(int rev){
		try {
			directXQuery( "delete node //*:"+ getRevFormula( rev ) );
			if (countRevisionFormula( rev ) == 0 ){
				return true;
			} else {
				return false;
			}
		} catch ( XQException | IOException | QueryException e ) {
			e.printStackTrace();
			return false;
		}
	}
	public boolean updateFormula(Node n){
		@Language("XQuery") final String xUpdate = "declare namespace mws=\"http://search.mathweb.org/ns\";\n" +
			"declare variable $input external;\n" +
			"for $e in $input/mws:expr\n" +
			"return ( delete node //*[@url=$e/@url], insert node $e into /mws:harvest[1])";
		try {
			XQConnection conn = getXqConnection();
			XQPreparedExpression xqpe = conn.prepareExpression( xUpdate );
			xqpe.bindNode( new QName( "input" ), n, null );
			xqpe.executeQuery();
				return true;
		} catch ( XQException e ) {
			e.printStackTrace();
			return false;
		}
	}
}
