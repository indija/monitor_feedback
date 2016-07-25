package ch.uzh.ifi.feedback.library.rest;

public interface ISerializationService<T> {
	
	String Serialize(T object);
	T Deserialize(String data);
	
}