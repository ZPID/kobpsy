package org.zpid.se4ojs.annotation.umls;
/**
 * 
 */


import static org.mockito.Mockito.verify;
import gov.nih.nlm.uts.webservice.security.UtsFault_Exception;

import java.util.Calendar;
import java.util.Date;

import static org.mockito.Mockito.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {

	}

	/**
	 * Test method for {@link org.zpid.se4ojs.annotation.umls.UtsConceptIDMappingClient#createProxyGrantTicket()}.
	 * @throws UtsFault_Exception 
	 */
	@Test
	public void testCreateProxyGrantTicket_isRenewedWhenExpiring() throws UtsFault_Exception {
		utsConceptIDMappingClient = PowerMockito.spy(UtsConceptIDMappingClient.getInstance());
		
		Date date = new Date();
		Calendar cal  = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, UtsConceptIDMappingClient.EXPIRY_INTERVALL);
		assert(utsConceptIDMappingClient.getDate().compareTo(date) < 0);
		
		PowerMockito.when(utsConceptIDMappingClient.getDate()).thenReturn(cal.getTime()); 
		utsConceptIDMappingClient.getSingleUseTicket();
		verify(utsConceptIDMappingClient).createProxyGrantTicket();
	}
	
	/**
	 * Test method for {@link org.zpid.se4ojs.annotation.umls.UtsConceptIDMappingClient#createProxyGrantTicket()}.
	 * @throws UtsFault_Exception 
	 */
	@Test
	public void testCreateProxyGrantTicket_isNotRenewedWhenNotExpiring() throws UtsFault_Exception {
		
		Date date = new Date();
		Calendar cal  = Calendar.getInstance();
		cal.add(Calendar.HOUR_OF_DAY, UtsConceptIDMappingClient.EXPIRY_INTERVALL);
		
		utsConceptIDMappingClient = PowerMockito.spy(UtsConceptIDMappingClient.getInstance());
		assert(utsConceptIDMappingClient.getDate().compareTo(date) > 0);
		
		PowerMockito.when(utsConceptIDMappingClient.getDate()).thenReturn(cal.getTime()); 
		utsConceptIDMappingClient.getSingleUseTicket();
		verify(utsConceptIDMappingClient, never()).createProxyGrantTicket();
	}

}

