package edu.university.system.dao;

import java.util.List;
import java.util.Optional;

public interface CrudDao<T, ID> {

    ID insertar(T entidad);

    boolean actualizar(T entidad);

    boolean eliminar(ID id);

    Optional<T> buscarPorId(ID id);

    List<T> listar();
}
