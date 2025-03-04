package services.amine;

import java.util.List;

public interface Iservice<T> {
    boolean save(T obj);
    void update(T obj);
    void delete(T obj);
    T findById(int id);
    List<T> findAll();
}
