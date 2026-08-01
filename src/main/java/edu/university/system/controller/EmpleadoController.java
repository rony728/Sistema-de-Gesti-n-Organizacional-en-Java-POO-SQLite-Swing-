package edu.university.system.controller;

import edu.university.system.dao.EmpleadoDao;
import edu.university.system.model.Empleado;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class EmpleadoController extends ControllerSupport {

    private final EmpleadoDao empleadoDao;
    private final PersonaController personaController;

    public EmpleadoController(EmpleadoDao empleadoDao) {
        this.empleadoDao = empleadoDao;
        this.personaController = null;
    }

    public EmpleadoController(EmpleadoDao empleadoDao, PersonaController personaController) {
        this.empleadoDao = empleadoDao;
        this.personaController = personaController;
    }

    public Long insertar(Empleado empleado) {
        AuthorizationService.require(Permission.CREATE);
        validarEmpleado(empleado, false);
        return empleadoDao.insertar(empleado);
    }

    public boolean actualizar(Empleado empleado) {
        AuthorizationService.require(Permission.UPDATE);
        validarEmpleado(empleado, true);
        return empleadoDao.actualizar(empleado);
    }

    public boolean eliminar(Long id) {
        AuthorizationService.require(Permission.DELETE);
        requireId(id, "Empleado");
        return empleadoDao.eliminar(id);
    }

    public Optional<Empleado> buscarPorId(Long id) {
        requireId(id, "Empleado");
        return empleadoDao.buscarPorId(id);
    }

    public List<Empleado> listar() {
        return empleadoDao.listar();
    }

    public List<Empleado> buscar(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listar();
        }
        return empleadoDao.buscar(criterio);
    }

    private void validarEmpleado(Empleado empleado, boolean requireEntityId) {
        requireObject(empleado, "Empleado");
        if (personaController != null) {
            personaController.validarPersona(empleado, requireEntityId);
        } else {
            validarDatosPersona(empleado, requireEntityId);
        }
        requireText(empleado.getCodigoEmpleado(), "Codigo de empleado", 3);
        requireDate(empleado.getFechaContratacion(), "Fecha de contratacion");
        validateNonNegative(empleado.getSalario(), "Salario");
        validatePhotoPath(empleado.getRutaFotografia());
        requireObject(empleado.getCargo(), "Cargo del empleado");
        requireId(empleado.getCargo().getId(), "Cargo del empleado");
        requireObject(empleado.getDepartamento(), "Departamento del empleado");
        requireId(empleado.getDepartamento().getId(), "Departamento del empleado");
    }

    private void validarDatosPersona(Empleado empleado, boolean requireEntityId) {
        if (requireEntityId) {
            requireId(empleado.getId(), "Empleado");
        }
        requireText(empleado.getIdentidad(), "Identidad", 5);
        requireText(empleado.getNombres(), "Nombres", 2);
        requireText(empleado.getApellidos(), "Apellidos", 2);
        validateOptionalEmail(empleado.getCorreoElectronico(), "Correo electronico");
    }

    private void validatePhotoPath(String rutaFotografia) {
        if (rutaFotografia == null || rutaFotografia.isBlank()) {
            return;
        }

        Path photoPath = Path.of(rutaFotografia.trim());
        if (!Files.exists(photoPath) || !Files.isRegularFile(photoPath) || !Files.isReadable(photoPath)) {
            throw new ValidationException("La fotografia seleccionada no existe o no se puede leer.");
        }

        String fileName = photoPath.getFileName().toString().toLowerCase(Locale.ROOT);
        if (!(fileName.endsWith(".jpg")
                || fileName.endsWith(".jpeg")
                || fileName.endsWith(".png")
                || fileName.endsWith(".gif")
                || fileName.endsWith(".bmp"))) {
            throw new ValidationException("La fotografia debe ser JPG, JPEG, PNG, GIF o BMP.");
        }
    }
}
