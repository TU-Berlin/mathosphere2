package com.formulasearchengine.mathosphere.basex.rest;

import com.formulasearchengine.mathosphere.basex.domain.MathRequest;
import com.formulasearchengine.mathosphere.basex.domain.MathUpdate;
import restx.annotations.GET;
import restx.annotations.POST;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;

@Component
@RestxResource
public class BaseXResource {
	@GET("/texquery")
	@PermitAll
	public MathRequest texquery( String query ) {
		return new MathRequest( query ).setType( "tex" ).run();
	}
	@POST("/texquery")
	@PermitAll
	public MathRequest texquery( MathRequest q ) {
		if( q.getType() == null ||  "".equals( q.getType()) ){
			q.setType( "tex" );
		}
		return q.run();
	}
	@GET("/xquery")
	@PermitAll
	public MathRequest xquery( String query ) {
		return new MathRequest( query ).setType( "xquery" ).run();
	}
	@POST("/xquery")
	@PermitAll
	public MathRequest xquery( MathRequest q ) {
		if( q.getType() == null ||  "".equals( q.getType()) ){
			q.setType( "xquery" );
		}
		return q.run();
	}
	@GET("/mwsquery")
	@PermitAll
	public MathRequest mwsquery( String q ) {
		return new MathRequest( q ).run();
	}
	@POST("/mwsquery")
	@PermitAll
	public MathRequest mwsquery( MathRequest q ) {
		return q.run();
	}
	@POST("/")
	@PermitAll
	public MathRequest query( MathRequest q ) {
		return q.run();
	}
	@POST("/update")
	@PermitAll
	public MathUpdate update( MathUpdate u ) {
		return u.run();
	}
}
