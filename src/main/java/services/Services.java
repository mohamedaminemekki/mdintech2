package services;

import entities.mariem.Trip;

import java.sql.SQLException;
import java.util.List;
public interface Services<T> {
    List<T> readList() throws SQLException;
    void add(T t) throws SQLException;
    void update(T t) throws SQLException;
    void addP(T t) throws SQLException;
    void delete(int id) throws SQLException;  // Ajout de la méthode delete

    Trip getById(int id) throws SQLException;
}
