package services.tasnim;

import entities.tasnim.Product;

import java.sql.SQLException;
import java.util.List;

public interface IService<T> {
    void add(T t) throws SQLException;
    void update(T t) throws SQLException;

    void update(Product product) throws SQLException;

    void delete(int id) throws SQLException;
    List<T> readList() throws SQLException;
}
