package org.restlet.ext.protobuf;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;
import org.restlet.representation.Representation;

import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import com.googlecode.protobuf.format.JsonFormat;
import com.googlecode.protobuf.format.XmlFormat;

//public class ProtobufRepresentation<T extends Message> extends
public class ProtobufRepresentation<T> extends
		OutputRepresentation {

	private static final MediaType mediaType = MediaType.register(
			"application/x-protobuf", "Google Protocol Buffer");

	private T message;


	@SuppressWarnings("unchecked")
	public ProtobufRepresentation(Representation serializedRepresentation,
			T prototype, MediaType mt) throws IOException {
		super(mt);

		Builder builder = ((Message)prototype).newBuilderForType();
		if (mt.isCompatible(MediaType.APPLICATION_JSON)) {
			JsonFormat.merge(serializedRepresentation.getReader(), builder);
		} else if (mt.isCompatible(MediaType.APPLICATION_XML)) {
			XmlFormat.merge(serializedRepresentation.getReader(), builder);
		} else if (mt.isCompatible(mediaType)) {
			builder.mergeFrom(serializedRepresentation.getStream());
		}
		
		setMessage((T) builder.build());
	}
	
	public ProtobufRepresentation(T message) {
		super(mediaType);
		this.message = message;
	}

	public ProtobufRepresentation(T message, MediaType mt) {
		super(mt);
		this.message = message;
	}

	public T getMessage() {
		return message;
	}

	public long getSize() {
		if (mediaType.equals(getMediaType())) {
			//Message cast
			return ((Message)message).getSerializedSize();
		}
		return super.getSize();
	}

	public String getText() {
		return message.toString();
	}

	@Override
	public void release() {
		message = null;
		super.release();
	}

	public void setMessage(T message) {
		if (null == message) {
			throw new IllegalArgumentException(
					"Message argument may not be null.");
		}

		this.message = message;
	}

	@Override
	public void write(OutputStream os) throws IOException {
		if (getMediaType().isCompatible(MediaType.APPLICATION_JSON)) {
			OutputStreamWriter output = new OutputStreamWriter(os, "UTF-8");
			JsonFormat.print((Message)message, output);
			output.flush();
		} else if (getMediaType().isCompatible(MediaType.APPLICATION_XML)) {
			OutputStreamWriter output = new OutputStreamWriter(os, "UTF-8");
			XmlFormat.print((Message)message, output);
			output.flush();
		} else {
			((Message)message).writeTo(os);
		}
	}

}
