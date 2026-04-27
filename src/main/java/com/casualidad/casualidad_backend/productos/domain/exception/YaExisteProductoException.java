package com.casualidad.casualidad_backend.productos.domain.exception;

public class YaExisteProductoException extends RuntimeException {
    public YaExisteProductoException(String messagge) {
        super(messagge);
    }

}
