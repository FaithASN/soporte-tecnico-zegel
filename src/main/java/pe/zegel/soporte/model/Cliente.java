package pe.zegel.soporte.model;

/** Valor inmutable asociado a una solicitud; no tiene un CRUD independiente. */
public record Cliente(String nombre, String email) { }
