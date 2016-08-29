package demo;

import demo.FeignClientWithServerListApplicationTests.TestApplication;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.boot.test.WebIntegrationTest;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = TestApplication.class)
//Increase hystrix timeout or else requests timeout on CI server
@WebIntegrationTest(randomPort = true, value = {"myexample.ribbon.listOfServers:example.com", "hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds: 60000"})
@DirtiesContext
public class FeignClientWithServerListApplicationTests {

	@Autowired
	private ServiceRestClient client;
	
	@Test
	public void clientConnects() {
		assertTrue(client.hello().contains("<html"));
	}

	@Configuration
	@EnableAutoConfiguration
	@EnableFeignClients
	protected static class TestApplication {

		@Bean
		public FallbackRestClient fallbackRestClient() {
			return new FallbackRestClient();
		}

		@Bean
		public FallbackClient fallback() {
			return new FallbackClient();
		}

		public static void main(String[] args) {
	        SpringApplication.run(FeignClientApplication.class, args);
	    }
	}

	@FeignClient(value = "myexample", fallback = FallbackRestClient.class)
	public static interface ServiceRestClient {
		@RequestMapping(value="/", method=RequestMethod.GET)
		String hello();
	}

	static class FallbackRestClient implements ServiceRestClient {

		@Override
		public String hello() {
			return "fallback";
		}
	}
	
}