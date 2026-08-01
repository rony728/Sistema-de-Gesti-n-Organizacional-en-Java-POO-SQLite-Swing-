package edu.university.system.controller;

import edu.university.system.dao.EmpresaDao;
import edu.university.system.model.Empresa;

import java.util.List;
import java.util.Optional;

public class EmpresaController extends ControllerSupport {

    private final EmpresaDao empresaDao;

    public EmpresaController(EmpresaDao empresaDao) {
        this.empresaDao = empresaDao;
    }

    public Long insertar(Empresa empresa) {
        AuthorizationService.require(Permission.MANAGE_COMPANIES);
        validarEmpresa(empresa, false);
        return empresaDao.insertar(empresa);
    }

    public boolean actualizar(Empresa empresa) {
        AuthorizationService.require(Permission.MANAGE_COMPANIES);
        validarEmpresa(empresa, true);
        return empresaDao.actualizar(empresa);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.MANAGE_COMPANIES);
        requireId(id, "Empresa");
        return empresaDao.eliminar(id);
    }

    public Optional<Empresa> buscarPorId(Long id) {
        requireId(id, "Empresa");
        return empresaDao.buscarPorId(id);
    }

    public List<Empresa> listar() {
        return empresaDao.listar();
    }

    public List<Empresa> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return empresaDao.buscar(criterio);
    }

    private void validarEmpresa(Empresa empresa, boolean requireEntityId) {
        requireObject(empresa, "Empresa");
        if (requireEntityId) {
            requireId(empresa.getId(), "Empresa");
        }
        requireText(empresa.getNombre(), "Nombre de la empresa", 2);
        requireText(empresa.getRtn(), "RTN de la empresa", 8);
        requireText(empresa.getDireccion(), "Direccion de la empresa", 5);
        validateOptionalPhone(empresa.getTelefono(), "Telefono de la empresa");
        validateOptionalEmail(empresa.getCorreoElectronico(), "Correo electronico de la empresa");
        requireObject(empresa.getPais(), "Pais de la empresa");
        requireId(empresa.getPais().getId(), "Pais de la empresa");
    }
}
