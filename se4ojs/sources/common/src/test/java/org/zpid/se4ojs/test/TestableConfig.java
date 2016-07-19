package org.zpid.se4ojs.test;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;

import org.zpid.se4ojs.app.Config;

public class TestableConfig extends Config {

	public static boolean isDummyPropertyFile = false;

	public void setPropValue(String property, String propValue) {
		getProperties().setProperty(property, propValue);
	}


	public TestableConfig() {
		super();
		INSTANCE = this;
	}


	@Override
	protected Path getPropfilePath() throws IOException, URISyntaxException {
		if (isDummyPropertyFile) {
			return File.createTempFile("dummy", "properties").toPath();
		}
		return super.getPropfilePath();
	}

}

