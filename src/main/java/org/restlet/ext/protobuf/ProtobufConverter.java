package org.restlet.ext.protobuf;

import java.io.IOException;
import java.util.List;

import org.restlet.data.MediaType;
import org.restlet.data.Preference;
import org.restlet.engine.converter.ConverterHelper;
import org.restlet.engine.resource.VariantInfo;
import org.restlet.representation.Representation;
import org.restlet.representation.Variant;
import org.restlet.resource.Resource;

import com.google.protobuf.Message;

public class ProtobufConverter extends ConverterHelper {

	private static final MediaType APPLICATION_PROTOBUF = MediaType.register("application/x-protobuf",
			"Google Protocol Buffer");

	private static final VariantInfo VARIANT_APPLICATION_PROTOBUF = new VariantInfo(APPLICATION_PROTOBUF);

	/** Variant with media type application/json. */
	private static final VariantInfo VARIANT_JSON = new VariantInfo(MediaType.APPLICATION_JSON);

	private static final VariantInfo VARIANT_XML = new VariantInfo(MediaType.APPLICATION_XML);

	protected <T extends Message> ProtobufRepresentation<T> createProtobuf(MediaType mediaType, T source) {
		return new ProtobufRepresentation<T>(source);
	}

	protected <T extends Message> ProtobufRepresentation<T> createProtobufJson(MediaType mediaType, T source) {
		return new ProtobufRepresentation<T>(source, mediaType);
	}

	@Override
	public List<Class<?>> getObjectClasses(Variant source) {
		List<Class<?>> result = null;

		if (VARIANT_JSON.isCompatible(source) || VARIANT_APPLICATION_PROTOBUF.isCompatible(source)
				|| VARIANT_XML.isCompatible(source)) {
			result = addObjectClass(result, Object.class);
			result = addObjectClass(result, ProtobufRepresentation.class);
		}

		return result;
	}

	@Override
	public List<VariantInfo> getVariants(Class<?> source) {
		List<VariantInfo> result = null;

		if (source != null) {
			result = addVariant(result, VARIANT_JSON);
			result = addVariant(result, VARIANT_APPLICATION_PROTOBUF);
			result = addVariant(result, VARIANT_XML);
		}

		return result;
	}

	@Override
	public float score(Object source, Variant target, Resource resource) {
		float result = -1.0F;

		if (source instanceof ProtobufRepresentation<?>) {
			result = 1.0F;
		} else {
			if (target == null) {
				result = 0.5F;
			} else if (VARIANT_JSON.isCompatible(target)) {
				result = 0.8F;
			} else if (VARIANT_APPLICATION_PROTOBUF.isCompatible(target)) {
				result = 0.8F;
			} else if (VARIANT_XML.isCompatible(target)) {
				result = 0.8F;
			} else {
				result = 0.5F;
			}
		}

		return result;
	}

	@Override
	public <T> float score(Representation source, Class<T> target, Resource resource) {
		float result = -1.0F;

		if (source instanceof ProtobufRepresentation<?>) {
			result = 1.0F;
		} else if ((target != null) && ProtobufRepresentation.class.isAssignableFrom(target)) {
			result = 1.0F;
		} else if (VARIANT_JSON.isCompatible(source)) {
			result = 0.8F;
		} else if (VARIANT_APPLICATION_PROTOBUF.isCompatible(source)) {
			result = 0.8F;
		} else if (VARIANT_XML.isCompatible(source)) {
			result = 0.8F;
		}

		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T toObject(Representation source, Class<T> target, Resource resource) throws IOException {
		Object result = null;

		ProtobufRepresentation<?> protobufSource = null;
		if (source instanceof ProtobufRepresentation<?>) {
			protobufSource = (ProtobufRepresentation<?>) source;
		} else if (VARIANT_JSON.isCompatible(source)) {
			// TODO
		}

		if (protobufSource != null) {
			if ((target != null) && ProtobufRepresentation.class.isAssignableFrom(target)) {
				result = protobufSource;
			} else {
				result = protobufSource.getMessage();
			}
		}

		return (T) result;
	}

	@Override
	public Representation toRepresentation(Object source, Variant target, Resource resource) {
		Representation result = null;

		if (source instanceof ProtobufRepresentation) {
			result = (ProtobufRepresentation<?>) source;
		} else {
			if (target.getMediaType() == null) {
				target.setMediaType(MediaType.APPLICATION_JSON);
			}

			if ((VARIANT_JSON.isCompatible(target) || VARIANT_XML.isCompatible(target))
					&& (Message.class.isAssignableFrom(source.getClass()))) {
				Message messageSource = (Message) source;
				result = createProtobufJson(target.getMediaType(), messageSource);
			} else if ((VARIANT_JSON.isCompatible(target) || VARIANT_XML.isCompatible(target))
					&& Iterable.class.isAssignableFrom(source.getClass())) {
				@SuppressWarnings("unchecked")
				Iterable<Message> messageSource = (Iterable<Message>) source;
				result = new ProtobufIterableRepresentation(messageSource, target.getMediaType());
			} else if (VARIANT_APPLICATION_PROTOBUF.isCompatible(target)
					&& (Message.class.isAssignableFrom(source.getClass()))) {
				Message messageSource = (Message) source;
				result = createProtobuf(target.getMediaType(), messageSource);
			} else if (VARIANT_APPLICATION_PROTOBUF.isCompatible(target)
					&& Iterable.class.isAssignableFrom(source.getClass())) {
				@SuppressWarnings("unchecked")
				Iterable<Message> messageSource = (Iterable<Message>) source;
				result = new ProtobufIterableRepresentation(messageSource);
			}
		}

		return result;
	}

	@Override
	public <T> void updatePreferences(List<Preference<MediaType>> preferences, Class<T> entity) {
		updatePreferences(preferences, MediaType.APPLICATION_JSON, 1.0F);
		updatePreferences(preferences, APPLICATION_PROTOBUF, 1.0F);
		updatePreferences(preferences, MediaType.APPLICATION_XML, 1.0F);
	}

}
