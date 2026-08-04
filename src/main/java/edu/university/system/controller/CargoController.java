package edu.university.system.controller;

import edu.university.system.dao.CargoDao;
import edu.university.system.model.Cargo;

import java.util.List;
import java.util.Optional;

public class CargoController extends ControllerSupport {

    private final CargoDao cargoDao;

    public CargoController(CargoDao cargoDao) {
        this.cargoDao = cargoDao;
    }

    public Long insertar(Cargo cargo) {
        AuthorizationService.require(Permission.CREATE);
        validarCargo(cargo, false);
        return cargoDao.insertar(cargo);
    }

    public boolean actualizar(Cargo cargo) {
        AuthorizationService.require(Permission.UPDATE);
        validarCargo(cargo, true);
        return cargoDao.actualizar(cargo);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.DELETE);
        requireId(id, "Cargo");
        return cargoDao.eliminar(id);
    }

    public Optional<Cargo> buscarPorId(Long id) {
        requireId(id, "Cargo");
        return cargoDao.buscarPorId(id);
    }

    public List<Cargo> listar() {
        return cargoDao.listar();
    }

    public List<Cargo> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return cargoDao.buscar(criterio);
    }

    private void validarCargo(Cargo cargo, boolean requireEntityId) {
        requireObject(cargo, "Cargo");
        if (requireEntityId) {
            requireId(cargo.getId(), "Cargo");
        }
        requireText(cargo.getNombre(), "Nombre del cargo", 2);
        validateNonNegative(cargo.getSalarioBase(), "Salario base");
    }
}
