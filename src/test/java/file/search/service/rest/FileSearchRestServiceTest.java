package file.search.service.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import file.search.service.rest.exception.FileSearcherException;
import file.search.service.rest.exception.InvalidFileSearchQueryException;
import file.search.service.search.FileSearcher;
import file.search.service.search.Result;

/**
 * Tests for File Search Rest API.
 * 
 */

@RunWith(SpringRunner.class)
@SpringBootTest
public class FileSearchRestServiceTest {

	@Autowired
	private WebApplicationContext context;

	private MockMvc mvc;
	
	@Autowired
	private FileSearcher indexSearcher;

	@Before
	public void setUp() {
		this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();		
	}

	@Test
	public void testApllicationStartup() throws Exception {
		this.mvc.perform(get("/")).andExpect(status().isOk());
	}
	
	@Test
	public void testSearch() throws Exception {
		this.mvc.perform(get("/search")).andExpect(status().is4xxClientError());
	}
	
	@Test
	public void searchByOneWord() throws Exception {
		this.mvc.perform(get("/search?query=contents:Test23")).andExpect(status().isOk());				
	}
	
	@Test
	public void searchByMultipleWord() throws Exception {
		this.mvc.perform(get("/search?query=contents:Test23 contents:Test1")).andExpect(status().isOk());			
	}
	
	@Test
	public void searchSimpleQuery() throws FileSearcherException, InvalidFileSearchQueryException{
		Result result = indexSearcher.searchIndex("contents:Test23", 10, true);
		Assert.assertTrue(result.getAvailable()>=0);		
	}
		
	@Test(expected = InvalidFileSearchQueryException.class) 
	public void searchForInvalidSearch() throws FileSearcherException, InvalidFileSearchQueryException{		
			Result result = indexSearcher.searchIndex("", 10, true);
	}
}