package org.zpid.se4ojs.annotation.umls;
/**
 * 
 */


import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;

import java.util.Calendar;
import java.util.Date;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * @author barth
 *
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest( {UtsConceptIDMappingClient.class })
public class TestUtsConceptIDMappingClient {
	
	private UtsConceptIDMappingClient utsConceptIDMappingClient;
	private OntologyMappingHandler mappingHandler;

	@Before
	public void setUpBeforeClass() throws UtsFault_Exception, gov.nih.nlm.uts.webservice.metadata.UtsFault_Exception {
		UtsServiceTicketHandler ticketHandler = mock(UtsServiceTicketHandler.class);
		Mockito.when(ticketHandler.getAllUMLSVersions()).thenReturn(UtsConceptIDMappingClient.UMLS_VERSION);
		mappingHandler = mock(OntologyMappingHandler.class);
		utsConceptIDMappingClient = PowerMockito.spy(UtsConceptIDMappingClient.getInstance(
				mappingHandler, ticketHandler));
	}
	
	/**
	 * Test method for {@link org.zpid.se4ojs.annotation.umls.UtsConceptIDMappingClient#createProxyGrantTicket()}.
	 * @throws UtsFault_Exception 
	 */
	@Test
	@Ignore //ignore this test by default since the project won't build if the user has not configured any uts-service credentials
	public void testCreateProxyGrantTicket_isRenewedWhenExpiring() throws UtsFault_Exception {
		UtsServiceTicketHandler ticketHandler = PowerMockito.spy(new UtsServiceTicketHandler());
		utsConceptIDMappingClient = PowerMockito.spy(UtsConceptIDMappingClient.getInstance(
				mock(OntologyMappingHandler.class), ticketHandler));
		
		Date date = new Date();
		Calendar cal  = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, UtsServiceTicketHandler.EXPIRY_INTERVALL);
		assert(utsConceptIDMappingClient.getUtsServiceTicketHandler().getDate().compareTo(date) < 0);
		
		Mockito.when(ticketHandler.getDate()).thenReturn(cal.getTime()); 
		utsConceptIDMappingClient.getUtsServiceTicketHandler().getSingleUseTicket();
		verify(ticketHandler, Mockito.times(2)).createProxyGrantTicket();
	}
	
	/**
	 * Test method for {@link org.zpid.se4ojs.annotation.umls.UtsConceptIDMappingClient#createProxyGrantTicket()}.
	 * @throws UtsFault_Exception 
	 */
	@Test
	public void testCreateProxyGrantTicket_isNotRenewedWhenNotExpiring() throws UtsFault_Exception {
		UtsServiceTicketHandler ticketHandler = PowerMockito.spy(new UtsServiceTicketHandler());
		utsConceptIDMappingClient = PowerMockito.spy(UtsConceptIDMappingClient.getInstance(
				mock(OntologyMappingHandler.class), ticketHandler));
		
		Date date = new Date();
		Calendar cal  = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, UtsServiceTicketHandler.EXPIRY_INTERVALL - 1);
		
		assert(utsConceptIDMappingClient.getUtsServiceTicketHandler().getDate().compareTo(date) > 0);
		
		Mockito.when(ticketHandler.getDate()).thenReturn(cal.getTime());  
		utsConceptIDMappingClient.getUtsServiceTicketHandler().getSingleUseTicket();
		verify(ticketHandler).createProxyGrantTicket();
	}
	
	@Test
	public void testExtractConceptName_MultiWord_Stopwords() {
		String conceptNameToAdapt = "abuse of power";
		String expectedConceptName = "Abuse_Power";
		
		OntologyMappingNcboUmls mapping = mock(OntologyMappingNcboUmls.class);
		Mockito.when(mapping.getMultiWordDelim()).thenReturn(UtsConceptIDMappingClient.DEFAULT_MULTI_WORD_DELIM);
		Mockito.when(mapping.getConceptSeparator()).thenReturn(UtsConceptIDMappingClient.DEFAULT_MULTI_WORD_DELIM);
		Mockito.when(mapping.getStopwords()).thenReturn(UtsConceptIDMappingClient.OMIT_STOPWORDS);
		Mockito.when(mappingHandler.getSource("PSY")).thenReturn(mapping);
		
		String extractedConceptName = utsConceptIDMappingClient.extractConceptName("PSY", conceptNameToAdapt);
		assertEquals("Wrong concept name extracted", expectedConceptName, extractedConceptName);
	}
	
	@Test
	public void testExtractConceptName_Capitalize_NoStopwords() throws UtsFault_Exception {
		
		String conceptNameToAdapt = "abuse of power";
		String expectedConceptName = "Abuse?Of?Power";
		
		OntologyMappingNcboUmls mapping = mock(OntologyMappingNcboUmls.class);
		Mockito.when(mapping.getMultiWordDelim()).thenReturn("?");
		Mockito.when(mapping.getConceptSeparator()).thenReturn("");
		Mockito.when(mapping.getStopwords()).thenReturn("");
		Mockito.when(mappingHandler.getSource("PSY")).thenReturn(mapping);

		String extractedConceptName = utsConceptIDMappingClient.extractConceptName("PSY", conceptNameToAdapt);
		assertEquals("Wrong concept name extracted", expectedConceptName, extractedConceptName);
	}
	
	@Test
	public void testExtractConceptName_NoMultiWordDelim() throws UtsFault_Exception {
		String conceptNameToAdapt = "abuse of power";
		String expectedConceptName = "AbuseOfPower";
		
		OntologyMappingNcboUmls mapping = mock(OntologyMappingNcboUmls.class);
		Mockito.when(mapping.getMultiWordDelim()).thenReturn("");
		Mockito.when(mapping.getConceptSeparator()).thenReturn("");
		Mockito.when(mapping.getStopwords()).thenReturn("");
		Mockito.when(mappingHandler.getSource("PSY")).thenReturn(mapping);
		String extractedConceptName = utsConceptIDMappingClient.extractConceptName("PSY", conceptNameToAdapt);
		assertEquals("Wrong concept name extracted", expectedConceptName, extractedConceptName);
	}
	@Test
	public void testExtractConceptName_TestAPAConceptWBraces() throws UtsFault_Exception {
		String conceptNameToAdapt = "lead (Metal)";
		String expectedConceptName = "Lead_(Metal)";
		
		OntologyMappingNcboUmls mapping = mock(OntologyMappingNcboUmls.class);
//		%23, P, _ , stopWords
		Mockito.when(mapping.getMultiWordDelim()).thenReturn("_");
		Mockito.when(mapping.getConceptSeparator()).thenReturn("%23");
		Mockito.when(mapping.getStopwords()).thenReturn("stopWords");
		Mockito.when(mappingHandler.getSource("PSY")).thenReturn(mapping);
		String extractedConceptName = utsConceptIDMappingClient.extractConceptName("PSY", conceptNameToAdapt);
		assertEquals("Wrong concept name extracted", expectedConceptName, extractedConceptName);
	}
	
}
