package de.tuberlin.dima.schubotz.fse.mappers;

import com.google.common.collect.Multiset;
import de.tuberlin.dima.schubotz.fse.types.DatabaseResultTuple;
import de.tuberlin.dima.schubotz.fse.types.DatabaseTuple;
import de.tuberlin.dima.schubotz.fse.utils.CMMLInfo;
import de.tuberlin.dima.schubotz.fse.utils.SafeLogWrapper;
import net.sf.saxon.s9api.XQueryExecutable;
import org.apache.flink.api.java.functions.RichFlatMapFunction;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.util.Collector;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.util.*;

public class MainMapper extends RichFlatMapFunction<DatabaseTuple, DatabaseResultTuple> {
	private Set<Query> queries;
	private Map<Tuple2<Integer, String>, Integer> votes;
	private static final SafeLogWrapper LOG = new SafeLogWrapper( MainMapper.class );

	public Query getQuery(Integer i, String s){
		Query q = new Query();
		q.qId=i;
		q.qfId=s;
		return q;
	}
	class Query {
		public Integer qId;
		public String qfId;
		public XQueryExecutable CDQuery; // Measure 1
		public XQueryExecutable dataTypeQuery; // Measure 2
		public Multiset queryTokens; // Measure 3
		public XQueryExecutable standardQuery; // Measure 4
	}

	@Override
	public void open (Configuration parameters) throws Exception {
		loadQueries(
			getRuntimeContext().<Tuple3<Integer, String, String>>getBroadcastVariable( "Queries" )
		);
		loadVotes(
			getRuntimeContext().<Tuple3<Integer, String, Integer>>getBroadcastVariable( "Votes" )
		);
	}

	public void loadVotes (Collection<Tuple3<Integer, String, Integer>> voteDataset) {
        votes = new HashMap<>(voteDataset.size());
		for ( Tuple3<Integer, String, Integer> v : voteDataset ) {
			votes.put( new Tuple2<>( v.f0, v.f1 ), v.f2 );
		}
	}

	public void loadQueries (Collection<Tuple3<Integer, String, String>> broadcastVariable) throws IOException, ParserConfigurationException {
        queries = new HashSet<>(broadcastVariable.size());
		for ( final Tuple3<Integer, String, String> query : broadcastVariable ) {
			final Query q = new Query();
			q.qId = query.getField( 0 );
			q.qfId = query.getField( 1 );
			final String mml = query.getField( 2 );
			final CMMLInfo cmml = new CMMLInfo( mml );
			q.standardQuery = cmml.getXQuery();
			try {
				final CMMLInfo strict = new CMMLInfo( mml ).toStrictCmml();
				q.CDQuery = strict.getXQuery();
                LOG.debug("parsed", q.qId, q.qfId);
			} catch (final TransformerException|IOException|ParserConfigurationException e){
				LOG.error( "cannot parse cmml query for ", q.qId, q.qfId, mml, e);
			}

			//CMMLInfo dataTypes = cmml.clone().toDataCmml();
			//q.dataTypeQuery = dataTypes.getXQuery();
			q.queryTokens = cmml.getElements();
			queries.add( q );
		}
	}

	@Override
	public void flatMap (DatabaseTuple in, Collector<DatabaseResultTuple> out) {
		for (final Query query : queries ) {
			try {
				checkQuery(in, out, query);
			}catch (final IOException|ParserConfigurationException e){
				LOG.error( "CANNOT write record ",e,query);//, fId,vote, isFormula, depth, Coverage, cdMatch, dataMatch );
			}
		}
	}

	public boolean checkQuery(DatabaseTuple in, Collector<DatabaseResultTuple> out, Query query) throws IOException, ParserConfigurationException {
		final Integer fId = in.getNamedField( DatabaseTuple.fields.fId );
		final String page = in.getNamedField( DatabaseTuple.fields.pageId );
		final Tuple2<Integer, String> signature = new Tuple2<>( query.qId, page );
		final Integer vote = votes.get( signature );
		if ( vote == null  ){
			return true;
		}

		final CMMLInfo cmmlInfo = new CMMLInfo( in.getNamedField( DatabaseTuple.fields.value ).toString() );
        Boolean isFormula = null;
        try {
            isFormula = cmmlInfo.isEquation();
        } catch (final TransformerException|SAXException|XPathExpressionException e) {
            LOG.error("Could not determine if isFormula",fId,e);
        }
        final Integer depth = cmmlInfo.getDepth( query.standardQuery );
		final Double coverage = cmmlInfo.getCoverage( query.queryTokens );
		final Boolean cdMatch = null;// cmmlInfo.toStrictCmml().isMatch( query.CDQuery );
		final Boolean dataMatch = false; //cmmlInfo.toDataCmml().isMatch( query.dataTypeQuery );
		out.collect(
			makeRecord( query, fId, vote, isFormula, depth, coverage, cdMatch, dataMatch )
		  );
		return false;
	}

	public static DatabaseResultTuple makeRecord (Query query, Integer fId, Integer vote, Boolean isFormula, Integer depth, Double coverage, Boolean cdMatch, Boolean dataMatch) {
		final DatabaseResultTuple r = new DatabaseResultTuple();
		r.setNamedField( DatabaseResultTuple.fields.queryNum, query.qId );
		r.setNamedField( DatabaseResultTuple.fields.queryFormulaID, query.qfId );
		r.setNamedField( DatabaseResultTuple.fields.fId, fId );
		r.setNamedFieldB( DatabaseResultTuple.fields.cdMatch, cdMatch );
		r.setNamedFieldB( DatabaseResultTuple.fields.dataMatch, dataMatch );
		r.setNamedFieldD( DatabaseResultTuple.fields.queryCoverage, coverage );
		r.setNamedFieldI( DatabaseResultTuple.fields.matchDepth, depth );
		r.setNamedFieldB( DatabaseResultTuple.fields.isFormulae, isFormula );
		r.setNamedField( DatabaseResultTuple.fields.vote, vote );
		return r;
	}
}
