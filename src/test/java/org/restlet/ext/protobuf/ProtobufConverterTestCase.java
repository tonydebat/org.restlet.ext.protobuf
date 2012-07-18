package org.restlet.ext.protobuf;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.service.ConverterService;
import org.restlet.test.RestletTestCase;

public class ProtobufConverterTestCase extends RestletTestCase {

	@Test
    public void testObjectionToRepresentation() {
        ConverterService cs = new ConverterService();
        Representation rep = cs.toRepresentation(org.restlet.ext.protobuf.Sample.PersonDto.newBuilder().build(), new Variant(
                MediaType.APPLICATION_XML), null);
        assertTrue(rep instanceof ProtobufRepresentation<?>);
    }

	@Test
    public void testRepresentationToObject() throws IOException, JAXBException {
        ConverterService cs = new ConverterService();
        ProtobufRepresentation<org.restlet.ext.protobuf.Sample.PersonDto> protobufRep = new ProtobufRepresentation<org.restlet.ext.protobuf.Sample.PersonDto>(
        		org.restlet.ext.protobuf.Sample.PersonDto.newBuilder().build(), MediaType.APPLICATION_XML);
        Object rep = cs.toObject(protobufRep, org.restlet.ext.protobuf.Sample.PersonDto.class, null);
        assertTrue(rep instanceof org.restlet.ext.protobuf.Sample.PersonDto);
    }
}
