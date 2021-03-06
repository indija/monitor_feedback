package ch.uzh.ifi.feedback.orchestrator.test;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.time.Instant;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.ClientProtocolException;
import ch.uzh.ifi.feedback.orchestrator.model.FeedbackParameter;
import static java.util.Arrays.asList;

public class OrchestratorServletParameterTest extends OrchestratorServletTest {
	
	private static int NUMBER_OF_PARAMETERS = 648;
	
	public void testRetrievingAllParameters() throws ClientProtocolException, IOException {
		FeedbackParameter[] retrievedParameters = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/parameters", 
				FeedbackParameter[].class);
		assertEquals(retrievedParameters.length, NUMBER_OF_PARAMETERS);
	}
	
	public void testRetrievingSingleParameter() throws ClientProtocolException, IOException {
		FeedbackParameter parameter = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/parameters/6640", 
				FeedbackParameter.class);
		
		assertEquals(parameter.getId(), new Integer(6640));
		assertEquals(parameter.getKey(), "maxLength");
		assertEquals(parameter.getValue(), 200.0);
	}
	
	public void testRetrievingAllParametersForGeneralConfiguration() throws ClientProtocolException, IOException  {
		FeedbackParameter[] retrievedParameters = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/general_configurations/148/parameters", 
				FeedbackParameter[].class);
		assertEquals(retrievedParameters.length, 2);
	}
	
	public void testRetrievingAllParametersForMechanism() throws ClientProtocolException, IOException  {    
		FeedbackParameter[] retrievedParameters = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/mechanisms/829/parameters", 
				FeedbackParameter[].class);
        
		assertEquals(retrievedParameters.length, 2);
	}
	
	public void testInsertParameterForGeneralConfiguration() throws ClientProtocolException, IOException  {
		
		InputStream stream = this.getClass().getResourceAsStream("parameter_insert.json");
		String jsonString = IOUtils.toString(stream); 
		
		FeedbackParameter createdParameter = PostSuccess(
				"http://localhost:8080/orchestrator/feedback/en/applications/35/general_configurations/148/parameters", 
				jsonString,
				FeedbackParameter.class);
        
		assertEquals(createdParameter.getKey(), "test");
		assertEquals(createdParameter.getValue(), "test");
	}
	
	public void testInsertParameterForMechanism() throws ClientProtocolException, IOException  {
		
		InputStream stream = this.getClass().getResourceAsStream("parameter_insert.json");
		String jsonString = IOUtils.toString(stream); 
		
		FeedbackParameter createdParameter = PostSuccess(
				"http://localhost:8080/orchestrator/feedback/en/applications/35/mechanisms/829/parameters", 
				jsonString,
				FeedbackParameter.class);
        
		assertEquals(createdParameter.getKey(), "test");
		assertEquals(createdParameter.getValue(), "test");
	}
	
	public void testUpdateParameter() throws ClientProtocolException, IOException  {
		
		InputStream stream = this.getClass().getResourceAsStream("parameter_update.json");
		String jsonString = IOUtils.toString(stream); 
		
		FeedbackParameter updatedParameter = PutSuccess(
				"http://localhost:8080/orchestrator/feedback/en/applications/35/parameters", 
				jsonString,
				FeedbackParameter.class);
        
		assertEquals(updatedParameter.getId(), new Integer(6640));
		assertEquals(updatedParameter.getKey(), "maxLength");
		assertEquals(updatedParameter.getValue(), 100.0);
	}
	
	public void testRetrievalWithFallbackLanguage() throws ClientProtocolException, IOException
	{
		InputStream stream = this.getClass().getResourceAsStream("parameter_insert_de.json");
		String jsonString = IOUtils.toString(stream); 
		
		FeedbackParameter createdParameter = PostSuccess(
				"http://localhost:8080/orchestrator/feedback/de/applications/35/mechanisms/829/parameters", 
				jsonString,
				FeedbackParameter.class);
		
		FeedbackParameter[] retrievedParameters = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/de/mechanisms/829/parameters", FeedbackParameter[].class);
		
		assertEquals(retrievedParameters.length, 3);
		assertTrue(asList(retrievedParameters).stream().filter(p -> p.getLanguage().equals("de")).count() == 1);
		assertTrue(asList(retrievedParameters).stream().filter(p -> p.getLanguage().equals("en")).count() == 2);
	}
	
	public void testHistorization() throws ClientProtocolException, IOException, InterruptedException
	{
		InputStream stream = this.getClass().getResourceAsStream("parameter_update.json");
		String jsonString = IOUtils.toString(stream); 
		
		PutSuccess(
				"http://localhost:8080/orchestrator/feedback/en/applications/35/parameters", 
				jsonString,
				FeedbackParameter.class);
		Timestamp ts1 = Timestamp.from(Instant.now());
		
		stream = this.getClass().getResourceAsStream("parameter_update_2.json");
		jsonString = IOUtils.toString(stream);
		FeedbackParameter updatedParameter2 = PutSuccess(
				"http://localhost:8080/orchestrator/feedback/en/applications/35/parameters", 
				jsonString,
				FeedbackParameter.class);
		Timestamp ts2 = Timestamp.from(Instant.now());
        
		FeedbackParameter parameter = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/parameters/6640", 
				FeedbackParameter.class);
		
		assertEquals(parameter.getValue(), 150.0);
		
		parameter = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/parameters/6640?timestamp="+ts1.toString().replace(" ", "%20"), 
				FeedbackParameter.class);
		
		assertEquals(parameter.getValue(), 100.0);
		
		parameter = GetSuccess(
				"http://localhost:8080/orchestrator/feedback/en/parameters/6640?timestamp="+ts2.toString().replace(" ", "%20"), 
				FeedbackParameter.class);
		
		assertEquals(parameter.getValue(), 150.0);
	}
}
