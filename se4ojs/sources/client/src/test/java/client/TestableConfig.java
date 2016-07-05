package client;

import org.zpid.se4ojs.app.Config;

public class TestableConfig extends Config {


	protected void setPropValue(String property, String propValue) {
		getProperties().setProperty(property, propValue);
	}


	public TestableConfig() {
		super();
		INSTANCE = this;
	}

}

