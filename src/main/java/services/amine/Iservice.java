package services.amine;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.List;

public interface Iservice<T> {
    boolean save(T obj) throws JsonProcessingException;
    void update(T obj);
    void delete(T obj);
    T findById(int id);
    List<T> findAll();
}
