package org.eclipse.basyx.aas.registry.service.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import lombok.experimental.UtilityClass;

@UtilityClass
public class DescriptorCopies {

	@SuppressWarnings("unchecked")
	public static <T extends Serializable> T deepClone(T toClone) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(toClone);
			ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
			ObjectInputStream ois = new ObjectInputStream(bais);
			return (T) ois.readObject();
		} catch (IOException | ClassNotFoundException e) {
			throw new DeepCopyException("This exception should never occur!", e);
		}
	}

	public static <T extends Serializable> List<T> deepCloneCollection(Collection<T> values) {
		List<T> clonedValues = new ArrayList<>(values.size());
		for (T eachItem : values) {
			T clone = deepClone(eachItem);
			clonedValues.add(clone);
		}
		return clonedValues;
	}

	private static class DeepCopyException extends RuntimeException {

		private static final long serialVersionUID = 1L;

		public DeepCopyException(String msg, Exception e) {
			super(msg, e);
		}

	}
}
