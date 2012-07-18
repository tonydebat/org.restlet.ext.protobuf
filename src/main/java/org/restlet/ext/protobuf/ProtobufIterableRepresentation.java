package org.restlet.ext.protobuf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import com.google.protobuf.Message;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.XmlFormat;

public class ProtobufIterableRepresentation extends OutputRepresentation {

	private static final MediaType mediaType = MediaType.register(
			"application/x-protobuf", "Google Protocol Buffer");

	private Iterable<Message> messages;

	public ProtobufIterableRepresentation(
			Representation serializedRepresentation, Iterable<Message> prototype)
			throws IOException {
		super(mediaType);

		if (!serializedRepresentation.getMediaType().equals(mediaType)) {
			throw new IllegalArgumentException(
					"The serialized representation must have this media type: "
							+ mediaType.toString());
		}

		InputStream is = serializedRepresentation.getStream();
		is.close();
	}


	public ProtobufIterableRepresentation(Iterable<Message> message) {
		super(mediaType);
		this.messages = message;
	}

	public ProtobufIterableRepresentation(Iterable<Message> message,
			MediaType mt) {
		super(mt);
		this.messages = message;
	}


	public Iterable<Message> getMessage() {
		return messages;
	}

	public long getSize() {

		return super.getSize();
	}

	public String getText() {
		return messages.toString();
	}


	@Override
	public void release() {
		messages = null;
		super.release();
	}


	public void setMessage(Iterable<Message> message) {
		if (null == message) {
			throw new IllegalArgumentException(
					"Message argument may not be null.");
		}

		this.messages = message;
	}

	@Override
	public void write(OutputStream os) throws IOException {
		if (getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
			OutputStreamWriter output = new OutputStreamWriter(os, "UTF-8");
			printJson(messages, output);
			output.flush();
		} else if (getMediaType().isCompatible(MediaType.APPLICATION_XML)) {
			OutputStreamWriter output = new OutputStreamWriter(os, "UTF-8");
			printXml(messages, output);
			output.flush();
		} else {
			for (Message m : messages) {
				m.writeDelimitedTo(os);
			}
		}
	}
	
	
	private void printJson(Iterable<Message> messages, Appendable appendable) throws IOException {
		appendable.append('[');
		boolean firstIteration = true;
		for (Message m : messages) {
			if(!firstIteration) {
				appendable.append(',');
			}
			firstIteration = false;
			JsonFormat.print(m,  appendable);
		}
		appendable.append(']');
	}

	private void printXml(Iterable<Message> messages, Appendable appendable) throws IOException {
		appendable.append("<List>");
		for (Message m : messages) {
			XmlFormat.print(m,  appendable);
		}
		appendable.append("</List>");
	}

}
