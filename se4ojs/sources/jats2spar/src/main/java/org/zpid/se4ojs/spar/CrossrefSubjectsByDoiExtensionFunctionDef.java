package org.zpid.se4ojs.spar;

import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.value.SequenceExtent;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;

import org.zpid.se4ojs.links.CrossrefApiCaller;


public class CrossrefSubjectsByDoiExtensionFunctionDef extends
		ExtensionFunctionDefinition {

	private CrossrefApiCaller crossref = new CrossrefApiCaller();

	@Override
	public StructuredQName getFunctionQName() {
		return new StructuredQName("crossref", "http://www.zpid.de/crossref",
				"subjectsByDoi");
	}

	@Override
	public SequenceType[] getArgumentTypes() {
		return new SequenceType[] { SequenceType.STRING_SEQUENCE };
	}

	@Override
	public SequenceType getResultType(SequenceType[] suppliedArgumentTypes) {
		return SequenceType.STRING_SEQUENCE;
	}

	@Override
	public ExtensionFunctionCall makeCallExpression() {
		return new ExtensionFunctionCall() {

			@Override
			public Sequence call(XPathContext context, Sequence[] arguments)
					throws XPathException {
				// TODO build in configuration option and only call if
				// crossrefExternalLink-Option is set
				String doi = ((StringValue) arguments[0]).asString();
				return new SequenceExtent(crossref.getSubjectsByDoi(doi));

			}
		};
	}

}
