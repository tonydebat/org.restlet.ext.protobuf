package org.restlet.ext.protobuf;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.restlet.Application;
import org.restlet.Client;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Preference;
import org.restlet.data.Protocol;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.test.RestletTestCase;


public class ProtobufIntegrationConverterTestCase extends RestletTestCase {

	private Component component;

	private String uri;

	public void setUp() throws Exception {
		super.setUp();
		this.component = new Component();
		final Server server = this.component.getServers().add(Protocol.HTTP, 0);
		final Application application = createApplication(this.component);
		this.component.getDefaultHost().attach(application);
		this.component.start();
		uri = "http://localhost:" + server.getEphemeralPort() + "/test";
	}

	public void tearDown() throws Exception {
		if (component != null) {
			component.stop();
		}

		this.component = null;
		super.tearDown();
	}

	protected Application createApplication(Component component) {
		final Application application = new Application() {
			@Override
			public Restlet createInboundRoot() {
				final Router router = new Router(getContext());
				router.attach("/test", SampleResource.class);
				return router;
			}
		};

		return application;
	}

	@Test
	public void test_get_xml() throws Exception {
		Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
		Request request = new Request(Method.GET, uri);
		List<Preference<MediaType>> m = new ArrayList<Preference<MediaType>>();
		m.add(new Preference<MediaType>(MediaType.APPLICATION_XML));
		request.getClientInfo().setAcceptedMediaTypes(m);

		Response response = client.handle(request);
		response.getEntity();
		assertThat(response.getEntityAsText(),
				is("<PersonDto><name>Dylan</name><age>10</age></PersonDto>"));
	}

	@Test
	public void test_get_json() throws Exception {
		Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
		Request request = new Request(Method.GET, uri);
		request.setEntity("", MediaType.APPLICATION_JSON);
		List<Preference<MediaType>> m = new ArrayList<Preference<MediaType>>();
		m.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
		request.getClientInfo().setAcceptedMediaTypes(m);

		Response response = client.handle(request);
		response.getEntity();
		assertThat(response.getEntityAsText(),
				is("{\"name\": \"Dylan\",\"age\": 10}"));
	}

	@Test
	public void test_get_protobuf() throws Exception {
		Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
		Request request = new Request(Method.GET, uri);
		request.setEntity("", MediaType.APPLICATION_JSON);
		List<Preference<MediaType>> m = new ArrayList<Preference<MediaType>>();
		m.add(new Preference<MediaType>(new MediaType("application/x-protobuf")));
		request.getClientInfo().setAcceptedMediaTypes(m);

		Response response = client.handle(request);
		response.getEntity();
		assertThat(response.getEntity().getMediaType(), is(new MediaType("application/x-protobuf")));
	}

	@Test
	public void test_post_json_return_xml() throws Exception {
		Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
		Request request = new Request(Method.POST, uri);
		request.setEntity("{\"name\": \"Bob\",\"age\": 60}",
				MediaType.APPLICATION_JSON);
		List<Preference<MediaType>> m = new ArrayList<Preference<MediaType>>();
		m.add(new Preference<MediaType>(MediaType.APPLICATION_XML));
		request.getClientInfo().setAcceptedMediaTypes(m);

		Response response = client.handle(request);
		response.getEntity();
		assertThat(response.getEntityAsText(),
				is("<PersonDto><name>Bob</name><age>60</age></PersonDto>"));
	}

	@Test
	public void test_post_xml_return_json() throws Exception {
		Client client = new Client(new Context(), Arrays.asList(Protocol.HTTP));
		Request request = new Request(Method.POST, uri);
		request.setEntity(
				"<PersonDto><name>Bob</name><age>60</age></PersonDto>",
				MediaType.APPLICATION_XML);
		List<Preference<MediaType>> m = new ArrayList<Preference<MediaType>>();
		m.add(new Preference<MediaType>(MediaType.APPLICATION_JSON));
		request.getClientInfo().setAcceptedMediaTypes(m);

		Response response = client.handle(request);
		response.getEntity();
		assertThat(response.getEntityAsText(),
				is("{\"name\": \"Bob\",\"age\": 60}"));
	}

	public static class SampleResource extends ServerResource {

		@Post("json,xml")
		public Sample.PersonDto postJones(Representation personRepresentation) {
			assertNotNull(personRepresentation);
			Sample.PersonDto personDto = Sample.PersonDto.newBuilder().build();
			ProtobufRepresentation<Sample.PersonDto> protobufRepresentation = null;
			try {
				protobufRepresentation = new ProtobufRepresentation<Sample.PersonDto>(
						personRepresentation, personDto,
						personRepresentation.getMediaType());
			} catch (IOException e) {
				throw new ResourceException(e);
			}
			return protobufRepresentation.getMessage();
		}

		@Get("xml,json,protobuf")
		public Sample.PersonDto getSample() {
			return getPersonDto();
		}

		@Put("xml:xml")
		public ProtobufRepresentation<Sample.PersonDto> putSample(
				Sample.PersonDto sample) {
			assertNotNull(sample);
			return new ProtobufRepresentation<Sample.PersonDto>(getPersonDto());
		}

	}

	private static Sample.PersonDto getPersonDto() {
		return Sample.PersonDto.newBuilder().setName("Dylan").setAge(10)
				.build();
	}
}
