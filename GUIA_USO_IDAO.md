# 📚 Guía de Uso de la Interfaz Genérica Idao

## 🎯 ¿Qué es Idao?

`Idao` es una **interfaz genérica** que define las operaciones CRUD (Create, Read, Update, Delete) básicas para cualquier entidad de tu aplicación. Al implementar esta interfaz en tus servicios, garantizas que todos tengan los mismos métodos estándar, promoviendo la **consistencia** y **reutilización de código**.

---

## 🔧 Componentes del Patrón

### 1. **Interfaz Idao<T, ID>**
- **T**: Tipo de la entidad (Usuario, Producto, Cliente, etc.)
- **ID**: Tipo del identificador (Integer, Long, String, etc.)

### 2. **Repository (JpaRepository)**
- Proporciona los métodos de acceso a datos
- Spring Data JPA genera automáticamente las consultas SQL

### 3. **Service (Implementa Idao)**
- Contiene la lógica de negocio
- Implementa los métodos de la interfaz Idao
- Puede agregar métodos personalizados

### 4. **Controller (Usa el Service)**
- Expone endpoints REST
- Llama a los métodos del servicio
- Maneja las respuestas HTTP

---

## 📋 Métodos de la Interfaz Idao

### 1. `obtenerTodos()` - Listar todos
```java
List<T> obtenerTodos();
```
**Retorna:** Lista con todas las entidades  
**SQL Generado:** `SELECT * FROM tabla`

**Ejemplo de uso en Controller:**
```java
@GetMapping
public ResponseEntity<List<Usuario>> listarTodos() {
    List<Usuario> usuarios = usuarioService.obtenerTodos();
    return ResponseEntity.ok(usuarios);
}
```

---

### 2. `obtenerPorId(ID id)` - Buscar por ID
```java
Optional<T> obtenerPorId(ID id);
```
**Retorna:** `Optional<T>` (puede estar vacío si no existe)  
**SQL Generado:** `SELECT * FROM tabla WHERE id = ?`

**Ejemplo de uso en Controller:**
```java
@GetMapping("/{id}")
public ResponseEntity<Usuario> buscarPorId(@PathVariable Integer id) {
    Optional<Usuario> usuario = usuarioService.obtenerPorId(id);
    
    // Opción 1: Con map
    return usuario
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
    
    // Opción 2: Con if-else
    if (usuario.isPresent()) {
        return ResponseEntity.ok(usuario.get());
    } else {
        return ResponseEntity.notFound().build();
    }
}
```

---

### 3. `guardar(T entidad)` - Crear nueva entidad
```java
T guardar(T entidad);
```
**Retorna:** La entidad guardada con su ID generado  
**SQL Generado:** `INSERT INTO tabla (...) VALUES (...)`

**Ejemplo de uso en Controller:**
```java
@PostMapping
public ResponseEntity<Usuario> crear(@RequestBody Usuario usuario) {
    Usuario nuevoUsuario = usuarioService.guardar(usuario);
    return ResponseEntity.status(HttpStatus.CREATED).body(nuevoUsuario);
}
```

---

### 4. `actualizar(ID id, T entidad)` - Actualizar existente
```java
T actualizar(ID id, T entidad);
```
**Retorna:** La entidad actualizada, o `null` si no existe  
**SQL Generado:** `UPDATE tabla SET ... WHERE id = ?`

**Ejemplo de uso en Controller:**
```java
@PutMapping("/{id}")
public ResponseEntity<Usuario> actualizar(
    @PathVariable Integer id,
    @RequestBody Usuario usuario
) {
    Usuario actualizado = usuarioService.actualizar(id, usuario);
    
    if (actualizado != null) {
        return ResponseEntity.ok(actualizado);
    } else {
        return ResponseEntity.notFound().build();
    }
}
```

---

### 5. `eliminar(ID id)` - Eliminar por ID
```java
boolean eliminar(ID id);
```
**Retorna:** `true` si se eliminó, `false` si no existía  
**SQL Generado:** `DELETE FROM tabla WHERE id = ?`

**Ejemplo de uso en Controller:**
```java
@DeleteMapping("/{id}")
public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
    boolean eliminado = usuarioService.eliminar(id);
    
    if (eliminado) {
        return ResponseEntity.noContent().build(); // 204 No Content
    } else {
        return ResponseEntity.notFound().build(); // 404 Not Found
    }
}
```

---

### 6. `existe(ID id)` - Verificar existencia
```java
boolean existe(ID id);
```
**Retorna:** `true` si existe, `false` si no  
**SQL Generado:** `SELECT COUNT(*) FROM tabla WHERE id = ?`

**Ejemplo de uso:**
```java
@GetMapping("/{id}/existe")
public ResponseEntity<Boolean> existe(@PathVariable Integer id) {
    boolean existe = usuarioService.existe(id);
    return ResponseEntity.ok(existe);
}
```

---

### 7. `contar()` - Contar total de registros
```java
long contar();
```
**Retorna:** Número total de registros  
**SQL Generado:** `SELECT COUNT(*) FROM tabla`

**Ejemplo de uso:**
```java
@GetMapping("/count")
public ResponseEntity<Long> contar() {
    long total = usuarioService.contar();
    return ResponseEntity.ok(total);
}
```

---

## 🚀 Cómo Implementar Idao en tus Servicios

### Paso 1: Tu Service debe implementar Idao
```java
@Service
public class ClienteService implements Idao<Cliente, Integer> {
    
    private final ClienteRepository clienteRepository;
    
    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }
    
    // Implementar todos los métodos de Idao...
}
```

### Paso 2: Implementar los 7 métodos obligatorios
Puedes copiar la estructura de `UsuarioService.java` y adaptarla:
- Cambia `Usuario` por tu entidad (Cliente, Producto, etc.)
- Cambia `usuarioRepository` por tu repository
- Ajusta el método `actualizar()` según los campos de tu entidad

### Paso 3: Agregar métodos personalizados (opcional)
```java
@Service
public class ClienteService implements Idao<Cliente, Integer> {
    
    // ... métodos de Idao ...
    
    // Métodos personalizados específicos de Cliente
    public List<Cliente> buscarPorCiudad(String ciudad) {
        return clienteRepository.findByCiudad(ciudad);
    }
    
    public Optional<Cliente> buscarPorCedula(String cedula) {
        return clienteRepository.findByCedula(cedula);
    }
}
```

---

## 📝 Ejemplo Completo: Controller con Idao

```java
package com.example.springbootagrolink.controllers;

import com.example.springbootagrolink.model.Cliente;
import com.example.springbootagrolink.services.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    // GET /api/clientes - Listar todos
    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        List<Cliente> clientes = clienteService.obtenerTodos();
        return ResponseEntity.ok(clientes);
    }

    // GET /api/clientes/{id} - Buscar por ID
    @GetMapping("/{id}")
    public ResponseEntity<Cliente> buscarPorId(@PathVariable Integer id) {
        return clienteService.obtenerPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/clientes - Crear nuevo
    @PostMapping
    public ResponseEntity<Cliente> crear(@RequestBody Cliente cliente) {
        Cliente nuevo = clienteService.guardar(cliente);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /api/clientes/{id} - Actualizar
    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(
        @PathVariable Integer id,
        @RequestBody Cliente cliente
    ) {
        Cliente actualizado = clienteService.actualizar(id, cliente);
        return actualizado != null 
            ? ResponseEntity.ok(actualizado)
            : ResponseEntity.notFound().build();
    }

    // DELETE /api/clientes/{id} - Eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        boolean eliminado = clienteService.eliminar(id);
        return eliminado
            ? ResponseEntity.noContent().build()
            : ResponseEntity.notFound().build();
    }

    // GET /api/clientes/count - Contar total
    @GetMapping("/count")
    public ResponseEntity<Long> contar() {
        return ResponseEntity.ok(clienteService.contar());
    }

    // GET /api/clientes/{id}/existe - Verificar existencia
    @GetMapping("/{id}/existe")
    public ResponseEntity<Boolean> existe(@PathVariable Integer id) {
        return ResponseEntity.ok(clienteService.existe(id));
    }
}
```

---

## ✅ Ventajas de Usar Idao

### 1. **Consistencia**
Todos los servicios tienen los mismos métodos CRUD con los mismos nombres.

### 2. **Reutilización**
No necesitas escribir los mismos métodos una y otra vez.

### 3. **Mantenibilidad**
Si necesitas cambiar algo en el CRUD, lo cambias en un solo lugar.

### 4. **Escalabilidad**
Agregar nuevas entidades es más rápido: solo implementa Idao.

### 5. **Documentación**
La interfaz sirve como documentación de qué métodos debe tener cada servicio.

### 6. **Testing**
Más fácil crear mocks y pruebas unitarias con una interfaz estándar.

---

## 🎨 Patrón de Diseño

Este patrón sigue varios principios de diseño:

- **DRY (Don't Repeat Yourself)**: No repites código CRUD
- **SOLID - Interface Segregation**: Interfaz específica para operaciones DAO
- **SOLID - Dependency Inversion**: Dependes de abstracciones (Idao) no de implementaciones
- **Template Method Pattern**: Defines la estructura, cada servicio implementa los detalles

---

## 🔄 Flujo Completo de una Petición

```
1. Cliente HTTP → Controller
2. Controller → Service (usa métodos de Idao)
3. Service → Repository (JpaRepository)
4. Repository → Base de Datos (SQL)
5. Base de Datos → Repository (resultado)
6. Repository → Service (entidad)
7. Service → Controller (entidad procesada)
8. Controller → Cliente HTTP (JSON)
```

---

## 📌 Notas Importantes

### Optional vs null
- `obtenerPorId()` retorna `Optional<T>` para manejar mejor la ausencia de datos
- `actualizar()` retorna `null` si no existe (puedes cambiarlo a Optional si prefieres)

### Transacciones
Si necesitas operaciones transaccionales, agrega `@Transactional`:
```java
@Transactional
public Usuario guardar(Usuario entidad) {
    return usuarioRepository.save(entidad);
}
```

### Validaciones
Puedes agregar validaciones en los servicios antes de guardar:
```java
@Override
public Usuario guardar(Usuario entidad) {
    // Validar que el correo no exista
    if (usuarioRepository.existsByCorreo(entidad.getCorreo())) {
        throw new IllegalArgumentException("El correo ya existe");
    }
    return usuarioRepository.save(entidad);
}
```

### Relaciones JPA
En el método `actualizar()`, ten cuidado al actualizar entidades relacionadas:
- Verifica que las entidades relacionadas existan
- Usa FetchType.LAZY para evitar cargar datos innecesarios
- Considera usar DTOs para evitar lazy loading exceptions

---

## 🎓 Próximos Pasos

1. ✅ Implementa Idao en todos tus servicios existentes
2. ✅ Actualiza tus controladores para usar los métodos de Idao
3. ✅ Agrega métodos personalizados según las necesidades de cada entidad
4. 📝 Considera crear DTOs (Data Transfer Objects) para separar la capa de presentación
5. 🔒 Agrega seguridad y validaciones con Spring Security y Bean Validation
6. 📊 Implementa paginación con `Pageable` para grandes volúmenes de datos

---

## 📚 Recursos Adicionales

- [Spring Data JPA Documentation](https://spring.io/projects/spring-data-jpa)
- [Java Generics Tutorial](https://docs.oracle.com/javase/tutorial/java/generics/)
- [Optional in Java](https://docs.oracle.com/javase/8/docs/api/java/util/Optional.html)
- [REST API Best Practices](https://restfulapi.net/)

---

**¡Felicidades! Ahora tienes un patrón DAO genérico y reutilizable para toda tu aplicación.** 🎉

