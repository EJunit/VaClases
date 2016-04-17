package hn.uth.hackaton.Mensajes;

/**
 * Modelo de datos estático para alimentar la aplicación
 */
public class Mensajes {

    private String titulo;
    private String mensaje;
    private String fecha;

    public Mensajes(){
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
