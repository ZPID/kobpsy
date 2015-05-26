package org.zpid.se4ojs.annotation;

import java.io.OutputStream;

import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFWriter;
import com.hp.hpl.jena.rdf.model.impl.ModelCom;
import com.hp.hpl.jena.shared.JenaException;

public class ModelComSubstitute extends ModelCom{

	public ModelComSubstitute(Graph base) {
		super(base);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Model write(OutputStream writer) {
        getWriter() .write(this, writer, "");
        return this; 
	}

	@Override
	public RDFWriter getWriter() {
		  try {
	            return (RDFWriter) Class.forName("org.zpid.se4ojs.annotation.BasicSubstitute").newInstance();
	        } catch (Exception e) {
	            if ( e instanceof JenaException )
	                throw (JenaException)e ;
	            throw new JenaException(e);
	        }
	}
	
	

}
